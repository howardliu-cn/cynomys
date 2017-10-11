#!/bin/sh

# resolve links - $0 may be a softlink
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set CYNOMYS_HOME if not already set
[ -z "${CYNOMYS_HOME}" ] && CYNOMYS_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

# Copy CYNOMYS_BASE from CYNOMYS_HOME if not already set
[ -z "${CYNOMYS_BASE}" ] && CYNOMYS_BASE="${CYNOMYS_HOME}"

# Ensure that any user defined CLASSPATH variables are not used on startup,
# but allow them to be specified in setenv.sh, in rare case when it is needed.
CLASSPATH=

if [ -r "${CYNOMYS_BASE}/bin/setenv.sh" ]; then
  . "${CYNOMYS_BASE}/bin/setenv.sh"
elif [ -r "${CYNOMYS_HOME}/bin/setenv.sh" ]; then
  . "${CYNOMYS_HOME}/bin/setenv.sh"
fi

# Ensure that neither CYNOMYS_HOME nor CYNOMYS_BASE contains a colon
# as this is used as the separator in the classpath and Java provides no
# mechanism for escaping if the same character appears in the path.
case ${CYNOMYS_HOME} in
  *:*) echo "Using CYNOMYS_HOME:   ${CYNOMYS_HOME}";
       echo "Unable to start as CYNOMYS_HOME contains a colon (:) character";
       exit 1;
esac
case ${CYNOMYS_BASE} in
  *:*) echo "Using CYNOMYS_BASE:   ${CYNOMYS_BASE}";
       echo "Unable to start as CYNOMYS_BASE contains a colon (:) character";
       exit 1;
esac

if [ -r "$CYNOMYS_HOME"/bin/setclasspath.sh ]; then
    . "$CYNOMYS_HOME"/bin/setclasspath.sh
else
    echo "Cannot find $CYNOMYS_HOME/bin/setclasspath.sh"
    echo "This file is needed to run this program"
    exit 1
fi

if [ ! -z "${CLASSPATH}" ]; then
  CLASSPATH="${CLASSPATH}":
fi
CLASSPATH="${CLASSPATH}""${CYNOMYS_HOME}/lib/*"

CYNOMYS_OUT=${CYNOMYS_OUT}
if [ -z "${CYNOMYS_OUT}" ] ; then
  CYNOMYS_OUT="$CYNOMYS_BASE"/logs/cynomys.out
fi

CYNOMYS_TIMDIR=${CYNOMYS_TIMDIR}
if [ -z "${CYNOMYS_TIMDIR}" ];then
  CYNOMYS_TIMDIR="$CYNOMYS_BASE"/temp
fi

LOGGING_CONFIG=${LOGGING_CONFIG}
if [ -z "${LOGGING_CONFIG}" ];then
  if [ -r "${CYNOMYS_BASE}/conf/logback.xml" ];then
    LOGGING_CONFIG="-Dlogback.configurationFile=${CYNOMYS_BASE}/conf/logback.xml"
  fi
fi

# Set UMASK unless it has been overridden
if [ -z "$UMASK" ]; then
    UMASK="0027"
fi
umask ${UMASK}

USE_NOHUP=${USE_NOHUP}
if [ -z "$USE_NOHUP" ]; then
    USE_NOHUP="true"
fi
unset _NOHUP
if [ "$USE_NOHUP" = "true" ]; then
    _NOHUP=nohup
fi

# When no TTY is available, don't output to console
have_tty=0
if [ "`tty`" != "not a tty" ]; then
    have_tty=1
fi

CYNOMYS_PID=${CYNOMYS_PID}
if [ -z "$CYNOMYS_PID" ] ; then
  CYNOMYS_PID="${CYNOMYS_BASE}/conf/CYNOMYS_PID"
fi

# Execute The Requested Command
#only output this if we have a TTY
if [ ${have_tty} -eq 1 ]; then
  echo "Using CYNOMYS_BASE:   $CYNOMYS_BASE"
  echo "Using CYNOMYS_HOME:   $CYNOMYS_HOME"
  echo "Using CYNOMYS_TIMDIR: $CYNOMYS_TIMDIR"
  echo "Using JAVA_HOME:      $JAVA_HOME"
  echo "Using JRE_HOME:       $JRE_HOME"
  echo "Using CLASSPATH:      $CLASSPATH"
  if [ ! -z "$JAVA_OPTS" ]; then
    echo "Using JAVA_OPTS:      \"${JAVA_OPTS}\""
  fi
  if [ ! -z "$CYNOMYS_PID" ]; then
    echo "Using CYNOMYS_PID:    $CYNOMYS_PID"
  fi
fi

if [ "$1" = "start" ]; then
  if [ -z "${CYNOMYS_PID}" ];then
    if [ -f "${CYNOMYS_PID}" ];then
      if [ -s "${CYNOMYS_PID}" ];then
        echo "Existing PID file found during start."
        if [ -r "${CYNOMYS_PID}" ];then
          PID=`cat ${CYNOMYS_PID}`
          ps -p ${PID} > /dev/null 2>&1
          if [ $? -eq 0 ];then
            echo "Cynomys Server appears to still be running with PID $PID. Start aborted."
            echo "If the following process is not a Cynomys Server process, remove the PID file and try again:"
            ps -f -p $PID
            exit 1
          else
            echo "Removing/clearing stale PID file."
            rm -f "$CYNOMYS_PID" /dev/null 2>&1
            if [ $? != 0 ];then
              if [ -w "$CYNOMYS_PID" ];then
                cat /dev/null > ${CYNOMYS_PID}
              else
                echo "Unable to remove or clear stale PID file. Start aborted."
                exit 1
              fi
            fi
          fi
        else
          echo "Unable to read PID file. Start aborted."
          exit 1
        fi
      else
        rm -f "${CYNOMYS_PID}" > /dev/null 2>&1
        if [ $? != 0 ];then
          if [ ! -w "$CYNOMYS_PID" ];then
            echo "Unable to remove or write to empty PID file. Start aborted."
            exit 1
          fi
        fi
      fi
    fi
  fi

  shift
  touch "${CYNOMYS_OUT}"

  eval ${_NOHUP} "\"${_RUN_JAVA}\"" "\"${LOGGING_CONFIG}\"" ${JAVA_OPTS} \
    -classpath "\"${CLASSPATH}\"" \
    -Dcynomys.base="\"${CYNOMYS_BASE}\"" \
    -Dcynomys.home="\"${CYNOMYS_HOME}\"" \
    -Djava.io.tmpdir="\"${CYNOMYS_TIMDIR}\"" \
    cn.howardliu.monitor.cynomys.proxy.CynomysProxyServer "$@" start \
    >> "${CYNOMYS_OUT}" 2>&1 &

  if [ ! -z "${CYNOMYS_PID}" ];then
    echo $! > "${CYNOMYS_PID}"
  fi

  echo "Cynomys Server started."

elif [ "$1" = "stop" ]; then
  shift
  SLEEP=5
  if [ ! -z "$1" ];then
    echo $1 | grep "[^0-9]" > /dev/null 2>&1
    if [ $? -gt 0 ];then
      SLEEP=$1
      shift
    fi
  fi

  FORCE=0
  if [ "$1" = "-force" ];then
    FORCE=1
    shift
  fi

  if [ ! -z "${CYNOMYS_PID}" ];then
    if [ -f "${CYNOMYS_PID}" ];then
      if [ -s "${CYNOMYS_PID}" ];then
        kill -0 `cat "${CYNOMYS_PID}"` > /dev/null 2>&1
        if [ $? -gt 0 ];then
          echo "PID file found but no matching process was found. Stop aborted."
          exit 1
        fi
      else
        echo "PID file is empty and has been ignored."
      fi
    else
      echo "$CYNOMYS_PID was set but the specified file does not exist. Is Cynomys Server running? Stop aborted."
      exit 1
    fi
  fi

  eval "\"${_RUN_JAVA}\"" \
    -classpath "\"${CLASSPATH}\"" \
    -Dcynomys.base="\"${CYNOMYS_BASE}\"" \
    -Dcynomys.home="\"${CYNOMYS_HOME}\"" \
    -Djava.io.tmpdir="\"${CYNOMYS_TIMDIR}\"" \
    cn.howardliu.monitor.cynomys.proxy.CynomysProxyServer "$@" stop

  # stop failed. Shutdown port disabled? Try a normal kill.
  if [ $? != 0 ];then
    if [ ! -z "${CYNOMYS_PID}" ];then
      echo "The stop command failed. Attempting to signal the process to stop through OS signal."
      kill -15 `cat "${CYNOMYS_PID}"` > /dev/null 2>&1
    fi
  fi

  if [ ! -z "${CYNOMYS_PID}" ];then
    if [ -f "${CYNOMYS_PID}" ];then
      while [ ${SLEEP} -ge 0 ]; do
        kill -0 `cat "${CYNOMYS_PID}"` > /dev/null 2>&1
        if [ $? -gt 0 ];then
          rm -f "${CYNOMYS_PID}" > /dev/null 2>&1
          if [ $? != 0 ];then
            if [ -w "${CYNOMYS_PID}" ];then
              cat /dev/null > "${CYNOMYS_PID}"
              # If Cynomys Server has stopped don't try and force a stop with an empty PID file.
              FORCE=0
            else
              echo "The PID file could not be removed or cleared."
            fi
          fi
          echo "Cynomys Server stopped."
          break
        fi
        if [ ${SLEEP} -gt 0 ];then
          sleep 1
        fi
        if [ ${SLEEP} -eq 0 ];then
          echo "Cynomys Server did not stop in time."
          if [ ${FORCE} -eq 0 ];then
            echo "PID file was not removed."
          fi
          echo "To aid diagnostics a thread dump has been written to standard out."
          kill -3 `cat "${CYNOMYS_PID}"`
        fi
        SLEEP=`expr ${SLEEP} - 1`
      done
    fi
  fi

  KILL_SLEEP_INTERVAL=5
  if [ ${FORCE} -eq 1 ];then
    if [ -z "${CYNOMYS_PID}" ];then
      echo "Kill failed: ${CYNOMYS_PID} not set"
    else
      if [ -f "$CYNOMYS_PID" ];then
        PID=`cat "$CYNOMYS_PID"`
        echo "Killing Cynomys Server with the PID: $PID"
        kill -9 $PID
        while [ ${KILL_SLEEP_INTERVAL} -ge 0 ];do
          kill -0 `cat "${CYNOMYS_PID}"` > /dev/null 2>&1
          if [ $? -gt 0 ];then
            rm -f "${CYNOMYS_PID}" > /dev/null 2>&1
            if [ $? != 0 ];then
              if [ -w "${CYNOMYS_PID}" ];then
                cat /dev/null > "${CYNOMYS_PID}"
              else
                echo "The PID file could not be removed."
              fi
            fi
            echo "The Cynomys Server process has been killed."
            break
          fi
          if [ ${KILL_SLEEP_INTERVAL} -gt 0 ];then
            sleep 1
          fi
          KILL_SLEEP_INTERVAL=`expr ${KILL_SLEEP_INTERVAL} - 1`
        done
        if [ ${KILL_SLEEP_INTERVAL} -lt 0 ];then
          echo "Cynomys Server has not been killed completely yet. The process might be waiting on some system call or might be UNINTERRUPTIBLE."
        fi
      fi
    fi
  fi
else
    echo "Usage: runner.sh (commands ... )"
    echo "commands:"
    echo "start          Start Cynomys Server in a separate window"
    echo "stop           Stop Cynomys Server, waiting up to 5 seconds for the process to end"
    echo "stop n         Stop Cynomys Server, waiting up to n seconds for the process to end"
    echo "stop -force    Stop Cynomys Server, waiting up to 5 seconds and then use kill -KILL if still running"
    echo "stop n -force  Stop Cynomys Server, waiting up to n seconds and then use kill -KILL if still running"
    echo "Note: Waiting for the process to end and use of the -force option require that \$CYNOMYS_PID is defined"
    exit 1
fi
