[CloudI](https://cloudi.org) Java Tutorial
=========================================

[![Build Status](https://app.travis-ci.com/CloudI/cloudi_tutorial_java.svg?branch=master)](https://app.travis-ci.com/CloudI/cloudi_tutorial_java)

This repository contains the example source code for the tutorial at
[https://cloudi.org/tutorial_java.html](https://cloudi.org/tutorial_java.html).  The source code provides local real-time recommendations for
the electronic books available at
[Project Gutenberg](http://www.gutenberg.org/).

While the source code is meant to show information related to Java development
with the Java CloudI API, additional source code dependencies are used:

* [Apache Log4j](https://logging.apache.org/log4j/) to allow Apache Mahout to use the CloudI log file through stdout/stderr (which keeps all the service log output in the `cloudi.log` file)
* [Apache Commons DBCP](http://commons.apache.org/dbcp/) for the Apache Mahout database connection pool
* [Apache Mahout](https://mahout.apache.org/) for the recommendations in Java
* [Gson](https://github.com/google/gson) for JSON serialization in Java
* [Jcommander](http://jcommander.org/) for command-line parsing in Java of CloudI service configuration arguments
* [PostgreSQL Driver](https://search.maven.org/artifact/org.postgresql/postgresql/9.4.1212.jre7/bundle) for Java database usage along with [PostgreSQL](https://www.postgresql.org/) and Apache Mahout

BUILD
-----

Use maven and JDK 1.7 or 1.8 (Java 7 or 8) to build:

    mvn clean package

RUNNING
-------

Refer to the [Java Tutorial](https://cloudi.org/tutorial_java.html#how_do_book_recommendations_occur) for detailed steps.

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

