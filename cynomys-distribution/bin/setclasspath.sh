#!/bin/bash

JAVA_HOME=${JAVA_HOME}
JRE_HOME=${JRE_HOME}

if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
    JAVA_PATH=`which java 2>/dev/null`
    if [ "x$JAVA_PATH" != "x" ]; then
      JAVA_PATH=`dirname ${JAVA_PATH} 2>/dev/null`
      JRE_HOME=`dirname ${JAVA_PATH} 2>/dev/null`
    fi
    if [ "x$JRE_HOME" = "x" ]; then
      if [ -x /usr/bin/java ]; then
        JRE_HOME=/usr
      fi
    fi
    if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
        echo "Neither the JAVA_HOME nor the JRE_HOME environment variable is defined"
        echo "At least one of these environment variable is needed to run this program"
        exit 1
    fi
fi

if [ -z "$JRE_HOME" ]; then
  JRE_HOME="$JAVA_HOME"
fi

_RUN_JAVA=${_RUN_JAVA}
if [ -z "$_RUN_JAVA" ]; then
  _RUN_JAVA="$JRE_HOME"/bin/java
fi

if [ ! -e ${_RUN_JAVA} ]; then
    echo "Not found java command"
    exit 1
fi
