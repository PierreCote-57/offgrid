#!/bin/bash
#
#	ping.sh
#
#	Runs the ping monitor. The build goes through the Maven wrapper, as every build here does,
#	but the app itself runs as a plain java: the menu reads System.in, and spring-boot:run
#	forks its own JVM rather than handing the process a terminal.
#
set -e
cd "$(dirname "$0")"

CLASSPATH_FILE="target/pingClasspath.txt"
MAIN_CLASS="com.lc.offgrid.pingapp.OffgridApplicationPing"

./mvnw -q compile dependency:build-classpath -Dmdep.outputFile="$CLASSPATH_FILE"

java -cp "target/classes:$(cat "$CLASSPATH_FILE")" "$MAIN_CLASS" "$@"
