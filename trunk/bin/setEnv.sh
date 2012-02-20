#!/bin/sh

#
# Dynamically build the classpath
#
CLASSPATH=

NGN_JAVA_HOME=`cat java_home`

MEMORY=`cat memory`

for i in "../modules/"*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done
for i in "../lib/"*.jar; do
  CLASSPATH="$CLASSPATH":"$i"
done

while read line; do CLASSPATH="$CLASSPATH":"$line"; done < classpath

#
# Export classpath value
#
export CLASSPATH
export NGN_JAVA_HOME

#
# Set JRE useful options or shell commands (not used ATM)
#
# -noverify to not check class at run (useful for the sctp stack)
# -XX:+UseConcMarkSweepGC option : less freezes due to garbage collector but lower performances (~20%)
# but it can be used without problems if we have CPU time to spare

# -Djava.net.preferIPv4Stack=true option : to use a large number of IP addresses 
# (around 10000 ipaddresses)

# sudo ulimit -n 65735 shell command : to enlarge the maximum number of files to open simultaneously
# (note that network sockets are considered as files)
