//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.lang.Process;
import java.lang.Runtime;
import java.lang.Thread;
import java.lang.InterruptedException;
import org.xml.sax.SAXException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GutenbergRefresh implements Runnable
{
    private final String executable_download;
    private final String executable_cleanup;
    private final String directory;

    public GutenbergRefresh(String executable_download,
                            String executable_cleanup,
                            String directory)
    {
        this.executable_download = executable_download;
        this.executable_cleanup = executable_cleanup;
        this.directory = directory;
    }

    public void run()
    {
        try
        {
            if (this.download() != 0)
                throw new IOException("download failed");
            File[] files = new File(this.directory).listFiles();
            if (files == null)
                throw new IOException("directory invalid");
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parseFiles(files, parser);
            if (this.cleanup() != 0)
                throw new IOException("cleanup failed");
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
        }
    }

    private int download()
        throws java.io.IOException,
               java.lang.InterruptedException
    {
        return this.execute(this.executable_download,
                            this.directory);
    }

    private int cleanup()
        throws java.io.IOException,
               java.lang.InterruptedException
    {
        return this.execute(this.executable_cleanup,
                            this.directory);
    }

    private int execute(final String... args)
        throws java.io.IOException,
               java.lang.InterruptedException

    {
        final ProcessBuilder pb =
            new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        final Process p = pb.start();
        ArrayList<String> out = new ArrayList<String>();
        BufferedReader in = new BufferedReader(
            new InputStreamReader(p.getInputStream()));
        String line_in = null;
        while ((line_in = in.readLine()) != null)
            out.add(line_in);
        final int result = p.waitFor();
        if (result != 0)
        {
            for (String line_out : out)
                Main.out.println(line_out);
        }
        return result;
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

