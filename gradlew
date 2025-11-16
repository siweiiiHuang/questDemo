#!/usr/bin/env sh

set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Gradle wrapper jar not found. Please generate it with 'gradle wrapper'." >&2
  exit 1
fi

exec "$DIR/gradle/wrapper/gradle-wrapper" "$@"


