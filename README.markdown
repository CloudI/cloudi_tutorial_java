[CloudI](http://cloudi.org) Java Tutorial
=========================================

[![Build Status](https://travis-ci.org/CloudI/cloudi_tutorial_java.png)](https://travis-ci.org/CloudI/cloudi_tutorial_java)

This repository contains the example source code for the tutorial at
[http://cloudi.org/tutorial_java.html](http://cloudi.org/tutorial_java.html).  The source code provides local real-time recommendations for
the electronic books available at
[Project Gutenberg](http://www.gutenberg.org/).

While the source code is meant to show information related to Java development
with the Java CloudI API, additional source code dependencies are used:

* [Erlang/OTP jinterface Library](http://erlang.org/doc/apps/jinterface/jinterface_users_guide.html) for CloudI API usage of the Erlang Binary Term Format
* [Gson](https://github.com/google/gson) for JSON serialization in Java
* [Jcommander](http://jcommander.org/) for command-line parsing in Java of CloudI service configuration arguments
* [Lenskit](http://lenskit.org/) for the recommendations in Java
* [PostgreSQL JDBC Driver](http://search.maven.org/#artifactdetails|org.postgresql|postgresql|9.3-1104-jdbc4|jar) for Java database usage along with [PostgreSQL](https://www.postgresql.org/) and Lenskit
* [Simple Logging Facade for Java (SLF4J)](http://www.slf4j.org/) to allow Lenskit to use the CloudI log file through stderr (which keeps all the service log output in the `cloudi.log` file)

BUILD
-----

Use maven and JDK 1.7 or higher (>= Java 7) to build (and run):

    mvn clean package

RUNNING
-------

Refer to the [Java Tutorial](http://cloudi.org/tutorial_java.html#how_do_book_recommendations_occur) for detailed steps.

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

