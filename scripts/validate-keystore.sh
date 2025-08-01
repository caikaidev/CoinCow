#!/bin/bash

# Keystore Validation Script
# This script validates keystore configuration and GitHub Secrets setup

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to validate keystore file
validate_keystore_file() {
    local keystore_file="$1"
    local keystore_password="$2"
    local key_alias="$3"
    local key_password="$4"
    
    print_info "Validating keystore file: $keystore_file"
    
    # Check if file exists
    if [ ! -f "$keystore_file" ]; then
        print_error "Keystore file not found: $keystore_file"
        return 1
    fi
    
    # Check keystore integrity
    if ! keytool -list -keystore "$keystore_file" -storepass "$keystore_password" >/dev/null 2>&1; then
        print_error "Invalid keystore password or corrupted keystore"
        return 1
    fi
    
    # Check if alias exists
    if ! keytool -list -keystore "$keystore_file" -storepass "$keystore_password" -alias "$key_alias" >/dev/null 2>&1; then
        print_error "Key alias '$key_alias' not found in keystore"
        return 1
    fi
    
    # Validate key password
    if ! keytool -list -keystore "$keystore_file" -storepass "$keystore_password" -alias "$key_alias" -keypass "$key_password" >/dev/null 2>&1; then
        print_error "Invalid key password for alias '$key_alias'"
        return 1
    fi
    
    print_success "Keystore validation passed"
    return 0
}

# Function to display keystore information
display_keystore_info() {
    local keystore_file="$1"
    local keystore_password="$2"
    local key_alias="$3"
    
    print_info "Keystore Information:"
    echo ""
    
    # Display keystore details
    keytool -list -v -keystore "$keystore_file" -storepass "$keystore_password" -alias "$key_alias" | grep -E "(Alias name|Creation date|Entry type|Certificate chain length|Certificate fingerprints)"
    
    echo ""
    
    # Display certificate validity
    local validity_info=$(keytool -list -v -keystore "$keystore_file" -storepass "$keystore_password" -alias "$key_alias" | grep -A 1 "Valid from")
    echo "Certificate Validity:"
    echo "$validity_info"
    echo ""
}

# Function to test signing simulation
test_signing_simulation() {
    local keystore_file="$1"
    local keystore_password="$2"
    local key_alias="$3"
    local key_password="$4"
    
    print_info "Testing signing simulation..."
    
    # Create a dummy file to sign
    local test_file="test_signing.txt"
    echo "Test content for signing validation" > "$test_file"
    
    # Try to sign the file (this simulates APK signing)
    if command_exists jarsigner; then
        if jarsigner -keystore "$keystore_file" -storepass "$keystore_password" -keypass "$key_password" -signedjar "signed_$test_file" "$test_file" "$key_alias" >/dev/null 2>&1; then
            print_success "Signing simulation successful"
            rm -f "$test_file" "signed_$test_file"
            return 0
        else
            print_error "Signing simulation failed"
            rm -f "$test_file" "signed_$test_file"
            return 1
        fi
    else
        print_warning "jarsigner not available, skipping signing simulation"
        rm -f "$test_file"
        return 0
    fi
}

# Function to validate environment variables
validate_env_vars() {
    print_info "Validating environment variables..."
    
    local missing_vars=()
    
    if [ -z "$KEYSTORE_FILE" ]; then
        missing_vars+=("KEYSTORE_FILE")
    fi
    
    if [ -z "$KEYSTORE_PASSWORD" ]; then
        missing_vars+=("KEYSTORE_PASSWORD")
    fi
    
    if [ -z "$KEY_ALIAS" ]; then
        missing_vars+=("KEY_ALIAS")
    fi
    
    if [ -z "$KEY_PASSWORD" ]; then
        missing_vars+=("KEY_PASSWORD")
    fi
    
    if [ ${#missing_vars[@]} -gt 0 ]; then
        print_error "Missing environment variables: ${missing_vars[*]}"
        print_info "Set these variables or provide them as command line arguments"
        return 1
    fi
    
    print_success "All required environment variables are set"
    return 0
}

# Function to check GitHub Secrets setup
check_github_secrets() {
    print_info "GitHub Secrets Checklist:"
    echo ""
    echo "Ensure these secrets are configured in your GitHub repository:"
    echo "  • KEYSTORE_BASE64 - Base64 encoded keystore file"
    echo "  • KEYSTORE_PASSWORD - Keystore password"
    echo "  • KEY_ALIAS - Key alias name"
    echo "  • KEY_PASSWORD - Key password"
    echo "  • COINGECKO_API_KEY - CoinGecko API key"
    echo ""
    echo "To configure secrets:"
    echo "  1. Go to GitHub repository → Settings → Secrets and variables → Actions"
    echo "  2. Click 'New repository secret'"
    echo "  3. Add each secret with the exact name and value"
    echo ""
}

# Main function
main() {
    print_info "🔐 Keystore Validation Tool"
    echo ""
    
    # Check dependencies
    if ! command_exists keytool; then
        print_error "keytool is not available. Please install Java JDK."
        exit 1
    fi
    
    # Parse command line arguments or use environment variables
    KEYSTORE_FILE="${1:-$KEYSTORE_FILE}"
    KEYSTORE_PASSWORD="${2:-$KEYSTORE_PASSWORD}"
    KEY_ALIAS="${3:-$KEY_ALIAS}"
    KEY_PASSWORD="${4:-$KEY_PASSWORD}"
    
    # If no arguments provided, try to validate environment variables
    if [ $# -eq 0 ]; then
        if ! validate_env_vars; then
            print_info "Usage: $0 <keystore_file> <keystore_password> <key_alias> <key_password>"
            print_info "Or set environment variables: KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD"
            exit 1
        fi
    fi
    
    # Validate required parameters
    if [ -z "$KEYSTORE_FILE" ] || [ -z "$KEYSTORE_PASSWORD" ] || [ -z "$KEY_ALIAS" ] || [ -z "$KEY_PASSWORD" ]; then
        print_error "Missing required parameters"
        print_info "Usage: $0 <keystore_file> <keystore_password> <key_alias> <key_password>"
        exit 1
    fi
    
    # Run validations
    local validation_passed=true
    
    # Validate keystore
    if ! validate_keystore_file "$KEYSTORE_FILE" "$KEYSTORE_PASSWORD" "$KEY_ALIAS" "$KEY_PASSWORD"; then
        validation_passed=false
    fi
    
    # Display keystore information if validation passed
    if [ "$validation_passed" = true ]; then
        display_keystore_info "$KEYSTORE_FILE" "$KEYSTORE_PASSWORD" "$KEY_ALIAS"
        
        # Test signing simulation
        if ! test_signing_simulation "$KEYSTORE_FILE" "$KEYSTORE_PASSWORD" "$KEY_ALIAS" "$KEY_PASSWORD"; then
            validation_passed=false
        fi
    fi
    
    # Show GitHub Secrets checklist
    check_github_secrets
    
    # Final result
    echo ""
    if [ "$validation_passed" = true ]; then
        print_success "🎉 All validations passed! Your keystore is ready for production use."
        echo ""
        print_info "Next steps:"
        echo "  1. Ensure GitHub Secrets are configured correctly"
        echo "  2. Test the GitHub Actions workflow with a test tag"
        echo "  3. Backup your keystore file securely"
    else
        print_error "❌ Validation failed. Please fix the issues above."
        exit 1
    fi
}

# Run main function
main "$@"