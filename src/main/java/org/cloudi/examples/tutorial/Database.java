//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:

package org.cloudi.examples.tutorial;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database
{
    public static Connection pgsql(final Arguments arguments)
    {
        try
        {
            Class.forName("org.postgresql.Driver");
            Connection c =
                DriverManager.getConnection(arguments.getPGSQLURL(),
                                            arguments.getPGSQLUsername(),
                                            arguments.getPGSQLPassword());
            c.setAutoCommit(false);
            return c;
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
            return null;
        }
    }
}

