[CloudI](http://cloudi.org) Java Tutorial
=========================================

[![Build Status](https://travis-ci.org/CloudI/tutorial_book_service_example.png)](https://travis-ci.org/CloudI/tutorial_book_service_example)

The book recommendation system provides a convenient way to browse new electronic books available from Project Gutenberg and provides recommendations.

The repository is example source code for the CloudI Book Recommendation Service [tutorial](https://github.com/CloudI/tutorial_book_service).


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
      {args, "-ea:org.cloudi... "
             "-Duser.dir=$PWD "
             "-jar $PWD/target/cloudi_tutorial_java-1.5.1-SNAPSHOT-jar-with-dependencies.jar "
             "-pgsql_hostname localhost "
             "-pgsql_port 5432 "
             "-pgsql_database cloudi_tutorial_java "
             "-pgsql_username cloudi_tutorial_java "
             "-pgsql_password cloudi_tutorial_java"},
      {options, [{owner, [{user, "$USER"}]}]}]]
    EOF


To dynamically add the CloudI service configuration that starts the service's execution use:

    curl -X POST -d @tutorial.conf http://localhost:6464/cloudi/api/rpc/services_add.erl


Refresh the database:

    curl http://localhost:6464/tutorial/refresh


