#!/bin/sh

# This script is used to periodically exercise the MySQL Book Server to keep the connection alive

curl http://localhost:6467/recommend/book/newbooks

