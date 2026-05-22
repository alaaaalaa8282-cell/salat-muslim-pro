#!/bin/sh
##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
APP_HOME="`pwd -P`"

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# OS specific support
JAVA_HOME_DIRS="/usr/lib/jvm/java-17-openjdk-amd64 /usr/lib/jvm/temurin-17"
for d in $JAVA_HOME_DIRS; do [ -d "$d" ] && export JAVA_HOME="$d" && break; done

JAVACMD="${JAVA_HOME:-}/bin/java"
[ -z "${JAVA_HOME:-}" ] && JAVACMD="java"

exec "$JAVACMD" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain "$@"
