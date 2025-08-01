# GitHub Actions Workflows

This directory contains GitHub Actions workflows for automated building and deployment of the Crypto Tracker App.

## Workflows

### 1. Release Build (`release.yml`)

**Purpose**: Automatically builds and releases AAB files for Google Play Store deployment.

**Triggers**:
- **Git Tags**: Automatically triggered when pushing tags matching `v*` pattern (e.g., `v1.0.0`)
- **Manual Dispatch**: Can be manually triggered from GitHub Actions tab with custom version parameters

**What it does**:
- Sets up Android build environment (JDK 11, Android SDK)
- Caches Gradle dependencies for faster builds
- Decodes signing keystore from GitHub Secrets
- Builds signed AAB (Android App Bundle)
- Uploads build artifacts (AAB and ProGuard mapping)
- Creates GitHub release with detailed information

**Required Secrets**:
- `KEYSTORE_BASE64`: Base64-encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias name
- `KEY_PASSWORD`: Key password
- `COINGECKO_API_KEY`: API key for CoinGecko service

### 2. Build Check (`build-check.yml`)

**Purpose**: Validates code quality and build integrity on pull requests and pushes.

**Triggers**:
- Pull requests to `main` or `develop` branches
- Pushes to `main` or `develop` branches

**What it does**:
- Builds debug APK to verify compilation
- Runs lint checks for code quality
- Executes unit tests
- Uploads test and lint reports as artifacts

## Usage

### Creating a Release

#### Method 1: Git Tags (Recommended)
```bash
# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

#### Method 2: Manual Dispatch
1. Go to GitHub Actions tab
2. Select "Build and Release AAB" workflow
3. Click "Run workflow"
4. Enter version name and code (optional)
5. Click "Run workflow"

### Setting up Secrets

Before using the release workflow, configure these secrets in your GitHub repository:

1. **Generate Keystore** (if you don't have one):
```bash
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
```

2. **Encode Keystore to Base64**:
```bash
base64 -i release.keystore | pbcopy  # macOS
base64 -i release.keystore | xclip -selection clipboard  # Linux
```

3. **Add Secrets in GitHub**:
   - Go to Settings → Secrets and variables → Actions
   - Add the following secrets:
     - `KEYSTORE_BASE64`: The base64-encoded keystore content
     - `KEYSTORE_PASSWORD`: Your keystore password
     - `KEY_ALIAS`: Your key alias (e.g., "release")
     - `KEY_PASSWORD`: Your key password
     - `COINGECKO_API_KEY`: Your CoinGecko API key

### Version Management

The workflow automatically manages versions:

- **Version Name**: Extracted from Git tag (removes 'v' prefix)
- **Version Code**: Uses GitHub run number for uniqueness
- **Manual Override**: Can specify custom versions via manual dispatch

### Build Artifacts

Each successful build generates:

- **AAB File**: Ready for Google Play Store upload
- **ProGuard Mapping**: For crash report analysis
- **Build Reports**: Lint and test results (build-check only)

### Troubleshooting

#### Common Issues

1. **Keystore Errors**:
   - Verify keystore secrets are correctly set
   - Ensure keystore is valid and not corrupted
   - Check that alias and passwords match

2. **Build Failures**:
   - Check Gradle build logs in Actions tab
   - Verify all dependencies are available
   - Ensure API keys are properly configured

3. **Version Conflicts**:
   - Each release must have a unique version code
   - Use different tags for different releases
   - Check existing releases for version conflicts

#### Getting Help

- Check the Actions tab for detailed build logs
- Review the build summary in each workflow run
- Verify all required secrets are configured
- Ensure your local build works before pushing

## Security Notes

- Keystore and passwords are stored as encrypted GitHub Secrets
- Secrets are only accessible during workflow execution
- Keystore files are automatically cleaned up after builds
- API keys are injected securely during build time

## Maintenance

- Update workflow dependencies regularly
- Rotate signing credentials periodically
- Monitor build success rates and performance
- Keep ProGuard mapping files for crash analysis