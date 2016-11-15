//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:

package org.cloudi.examples.tutorial;

import com.beust.jcommander.Parameter;

public class Arguments
{
    @Parameter(names = "-pgsql_hostname", description = "PostgreSQL hostname")
    private String pgsql_hostname = "localhost";

    @Parameter(names = "-pgsql_port", description = "PostgreSQL port")
    private int pgsql_port = 5432;

    @Parameter(names = "-pgsql_database", description = "PostgreSQL database")
    private String pgsql_database = "cloudi_tutorial_java";

    @Parameter(names = "-pgsql_username", description = "PostgreSQL username")
    private String pgsql_username = "cloudi_tutorial_java";

    @Parameter(names = "-pgsql_password", description = "PostgreSQL password")
    private String pgsql_password = "cloudi_tutorial_java";

    public String getPGSQLURL()
    {
        return "jdbc:postgresql://" +
               this.pgsql_hostname + ":" +
               this.pgsql_port + "/" +
               this.pgsql_database;
    }

    public String getPGSQLUsername()
    {
        return this.pgsql_username;
    }

    public String getPGSQLPassword()
    {
        return this.pgsql_password;
    }
}

