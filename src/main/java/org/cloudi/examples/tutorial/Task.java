//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.sql.Connection;
import com.ericsson.otp.erlang.OtpErlangPid;
import org.cloudi.API;

public class Task implements Runnable
{
    private final int thread_index;
    private API api;
    private ExecutorService refresh_executor;
    private Future<?> refresh_pending;
    private static LenskitData lenskit_instance;

    public Task(final int thread_index)
    {
        this.thread_index = thread_index;
        try
        {
            this.api = new API(thread_index);
            this.refresh_executor = Executors.newSingleThreadExecutor();
            this.refresh_pending = null;
        }
        catch (API.InvalidInputException e)
        {
            e.printStackTrace(Main.err);
            System.exit(1);
        }
        catch (API.MessageDecodingException e)
        {
            e.printStackTrace(Main.err);
            System.exit(1);
        }
        catch (API.TerminateException e)
        {
            Main.error(this, "terminate before initialization");
            System.exit(1);
        }
    }

    public static LenskitData lenskit()
    {
        return Task.lenskit(false);
    }

    public synchronized static LenskitData lenskit(final boolean refresh)
    {
        if (Task.lenskit_instance == null || refresh)
        {
            final Connection db = Database.pgsql(Main.arguments());
            if (db == null)
                return null;
            try
            {
                Task.lenskit_instance = new LenskitData(db);
            }
            catch (Exception e)
            {
                e.printStackTrace(Main.err);
                Database.close(db);
            }
            finally
            {
                Database.close(db);
            }
        }
        return Task.lenskit_instance;
    }

    public void run()
    {
        try
        {
            Main.info(this, "initialization begin");
            // initialization timeout is enforced
            // based on the service configuration value
            Task.lenskit();

            // subscribe to different CloudI service name patterns
            if (this.api.process_index() == 0 &&
                this.thread_index == 0)
            {
                // only a single thread of a single OS process
                // should handle items refresh due to filesystem usage
                this.api.subscribe("items/refresh/get", this,
                                   "itemsRefresh");
            }
            if (this.thread_index == 0)
            {
                // only a single thread in any OS process
                // should handle recommendation refresh due to global data
                this.api.subscribe("recommendation/refresh/get", this,
                                   "recommendationRefresh");
            }
            this.api.subscribe("recommendation/update/post", this,
                               "recommendationUpdate");

            Main.info(this, "initialization end");
            Object result = this.api.poll(); // accept service requests
            assert result == Boolean.FALSE;  // poll did not timeout
        }
        catch (API.TerminateException e)
        {
            // service termination
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
        }
        Main.info(this, "termination begin");
        // termination timeout is enforced based on MaxT/MaxR
        // (or the timeout_terminate service configuration option)
        this.refresh_executor.shutdownNow();
        Main.info(this, "termination end");
    }

    public Object itemsRefresh(Integer command, String name,
                               String pattern, byte[] request_info,
                               byte[] request, Integer timeout,
                               Byte priority, byte[] trans_id,
                               OtpErlangPid pid)
    {
        // refresh all item data
        if (this.refresh_pending != null &&
            this.refresh_pending.isDone() == false)
        {
            return JSONResponse.failure("pending").toString().getBytes();
        }
        final String D = System.getProperty("file.separator");
        final String executable_path = System.getProperty("user.dir") + D +
                                       "scripts" + D;
        final String executable_download = executable_path +
                                           "gutenberg_refresh_download";
        final String executable_cleanup = executable_path +
                                          "gutenberg_refresh_cleanup";
        final String directory = System.getProperty("java.io.tmpdir") + D +
                                 (new API.TransId(trans_id)).toString();
        final Connection db = Database.pgsql(Main.arguments());
        if (db == null)
        {
            return JSONResponse.failure("db").toString().getBytes();
        }
        // refresh may take a long time and can be done asynchronously
        this.refresh_pending = this.refresh_executor.submit(
            new GutenbergRefresh(db,
                                 executable_download,
                                 executable_cleanup,
                                 directory));

        return JSONResponse.success().toString().getBytes();
    }

    public Object recommendationRefresh(Integer command, String name,
                                        String pattern, byte[] request_info,
                                        byte[] request, Integer timeout,
                                        Byte priority, byte[] trans_id,
                                        OtpErlangPid pid)
    {
        if (Task.lenskit(true) == null)
        {
            return JSONResponse.failure("lenskit").toString().getBytes();
        }
        return JSONResponse.success().toString().getBytes();
    }

    public Object recommendationUpdate(Integer command, String name,
                                       String pattern, byte[] request_info,
                                       byte[] request, Integer timeout,
                                       Byte priority, byte[] trans_id,
                                       OtpErlangPid pid)
    {
        // rate a single user_id/item_id and
        // generate a new list of recommendations
        final JSONRateRequest requestObject =
            JSONRateRequest.fromString(new String(request));
        final Connection db = Database.pgsql(Main.arguments());
        if (db == null)
        {
            return JSONResponse.failure("db").toString().getBytes();
        }
        Task.lenskit().rate(db,
                            requestObject.getUserId(),
                            requestObject.getItemId(),
                            requestObject.getRating());
        Database.close(db);

        return JSONResponse.success().toString().getBytes();
    }

}
