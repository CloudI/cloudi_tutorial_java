package com.impactsoftware.bookutilities;


import org.cloudi.API;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ParseCatalogHandler extends DefaultHandler {
    Item item = new Item();
    StringBuffer data = new StringBuffer();
    boolean isLanguageFlag;
    boolean isEbookFlag;
    boolean isCreatorFlag;
    boolean isAgentFlag;
    boolean isDescriptionFlag;
    boolean isSubjectFlag;
    RecommendationData recommendationData = new RecommendationData();

    public ParseCatalogHandler() {
    }

    public ParseCatalogHandler(RecommendationData recommendationData) {
        this.recommendationData = recommendationData;
        recommendationData.getCloudIAPI();
    }

    public void startDocument() {
        //API.out.println("startDocument");
    }

    public void endDocument() {
        //API.out.println("endDocument");
    }

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) {
        //API.out.println("startElement=" + localName + ", uri=" + uri + ", qName=" + qName);

        //
        // set flags when ending tags are encountered
        //
        if ("pgterms:ebook".equalsIgnoreCase(qName)) {
            item = new Item();
            isEbookFlag = true;
            
            String temp = attributes.getValue("rdf:about");          
            int pos = temp.indexOf("ebooks");
            
            item.setID(temp.substring(pos + 1 + "ebooks".length()));
        }

        if ("dcterms:creator".equalsIgnoreCase(qName)) {
            isCreatorFlag = true;
        }
        
        if ("pgterms:agent".equalsIgnoreCase(qName)) {
            isAgentFlag = true;
        }

        if ("dcterms:language".equalsIgnoreCase(qName)) {
            isLanguageFlag = true;
        }

        if ("rdf:Description".equalsIgnoreCase(qName)) {
            isDescriptionFlag = true;
        }


        if ("dcterms:subject".equalsIgnoreCase(qName)) {
            isSubjectFlag = true;
        }

        
        if (isEbookFlag == true && isCreatorFlag == true && isAgentFlag == true && "pgterms:webpage".equalsIgnoreCase(qName)) {
            String temp = attributes.getValue("rdf:resource");   

            item.setWebPage(temp);
        }
        
  
        data = new StringBuffer();
    }

    public void endElement(String uri, String localName, String qName) {
        
        //String tempMessage = "endElement " + localName;
        //api.out.println("endElement " + localName);
        //api.out.println(tempMessage);


        if (isEbookFlag == true && isCreatorFlag == true && isAgentFlag == true && "pgterms:name".equalsIgnoreCase(qName)) {
            item.setCreator(data.toString());
        }


        if (isEbookFlag == true && "dcterms:title".equalsIgnoreCase(qName)) {
            item.setTitle(data.toString());
        }

        if (isEbookFlag == true && "dcterms:issued".equalsIgnoreCase(qName)) {
            item.setDateCreated(data.toString());
        }

        if (isEbookFlag == true && isLanguageFlag == true && isDescriptionFlag == true && "rdf:value".equalsIgnoreCase(qName)) {
            item.setLanguage(data.toString());
        }

        if (isEbookFlag == true && "pgterms:downloads".equalsIgnoreCase(qName)) {            
            item.setDownloads(new Double(data.toString()).doubleValue());
        }

        if ("dcterms:subject".equalsIgnoreCase(qName)) {
            isSubjectFlag = false;
        }

        if (isSubjectFlag == true  && isDescriptionFlag == true && "rdf:value".equalsIgnoreCase(qName)) {
            item.setSubject(data.toString());
        }
        
        //
        // reset flags when ending tags are encountered
        //        
        if ("rdf:Description".equalsIgnoreCase(qName)) {
            isDescriptionFlag = false;
        }

        
        if ("dcterms:language".equalsIgnoreCase(qName)) {
            isLanguageFlag = false;
        }


        if ("pgterms:agent".equalsIgnoreCase(qName)) {
            isAgentFlag = false;
        }

        if ("pgterms:creator".equalsIgnoreCase(qName)) {
            isCreatorFlag = false;
        }

        if ("dcterms:subject".equalsIgnoreCase(qName)) {
            isSubjectFlag = false;
        }


        if ("pgterms:ebook".equalsIgnoreCase(qName)) {
            isEbookFlag = false;
            
            // save the item
            //API.out.println(item.toString());
            if (recommendationData != null) {   
                recommendationData.saveItem(item);
            } else {
                API.err.println("Item " + item.getID() + " not be saved because RecommendationData instance is null");
            }
        }


        data = new StringBuffer();
    }

    public void characters(char[] ch, int start, int length) {
        data.append(ch, start, length);
    }
}
