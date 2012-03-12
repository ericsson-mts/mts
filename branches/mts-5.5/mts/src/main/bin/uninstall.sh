#!/bin/sh

#
# Set CLASSPATH variable
#
JAVA_HOME=`cat java_home`

#
# Start CML Loader Gui
#
"$JAVA_HOME/bin/java" -jar ../Uninstaller/uninstaller.jar