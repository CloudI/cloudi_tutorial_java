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
    private final ExecutorService item_refresh_executor;
    private Future<?> item_refresh_pending;
    private final int thread_index;
    private API api;
    private static LenskitData lenskit_instance;

    public Task(final int thread_index)
    {
        this.item_refresh_executor = Executors.newSingleThreadExecutor();
        this.item_refresh_pending = null;
        this.thread_index = thread_index;
        try
        {
            this.api = new API(thread_index);
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
        final LenskitData lenskit = Task.lenskit_instance;
        if (lenskit == null)
        {
            return Task.lenskit(false);
        }
        else
        {
            return lenskit;
        }
    }

    public synchronized static LenskitData lenskit(final boolean refresh)
    {
        LenskitData lenskit = Task.lenskit_instance;
        if (lenskit == null || refresh)
        {
            final Connection db = Database.pgsql(Main.arguments());
            if (db == null)
                return null;
            try
            {
                lenskit = new LenskitData(db);
                Task.lenskit_instance = lenskit;
            }
            catch (Exception e)
            {
                e.printStackTrace(Main.err);
                Database.close(db);
                return null;
            }
            finally
            {
                Database.close(db);
            }
        }
        return lenskit;
    }

    public void run()
    {
        try
        {
            Main.info(this, "initialization begin");
            // initialization timeout is enforced
            // based on the service configuration value
            if (Task.lenskit() == null)
            {
                throw new RuntimeException("Lenskit initialization failed");
            }

            // subscribe to different CloudI service name patterns
            if (this.api.process_index() == 0 &&
                this.thread_index == 0)
            {
                // only a single thread of a single OS process
                // should handle items refresh due to filesystem usage
                this.subscribe("item/refresh",
                               "itemRefresh");
            }
            if (this.thread_index == 0)
            {
                // only a single thread in any OS process
                // should handle recommendation refresh due to global data
                this.subscribe("recommendation/refresh",
                               "recommendationRefresh");
            }
            this.subscribe("item/list",
                           "itemList");
            this.subscribe("recommendation/update",
                           "recommendationUpdate");
            this.subscribe("recommendation/list",
                           "recommendationList");

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
        this.item_refresh_executor.shutdownNow();
        Main.info(this, "termination end");
    }

    private void subscribe(final String service_name_suffix,
                           final String method_name)
        throws NoSuchMethodException
    {
        // plain HTTP request for manual testing with JSON data in POST request
        this.api.subscribe(service_name_suffix + "/post", this,
                           method_name);
        // websockets request for HTML/JavaScript usage
        this.api.subscribe(service_name_suffix, this,
                           method_name);
    }

    public Object itemRefresh(Integer command, String name,
                              String pattern, byte[] request_info,
                              byte[] request, Integer timeout,
                              Byte priority, byte[] trans_id,
                              OtpErlangPid pid)
    {
        // refresh all item data asynchronously
        if (this.item_refresh_pending != null &&
            this.item_refresh_pending.isDone() == false)
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
        this.item_refresh_pending = this.item_refresh_executor.submit(
            new GutenbergRefresh(db,
                                 executable_download,
                                 executable_cleanup,
                                 directory));
        return JSONResponse.success().toString().getBytes();
    }

    public Object itemList(Integer command, String name,
                           String pattern, byte[] request_info,
                           byte[] request, Integer timeout,
                           Byte priority, byte[] trans_id,
                           OtpErlangPid pid)
    {
        // generate a list of items with the user's ratings
        final JSONItemListRequest request_json =
            JSONItemListRequest.fromString(new String(request));
        if (request_json.getUserId() <= 0)
        {
            return JSONResponse.failure("json").toString().getBytes();
        }
        final Connection db = Database.pgsql(Main.arguments());
        if (db == null)
        {
            return JSONResponse.failure("db").toString().getBytes();
        }
        final LenskitData lenskit = Task.lenskit();
        if (lenskit == null)
        {
            return JSONResponse.failure("lenskit").toString().getBytes();
        }
        final JSONResponse response_json =
            lenskit.itemList(db,
                             request_json.getUserId());
        Database.close(db);
        return response_json.toString().getBytes();
    }

    public Object recommendationRefresh(Integer command, String name,
                                        String pattern, byte[] request_info,
                                        byte[] request, Integer timeout,
                                        Byte priority, byte[] trans_id,
                                        OtpErlangPid pid)
    {
        // update the model used to generate recommendations
        // (new ratings won't be used until this occurs)
        final LenskitData lenskit = Task.lenskit(true);
        if (lenskit == null)
        {
            return JSONResponse.failure("lenskit").toString().getBytes();
        }
        // handle like a request for a recommendation list
        return this.recommendationList(command, name, pattern,
                                       request_info, request,
                                       timeout, priority, trans_id, pid);
    }

    public Object recommendationUpdate(Integer command, String name,
                                       String pattern, byte[] request_info,
                                       byte[] request, Integer timeout,
                                       Byte priority, byte[] trans_id,
                                       OtpErlangPid pid)
    {
        // rate a single user_id/item_id and
        // generate a new list of recommendations with rating predictions
        final JSONRecommendationUpdateRequest request_json =
            JSONRecommendationUpdateRequest.fromString(new String(request));
        if (request_json.getUserId() <= 0 ||
            request_json.getItemId() <= 0 ||
            request_json.getRating() < LenskitData.RATING_MIN ||
            request_json.getRating() > LenskitData.RATING_MAX)
        {
            return JSONResponse.failure("json").toString().getBytes();
        }
        final Connection db = Database.pgsql(Main.arguments());
        if (db == null)
        {
            return JSONResponse.failure("db").toString().getBytes();
        }
        final LenskitData lenskit = Task.lenskit();
        if (lenskit == null)
        {
            return JSONResponse.failure("lenskit").toString().getBytes();
        }
        final JSONResponse response_json =
            lenskit.recommendationUpdate(db,
                                         request_json.getUserId(),
                                         request_json.getItemId(),
                                         request_json.getRating());
        Database.close(db);
        return response_json.toString().getBytes();
    }

    public Object recommendationList(Integer command, String name,
                                     String pattern, byte[] request_info,
                                     byte[] request, Integer timeout,
                                     Byte priority, byte[] trans_id,
                                     OtpErlangPid pid)
    {
        // generate a list of recommendations with rating predictions
        final JSONRecommendationListRequest request_json =
            JSONRecommendationListRequest.fromString(new String(request));
        if (request_json.getUserId() <= 0)
        {
            return JSONResponse.failure("json").toString().getBytes();
        }
        final Connection db = Database.pgsql(Main.arguments());
        if (db == null)
        {
            return JSONResponse.failure("db").toString().getBytes();
        }
        final LenskitData lenskit = Task.lenskit();
        if (lenskit == null)
        {
            return JSONResponse.failure("lenskit").toString().getBytes();
        }
        final JSONResponse response_json =
            lenskit.recommendationList(db,
                                       request_json.getUserId());
        Database.close(db);
        return response_json.toString().getBytes();
    }

}
