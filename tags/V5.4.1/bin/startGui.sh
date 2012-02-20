#!/bin/sh

#
# Set CLASSPATH variable
#
. ./setEnv.sh

#
# start GUI interface
#
"$NGN_JAVA_HOME/bin/java" -Dfile.encoding=ISO-8859-15 -Djava.library.path=../lib -Xss128k -Xmx${MEMORY}m -XX:+UseConcMarkSweepGC -noverify com.devoteam.srit.xmlloader.gui.TesterGui $1

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

# wait for the user to press a key after IMSLoader ends
read
