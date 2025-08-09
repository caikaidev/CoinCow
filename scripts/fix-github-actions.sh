#!/bin/bash

# GitHub Actions Fix Script
# This script addresses common issues with the CI/CD pipeline

set -e

echo "🔧 Fixing GitHub Actions Configuration..."

# Check if we're in the right directory
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Error: Not in project root directory"
    echo "Please run this script from the project root"
    exit 1
fi

echo "📋 Checking current configuration..."

# Check Java version in workflows
echo "🔍 Checking Java version in workflows..."
if grep -q "java-version: '11'" .github/workflows/*.yml; then
    echo "⚠️  Found Java 11 configuration in workflows"
    echo "   Updating to Java 17..."
    
    # Update build-check.yml
    if [ -f ".github/workflows/build-check.yml" ]; then
        sed -i.bak "s/java-version: '11'/java-version: '17'/g" .github/workflows/build-check.yml
        rm -f .github/workflows/build-check.yml.bak
        echo "   ✅ Updated build-check.yml"
    fi
    
    # Update release.yml
    if [ -f ".github/workflows/release.yml" ]; then
        sed -i.bak "s/java-version: '11'/java-version: '17'/g" .github/workflows/release.yml
        rm -f .github/workflows/release.yml.bak
        echo "   ✅ Updated release.yml"
    fi
else
    echo "✅ Java version configuration is correct"
fi

# Check if CryptoWidgetReceiver exists
echo "🔍 Checking widget receiver class..."
if [ ! -f "app/src/main/java/com/kcode/gankotlin/widget/CryptoWidgetReceiver.kt" ]; then
    echo "❌ CryptoWidgetReceiver.kt is missing"
    echo "   This will cause build failures"
    echo "   Please create the missing widget receiver class"
else
    echo "✅ Widget receiver class exists"
fi

# Check local.properties for API key
echo "🔍 Checking local.properties..."
if [ ! -f "local.properties" ]; then
    echo "⚠️  local.properties not found"
    echo "   Creating with dummy API key for local builds..."
    echo "COINGECKO_API_KEY=dummy_key_for_local_build" > local.properties
    echo "   ✅ Created local.properties"
    echo "   📝 Remember to add your real API key for production builds"
else
    if ! grep -q "COINGECKO_API_KEY" local.properties; then
        echo "⚠️  COINGECKO_API_KEY not found in local.properties"
        echo "   Adding dummy API key..."
        echo "COINGECKO_API_KEY=dummy_key_for_local_build" >> local.properties
        echo "   ✅ Added API key to local.properties"
    else
        echo "✅ API key configuration found"
    fi
fi

# Check required secrets documentation
echo "🔍 Checking GitHub Secrets documentation..."
echo ""
echo "📝 Required GitHub Secrets:"
echo "   The following secrets must be configured in your GitHub repository:"
echo "   (Settings → Secrets and variables → Actions)"
echo ""
echo "   🔑 KEYSTORE_BASE64     - Base64 encoded keystore file"
echo "   🔑 KEYSTORE_PASSWORD   - Keystore password"
echo "   🔑 KEY_ALIAS          - Key alias name"
echo "   🔑 KEY_PASSWORD       - Key password"
echo "   🔑 COINGECKO_API_KEY  - CoinGecko API key"
echo ""

# Test local build
echo "🧪 Testing local build..."
if ./gradlew assembleDebug --dry-run > /dev/null 2>&1; then
    echo "✅ Local build configuration looks good"
else
    echo "⚠️  Local build test failed"
    echo "   Run './gradlew assembleDebug' to see detailed errors"
fi

echo ""
echo "🎉 GitHub Actions fix script completed!"
echo ""
echo "📋 Next Steps:"
echo "1. Commit and push the changes made by this script"
echo "2. Ensure all required secrets are configured in GitHub"
echo "3. Test the workflow by creating a new tag: git tag v1.0.0 && git push origin v1.0.0"
echo "4. Monitor the GitHub Actions tab for build results"
echo ""
echo "🔗 Useful Links:"
echo "   • GitHub Actions: https://github.com/$(git config --get remote.origin.url | sed 's/.*github.com[:/]\([^.]*\).*/\1/')/actions"
echo "   • Secrets Settings: https://github.com/$(git config --get remote.origin.url | sed 's/.*github.com[:/]\([^.]*\).*/\1/')/settings/secrets/actions"