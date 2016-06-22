//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import com.ericsson.otp.erlang.OtpErlangPid;
import org.cloudi.API;

public class Task implements Runnable
{
    private API api;
    private ExecutorService refresh_executor;
    private Future<?> refresh_pending;

    public Task(final int thread_index)
    {
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

    public void run()
    {
        try
        {
            Main.info(this, "initialization begin");
            // initialization timeout is enforced
            // based on the service configuration value

            // subscribe to different CloudI service name patterns
            this.api.subscribe("refresh/get", this,
                               "refresh");
/*
            this.api.subscribe("generate_ratings/get", this,
                               "startGenerateRatings");
            this.api.subscribe("load_predictions/get", this,
                               "startLoadPredictions");
            this.api.subscribe("generate_item_attributes/get", this,
                               "startGenerateItemAttributes");
*/

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

    public Object refresh(Integer command, String name,
                          String pattern, byte[] request_info,
                          byte[] request, Integer timeout,
                          Byte priority, byte[] trans_id,
                          OtpErlangPid pid)
    {
        Main.info(this, "refresh begin");

        if (this.refresh_pending != null &&
            this.refresh_pending.isDone() == false) {
            return ("pending".getBytes());
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
        // refresh may take a long time and can be done asynchronously
        this.refresh_pending = this.refresh_executor.submit(
            new GutenbergRefresh(executable_download,
                                 executable_cleanup,
                                 directory));

        Main.info(this, "refresh end");
        return ("ok".getBytes());
    }

/*
    public Object startGenerateRatings(Integer command, String name,
                                       String pattern, byte[] request_info,
                                       byte[] request, Integer timeout,
                                       Byte priority, byte[] trans_id,
                                       OtpErlangPid pid) {

        API.out.println("startGenerateRatings starts");

        // create a new instance of the RecommendationData class
        RecommendationData recommendationData = new RecommendationData();
        recommendationData.setCloudIAPI(api);
        recommendationData.generateItemRatings();
        recommendationData.generateUserFile();

        API.out.println("startGenerateRatings ends");
        return ("startGenerateRatings ends".getBytes());
    }

    public Object startGenerateItemAttributes(Integer command, String name,
                                              String pattern,
                                              byte[] request_info,
                                              byte[] request, Integer timeout,
                                              Byte priority, byte[] trans_id,
                                              OtpErlangPid pid) {


        API.out.println("startGenerateItemAttributes starts");

        // create a new instance of the RecommendationData class
        RecommendationData recommendationData = new RecommendationData();
        recommendationData.setCloudIAPI(api);
        recommendationData.generateItemAttributes();


        API.out.println("startGenerateItemAttributes ends");
        return ("startGenerateItemAttributes ends".getBytes());
    }

    public Object startLoadPredictions(Integer command, String name,
                                       String pattern, byte[] request_info,
                                       byte[] request, Integer timeout,
                                       Byte priority, byte[] trans_id,
                                       OtpErlangPid pid) {

        API.out.println("startLoadPredictions starts");

        ItemPrediction itemPrediction = new ItemPrediction();
        itemPrediction.load("/opt/book/data/item_predict.txt", api);

        API.out.println("startLoadPredictions ends");
        return ("startLoadPredictions ends".getBytes());
    }
*/
}
