//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Process;
import java.lang.Runtime;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

public class GutenbergRefresh implements Runnable
{
    private final Connection db;
    private final String executable_download;
    private final String executable_cleanup;
    private final String directory;
    private boolean cleared;

    public GutenbergRefresh(Connection db,
                            String executable_download,
                            String executable_cleanup,
                            String directory)
    {
        this.db = db;
        this.executable_download = executable_download;
        this.executable_cleanup = executable_cleanup;
        this.directory = directory;
        this.cleared = false;
    }

    public void run()
    {
        try
        {
            if (this.download() != 0)
                throw new IOException("download failed");
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            this.parseFiles(new File(this.directory), parser);
            if (this.cleanup() != 0)
                throw new IOException("cleanup failed");
            Main.info(this, "refreshed");
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
    
    private void parseFiles(final File directory, final SAXParser parser)
        throws java.io.IOException
    {
        for (final File file : directory.listFiles())
        {
            if (file.isDirectory())
            {
                this.parseFiles(file, parser);
            }
            else
            {
                try
                {
                    parser.parse(file, new GutenbergRefreshParse(this));
                }
                catch (Exception e)
                {
                    Main.error(this, "parse %s failed\n", file.toString());
                    e.printStackTrace(Main.err);
                }
            }
        }
    }

    public void save(final String item_id,
                     final String item_web_page,
                     final String item_creator,
                     final String item_title,
                     final String item_date_created,
                     final List<String> item_language,
                     final int item_downloads,
                     final List<String> item_subject)
    {
        try
        {
            if (! this.cleared)
            {
                Statement delete = this.db.createStatement();
                delete.execute("DELETE FROM books");
                delete.close(); 
                this.cleared = true;
            }
            PreparedStatement insert =
                this.db.prepareStatement(
                    "INSERT INTO books (id, creator, creator_link, title, "   +
                                       "date_created, languages, subjects, "  +
                                       "downloads) "                          +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            insert.setString(1, item_id);
            insert.setString(2, item_creator);
            insert.setString(3, item_web_page);
            insert.setString(4, item_title);
            insert.setDate(5, Database.toDate(item_date_created));
            insert.setArray(6, Database.toArray(this.db, item_language));
            insert.setArray(7, Database.toArray(this.db, item_subject));
            insert.setInt(8, item_downloads);
            insert.executeUpdate();
            insert.close();
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
        }
        /*
        Main.info(this, "item(%s,%s,%s,%s,%s,%s,%s,%s)\n",
                  item_id, item_web_page, item_creator,
                  item_title, item_date_created, item_language.toString(),
                  item_downloads, item_subject.toString());
                  */
    }
}

