#!/bin/sh

APP_HOME=$(cd -P "${0%/*}" >/dev/null && printf '%s\n' "$PWD") || exit
APP_BASE_NAME=${0##*/}

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1; then
    echo "ERROR: Java was not found. Set JAVA_HOME to a JDK 17 installation." >&2
    exit 1
fi

exec "$JAVACMD" -Xmx64m -Dorg.gradle.appname="$APP_BASE_NAME" \
    -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
