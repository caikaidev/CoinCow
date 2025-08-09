# Google Play Store Deployment Guide

This guide covers the complete process of deploying the CoinCow crypto tracker app to Google Play Store.

## Prerequisites

### 1. Google Play Console Account
- Create a Google Play Console developer account ($25 one-time fee)
- Complete account verification and payment setup

### 2. App Signing Setup
- Generate a release keystore (if not already done)
- Configure GitHub Secrets for automated builds

### 3. Required Secrets Configuration

Set these secrets in your GitHub repository (Settings → Secrets and variables → Actions):

```bash
KEYSTORE_BASE64=<base64-encoded-keystore-file>
KEYSTORE_PASSWORD=<your-keystore-password>
KEY_ALIAS=<your-key-alias>
KEY_PASSWORD=<your-key-password>
COINGECKO_API_KEY=<your-coingecko-api-key>
```

## Step-by-Step Deployment Process

### Phase 1: Generate Release Build

#### Option A: Automatic Build (Recommended)
```bash
# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

#### Option B: Manual Build
1. Go to GitHub Actions tab
2. Select "Build and Release AAB" workflow
3. Click "Run workflow"
4. Enter version details
5. Download the generated AAB file

### Phase 2: Google Play Console Setup

#### 1. Create New App
1. Go to [Google Play Console](https://play.google.com/console)
2. Click "Create app"
3. Fill in app details:
   - **App name**: CoinCow - Crypto Tracker
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free

#### 2. App Content Configuration

**Privacy Policy**
- Upload the included `privacy-policy.html` to your website
- Add the URL to Google Play Console

**App Category**
- Category: Finance
- Tags: cryptocurrency, crypto, bitcoin, trading, portfolio

**Content Rating**
- Complete the content rating questionnaire
- Expected rating: Everyone

**Target Audience**
- Age group: 13+
- Appeals to children: No

#### 3. Store Listing

**App Details**
```
Short description (80 chars):
Real-time crypto prices, portfolio tracking, and market analysis

Full description (4000 chars):
CoinCow is a comprehensive cryptocurrency tracking app that provides real-time market data, portfolio management, and advanced analytics for crypto enthusiasts and traders.

🚀 Key Features:
• Real-time cryptocurrency prices and market data
• Personal portfolio tracking and management
• Interactive price charts with technical indicators
• Customizable watchlists for favorite coins
• Home screen widgets for quick price updates
• Offline support with local data caching
• Clean, Instagram-inspired user interface

📊 Market Data:
• Live prices for 1000+ cryptocurrencies
• 24h price changes and market cap data
• Historical price charts and trends
• Market volume and supply information
• Powered by CoinGecko API

💼 Portfolio Management:
• Track your crypto investments
• Calculate profits and losses
• Portfolio performance analytics
• Multiple portfolio support

🎨 Modern Design:
• Instagram-style card-based interface
• Material 3 design system
• Dark and light theme support
• Smooth animations and transitions

📱 Widgets:
• Home screen widgets for quick access
• Customizable widget layouts
• Real-time price updates
• Multiple widget sizes

Whether you're a seasoned trader or just getting started with cryptocurrency, CoinCow provides all the tools you need to stay informed and make better investment decisions.

Download CoinCow today and take control of your crypto portfolio!
```

**Graphics Assets Required**
- App icon: 512x512 PNG (already included in project)
- Feature graphic: 1024x500 PNG
- Screenshots: At least 2, up to 8 (phone screenshots)
- Optional: Tablet screenshots, TV screenshots

#### 4. App Bundle Upload

1. Go to "Release" → "Production"
2. Click "Create new release"
3. Upload the AAB file from GitHub Actions artifacts
4. Add release notes:

```
🎉 Initial Release - CoinCow v1.0.0

Welcome to CoinCow, your new favorite crypto tracking companion!

✨ What's New:
• Real-time cryptocurrency price tracking
• Beautiful Instagram-inspired interface
• Portfolio management and analytics
• Interactive price charts
• Home screen widgets
• Offline support with data caching

🚀 Features:
• Track 1000+ cryptocurrencies
• Customizable watchlists
• Real-time market data
• Portfolio performance tracking
• Material 3 design
• Dark/light theme support

This is our initial release. We're excited to bring you the best crypto tracking experience on Android!

Got feedback? Contact us at [your-email]
```

### Phase 3: Pre-Launch Testing

#### Internal Testing (Recommended)
1. Create an internal testing track
2. Add test users (up to 100)
3. Upload AAB to internal testing
4. Test thoroughly before production release

#### Closed Testing (Optional)
1. Create a closed testing track
2. Add external testers
3. Gather feedback and fix issues

### Phase 4: Production Release

#### Final Checklist
- [ ] All store listing information completed
- [ ] Privacy policy uploaded and linked
- [ ] Content rating completed
- [ ] App bundle uploaded and validated
- [ ] Release notes written
- [ ] Screenshots and graphics uploaded
- [ ] Pricing and distribution set
- [ ] App content declarations completed

#### Submit for Review
1. Review all information
2. Click "Send for review"
3. Wait for Google's review (typically 1-3 days)

## Common Issues and Solutions

### Build Issues

**Java Version Mismatch**
```bash
# Error: Unsupported Java version
# Solution: Ensure JDK 17 is used in GitHub Actions
```

**Keystore Problems**
```bash
# Error: Keystore validation failed
# Solution: Verify keystore secrets are correctly set
keytool -list -keystore release.keystore -storepass <password>
```

**Missing Dependencies**
```bash
# Error: Could not resolve dependencies
# Solution: Check gradle/libs.versions.toml for version conflicts
./gradlew dependencies --configuration releaseRuntimeClasspath
```

### Google Play Console Issues

**Upload Rejected**
- Ensure AAB is signed with release keystore
- Check that version code is higher than previous uploads
- Verify target SDK version meets Google's requirements

**Policy Violations**
- Review Google Play policies
- Ensure privacy policy is accessible
- Check content rating accuracy

**App Bundle Validation Errors**
- Use `bundletool` to validate AAB locally:
```bash
bundletool validate --bundle=app-release.aab
```

## Monitoring and Updates

### Post-Launch Monitoring
1. Monitor crash reports in Play Console
2. Check user reviews and ratings
3. Monitor app performance metrics
4. Track download and engagement statistics

### Update Process
1. Increment version code and name in `build.gradle.kts`
2. Create new Git tag: `git tag v1.0.1`
3. Push tag to trigger automated build
4. Upload new AAB to Play Console
5. Add release notes describing changes
6. Submit update for review

## Support and Resources

### Useful Links
- [Google Play Console](https://play.google.com/console)
- [Android App Bundle Documentation](https://developer.android.com/guide/app-bundle)
- [Google Play Policies](https://play.google.com/about/developer-content-policy/)
- [App Signing Documentation](https://developer.android.com/studio/publish/app-signing)

### Getting Help
- Check GitHub Actions logs for build issues
- Review Google Play Console help documentation
- Contact Google Play Developer Support for policy questions

## Security Best Practices

### Keystore Management
- Store keystore securely and create backups
- Use strong passwords for keystore and key
- Never commit keystore files to version control
- Rotate keys periodically for security

### API Key Security
- Store API keys in GitHub Secrets
- Use different keys for development and production
- Monitor API key usage and set rate limits
- Rotate API keys regularly

---

**Need Help?** 
If you encounter issues during deployment, check the troubleshooting section or create an issue in the GitHub repository.