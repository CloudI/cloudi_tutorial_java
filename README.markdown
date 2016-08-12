[CloudI](http://cloudi.org) Java Tutorial
=========================================

[![Build Status](https://travis-ci.org/CloudI/cloudi_tutorial_java.png)](https://travis-ci.org/CloudI/cloudi_tutorial_java)

The book recommendation system provides a convenient way to browse new electronic books available from Project Gutenberg and provide your own recommendations.

BUILD
-----

    mvn clean package

RUN
---

To execute the tutorial dynamically, it is necessary to create the CloudI service configuration that specifies both the initialization and fault-tolerance constraints the CloudI service should be executed with (with the proplist format to rely on defaults): 

    export JAVA=`which java`
    export PWD=`pwd`
    export USER=`whoami`
    cat << EOF > website.conf
    [[{prefix, "/tutorial/java/"},
      {module, cloudi_service_filesystem},
      {args,
       [{directory, "$PWD/html/"}]},
      {dest_refresh, none},
      {count_process, 4}],
     [{prefix, "/tutorial/java/client/"},
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

    psql -h localhost cloudi_tutorial_java cloudi_tutorial_java < schema.sql


To dynamically add the CloudI service configuration that starts the service's execution use:

    curl -X POST -d @website.conf http://localhost:6464/cloudi/api/rpc/services_add.erl
    curl -X POST -d @tutorial.conf http://localhost:8080/cloudi/api/rpc/services_add.erl

Browse the website at [http://localhost:8080/tutorial/java/](http://localhost:8080/tutorial/java/)

SERVICE API EXAMPLES
--------------------

Refresh the database:

    curl -X POST -d '{}' http://localhost:8080/tutorial/java/service/item/refresh

Get the current list of items with the user's current ratings:

    curl -X POST -d '{"user_id": 1}' http://localhost:8080/tutorial/java/service/item/list

Update the recommendations model:

    curl -X POST -d '{"user_id": 1}' http://localhost:8080/tutorial/java/service/recommendation/refresh

Get the current list of recommendations:

    curl -X POST -d '{"user_id": 1}' http://localhost:8080/tutorial/java/service/recommendation/list

Update an item's rating:

    curl -X POST -d '{"user_id": 1, "item_id": 1, "rating": 5.0}' http://localhost:8080/tutorial/java/service/recommendation/update

