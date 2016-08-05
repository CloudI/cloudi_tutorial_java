package com.impactsoftware.bookutilities;

import java.io.File;
import org.cloudi.API;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;



public class LoadCatalog {
    
    public LoadCatalog() {
        super();
    }


    public String load(String directoryName, SAXParser parser, API apiReference) {
        String returnValue = "success";
        
        File[] files = new File(directoryName).listFiles();
        
        // Create a new instance of the RecommendationData class and set the CloudIAPI reference
        RecommendationData recommendationData = new RecommendationData();
        recommendationData.setCloudIAPI(apiReference);
        
        // start processing the files
        showFiles(files, parser, recommendationData);
        
        return returnValue;
    }
    
    public void showFiles(File[] files, SAXParser parser, RecommendationData recommendationData) {
        if (files != null) { 
            for (File file : files) {
                if (file.isDirectory()) {
                    //api.out.println("Directory: " + file.getName());
                    showFiles(file.listFiles(), parser, recommendationData); // Calls same method again.
                } else {
                    //API.out.println("File: " + file.getName());              
                    
                    try {

                        // Run the parser
                        parser.parse(file, new ParseCatalogHandler(recommendationData));

                    } catch (Exception e) {
                        API.err.println("LoadCatalog: " + e.toString());
                    }                    
                    
                }
            }
        }
    }
    
}
