//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:
package org.cloudi.examples.tutorial;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.lang.Process;
import java.lang.Runtime;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

public class GutenbergRefresh implements Runnable
{
    private final ServiceIdle idle;
    private final Connection db;
    private final String executable_download;
    private final String executable_cleanup;
    private final String directory;
    private HashSet<String> subjects;
    private HashSet<String> languages;
    private boolean cleared;
    private SQLException failure;

    public GutenbergRefresh(ServiceIdle idle,
                            Connection db,
                            String executable_download,
                            String executable_cleanup,
                            String directory)
    {
        this.idle = idle;
        this.db = db;
        this.executable_download = executable_download;
        this.executable_cleanup = executable_cleanup;
        this.directory = directory;
        this.subjects = new HashSet();
        this.languages = new HashSet();
        this.cleared = false;
        this.failure = null;
    }

    public void run()
    {
        String error = null;
        try
        {
            if (this.download() != 0)
            {
                error = "download";
                throw new IOException("download failed");
            }
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            try
            {
                this.parseFiles(new File(this.directory), parser);
                this.saveMetaData();
                this.db.commit();
            }
            catch (SQLException e)
            {
                error = "db";
                Database.printSQLException(e, Main.err);
                Database.rollback(this.db);
            }
            catch (Exception e)
            {
                Database.rollback(this.db);
                throw e;
            }
            if (this.cleanup() != 0)
            {
                if (error == null)
                    error = "cleanup";
                throw new IOException("cleanup failed");
            }
            Main.info(this, "item_refresh done");
        }
        catch (Exception e)
        {
            if (error == null)
                error = "parsing";
            e.printStackTrace(Main.err);
        }
        finally
        {
            Database.close(this.db);
        }
        if (error == null)
            this.idle.execute(GutenbergRefreshDone.success());
        else
            this.idle.execute(GutenbergRefreshDone.failure(error));
    }

    private int download()
        throws IOException,
               InterruptedException
    {
        return this.execute(this.executable_download,
                            this.directory);
    }

    private int cleanup()
        throws IOException,
               InterruptedException
    {
        return this.execute(this.executable_cleanup,
                            this.directory);
    }

    private int execute(final String... args)
        throws IOException,
               InterruptedException

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
        throws IOException,
               SQLException
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
                if (this.failure != null)
                {
                    throw this.failure;
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
        if (this.failure != null)
            return;
        Statement delete = null;
        PreparedStatement insert = null;
        try
        {
            if (! this.cleared)
            {
                delete = this.db.createStatement();
                delete.execute("DELETE FROM items");
                this.cleared = true;
            }
            insert = this.db.prepareStatement(
                "INSERT INTO items (id, creator, creator_link, title, "   +
                                   "date_created, languages, subjects, "  +
                                   "downloads) "                          +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            insert.setLong(1, Long.parseLong(item_id));
            insert.setString(2, item_creator);
            insert.setString(3, item_web_page);
            insert.setString(4, item_title);
            insert.setDate(5, Database.toDate(item_date_created));
            insert.setArray(6, Database.toArray(this.db, item_language));
            insert.setArray(7, Database.toArray(this.db, item_subject));
            insert.setInt(8, item_downloads);
            insert.executeUpdate();
            this.subjects.addAll(item_subject);
            this.languages.addAll(item_language);
        }
        catch (SQLException e)
        {
            // abort parsing and rethrow the exception
            this.failure = e;
        }
        finally
        {
            Database.close(delete);
            Database.close(insert);
        }
        /*
        Main.info(this, "item(%s,%s,%s,%s,%s,%s,%s,%s)\n",
                  item_id, item_web_page, item_creator,
                  item_title, item_date_created, item_language.toString(),
                  item_downloads, item_subject.toString());
                  */
    }

    private void saveMetaData()
        throws SQLException
    {
        if (this.failure != null)
            throw new IllegalArgumentException("invalid state");

        Statement deleteSubjects = null;
        PreparedStatement insertSubject = null;
        Statement deleteLanguages = null;
        PreparedStatement insertLanguage = null;
        try
        {
            deleteSubjects = this.db.createStatement();
            deleteSubjects.execute("DELETE FROM subjects");
            insertSubject = this.db.prepareStatement(
                "INSERT INTO subjects (subject, language) " +
                "VALUES (?, ?)");
            for (String subject : this.subjects)
            {
                insertSubject.setString(1, subject);
                insertSubject.setString(2, this.subjectLanguage(subject));
                insertSubject.executeUpdate();
            }
    
            deleteLanguages = this.db.createStatement();
            deleteLanguages.execute("DELETE FROM languages");
            insertLanguage = this.db.prepareStatement(
                "INSERT INTO languages (language) " +
                "VALUES (?)");
            for (String language : this.languages)
            {
                insertLanguage.setString(1, language);
                insertLanguage.executeUpdate();
            }
        }
        catch (SQLException e)
        {
            this.failure = e;
        }
        finally
        {
            Database.close(deleteSubjects);
            Database.close(insertSubject);
            Database.close(deleteLanguages);
            Database.close(insertLanguage);
        }

        if (this.failure != null)
            throw this.failure;
    }

    private String subjectLanguage(final String subject)
    {
        final String defaultLanguage = "en";
        try
        {
            final char subjectLanguage[] = {subject.charAt(0),
                                            subject.charAt(1)};
            if (! (Character.isLetter(subjectLanguage[0]) &&
                   Character.isUpperCase(subjectLanguage[0])))
                return defaultLanguage;
            if (! (Character.isLetter(subjectLanguage[1]) &&
                   Character.isUpperCase(subjectLanguage[1])))
                return defaultLanguage;
            if (! (subject.charAt(2) == ' '))
                return defaultLanguage;
            final String language = (new String(subjectLanguage)).toLowerCase();
            if (this.languages.contains(language))
                return language;
            else
                return defaultLanguage;
        }
        catch (IndexOutOfBoundsException e)
        {
            return defaultLanguage;
        }
    }
}

