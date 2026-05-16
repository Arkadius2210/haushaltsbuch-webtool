#!/bin/sh
# Maven Wrapper script
# This script downloads and runs the Maven Wrapper JAR if not present.

set -e

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ -r "$MAVEN_WRAPPER_PROPERTIES" ]; then
    WRAPPER_URL=$(grep "wrapperUrl" "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
    DISTRIBUTION_URL=$(grep "distributionUrl" "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
fi

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
    echo "Downloading Maven Wrapper..."
    if command -v wget > /dev/null; then
        wget -q -O "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL"
    elif command -v curl > /dev/null; then
        curl -sS -o "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL"
    else
        echo "Error: Neither wget nor curl found." >&2
        exit 1
    fi
fi

exec java -jar "$MAVEN_WRAPPER_JAR" "$@"
