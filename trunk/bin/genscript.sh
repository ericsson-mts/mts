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
# start IMSLoader CMD module
#
"$NGN_JAVA_HOME/bin/java" com.devoteam.srit.xmlloader.genscript.genscriptCmd $ARGS
# "/usr/lib/jvm/java-6-sun/bin/java" -Dfile.encoding=ISO-8859-15 -Djava.library.path=../lib -Xss128k -Xmx${MEMORY}m -XX:+UseConcMarkSweepGC -noverify com.devoteam.srit.xmlloader.cmd.TextImplementation $ARGS

# -noverify to not check class at run (useful for the sctp stack)
# -XX:+UseConcMarkSweepGC option = less freezes due to garbage collector but lower performances (~20%)
# but it can be used without problems if we have CPU time to spare

# -Djava.net.preferIPv4Stack=true option = to use a large number of IP addresses 
# (around 10000 ipaddresses)