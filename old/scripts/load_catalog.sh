#/bin/bash

#
# This script downloads the latest book file from Gutenberg and loads it into the database
#

DATA=/opt/book/data
CATALOG=/opt/book/utility

curl http://www.gutenberg.org/cache/epub/feeds/rdf-files.tar.zip -o $DATA/rdf-files.tar.zip

unzip -o $DATA/rdf-files.tar.zip -d $DATA
tar -xf $DATA/rdf-files.tar -C $DATA

curl http://localhost:6464/book/utility/load_catalog
