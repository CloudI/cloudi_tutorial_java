//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.io.File;
import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GutenbergRefresh implements Runnable
{
    private final String directory;

    public GutenbergRefresh(String directory)
    {
        this.directory = directory;
    }

    public void run()
    {
        try
        {
            File[] files = new File(this.directory).listFiles();
            if (files == null)
                throw new IOException("directory invalid");
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parseFiles(files, parser);
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
        }
    }
    
    private void parseFiles(File[] files, SAXParser parser)
        throws org.xml.sax.SAXException,
               java.io.IOException
    {
        if (files == null)
            return;
        for (File file : files)
        {
            if (file.isDirectory())
            {
                parseFiles(file.listFiles(), parser);
                return;
            }
            parser.parse(file, new GutenbergRefreshParse(this));
        }
    }

    public void save(final String item_id,
                     final String item_web_page,
                     final String item_creator,
                     final String item_title,
                     final String item_date_created,
                     final String item_language,
                     final String item_downloads,
                     final String item_subject)
    {
        Main.info(this, "item(%s,%s,%s,%s,%s,%s,%s,%s)",
                  item_id, item_web_page, item_creator,
                  item_title, item_date_created, item_language,
                  item_downloads, item_subject);
    }
}

