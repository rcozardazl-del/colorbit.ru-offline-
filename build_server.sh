#!/usr/bin/env bash
set -e

echo "=== Colorbit High-Performance Android Build Script ==="

CORES=$(nproc 2>/dev/null || echo 4)
RAM_KB=$(awk '/MemTotal/ {print $2}' /proc/meminfo 2>/dev/null || echo 8000000)
RAM_GB=$((RAM_KB / 1024 / 1024))
echo "Hardware detected: $CORES CPU cores, ${RAM_GB} GB RAM"

if [ "$RAM_GB" -ge 128 ]; then
  JVM_MAX="32g"
elif [ "$RAM_GB" -ge 32 ]; then
  JVM_MAX="16g"
elif [ "$RAM_GB" -ge 16 ]; then
  JVM_MAX="8g"
else
  JVM_MAX="4g"
fi

if [ ! -f "debug.keystore" ]; then
  if [ -f "debug.keystore.base64" ]; then
    base64 -d debug.keystore.base64 > debug.keystore
  else
    echo "Generating debug.keystore..."
    keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
  fi
fi

touch .env
cp -n .env.example .env 2>/dev/null || true
chmod +x gradlew

GRADLE_OPTS="-Xmx${JVM_MAX} -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Dorg.gradle.workers.max=${CORES} -Dorg.gradle.parallel=true -Dorg.gradle.caching=true -Dorg.gradle.vfs.watch=true" \
./gradlew assembleDebug --parallel --build-cache --max-workers="${CORES}" "$@"

echo "=== Build Complete! ==="
ls -la app/build/outputs/apk/debug/*.apk 2>/dev/null || true
