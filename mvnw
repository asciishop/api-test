#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.3.2
#
# Optional ENV vars:
#   JAVA_HOME - location of a JDK home dir, required on some OSs
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
#     e.g. to debug Maven itself, use
#     set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
#   MAVEN_SKIP_RC - flag to disable loading of mavenrc files
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ] ; then
  if [ -f /usr/local/etc/mavenrc ] ; then
    . /usr/local/etc/mavenrc
  fi
  if [ -f /etc/mavenrc ] ; then
    . /etc/mavenrc
  fi
  if [ -f "$HOME/.mavenrc" ] ; then
    . "$HOME/.mavenrc"
  fi
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true;;
  Darwin*) darwin=true ;;
esac

if [ "$JAVA_HOME" = "" ] ; then
  if $darwin ; then
    JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null)
    [ -z "$JAVA_HOME" ] && JAVA_HOME=/Library/Java/Home
  fi
fi

if [ -z "$JAVA_HOME" ] ; then
  javaExecutable="$(which java 2>/dev/null)"
  if [ -n "$javaExecutable" ] && ! [ "$(expr "\"$javaExecutable\"" : '\([^ ]*\)')" = "no" ]; then
    readLink=$(which readlink 2>/dev/null)
    if [ ! $(expr "$readLink" : '\([^ ]*\)') = "no" ]; then
      if $cygwin ; then
        javaExecutable=$(cygpath --unix "$javaExecutable")
      fi
      javaExecutable=$(readlink -f "$javaExecutable")
    fi
    javaHome=$(dirname "\"$javaExecutable\"")
    javaExecutable="$javaHome/../bin/java"
    JAVA_HOME="$javaHome/.."
  fi
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Error: JAVA_HOME not found in your environment." >&2
  echo "Please set the JAVA_HOME variable in your environment to match the" >&2
  echo "location of your Java installation." >&2
  exit 1
fi

MAVEN_OPTS="${MAVEN_OPTS:-}"

find_maven_basedir() {
  if [ -z "$1" ]
  then
    echo "Path not specified to find_maven_basedir"
    return 1
  fi

  basedir="$1"
  wdir="$1"
  while [ "$wdir" != '/' ] ; do
    if [ -d "$wdir"/.mvn ] ; then
      basedir=$wdir
      break
    fi
    wdir=$(cd "$wdir/.."; pwd)
  done
  echo "${basedir}"
}

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-}"
if [ "$MAVEN_PROJECTBASEDIR" = "" ] ; then
  MAVEN_PROJECTBASEDIR=$(find_maven_basedir "$(dirname "$0")")
  export MAVEN_PROJECTBASEDIR
fi

WRAPPER_JAR="${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

# shellcheck disable=SC2086
exec "$JAVA_HOME/bin/java" \
  ${MAVEN_OPTS} \
  ${MAVEN_DEBUG_OPTS} \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  -classpath "${WRAPPER_JAR}" \
  ${WRAPPER_LAUNCHER} \
  "$@"
