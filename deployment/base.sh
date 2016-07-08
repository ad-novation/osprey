#!/bin/sh
if [ -z `which java` ]; then
    echo "No Java executable found. Java version 8+ is required by this application."
    exit -1
fi
exec java -jar $0 "$@"
