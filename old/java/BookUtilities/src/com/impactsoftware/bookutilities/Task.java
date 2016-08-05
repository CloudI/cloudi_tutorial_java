package com.impactsoftware.bookutilities;


import com.ericsson.otp.erlang.OtpErlangPid;

import java.io.UnsupportedEncodingException;

import org.cloudi.API;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class Task {
    private API api;

    public Task(final int thread_index) {
        try {
            this.api = new API(thread_index);
        } catch (API.InvalidInputException e) {
            e.printStackTrace(API.err);
            System.exit(1);
        } catch (API.MessageDecodingException e) {
            e.printStackTrace(API.err);
            System.exit(1);
        } catch (API.TerminateException e) {
            System.exit(1);
        }
    }


    public void run() {

        try {

            // subscribe to different CloudI services
            this.api.subscribe("load_catalog/get", this, "startLoadCatalog");
            this.api.subscribe("generate_ratings/get", this,
                               "startGenerateRatings");
            this.api.subscribe("load_predictions/get", this,
                               "startLoadPredictions");
            this.api.subscribe("generate_item_attributes/get", this,
                               "startGenerateItemAttributes");
            
            // accept service requests
            this.api.poll();

        } catch (API.TerminateException e) {
            API.err.println("Book Utilities TerminateException caught " +
                            e.getMessage());
        } catch (Exception e) {
            API.err.println("Book Utilities Exception caught " +
                            e.getMessage());
        }
    }


    /**
     * This method calls the LoadCatalog class
     */
    public Object startLoadCatalog(Integer command, String name,
                                   String pattern, byte[] request_info,
                                   byte[] request, Integer timeout,
                                   Byte priority, byte[] trans_id,
                                   OtpErlangPid pid) {

        SAXParser parser = null;
        API.out.println("startLoadCatalog starts");

        // Create a new parser
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();

            if (parser != null) {
                // create a new instance of the LoadCatalog class and process all the files in the given directory
                LoadCatalog loadCatalog = new LoadCatalog();
                loadCatalog.load("/opt/book/data/cache", parser, api);
            }
        } catch (Exception e) {
            API.out.println("Exception creating parser: " + e.toString());
        }


        API.out.println("startLoadCatalog ends");
        return ("startLoadCatalog ends".getBytes());
    }

    /**
     * This method calls the RecommendationData class and the generateItemRatings method
     */
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

    /**
     * This method calls the RecommendationData class and the generateItemAttributes method
     */
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

    /**
     * This method calls the ItemPrediction class
     */
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


}
