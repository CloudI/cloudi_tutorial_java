//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:

package org.cloudi.examples.tutorial;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.io.PrintStream;
import java.util.List;

public class Database
{
    public static Connection pgsql(final Arguments arguments)
    {
        try
        {
            Class.forName("org.postgresql.Driver");
            Connection db =
                DriverManager.getConnection(arguments.getPGSQLURL(),
                                            arguments.getPGSQLUsername(),
                                            arguments.getPGSQLPassword());
            db.setAutoCommit(false);
            return db;
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
            return null;
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
            return null;
        }
    }

    public static Array toArray(Connection db, List<String> list)
        throws SQLException
    {
        String[] list_array = new String[list.size()];
        list_array = list.toArray(list_array);
        return db.createArrayOf("text", list_array);
    }

    public static Date toDate(String value)
    {
        return Date.valueOf(value);
    }

    public static void printSQLException(SQLException ex, PrintStream err)
    { 
        // based on JDBCTutorialUtilities
        final String sql_state = ex.getSQLState();
        Throwable e = ex;
        if (! Database.ignoreSQLException(sql_state))
        {
            e.printStackTrace(err);
            err.println("SQLState: " + sql_state);
            err.println("Error Code: " + ex.getErrorCode());
            err.println("Message: " + e.getMessage());

            e = ex.getCause();
            while (e != null)
            {
                err.println("Cause: " + e);
                e = e.getCause();
            }
        }
    }

    private static boolean ignoreSQLException(String sql_state)
        throws IllegalArgumentException
    {
        // based on JDBCTutorialUtilities
        if (sql_state == null)
            throw new IllegalArgumentException("SQL state was null");

        //// X0Y32: Jar file already exists in schema
        //if (sql_state.equalsIgnoreCase("X0Y32"))
        //    return true;

        //// 42Y55: Table already exists in schema
        //if (sql_state.equalsIgnoreCase("42Y55"))
        //    return true;

        return false;
    }

    public static void rollback(Connection db)
    {
        try
        {
            db.rollback();
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
        }
    }

    public static void close(Connection db)
    {
        if (db == null)
            return;
        try
        {
            db.close();
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
        }
    }

    public static void close(Statement obj)
    {
        if (obj == null)
            return;
        try
        {
            obj.close();
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
        }
    }

    public static void close(PreparedStatement obj)
    {
        if (obj == null)
            return;
        try
        {
            obj.close();
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
        }
    }

    public static void close(ResultSet obj)
    {
        if (obj == null)
            return;
        try
        {
            obj.close();
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
        }
    }

}

