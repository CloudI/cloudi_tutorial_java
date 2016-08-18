//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.sql.Connection;
import com.ericsson.otp.erlang.OtpErlangPid;
import org.cloudi.API;

public class Service implements Runnable
{
    private final ExecutorService item_refresh_executor;
    private Future<?> item_refresh_pending;
    private final int thread_index;
    private API api;
    private static LenskitData lenskit_instance;

    public Service(final int thread_index)
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
        final LenskitData lenskit = Service.lenskit_instance;
        if (lenskit == null)
        {
            return Service.lenskit(false);
        }
        else
        {
            return lenskit;
        }
    }

    public synchronized static LenskitData lenskit(final boolean refresh)
    {
        LenskitData lenskit = Service.lenskit_instance;
        if (lenskit == null || refresh)
        {
            final Connection db = Database.pgsql(Main.arguments());
            if (db == null)
                return null;
            try
            {
                lenskit = new LenskitData(db);
                Service.lenskit_instance = lenskit;
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
            if (Service.lenskit() == null)
            {
                throw new RuntimeException("Lenskit initialization failed");
            }

            // subscribe to different CloudI service name patterns
            if (this.api.process_index() == 0 &&
                this.thread_index == 0)
            {
                // only a single thread of a single OS process
                // should handle items refresh due to filesystem usage
                this.api.subscribe("item/refresh/post",
                                   this, "itemRefresh");
            }
            this.api.subscribe("item/list/post",
                               this, "itemList");
            this.api.subscribe("language/list/post",
                               this, "languageList");
            if (this.thread_index == 0)
            {
                // only a single thread in any OS process
                // should handle recommendation refresh due to global data
                this.api.subscribe("recommendation/refresh/post",
                                   this, "recommendationRefresh");
            }
            this.api.subscribe("recommendation/update/post",
                               this, "recommendationUpdate");
            this.api.subscribe("recommendation/list/post",
                               this, "recommendationList");
            if (this.thread_index != 0)
            {
                // persistent connections that lack a standard routing
                // identifier in their protocol (like websockets)
                // use the same service name for all requests
                // which must be routed based on the content of the request
                // (do not utilize thread 0, so that it can be used
                //  as a forward destination, for requests that require it)
                this.api.subscribe("client/get",
                                   this, "client");
            }

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

    public Object itemRefresh(Integer command, String name,
                              String pattern, byte[] request_info,
                              byte[] request, Integer timeout,
                              Byte priority, byte[] trans_id,
                              OtpErlangPid pid)
    {
        // refresh all item data asynchronously
        final JSONItemRefreshRequest request_json =
            JSONItemRefreshRequest.fromString(new String(request));
        if (request_json == null)
        {
            return JSONItemRefreshResponse
                .failure("json")
                .toString().getBytes();
        }
        if (this.item_refresh_pending != null &&
            this.item_refresh_pending.isDone() == false)
        {
            return JSONItemRefreshResponse
                .failure("pending")
                .toString().getBytes();
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
            return JSONItemRefreshResponse
                .failure("db")
                .toString().getBytes();
        }
        // refresh may take a long time and can be done asynchronously
        this.item_refresh_pending = this.item_refresh_executor.submit(
            new GutenbergRefresh(db,
                                 executable_download,
                                 executable_cleanup,
                                 directory));
        return JSONItemRefreshResponse.success().toString().getBytes();
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
        if (request_json == null)
        {
            return JSONItemListResponse
                .failure("json")
                .toString().getBytes();
        }
        final Connection db = Database.pgsql(Main.arguments());
        if (db == null)
        {
            return JSONItemListResponse
                .failure("db",
                         request_json.getUserId())
                .toString().getBytes();
        }
        final LenskitData lenskit = Service.lenskit();
        if (lenskit == null)
        {
            return JSONItemListResponse
                .failure("lenskit",
                         request_json.getUserId())
                .toString().getBytes();
        }
        final JSONResponse response_json =
            lenskit.itemList(db,
                             request_json.getUserId(),
                             request_json.getLanguage());
        Database.close(db);
        return response_json.toString().getBytes();
    }

    public Object languageList(Integer command, String name,
                               String pattern, byte[] request_info,
                               byte[] request, Integer timeout,
                               Byte priority, byte[] trans_id,
                               OtpErlangPid pid)
    {
        // generate a list of all the languages which have items available
        final JSONLanguageListRequest request_json =
            JSONLanguageListRequest.fromString(new String(request));
        if (request_json == null)
        {
            return JSONLanguageListResponse
                .failure("json")
                .toString().getBytes();
        }
        final Connection db = Database.pgsql(Main.arguments());
        if (db == null)
        {
            return JSONLanguageListResponse
                .failure("db")
                .toString().getBytes();
        }
        final LenskitData lenskit = Service.lenskit();
        if (lenskit == null)
        {
            return JSONLanguageListResponse
                .failure("lenskit")
                .toString().getBytes();
        }
        final JSONResponse response_json = lenskit.languageList(db);
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
        final JSONRecommendationRefreshRequest request_json =
            JSONRecommendationRefreshRequest.fromString(new String(request));
        if (request_json == null)
        {
            return JSONRecommendationRefreshResponse
                .failure("json")
                .toString().getBytes();
        }
        final LenskitData lenskit = Service.lenskit(true);
        if (lenskit == null)
        {
            return JSONRecommendationRefreshResponse
                .failure("lenskit")
                .toString().getBytes();
        }
        return JSONRecommendationRefreshResponse
            .success().toString().getBytes();
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
        if (request_json == null)
        {
            return JSONRecommendationUpdateResponse
                .failure("json")
                .toString().getBytes();
        }
        final Connection db = Database.pgsql(Main.arguments());
        if (db == null)
        {
            return JSONRecommendationUpdateResponse
                .failure("db",
                         request_json.getUserId())
                .toString().getBytes();
        }
        final LenskitData lenskit = Service.lenskit();
        if (lenskit == null)
        {
            return JSONRecommendationUpdateResponse
                .failure("lenskit",
                         request_json.getUserId())
                .toString().getBytes();
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
        if (request_json == null)
        {
            return JSONRecommendationListResponse
                .failure("json")
                .toString().getBytes();
        }
        final Connection db = Database.pgsql(Main.arguments());
        if (db == null)
        {
            return JSONRecommendationListResponse
                .failure("db",
                         request_json.getUserId())
                .toString().getBytes();
        }
        final LenskitData lenskit = Service.lenskit();
        if (lenskit == null)
        {
            return JSONRecommendationListResponse
                .failure("lenskit",
                         request_json.getUserId())
                .toString().getBytes();
        }
        final JSONResponse response_json =
            lenskit.recommendationList(db,
                                       request_json.getUserId());
        Database.close(db);
        return response_json.toString().getBytes();
    }

    public Object client(Integer command, String name, String pattern,
                         byte[] request_info, byte[] request,
                         Integer timeout, Byte priority, byte[] trans_id,
                         OtpErlangPid pid)
        throws API.ForwardAsyncException,
               API.ForwardSyncException,
               API.InvalidInputException
    {
        // handle any JSON request based on the "message_name" field
        final JSONRequest request_json =
            JSONRequest.fromString(new String(request));
        if (request_json == null)
        {
            return JSONResponse
                .failure("json")
                .toString().getBytes();
        }
        switch (request_json.getMessageName())
        {
            case JSONItemRefreshRequest.message_name_valid:
                final String name_item_refresh =
                    this.api.prefix() + "item/refresh/post";
                this.api.forward_(command, name_item_refresh,
                                  request_info, request,
                                  timeout, priority,
                                  trans_id, pid);
                return null;
            case JSONItemListRequest.message_name_valid:
                return this.itemList(command, name, pattern,
                                     request_info, request,
                                     timeout, priority,
                                     trans_id, pid);
            case JSONLanguageListRequest.message_name_valid:
                return this.languageList(command, name, pattern,
                                         request_info, request,
                                         timeout, priority,
                                         trans_id, pid);
            case JSONRecommendationRefreshRequest.message_name_valid:
                final String name_recommendation_refresh =
                    this.api.prefix() + "recommend/refresh/post";
                this.api.forward_(command, name_recommendation_refresh,
                                  request_info, request,
                                  timeout, priority,
                                  trans_id, pid);
                return null;
            case JSONRecommendationListRequest.message_name_valid:
                return this.recommendationList(command, name, pattern,
                                               request_info, request,
                                               timeout, priority,
                                               trans_id, pid);
            case JSONRecommendationUpdateRequest.message_name_valid:
                return this.recommendationUpdate(command, name, pattern,
                                                 request_info, request,
                                                 timeout, priority,
                                                 trans_id, pid);
            default:
                return JSONResponse
                    .failure("message_name")
                    .toString().getBytes();
        }
    }

}
