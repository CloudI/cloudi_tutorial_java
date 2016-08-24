[CloudI](http://cloudi.org) Java Tutorial
=========================================

[![Build Status](https://travis-ci.org/CloudI/cloudi_tutorial_java.png)](https://travis-ci.org/CloudI/cloudi_tutorial_java)

This repository contains the example source code for the tutorial at
_link_.  The source code provides local real-time recommendations for
the electronic books available at
[Project Gutenberg](http://www.gutenberg.org/).

While the source code is meant to show information related to Java development
with the Java CloudI API, additional source code dependencies are used:

* [Lenskit](http://lenskit.org/) for the recommendations in Java
* [Simple Logging Facade for Java (SLF4J)](http://www.slf4j.org/) to allow Lenskit to use the CloudI log file through stderr (which keeps all the service log output in the `cloudi.log` file)
* [PostgreSQL JDBC Driver](http://search.maven.org/#artifactdetails|org.postgresql|postgresql|9.3-1104-jdbc4|jar) for Java database usage along with [PostgreSQL](https://www.postgresql.org/) and Lenskit
* [Gson](https://github.com/google/gson) for JSON serialization in Java
* [Jcommander](http://jcommander.org/) for command-line parsing in Java of CloudI service configuration arguments
* [Erlang/OTP jinterface Library](http://erlang.org/doc/apps/jinterface/jinterface_users_guide.html) for CloudI API usage of the Erlang Binary Term Format

BUILD
-----

Use maven and JDK 1.7 (Java 7) to build (and run):

    mvn clean package

RUN
---

To execute the tutorial dynamically, it is necessary to create the CloudI service configuration that specifies both the initialization and fault-tolerance constraints the CloudI service should be executed with (with the proplist format to rely on defaults): 

    export JAVA=`which java`
    export PWD=`pwd`
    export USER=`whoami`
    cat << EOF > website.conf
    [[{prefix, "/"},
      {module, cloudi_service_filesystem},
      {args,
       [{directory, "$PWD/html/"}]},
      {dest_refresh, none},
      {count_process, 4}],
     [{prefix, "/tutorial/java/"},
      {module, cloudi_service_http_cowboy},
      {args,
       [{port, 8080}, {use_websockets, true}]},
      {timeout_async, 600000},
      {timeout_sync, 600000}]]
    EOF
    cat << EOF > tutorial.conf
    [[{prefix, "/tutorial/java/service/"},
      {file_path, "$JAVA"},
      {args, "-Dfile.encoding=UTF-8 "
             "-Dorg.slf4j.simpleLogger.defaultLogLevel=warn "
             "-server "
             "-ea:org.cloudi... "
             "-Xms3g -Xmx3g "
             "-jar $PWD/target/cloudi_tutorial_java-1.5.1-SNAPSHOT-jar-with-dependencies.jar "
             "-pgsql_hostname localhost "
             "-pgsql_port 5432 "
             "-pgsql_database cloudi_tutorial_java "
             "-pgsql_username cloudi_tutorial_java "
             "-pgsql_password cloudi_tutorial_java"},
      {timeout_init, 600000},
      {count_thread, 4},
      {options,
       [{owner, [{user, "$USER"}]},
        {directory, "$PWD"}]}]]
    EOF


Confirm your PostgreSQL database is setup with values that match the service configuration:

    psql -U postgres << EOF
    CREATE DATABASE cloudi_tutorial_java;
    CREATE USER cloudi_tutorial_java WITH PASSWORD 'cloudi_tutorial_java';
    GRANT ALL PRIVILEGES ON DATABASE cloudi_tutorial_java to cloudi_tutorial_java;
    EOF


Make sure the database schema is initialized:

    bunzip2 schema.sql.bz2
    psql -h localhost cloudi_tutorial_java cloudi_tutorial_java < schema.sql


To dynamically add the CloudI service configuration that starts the service's execution use:

    curl -X POST -d @website.conf http://localhost:6464/cloudi/api/rpc/services_add.erl
    curl -X POST -d @tutorial.conf http://localhost:8080/cloudi/api/rpc/services_add.erl

Browse the website at [http://localhost:8080/tutorial/java/](http://localhost:8080/tutorial/java/)

SERVICE API EXAMPLES
--------------------

Update an item's rating:

    curl -X POST -d '{"message_name": "recommendation_update", "user_id": 1, "item_id": 1, "rating": 5.0}' http://localhost:8080/tutorial/java/service/recommendation/update

Get the current list of recommendations:

    curl -X POST -d '{"message_name": "recommendation_list", "user_id": 1}' http://localhost:8080/tutorial/java/service/recommendation/list

Get the current list of items with the user's current ratings:

    curl -X POST -d '{"message_name": "item_list", "user_id": 1, "language": "en", "subject": "Philosophy"}' http://localhost:8080/tutorial/java/service/item/list

Get the current list of languages that items are available in:

    curl -X POST -d '{"message_name": "language_list"}' http://localhost:8080/tutorial/java/service/language/list

Get the current list of subjects that items are available in:

    curl -X POST -d '{"message_name": "subject_list"}' http://localhost:8080/tutorial/java/service/subject/list

Refresh the list of items with the books available at [gutenberg.org](http://www.gutenberg.org/):

    curl -X POST -d '{"message_name": "item_refresh"}' http://localhost:8080/tutorial/java/service/item/refresh

Update the recommendations model (public services often do this daily or weekly, but this can be done more frequently):

    curl -X POST -d '{"message_name": "recommendation_refresh"}' http://localhost:8080/tutorial/java/service/recommendation/refresh

