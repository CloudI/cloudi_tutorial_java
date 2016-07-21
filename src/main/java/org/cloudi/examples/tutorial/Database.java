//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:

package org.cloudi.examples.tutorial;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

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

    public static Array toArray(Connection c, List<String> list)
        throws SQLException
    {
        String[] list_array = new String[list.size()];
        list_array = list.toArray(list_array);
        return c.createArrayOf("text", list_array);
    }

    public static Date toDate(String value)
        throws IllegalArgumentException
    {
        return Date.valueOf(value);
    }
}

