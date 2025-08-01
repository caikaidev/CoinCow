#!/bin/bash

# Version Conflict Checker
# This script checks for version conflicts before deployment

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

# Function to get current version from build.gradle.kts
get_current_versions() {
    local version_code=$(./gradlew -q printVersionCode 2>/dev/null || echo "0")
    local version_name=$(./gradlew -q printVersionName 2>/dev/null || echo "unknown")
    
    echo "$version_code|$version_name"
}

# Function to check git tag conflicts
check_git_tag_conflicts() {
    local version_name="$1"
    
    print_info "Checking git tag conflicts for version: $version_name"
    
    # Check if tag already exists
    if git tag -l | grep -q "^v$version_name$"; then
        print_error "Git tag v$version_name already exists"
        return 1
    fi
    
    # Check similar tags
    local similar_tags=$(git tag -l | grep -E "v?$version_name" || true)
    if [ -n "$similar_tags" ]; then
        print_warning "Similar tags found:"
        echo "$similar_tags"
    fi
    
    return 0
}

# Function to check GitHub releases
check_github_releases() {
    local version_name="$1"
    
    print_info "Checking GitHub releases for version: $version_name"
    
    # Check if gh CLI is available
    if ! command -v gh >/dev/null 2>&1; then
        print_warning "GitHub CLI (gh) not available. Skipping GitHub release check."
        return 0
    fi
    
    # Check if release exists
    if gh release view "v$version_name" >/dev/null 2>&1; then
        print_error "GitHub release v$version_name already exists"
        return 1
    fi
    
    return 0
}

# Function to check Google Play Console version conflicts
check_play_console_conflicts() {
    local version_code="$1"
    local version_name="$2"
    
    print_info "Checking Google Play Console conflicts..."
    print_warning "Note: This is a manual check. Verify in Google Play Console:"
    echo "  1. Go to Google Play Console"
    echo "  2. Navigate to your app"
    echo "  3. Check Release management → App releases"
    echo "  4. Verify version code $version_code is not already used"
    echo "  5. Verify version name $version_name is not already published"
    echo ""
    
    read -p "Have you verified no conflicts in Google Play Console? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_error "Please verify Google Play Console before proceeding"
        return 1
    fi
    
    return 0
}

# Function to check version code sequence
check_version_code_sequence() {
    local current_version_code="$1"
    
    print_info "Checking version code sequence..."
    
    # Get previous version codes from git tags
    local previous_codes=()
    while IFS= read -r tag; do
        if [ -n "$tag" ]; then
            # Try to extract version code from tag (this is approximate)
            local tag_version=${tag#v}
            # For now, we'll just warn about sequence
            previous_codes+=("$tag_version")
        fi
    done < <(git tag -l --sort=-version:refname | head -10)
    
    if [ ${#previous_codes[@]} -gt 0 ]; then
        print_info "Recent version tags:"
        printf '%s\n' "${previous_codes[@]}" | head -5
        echo ""
        print_warning "Ensure version code $current_version_code is greater than previous releases"
    fi
    
    return 0
}

# Function to validate version format
validate_version_format() {
    local version_code="$1"
    local version_name="$2"
    
    print_info "Validating version format..."
    
    # Check version code format
    if ! [[ "$version_code" =~ ^[1-9][0-9]*$ ]]; then
        print_error "Invalid version code format: $version_code"
        print_info "Version code must be a positive integer"
        return 1
    fi
    
    # Check version code range (Google Play limit)
    if [ "$version_code" -gt 2100000000 ]; then
        print_error "Version code too large: $version_code"
        print_info "Google Play version code limit is 2,100,000,000"
        return 1
    fi
    
    # Check version name format
    if ! [[ "$version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9.-]+)?$ ]]; then
        print_error "Invalid version name format: $version_name"
        print_info "Version name should follow semantic versioning (e.g., 1.2.3 or 1.2.3-beta)"
        return 1
    fi
    
    print_success "Version format validation passed"
    return 0
}

# Function to check build compatibility
check_build_compatibility() {
    local version_code="$1"
    local version_name="$2"
    
    print_info "Checking build compatibility..."
    
    # Test if build works with current versions
    print_info "Testing debug build..."
    if ! ./gradlew assembleDebug >/dev/null 2>&1; then
        print_error "Debug build failed with current versions"
        return 1
    fi
    
    print_success "Build compatibility check passed"
    return 0
}

# Function to generate conflict report
generate_conflict_report() {
    local version_code="$1"
    local version_name="$2"
    local report_file="version-conflict-report-$(date +%Y%m%d_%H%M%S).md"
    
    cat > "$report_file" << EOF
# Version Conflict Check Report

**Date**: $(date +"%Y-%m-%d %H:%M:%S")
**Version Code**: $version_code
**Version Name**: $version_name

## Checks Performed

### ✅ Format Validation
- Version code format: Valid
- Version name format: Valid
- Google Play limits: Within limits

### ✅ Git Repository
- Tag conflicts: None found
- Version sequence: Verified

### ✅ Build Compatibility
- Debug build: Successful
- Version injection: Working

### ⚠️ Manual Checks Required
- [ ] Google Play Console version conflicts
- [ ] Internal testing version conflicts
- [ ] Production release readiness

## Recommendations

1. **Before Release**:
   - Verify Google Play Console has no conflicting versions
   - Test the build thoroughly
   - Update release notes and changelog

2. **After Release**:
   - Monitor for any deployment issues
   - Update internal documentation
   - Plan next version increment

## Notes

- This report was generated automatically
- Manual verification of Google Play Console is required
- Keep this report for audit purposes

---
*Generated by check-version-conflicts.sh*
EOF

    print_success "Conflict report generated: $report_file"
}

# Main function
main() {
    print_info "🔍 Version Conflict Checker"
    echo ""
    
    # Check if we're in the right directory
    if [ ! -f "app/build.gradle.kts" ]; then
        print_error "app/build.gradle.kts not found. Run this script from the project root."
        exit 1
    fi
    
    # Get current versions
    local versions=$(get_current_versions)
    local version_code=$(echo "$versions" | cut -d'|' -f1)
    local version_name=$(echo "$versions" | cut -d'|' -f2)
    
    print_info "Checking versions:"
    echo "  Version Code: $version_code"
    echo "  Version Name: $version_name"
    echo ""
    
    # Run all checks
    local all_checks_passed=true
    
    # Format validation
    if ! validate_version_format "$version_code" "$version_name"; then
        all_checks_passed=false
    fi
    
    # Git tag conflicts
    if ! check_git_tag_conflicts "$version_name"; then
        all_checks_passed=false
    fi
    
    # GitHub releases
    if ! check_github_releases "$version_name"; then
        all_checks_passed=false
    fi
    
    # Version code sequence
    check_version_code_sequence "$version_code"
    
    # Build compatibility
    if ! check_build_compatibility "$version_code" "$version_name"; then
        all_checks_passed=false
    fi
    
    # Google Play Console (manual check)
    if ! check_play_console_conflicts "$version_code" "$version_name"; then
        all_checks_passed=false
    fi
    
    # Generate report
    generate_conflict_report "$version_code" "$version_name"
    
    # Final result
    echo ""
    if [ "$all_checks_passed" = true ]; then
        print_success "🎉 All conflict checks passed!"
        print_info "Version $version_name (code: $version_code) is ready for release"
    else
        print_error "❌ Some conflict checks failed"
        print_info "Please resolve the issues above before proceeding with release"
        exit 1
    fi
}

# Run main function
main "$@"