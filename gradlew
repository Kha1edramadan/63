#!/bin/sh
# Gradle wrapper script for Unix

# Attempt to set APP_HOME
APP_HOME="${0%/*}"
APP_HOME="$(cd "$APP_HOME" && pwd)"

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
APP_NAME="Gradle"
APP_BASE_NAME="${0##*/}"

# Add default JVM options here.
if [ -n "$JAVA_OPTS" ]; then
    DEFAULT_JVM_OPTS="$JAVA_OPTS"
fi

# Resolve JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    if [ -n "$(which java 2>/dev/null)" ]; then
        JAVA="java"
    else
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
        exit 1
    fi
else
    JAVA="$JAVA_HOME/bin/java"
fi

GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
GRADLE_WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "ERROR: gradle-wrapper.jar not found. Run 'gradle wrapper' first."
    exit 1
fi

exec "$JAVA" $DEFAULT_JVM_OPTS \
    -classpath "$GRADLE_WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
