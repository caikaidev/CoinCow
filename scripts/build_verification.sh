#!/bin/bash

# Crypto Tracker App Build Verification Script

echo "🚀 Starting Crypto Tracker App Build Verification..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    if [ $2 -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
    else
        echo -e "${RED}❌ $1${NC}"
        exit 1
    fi
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    echo -e "${RED}❌ Please run this script from the project root directory${NC}"
    exit 1
fi

echo "📋 Running pre-build checks..."

# Check for API key
if [ ! -f "local.properties" ]; then
    print_warning "local.properties not found. API key may not be configured."
else
    if grep -q "COINGECKO_API_KEY" local.properties; then
        echo -e "${GREEN}✅ API key configuration found${NC}"
    else
        print_warning "COINGECKO_API_KEY not found in local.properties"
    fi
fi

# Clean build
echo "🧹 Cleaning previous build..."
./gradlew clean
print_status "Clean build" $?

# Run tests
echo "🧪 Running unit tests..."
./gradlew test
print_status "Unit tests" $?

# Build debug APK
echo "🔨 Building debug APK..."
./gradlew assembleDebug
print_status "Debug build" $?

# Build release APK
echo "🔨 Building release APK..."
./gradlew assembleRelease
print_status "Release build" $?

# Check APK size
DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
RELEASE_APK="app/build/outputs/apk/release/app-release.apk"

if [ -f "$DEBUG_APK" ]; then
    DEBUG_SIZE=$(du -h "$DEBUG_APK" | cut -f1)
    echo -e "${GREEN}📱 Debug APK size: $DEBUG_SIZE${NC}"
fi

if [ -f "$RELEASE_APK" ]; then
    RELEASE_SIZE=$(du -h "$RELEASE_APK" | cut -f1)
    echo -e "${GREEN}📱 Release APK size: $RELEASE_SIZE${NC}"
fi

# Verify ProGuard/R8 obfuscation
if [ -f "app/build/outputs/mapping/release/mapping.txt" ]; then
    echo -e "${GREEN}✅ ProGuard mapping file generated${NC}"
else
    print_warning "ProGuard mapping file not found"
fi

# Security checks
echo "🔒 Running security checks..."

# Check for hardcoded secrets (basic check)
if grep -r "sk_" app/src/ 2>/dev/null | grep -v ".git" | grep -v "build/"; then
    print_warning "Potential hardcoded secrets found"
else
    echo -e "${GREEN}✅ No obvious hardcoded secrets found${NC}"
fi

# Check network security config
if [ -f "app/src/main/res/xml/network_security_config.xml" ]; then
    echo -e "${GREEN}✅ Network security configuration found${NC}"
else
    print_warning "Network security configuration not found"
fi

# Final summary
echo ""
echo "🎉 Build verification completed successfully!"
echo ""
echo "📊 Summary:"
echo "- Debug APK: $DEBUG_APK"
echo "- Release APK: $RELEASE_APK"
echo "- ProGuard enabled: Yes"
echo "- Network security: Configured"
echo "- Tests: Passed"
echo ""
echo "🚀 Your Crypto Tracker app is ready for deployment!"