//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class GutenbergRefreshParse extends DefaultHandler
{
    private GutenbergRefresh gutenberg_refresh;
    private boolean is_language_flag;
    private boolean is_ebook_flag;
    private boolean is_creator_flag;
    private boolean is_agent_flag;
    private boolean is_description_flag;
    private boolean is_subject_flag;
    private String item_id;
    private String item_web_page;
    private String item_creator;
    private String item_title;
    private String item_date_created;
    private String item_language;
    private String item_downloads;
    private String item_subject;
    private StringBuffer contents;

    public GutenbergRefreshParse(GutenbergRefresh gutenberg_refresh)
    {
        this.gutenberg_refresh = gutenberg_refresh;
        this.is_language_flag = false;
        this.is_ebook_flag = false;
        this.is_creator_flag = false;
        this.is_agent_flag = false;
        this.is_description_flag = false;
        this.is_subject_flag = false;
        this.item_id = null;
        this.item_web_page = null;
        this.item_creator = null;
        this.item_title = null;
        this.item_date_created = null;
        this.item_language = null;
        this.item_downloads = null;
        this.item_subject = null;
        this.contents = new StringBuffer();
    }

    public void startDocument()
    {
    }

    public void endDocument()
    {
    }

    public void startElement(String uri, String local_name, String name,
                             Attributes attributes)
    {
        if ("pgterms:ebook".equalsIgnoreCase(name))
        {
            this.item_id = null;
            this.item_web_page = null;
            this.item_creator = null;
            this.item_title = null;
            this.item_date_created = null;
            this.item_language = null;
            this.item_downloads = null;
            this.item_subject = null;
            
            String about = attributes.getValue("rdf:about");          
            if (about.indexOf("ebooks/") == 0)
            {
                this.item_id = about.substring("ebooks/".length());
                this.is_ebook_flag = true;
            }
        }
        else if (this.is_ebook_flag == true)
        {
            if ("dcterms:creator".equalsIgnoreCase(name))
            {
                this.is_creator_flag = true;
            }
            else if ("pgterms:agent".equalsIgnoreCase(name))
            {
                this.is_agent_flag = true;
            }
            else if ("dcterms:language".equalsIgnoreCase(name))
            {
                this.is_language_flag = true;
            }
            else if ("rdf:Description".equalsIgnoreCase(name))
            {
                this.is_description_flag = true;
            }
            else if ("dcterms:subject".equalsIgnoreCase(name))
            {
                this.is_subject_flag = true;
            }
    
            if (this.is_creator_flag == true &&
                this.is_agent_flag == true &&
                "pgterms:webpage".equalsIgnoreCase(name))
            {
                this.item_web_page = attributes.getValue("rdf:resource");   
            }
        }
        this.contents = new StringBuffer();
    }

    public void endElement(String uri, String local_name, String name)
    {
        if (this.is_ebook_flag == true)
        {
            if ("dcterms:title".equalsIgnoreCase(name))
            {
                this.item_title = this.contents.toString();
            }
            else if ("dcterms:issued".equalsIgnoreCase(name))
            {
                this.item_date_created = this.contents.toString();
            }
            else if ("pgterms:downloads".equalsIgnoreCase(name))
            {            
                this.item_downloads = this.contents.toString();
               // (new Double(data.toString()).doubleValue());
            }
            if (this.is_creator_flag == true &&
                this.is_agent_flag == true &&
                "pgterms:name".equalsIgnoreCase(name))
            {
                this.item_creator = this.contents.toString();
            }
            if (this.is_description_flag == true &&
                "rdf:value".equalsIgnoreCase(name))
            {
                if (this.is_language_flag == true)
                {
                    this.item_language = this.contents.toString();
                }
                else if (this.is_subject_flag == true)
                {
                    this.item_subject = this.contents.toString();
                }
            }

            // reset flags when ending tags are encountered
            if ("pgterms:ebook".equalsIgnoreCase(name))
            {
                // save the item
                gutenberg_refresh.save(this.item_id,
                                       this.item_web_page,
                                       this.item_creator,
                                       this.item_title,
                                       this.item_date_created,
                                       this.item_language,
                                       this.item_downloads,
                                       this.item_subject);
                this.is_ebook_flag = false;
            }
            else if ("pgterms:creator".equalsIgnoreCase(name))
            {
                this.is_creator_flag = false;
            }
            else if ("pgterms:agent".equalsIgnoreCase(name))
            {
                this.is_agent_flag = false;
            }
            else if ("dcterms:language".equalsIgnoreCase(name))
            {
                this.is_language_flag = false;
            }
            else if ("rdf:Description".equalsIgnoreCase(name))
            {
                this.is_description_flag = false;
            }
            else if ("dcterms:subject".equalsIgnoreCase(name))
            {
                this.is_subject_flag = false;
            }
        }
        this.contents = new StringBuffer();
    }

    public void characters(char[] ch, int start, int length)
    {
        this.contents.append(ch, start, length);
    }
}
