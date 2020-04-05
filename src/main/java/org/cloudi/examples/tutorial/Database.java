//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:

package org.cloudi.examples.tutorial;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.io.PrintStream;
import java.util.List;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.apache.mahout.cf.taste.impl.model.jdbc.ConnectionPoolDataSource;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLJDBCDataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;

public class Database
{
    public static DataSource pgsql(final Arguments arguments)
    {
        PGSimpleDataSource db_data = new PGSimpleDataSource();
        db_data.setServerName(arguments.getPGSQLHostname());
        db_data.setPortNumber(arguments.getPGSQLPort());
        db_data.setDatabaseName(arguments.getPGSQLDatabase());
        db_data.setUser(arguments.getPGSQLUsername());
        db_data.setPassword(arguments.getPGSQLPassword());
        return new ConnectionPoolDataSource(db_data);
    }

    public static JDBCDataModel dataModel(final DataSource db_data)
    {
        final String preferenceTable = "ratings";
        final String columnUserID = "user_id";
        final String columnItemID = "item_id";
        final String columnPreference = "rating";
        final String columnTimestamp = "timestamp";
        return new PostgreSQLJDBCDataModel(db_data,
                                           preferenceTable,
                                           columnUserID,
                                           columnItemID,
                                           columnPreference,
                                           columnTimestamp);
    }

    public static Connection connection(final DataSource db_data)
    {
        try
        {
            final Connection db = db_data.getConnection();
            db.setAutoCommit(false);
            return db;
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
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

