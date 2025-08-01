# Keystore Management Scripts

This directory contains scripts for managing signing credentials for Google Play Store deployment.

## Scripts Overview

### 1. `setup-keystore.sh`
**Purpose**: Initial keystore creation and GitHub Secrets configuration.

**Usage**:
```bash
./scripts/setup-keystore.sh
```

**What it does**:
- Generates a new keystore with secure credentials
- Creates base64-encoded keystore for GitHub Secrets
- Generates a secrets configuration file
- Provides setup instructions

**When to use**:
- First-time setup for Google Play deployment
- When you need to create a new keystore from scratch

### 2. `validate-keystore.sh`
**Purpose**: Validates keystore configuration and credentials.

**Usage**:
```bash
# Using environment variables
export KEYSTORE_FILE="release.keystore"
export KEYSTORE_PASSWORD="your_password"
export KEY_ALIAS="release"
export KEY_PASSWORD="your_key_password"
./scripts/validate-keystore.sh

# Or with command line arguments
./scripts/validate-keystore.sh release.keystore password alias keypass
```

**What it does**:
- Validates keystore file integrity
- Checks credential correctness
- Tests signing simulation
- Displays keystore information
- Provides GitHub Secrets checklist

**When to use**:
- Before deploying to production
- Troubleshooting signing issues
- Verifying keystore after rotation

### 3. `rotate-keystore.sh`
**Purpose**: Rotates signing credentials for security.

**Usage**:
```bash
./scripts/rotate-keystore.sh
```

**What it does**:
- Backs up existing keystore
- Generates new signing credentials
- Creates new keystore or adds key to existing one
- Updates GitHub Secrets configuration
- Generates rotation documentation

**When to use**:
- Regular security maintenance (annually recommended)
- After suspected credential compromise
- When updating security policies

## Security Best Practices

### Keystore Security
- **Never commit keystores to version control**
- **Use strong, unique passwords**
- **Store keystores in secure, backed-up locations**
- **Limit access to keystore files**
- **Rotate credentials regularly**

### GitHub Secrets Management
- **Use repository secrets, not environment secrets**
- **Regularly audit secret access**
- **Remove unused secrets**
- **Use descriptive secret names**
- **Document secret purposes**

### Password Management
- **Use generated passwords (scripts provide this)**
- **Store passwords in secure password managers**
- **Never share passwords in plain text**
- **Use different passwords for keystore and key**

## Troubleshooting

### Common Issues

#### "keytool not found"
**Solution**: Install Java JDK
```bash
# macOS
brew install openjdk@11

# Ubuntu/Debian
sudo apt-get install openjdk-11-jdk

# CentOS/RHEL
sudo yum install java-11-openjdk-devel
```

#### "Invalid keystore password"
**Causes**:
- Incorrect password
- Corrupted keystore file
- Wrong keystore format

**Solutions**:
- Verify password accuracy
- Check keystore file integrity
- Regenerate keystore if corrupted

#### "Key alias not found"
**Causes**:
- Wrong alias name
- Key not present in keystore

**Solutions**:
- List keystore contents: `keytool -list -keystore release.keystore`
- Use correct alias name
- Add key to keystore if missing

#### "GitHub Actions build fails"
**Common causes**:
- Incorrect GitHub Secrets configuration
- Base64 encoding issues
- Environment variable problems

**Solutions**:
- Validate secrets using validation script
- Re-encode keystore to base64
- Check GitHub Actions logs for specific errors

### Validation Checklist

Before production deployment:
- [ ] Keystore file exists and is valid
- [ ] All passwords are correct
- [ ] Key alias exists in keystore
- [ ] GitHub Secrets are configured
- [ ] Local build works with keystore
- [ ] GitHub Actions workflow succeeds
- [ ] AAB file is properly signed

## File Structure

```
scripts/
├── README.md                 # This file
├── setup-keystore.sh        # Initial keystore setup
├── validate-keystore.sh     # Keystore validation
└── rotate-keystore.sh       # Credential rotation

Generated files (not committed):
├── release.keystore         # Signing keystore
├── github-secrets.txt       # Secrets configuration
├── keystore-backups/        # Keystore backups
└── keystore-rotation-*.md   # Rotation documentation
```

## Integration with CI/CD

These scripts integrate with the GitHub Actions workflows:

1. **Setup Phase**: Use `setup-keystore.sh` to create initial credentials
2. **Validation Phase**: Use `validate-keystore.sh` to verify configuration
3. **Maintenance Phase**: Use `rotate-keystore.sh` for regular security updates

The GitHub Actions workflow (`.github/workflows/release.yml`) expects these secrets:
- `KEYSTORE_BASE64`: Base64-encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias name
- `KEY_PASSWORD`: Key password

## Backup and Recovery

### Backup Strategy
- **Keystore Files**: Store in secure, encrypted backup
- **Passwords**: Use secure password manager
- **Documentation**: Keep rotation logs and setup notes

### Recovery Process
1. Restore keystore from backup
2. Verify keystore integrity with validation script
3. Update GitHub Secrets if needed
4. Test build process
5. Document recovery actions

## Compliance and Auditing

### Security Auditing
- Regular keystore validation
- GitHub Secrets access review
- Build process security assessment
- Credential rotation tracking

### Compliance Requirements
- Document all keystore operations
- Maintain backup and recovery procedures
- Regular security reviews
- Access control documentation

## Support

For issues with these scripts:
1. Check the troubleshooting section above
2. Validate your environment setup
3. Review GitHub Actions logs
4. Verify keystore file integrity

Remember: These scripts handle sensitive security credentials. Always follow security best practices and test thoroughly before production use.