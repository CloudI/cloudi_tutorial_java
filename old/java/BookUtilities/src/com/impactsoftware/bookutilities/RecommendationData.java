package com.impactsoftware.bookutilities;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.io.PrintWriter;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.math.BigDecimal;

import com.ericsson.otp.erlang.OtpErlangPid;

import java.io.UnsupportedEncodingException;

import org.cloudi.API;

/**
 * This class is responsible for generating the data files used by
 * the recommendation system.  It also handles persisting data to the
 * underlying database system.
 *
 * @author Bruce Kissinger, 2012
 */
public class RecommendationData {
    Connection conn = null;
    API api;

    public RecommendationData() {
        super();
    }

    /**
     * Set the CloudI API instance
     * @param apiReference
     */
    public void setCloudIAPI(API apiReference) {
        api = apiReference;
    }

    /**
     * Return the CloudI API instance
     * @return
     */
    public API getCloudIAPI() {
        return api;
    }


    /**
     * Save an item object in the database
     * @param item
     */
    public void saveItem(Item item) {

        Boolean itemExistsFlag = doesItemExist(item.getID());
        if (itemExistsFlag.booleanValue() == false) {
            addItem(item);
        } else {
            updateItem(item);
        }
    }


    /**
     * Generate rating file based on user's rating and download amounts
     */
    public void generateItemRatings() {        
        Double maxDownloads = null;

        setCharacterSet();

        try {
            // create the rating file
            PrintWriter out =
                new PrintWriter(new BufferedWriter(new FileWriter("/opt/book/data/rating.txt")));

            maxDownloads = getMaxDownloads();
            writeItemDownloads(out, maxDownloads);                        
            out.close();


            // create a training file
            out =
                new PrintWriter(new BufferedWriter(new FileWriter("/opt/book/data/rating_test.txt")));
            writeUserRatings(out);
            out.close();


        } catch (Exception e) {
            System.out.println("GenerateRatings: " + e.getMessage());            
        }

    }

    /**
     * Generate a file with all user ids to be used for item recommendation
     */
    public void generateUserFile() {        
        String startDelimiter = ".\\[";
        String userID = null;

        try {
            // create the user file
            PrintWriter out =
                new PrintWriter(new BufferedWriter(new FileWriter("/opt/book/data/users.txt")));


            try {
                byte[] service_request =
                    ("select cast(user_id as char(10)) from users;").getBytes();

                org.cloudi.API.Response response =
                    api.send_sync("/db/mysql/book", service_request);

                if (response.response.length > 0) {

                    try {
                        String response_string =
                            new String(response.response, "UTF-8");                    
                        //API.out.println("query has response of " + response_string);

                        // process results
                        String[] tokens = response_string.split(startDelimiter);

                        for (int i = 0; i < tokens.length; ++i) {
                             //API.out.println("Token " + i + "=" + tokens[i]);

                            // parse the user id
                            int start = tokens[i].indexOf("<<");
                            if (start >= 0) {
                                int end = tokens[i].indexOf(">>", start);
                                //API.out.println("start=" + start + ", end=" + end);

                                userID = tokens[i].substring(start + 3, end - 1);
                                // API.out.println("userID=" + userID);

                                // write the values to the data file
                                String str = userID + "\n";
                                out.write(str);

                            }
                        }

                    } catch (UnsupportedEncodingException e) {
                        API.err.println("UnsupportedEncodingException caught " +
                                        e.getMessage());
                    }

                } else {
                    API.out.println("query has no response");
                }


            } catch (API.TerminateException e) {
                API.err.println("TerminateException caught " + e.getMessage());
            } catch (API.MessageDecodingException e) {
                API.err.println("MessageDecodingException caught " +
                                e.getMessage());
            } catch (API.InvalidInputException e) {
                API.err.println("InvalidInputException caught " + e.getMessage());
            }
            ;
            
            out.close();

        } catch (Exception e) {
            System.out.println("GenerateUserFile: " + e.getMessage());            
        }

    }



    /**
     * Get the highest download quantity
     */
    private Double getMaxDownloads() {
        Double maxDownloads = null;
        String startDelimiter = ".\\[";

        try {
            byte[] service_request =
                ("SELECT max(download_quantity) FROM items").getBytes();

            org.cloudi.API.Response response =
                api.send_sync("/db/mysql/book", service_request);

            if (response.response.length > 0) {

                try {
                    String response_string =
                        new String(response.response, "UTF-8");
                    //API.out.println("query has response of " + response_string);

                    // process results
                    String[] tokens = response_string.split(startDelimiter);

                    for (int i = 0; i < tokens.length; ++i) {
                        //API.out.println("Token " + i + "=" + tokens[i]);

                        // parse the max download value
                        int end = tokens[i].indexOf("]");
                        if (end > 0) {
                            maxDownloads =
                                    new Double(tokens[i].substring(0, end));
                            //API.out.println("maxDownloads=" + maxDownloads);

                        }

                    }

                } catch (UnsupportedEncodingException e) {
                    API.err.println("UnsupportedEncodingException caught " +
                                    e.getMessage());
                }

            } else {
                API.out.println("query has no response");
            }


        } catch (API.TerminateException e) {
            API.err.println("TerminateException caught " + e.getMessage());
        } catch (API.MessageDecodingException e) {
            API.err.println("MessageDecodingException caught " +
                            e.getMessage());
        } catch (API.InvalidInputException e) {
            API.err.println("InvalidInputException caught " + e.getMessage());
        }
        ;

        return maxDownloads;
    }


    /**
     * Get all the item downloads and write the items popularity scaled from 1 to 5 to a file
     */
    private void writeItemDownloads(PrintWriter out, Double maxDownloads) {        
        String startDelimiter = ".\\[";
        String itemId;
        Double downloadQuantity;

        setCharacterSet();


        try {
            byte[] service_request =
                ("SELECT id, download_quantity FROM items WHERE download_quantity > 0").getBytes();

            org.cloudi.API.Response response =
                api.send_sync("/db/mysql/book", service_request);

            if (response.response.length > 0) {

                try {
                    String response_string =
                        new String(response.response, "UTF-8");                    
                    //API.out.println("query has response of " + response_string);

                    // process results
                    String[] tokens = response_string.split(startDelimiter);

                    for (int i = 0; i < tokens.length; ++i) {
                        // API.out.println("Token " + i + "=" + tokens[i]);

                        // parse the item id
                        int start = tokens[i].indexOf("<<");
                        if (start >= 0) {
                            int end = tokens[i].indexOf(">>", start);
                            // API.out.println("start=" + start + ", end=" + end);

                            itemId = tokens[i].substring(start + 3, end - 1);
                            // API.out.println("itemID=" + itemId);

                            // parse the download quantity
                            start = tokens[i].indexOf(",", end);
                            end = tokens[i].indexOf("]", start);
                            // API.out.println("start=" + start + ", end=" + end);

                            downloadQuantity =
                                    new Double(tokens[i].substring(start + 1,
                                                                   end));
                            // API.out.println("downloadQuantity=" + downloadQuantity);

                            // calculate the popularity
                            Double calculatedPopularity =
                                (downloadQuantity * 5) / maxDownloads;

                            BigDecimal bigAmount =
                                new BigDecimal(calculatedPopularity);
                            BigDecimal bigAmountScaled =
                                bigAmount.setScale(0, BigDecimal.ROUND_UP);

                            // write the values to the data file
                            String str =
                                "0 " + itemId + " " + bigAmountScaled.toString() +
                                "\n";
                            out.write(str);

                        }
                    }

                } catch (UnsupportedEncodingException e) {
                    API.err.println("UnsupportedEncodingException caught " +
                                    e.getMessage());
                }

            } else {
                API.out.println("query has no response");
            }


        } catch (API.TerminateException e) {
            API.err.println("TerminateException caught " + e.getMessage());
        } catch (API.MessageDecodingException e) {
            API.err.println("MessageDecodingException caught " +
                            e.getMessage());
        } catch (API.InvalidInputException e) {
            API.err.println("InvalidInputException caught " + e.getMessage());
        }
        ;

    }

    /**
     * Generate rating file based on user's rating and download amounts
     * @return
     */
    private void writeUserRatings(PrintWriter out) {
        String startDelimiter = ".\\[";
        String itemId;
        String userId;
        Double rating;

        try {
            String sqlQuery =
                "SELECT item_id, user_id, rating FROM user_item_ratings;";

            byte[] service_request = (sqlQuery).getBytes();
            byte[] request_info = ("na").getBytes();            
            Integer timeout = new Integer(600000);
            Byte priority = new Byte("0");

            org.cloudi.API.Response response =
                api.send_sync("/db/mysql/book", request_info, service_request, timeout, priority);            


            if (response.response.length > 0) {

                try {
                    String response_string =
                        new String(response.response, "UTF-8");
                     //API.out.println("query has response of " + response_string);

                    // process results
                    String[] tokens = response_string.split(startDelimiter);

                    for (int i = 0; i < tokens.length; ++i) {
                        // API.out.println("Token " + i + "=" + tokens[i]);

                        // parse the item id
                        int start = tokens[i].indexOf("<<");
                        if (start >= 0) {
                            int end = tokens[i].indexOf(">>", start);
                            itemId = tokens[i].substring(start + 3, end - 1);
                            // API.out.println("itemId=" + itemId);

                            // parse the userID
                            start = tokens[i].indexOf(",", end);
                            end = tokens[i].indexOf(",", start + 1);
                            userId = tokens[i].substring(start + 1, end);
                            // API.out.println("userId=" + userId);

                            // parse the rating
                            start = tokens[i].indexOf(",", end);
                            end = tokens[i].indexOf("]", start + 1);
                            rating =
                                    new Double(tokens[i].substring(start + 1, end));
                            // API.out.println("downloadQuantity=" + rating);

                            // write the values to the data file
                            String str =
                                userId + " " + itemId + " " + rating + "\n";
                            out.write(str);

                        }
                    }

                } catch (UnsupportedEncodingException e) {
                    API.err.println("UnsupportedEncodingException caught " +
                                    e.getMessage());
                }

            } else {
                API.out.println("query has no response");
            }


        } catch (API.TerminateException e) {
            API.err.println("TerminateException caught " + e.getMessage());
        } catch (API.MessageDecodingException e) {
            API.err.println("MessageDecodingException caught " +
                            e.getMessage());
        } catch (API.InvalidInputException e) {
            API.err.println("InvalidInputException caught " + e.getMessage());
        };
        
    }


    /**
     * Generate item attribute file.  This file will include a record for every item and it's subject
     * and another record for every item and it's creator.  
     */
    public void generateItemAttributes() {

        try {
            PrintWriter out =
                new PrintWriter(new BufferedWriter(new FileWriter("/opt/book/data/item_attributes.txt")));

            Long lastRecordNumber = writeItemSubject(out);            
            out.flush();
            writeItemCreator(out, lastRecordNumber);

            out.close();


        } catch (Exception e) {
            System.out.println("GenerateRatings: " + e.getMessage());
        }
    }

    /**
     * Write a record for every item and it's subject
     */
    private Long writeItemSubject(PrintWriter out) {
        String startDelimiter = ".\\[";
        String itemId;
        Long lastSubjectNumber = new Long(0);

        try {
            String sqlQuery = "SET @rownum := 0; " +
            "SELECT id, rn " +
            "FROM items a " +
            "JOIN ( " +
            "        select short_subject, @rownum:=@rownum + 1 as rn " +
            "        from subjects_vw " +
            ") b " +
            "ON substring_index(subject, '--',1) = short_subject;"; 
            //"WHERE id LIKE '" + request.substring(0, 1) + "%';";

            API.out.println("query is " + sqlQuery);

            byte[] service_request = (sqlQuery).getBytes();
            byte[] request_info = ("na").getBytes();            
            Integer timeout = new Integer(600000);
            Byte priority = new Byte("0");

            org.cloudi.API.Response response =
                api.send_sync("/db/mysql/book", request_info, service_request, timeout, priority);            

            if (response.response.length > 0) {

                try {
                    String response_string =
                        new String(response.response, "UTF-8");
                    //API.out.println("query has response of " + response_string);

                    // process results
                    String[] tokens = response_string.split(startDelimiter);

                    for (int i = 0; i < tokens.length; ++i) {
                        //API.out.println("Token " + i + "=" + tokens[i]);

                        // parse the item id
                        int start = tokens[i].indexOf("<<");
                        if (start >= 0) {
                            int end = tokens[i].indexOf(">>", start);
                            // API.out.println("start=" + start + ", end=" + end);

                            itemId = tokens[i].substring(start + 3, end - 1);
                            // API.out.println("itemID=" + itemId);

                            // parse the subject number
                            start = tokens[i].indexOf(",", end);
                            end = tokens[i].indexOf("]", start);
                            // API.out.println("start=" + start + ", end=" + end);

                            Long subjectNumber =
                                    new Long(tokens[i].substring(start + 1,
                                                                   end));
                            lastSubjectNumber = subjectNumber;
                            
                            // write the values to the data file
                            String str =
                                itemId + " " + subjectNumber.toString() + "\n";
                            out.write(str);
                            

                        }
                    }

                } catch (UnsupportedEncodingException e) {
                    API.err.println("UnsupportedEncodingException caught " +
                                    e.getMessage());
                }

            } else {
                API.out.println("item subject query has no response");
            }


        } catch (API.TerminateException e) {
            API.err.println("TerminateException caught " + e.getMessage());
        } catch (API.MessageDecodingException e) {
            API.err.println("MessageDecodingException caught " +
                            e.getMessage());
        } catch (API.InvalidInputException e) {
            API.err.println("InvalidInputException caught " + e.getMessage());
        };
        
        return lastSubjectNumber;
    }

    /**
     * Write a record for every item and it's creator (author)
     */
    private void writeItemCreator(PrintWriter out, Long lastRecordNumber) {
        String startDelimiter = ".\\[";
        String itemId;

        try {
            String sqlQuery = "SET @rownum := " + lastRecordNumber.toString() + "; " +
            "SELECT id, rn " +
            "FROM items a " +
            "JOIN ( " +
            "        select author, @rownum:=@rownum + 1 as rn " +
            "        from authors_vw " +
            ") b " +
            "ON creator = author; ";
            //"WHERE id LIKE '" + request.substring(1, 1) + "%';";

            API.out.println("query is " + sqlQuery);

            byte[] service_request = (sqlQuery).getBytes();
            byte[] request_info = ("na").getBytes();
            Integer timeout = new Integer(600000);
            Byte priority = new Byte("0");

            org.cloudi.API.Response response =
                api.send_sync("/db/mysql/book", request_info, service_request, timeout, priority);            

/*
            API.TransId transid = api.send_async("/db/mysql/book", request_info, service_request, timeout, priority);            
            org.cloudi.API.Response response = api.recv_async(timeout, transid.id, true);
*/

            if (response.response.length > 0) {

                try {
                    String response_string =
                        new String(response.response, "UTF-8");
                    API.out.println("Processing author query response");
                    //API.out.println("query has response of " + response_string);

                    // process results
                    String[] tokens = response_string.split(startDelimiter);

                    for (int i = 0; i < tokens.length; ++i) {
                        //API.out.println("Token " + i + "=" + tokens[i]);

                        // parse the item id
                        int start = tokens[i].indexOf("<<");
                        if (start >= 0) {
                            int end = tokens[i].indexOf(">>", start);
                            // API.out.println("start=" + start + ", end=" + end);

                            itemId = tokens[i].substring(start + 3, end - 1);
                            // API.out.println("itemID=" + itemId);

                            // parse the subject number
                            start = tokens[i].indexOf(",", end);
                            end = tokens[i].indexOf("]", start);
                            // API.out.println("start=" + start + ", end=" + end);

                            Long authorNumber =
                                    new Long(tokens[i].substring(start + 1,
                                                                   end));
                            
                            // write the values to the data file
                            String str =
                                itemId + " " + authorNumber.toString() + "\n";
                            out.write(str);

                        }
                    }

                } catch (UnsupportedEncodingException e) {
                    API.err.println("UnsupportedEncodingException caught " +
                                    e.getMessage());
                }

            } else {
                API.out.println("item creator query has no response");
            }


        } catch (API.TerminateException e) {
            API.err.println("TerminateException caught " + e.getMessage());
        } catch (API.MessageDecodingException e) {
            API.err.println("MessageDecodingException caught " +
                            e.getMessage());
        } catch (API.InvalidInputException e) {
            API.err.println("InvalidInputException caught " + e.getMessage());
        };
        
    }


    /**
     * Store the user item recommendations in the database
     * @param user_id
     * @param item_id
     * @param rating
     */
    public void saveUserItemRecommendation(String user_id, String item_id,
                                          String rating) {
        
        BigDecimal bigAmount = new BigDecimal(rating);
        BigDecimal bigAmountScaled = bigAmount.setScale(0, BigDecimal.ROUND_UP);
        
          try {            
            String sqlQuery = "INSERT INTO user_item_recommendations (user_id, item_id, rating) VALUES ('" + user_id + "', '" + item_id + "', " + new Integer(bigAmountScaled.toString()).intValue() 
                 + ")";            
            
            //API.out.println(sqlQuery);
            
            byte[] service_request = sqlQuery.getBytes();
            api.send_sync("/db/mysql/book", service_request);

        } catch (API.TerminateException e) {
            API.err.println("TerminateException caught " + e.getMessage());
        } catch (API.MessageDecodingException e) {
            API.err.println("MessageDecodingException caught " +
                            e.getMessage());
        } catch (API.InvalidInputException e) {
            API.err.println("InvalidInputException caught " + e.getMessage());
        };

    }



    /**
     * Clear the user item recommendations database records
     */
    public void clearUserItemRecommendations() {        

        try {            
            String sqlQuery = "DELETE FROM user_item_recommendations";            
            
            API.out.println(sqlQuery);
            
            byte[] service_request = sqlQuery.getBytes();
            api.send_sync("/db/mysql/book", service_request);

        } catch (API.TerminateException e) {
            API.err.println("TerminateException caught " + e.getMessage());
        } catch (API.MessageDecodingException e) {
            API.err.println("MessageDecodingException caught " +
                            e.getMessage());
        } catch (API.InvalidInputException e) {
            API.err.println("InvalidInputException caught " + e.getMessage());
        };

    }


    /**
     * Explicitly set the character set that shouw be used for all query results
     */
    private void setCharacterSet() {

        try {
            try {
                // set the character set that should be used for all query results
                byte[] service_request =
                    ("SET character_set_results = utf8").getBytes();

                org.cloudi.API.Response response =
                    api.send_sync("/db/mysql/book", service_request);


            } catch (API.TerminateException e) {
                API.err.println("TerminateException caught " + e.getMessage());
            } catch (API.MessageDecodingException e) {
                API.err.println("MessageDecodingException caught " +
                                e.getMessage());
            } catch (API.InvalidInputException e) {
                API.err.println("InvalidInputException caught " +
                                e.getMessage());
            }
            ;

            try {
                // set the character set that should be used for all client results
                byte[] service_request =
                    ("SET character_set_client = utf8").getBytes();

                org.cloudi.API.Response response =
                    api.send_sync("/db/mysql/book", service_request);


            } catch (API.TerminateException e) {
                API.err.println("TerminateException caught " + e.getMessage());
            } catch (API.MessageDecodingException e) {
                API.err.println("MessageDecodingException caught " +
                                e.getMessage());
            } catch (API.InvalidInputException e) {
                API.err.println("InvalidInputException caught " +
                                e.getMessage());
            }
            ;


        } catch (Exception e) {
            System.out.println("setCharacterSets: " + e.getMessage());
        }

    }


    /**
     * Check if an item is already stored in the database
     */
    private Boolean doesItemExist(String itemID) {
        Boolean returnFlag = false;
        String startDelimiter = ".\\[";

        try {
            byte[] service_request =
                ("SELECT count(*) FROM items WHERE ID='" + itemID  + "'").getBytes();

            org.cloudi.API.Response response =
                api.send_sync("/db/mysql/book", service_request);

            if (response.response.length > 0) {

                try {
                    String response_string =
                        new String(response.response, "UTF-8");
                    //API.out.println("query has response of " + response_string);

                    // process results
                    String[] tokens = response_string.split(startDelimiter);

                    for (int i = 0; i < tokens.length; ++i) {
                        //API.out.println("Token " + i + "=" + tokens[i]);

                        // parse the max download value
                        int end = tokens[i].indexOf("]");
                        if (end > 0) {
                            String temp = tokens[i].substring(0, end);
                            if ("1".equals(temp)) {
                                returnFlag = true;
                            } else {
                                returnFlag = false;
                            }

                        }

                    }

                } catch (UnsupportedEncodingException e) {
                    API.err.println("UnsupportedEncodingException caught " +
                                    e.getMessage());
                }

            } else {
                API.out.println("query has no response");
            }


        } catch (API.TerminateException e) {
            API.err.println("TerminateException caught " + e.getMessage());
        } catch (API.MessageDecodingException e) {
            API.err.println("MessageDecodingException caught " +
                            e.getMessage());
        } catch (API.InvalidInputException e) {
            API.err.println("InvalidInputException caught " + e.getMessage());
        };

        return returnFlag;
    }


    /**
     * Add an item to the database
     */
    private void addItem(Item item) {        

        try {            
            String sqlQuery = "INSERT INTO items (id, title, creator, lang, web_page, subject, date_created, download_quantity) VALUES('" + item.getID() + "'," + item.getTitleEscaped() + "," + item.getCreatorEscaped() + ", " + item.getLanguageEscaped() + "," + item.getWebPageEscaped() + "," +
               item.getSubjectEscaped() + ", " + item.getDateCreatedEscaped() + ", " + item.getDownloads() + ")";            
            
            API.out.println(sqlQuery);
            
            byte[] service_request = sqlQuery.getBytes();
            api.send_sync("/db/mysql/book", service_request);

        } catch (API.TerminateException e) {
            API.err.println("TerminateException caught " + e.getMessage());
        } catch (API.MessageDecodingException e) {
            API.err.println("MessageDecodingException caught " +
                            e.getMessage());
        } catch (API.InvalidInputException e) {
            API.err.println("InvalidInputException caught " + e.getMessage());
        };

    }

    /**
     * Update an item in the database
     */
    private void updateItem(Item item) {        

        //API.out.println("updating item " + item.getID());

        try {            
            String sqlQuery = "UPDATE items SET title = " + item.getTitleEscaped() + ", creator=" + item.getCreatorEscaped() + ", lang=" + item.getLanguageEscaped() + 
                              ", subject=" + item.getSubjectEscaped() + ", date_created=" + item.getDateCreatedEscaped() + ", web_page=" + item.getWebPageEscaped() + ", download_quantity=" + item.downloads + " WHERE id='" + item.getID() + "';";
            
            //API.out.println(sqlQuery);
            
            byte[] service_request = sqlQuery.getBytes();
            api.send_sync("/db/mysql/book", service_request);

        } catch (API.TerminateException e) {
            API.err.println("TerminateException caught " + e.getMessage());
        } catch (API.MessageDecodingException e) {
            API.err.println("MessageDecodingException caught " +
                            e.getMessage());
        } catch (API.InvalidInputException e) {
            API.err.println("InvalidInputException caught " + e.getMessage());
        };
        

    }
}
