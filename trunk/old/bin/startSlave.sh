#!/bin/sh

#
# Set CLASSPATH variable
#
. ./setEnv.sh

# Initialize the arguments list to pass to the java cmd
# We are not limited to a number of arguments
ARGS=""
while [ "$1" != "" ]
do
        ARGS="$ARGS $1"
        shift
done

#
# Start Slave Application
#
"$NGN_JAVA_HOME/bin/java" -Dfile.encoding=ISO-8859-15 -Djava.library.path=../lib -Xss128k -Xmx${MEMORY}m -XX:+UseConcMarkSweepGC -noverify com.devoteam.srit.xmlloader.master.SlaveImplementation $ARGS

read
