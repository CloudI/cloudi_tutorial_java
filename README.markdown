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
    cat << EOF > tutorial.conf
    [[{prefix, "/tutorial/"},
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

    curl -X POST -d @tutorial.conf http://localhost:6464/cloudi/api/rpc/services_add.erl


Refresh the database:

    curl http://localhost:6464/tutorial/items/refresh


