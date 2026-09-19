#!/usr/bin/env sh

##############################################################################
# Gradle wrapper for DragonBoy
##############################################################################

APP_NAME="DragonBoy"
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Download wrapper jar if missing
if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "Downloading Gradle wrapper..."
    WRAPPER_URL="https://services.gradle.org/distributions/gradle-8.4-bin.zip"
    curl -L -o "/tmp/gradle-wrapper.jar" \
        "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || \
    wget -q -O "/tmp/gradle-wrapper.jar" \
        "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar"
    cp "/tmp/gradle-wrapper.jar" "$GRADLE_WRAPPER_JAR"
fi

exec java ${JVM_OPTS} \
    -classpath "$GRADLE_WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain "$@"
