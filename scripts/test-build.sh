#!/bin/bash

# Test build script for local development
# This script mimics the GitHub Actions build process

set -e

echo "🔧 Testing local build process..."

# Check if required tools are available
echo "📋 Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "17" ]; then
    echo "❌ Java 17 or higher required. Found Java $JAVA_VERSION"
    exit 1
fi

echo "✅ Java $JAVA_VERSION found"

# Check if local.properties exists
if [ ! -f "local.properties" ]; then
    echo "⚠️  local.properties not found. Creating with dummy API key..."
    echo "COINGECKO_API_KEY=dummy_key_for_testing" > local.properties
fi

# Make gradlew executable
chmod +x ./gradlew

echo "🧹 Cleaning previous builds..."
./gradlew clean

echo "🔍 Running lint checks..."
./gradlew lintDebug

echo "🏗️  Building debug APK..."
./gradlew assembleDebug

echo "🧪 Running unit tests..."
./gradlew testDebugUnitTest

echo "📦 Building release AAB (if keystore is configured)..."
if [ -n "$KEYSTORE_FILE" ] && [ -n "$KEYSTORE_PASSWORD" ] && [ -n "$KEY_ALIAS" ] && [ -n "$KEY_PASSWORD" ]; then
    echo "🔑 Release keystore found, building signed AAB..."
    ./gradlew bundleRelease
else
    echo "⚠️  Release keystore not configured. Skipping AAB build."
    echo "   To build AAB, set these environment variables:"
    echo "   - KEYSTORE_FILE"
    echo "   - KEYSTORE_PASSWORD" 
    echo "   - KEY_ALIAS"
    echo "   - KEY_PASSWORD"
fi

echo "✅ Build test completed successfully!"

# Print build artifacts
echo ""
echo "📊 Build artifacts:"
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "  ✅ Debug APK: $(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)"
fi

if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
    echo "  ✅ Release AAB: $(du -h app/build/outputs/bundle/release/app-release.aab | cut -f1)"
fi

echo ""
echo "🎉 All checks passed! Your build should work in GitHub Actions."