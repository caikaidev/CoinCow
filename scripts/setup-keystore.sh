#!/bin/bash

# Keystore Setup Script for Google Play Deployment
# This script helps generate and configure signing credentials

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Function to generate secure password
generate_password() {
    if command_exists openssl; then
        openssl rand -base64 32 | tr -d "=+/" | cut -c1-25
    else
        # Fallback to date-based random string
        date +%s | sha256sum | base64 | head -c 25
    fi
}

# Function to validate keystore
validate_keystore() {
    local keystore_file="$1"
    local keystore_password="$2"
    local key_alias="$3"
    
    if ! keytool -list -keystore "$keystore_file" -storepass "$keystore_password" -alias "$key_alias" >/dev/null 2>&1; then
        return 1
    fi
    return 0
}

# Main script
main() {
    print_info "🔐 Keystore Setup for Google Play Deployment"
    echo ""
    
    # Check if keytool is available
    if ! command_exists keytool; then
        print_error "keytool is not available. Please install Java JDK."
        exit 1
    fi
    
    # Check if base64 is available
    if ! command_exists base64; then
        print_error "base64 command is not available."
        exit 1
    fi
    
    # Configuration
    KEYSTORE_FILE="release.keystore"
    KEY_ALIAS="release"
    VALIDITY_DAYS=10000  # ~27 years
    
    # Check if keystore already exists
    if [ -f "$KEYSTORE_FILE" ]; then
        print_warning "Keystore file '$KEYSTORE_FILE' already exists."
        read -p "Do you want to create a new one? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Using existing keystore."
        else
            rm -f "$KEYSTORE_FILE"
            print_info "Removed existing keystore."
        fi
    fi
    
    # Generate keystore if it doesn't exist
    if [ ! -f "$KEYSTORE_FILE" ]; then
        print_info "Generating new keystore..."
        
        # Generate secure passwords
        KEYSTORE_PASSWORD=$(generate_password)
        KEY_PASSWORD=$(generate_password)
        
        print_info "Creating keystore with the following details:"
        echo "  - Keystore file: $KEYSTORE_FILE"
        echo "  - Key alias: $KEY_ALIAS"
        echo "  - Validity: $VALIDITY_DAYS days"
        echo ""
        
        # Create keystore
        keytool -genkey -v \
            -keystore "$KEYSTORE_FILE" \
            -alias "$KEY_ALIAS" \
            -keyalg RSA \
            -keysize 2048 \
            -validity $VALIDITY_DAYS \
            -storepass "$KEYSTORE_PASSWORD" \
            -keypass "$KEY_PASSWORD" \
            -dname "CN=Crypto Tracker App, OU=Development, O=KCode, L=Unknown, ST=Unknown, C=US"
        
        if [ $? -eq 0 ]; then
            print_success "Keystore created successfully!"
        else
            print_error "Failed to create keystore."
            exit 1
        fi
    else
        # Use existing keystore - prompt for passwords
        print_info "Using existing keystore: $KEYSTORE_FILE"
        read -s -p "Enter keystore password: " KEYSTORE_PASSWORD
        echo
        read -s -p "Enter key password: " KEY_PASSWORD
        echo
        
        # Validate existing keystore
        if ! validate_keystore "$KEYSTORE_FILE" "$KEYSTORE_PASSWORD" "$KEY_ALIAS"; then
            print_error "Invalid keystore credentials or keystore is corrupted."
            exit 1
        fi
        print_success "Keystore validation successful!"
    fi
    
    # Generate base64 encoded keystore
    print_info "Encoding keystore to base64..."
    KEYSTORE_BASE64=$(base64 -i "$KEYSTORE_FILE")
    
    if [ $? -eq 0 ]; then
        print_success "Keystore encoded successfully!"
    else
        print_error "Failed to encode keystore."
        exit 1
    fi
    
    # Create secrets file
    SECRETS_FILE="github-secrets.txt"
    print_info "Creating GitHub Secrets configuration..."
    
    cat > "$SECRETS_FILE" << EOF
# GitHub Secrets Configuration for Google Play Deployment
# Copy these values to your GitHub repository secrets

KEYSTORE_BASE64:
$KEYSTORE_BASE64

KEYSTORE_PASSWORD:
$KEYSTORE_PASSWORD

KEY_ALIAS:
$KEY_ALIAS

KEY_PASSWORD:
$KEY_PASSWORD

# Instructions:
# 1. Go to your GitHub repository
# 2. Navigate to Settings → Secrets and variables → Actions
# 3. Click "New repository secret" for each secret above
# 4. Copy the value (without the secret name and colon)
# 5. Delete this file after setting up secrets for security

# Additional secrets you may need:
# COINGECKO_API_KEY: Your CoinGecko API key
EOF
    
    print_success "Secrets configuration saved to: $SECRETS_FILE"
    
    # Display summary
    echo ""
    print_info "📋 Setup Summary:"
    echo "  ✅ Keystore file: $KEYSTORE_FILE"
    echo "  ✅ Key alias: $KEY_ALIAS"
    echo "  ✅ Base64 encoding: Complete"
    echo "  ✅ GitHub secrets file: $SECRETS_FILE"
    echo ""
    
    print_warning "🔒 Security Reminders:"
    echo "  • Keep your keystore file secure and backed up"
    echo "  • Never commit keystore or passwords to version control"
    echo "  • Delete the secrets file after configuring GitHub"
    echo "  • Store keystore password in a secure password manager"
    echo ""
    
    print_info "🚀 Next Steps:"
    echo "  1. Configure GitHub Secrets using the generated file"
    echo "  2. Test the build workflow with a test tag"
    echo "  3. Securely backup your keystore file"
    echo "  4. Delete the secrets file: rm $SECRETS_FILE"
    echo ""
    
    print_success "Keystore setup completed successfully! 🎉"
}

# Run main function
main "$@"