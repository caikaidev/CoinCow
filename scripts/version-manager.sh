#!/bin/bash

# Version Management Script for Android App
# This script manages version codes and version names for Google Play deployment

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

# Function to check if we're in a git repository
check_git_repo() {
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not in a Git repository"
        return 1
    fi
    return 0
}

# Function to get current version from build.gradle.kts
get_current_version() {
    local build_file="app/build.gradle.kts"
    if [ ! -f "$build_file" ]; then
        print_error "Build file not found: $build_file"
        return 1
    fi
    
    # Extract current version using gradle
    local version_code=$(./gradlew -q printVersionCode 2>/dev/null || echo "1")
    local version_name=$(./gradlew -q printVersionName 2>/dev/null || echo "1.0.0")
    
    echo "Current Version Code: $version_code"
    echo "Current Version Name: $version_name"
}

# Function to get next version code
get_next_version_code() {
    local strategy="$1"
    
    case "$strategy" in
        "git-count")
            # Use git commit count
            git rev-list --count HEAD
            ;;
        "timestamp")
            # Use timestamp-based version code (YYMMDDHHNN format)
            date +"%y%m%d%H%M"
            ;;
        "increment")
            # Increment from current version
            local current=$(./gradlew -q printVersionCode 2>/dev/null || echo "1")
            echo $((current + 1))
            ;;
        "manual")
            # Manual input
            read -p "Enter version code: " version_code
            echo "$version_code"
            ;;
        *)
            print_error "Unknown version code strategy: $strategy"
            return 1
            ;;
    esac
}

# Function to get next version name
get_next_version_name() {
    local strategy="$1"
    
    case "$strategy" in
        "git-tag")
            # Get from latest git tag
            local tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
            if [ -n "$tag" ]; then
                echo "${tag#v}"  # Remove 'v' prefix if present
            else
                echo "1.0.0"
            fi
            ;;
        "semantic")
            # Semantic versioning increment
            local current=$(./gradlew -q printVersionName 2>/dev/null || echo "1.0.0")
            local increment_type="$2"
            
            IFS='.' read -ra VERSION_PARTS <<< "$current"
            local major=${VERSION_PARTS[0]:-1}
            local minor=${VERSION_PARTS[1]:-0}
            local patch=${VERSION_PARTS[2]:-0}
            
            case "$increment_type" in
                "major")
                    echo "$((major + 1)).0.0"
                    ;;
                "minor")
                    echo "$major.$((minor + 1)).0"
                    ;;
                "patch")
                    echo "$major.$minor.$((patch + 1))"
                    ;;
                *)
                    echo "$major.$minor.$((patch + 1))"
                    ;;
            esac
            ;;
        "manual")
            # Manual input
            read -p "Enter version name (e.g., 1.2.3): " version_name
            echo "$version_name"
            ;;
        *)
            print_error "Unknown version name strategy: $strategy"
            return 1
            ;;
    esac
}

# Function to validate version format
validate_version() {
    local version_code="$1"
    local version_name="$2"
    
    # Validate version code (must be positive integer)
    if ! [[ "$version_code" =~ ^[1-9][0-9]*$ ]]; then
        print_error "Invalid version code: $version_code (must be positive integer)"
        return 1
    fi
    
    # Validate version name (semantic versioning format)
    if ! [[ "$version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9]+)?$ ]]; then
        print_error "Invalid version name: $version_name (must follow semantic versioning)"
        return 1
    fi
    
    return 0
}

# Function to check for version conflicts
check_version_conflicts() {
    local version_code="$1"
    local version_name="$2"
    
    # Check if version code already exists in git tags
    local existing_tags=$(git tag -l | grep -E "v?$version_name$" || true)
    if [ -n "$existing_tags" ]; then
        print_warning "Version name $version_name already exists in git tags:"
        echo "$existing_tags"
        read -p "Continue anyway? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            return 1
        fi
    fi
    
    return 0
}

# Function to update version in build.gradle.kts
update_build_gradle() {
    local version_code="$1"
    local version_name="$2"
    local build_file="app/build.gradle.kts"
    
    print_info "Updating $build_file with new versions..."
    
    # Create a temporary file with updated versions
    local temp_file=$(mktemp)
    
    # Update version code and name in defaultConfig
    sed -E "s/versionCode = [0-9]+/versionCode = $version_code/g" "$build_file" | \
    sed -E "s/versionName = \"[^\"]+\"/versionName = \"$version_name\"/g" > "$temp_file"
    
    # Replace original file
    mv "$temp_file" "$build_file"
    
    print_success "Build file updated successfully"
}

# Function to create git tag
create_git_tag() {
    local version_name="$1"
    local tag_name="v$version_name"
    
    print_info "Creating git tag: $tag_name"
    
    # Create annotated tag
    git tag -a "$tag_name" -m "Release version $version_name"
    
    print_success "Git tag created: $tag_name"
    
    # Ask if user wants to push tag
    read -p "Push tag to remote? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git push origin "$tag_name"
        print_success "Tag pushed to remote"
    fi
}

# Function to generate changelog
generate_changelog() {
    local version_name="$1"
    local previous_tag="$2"
    
    print_info "Generating changelog for version $version_name..."
    
    local changelog_file="CHANGELOG-$version_name.md"
    
    cat > "$changelog_file" << EOF
# Changelog - Version $version_name

**Release Date**: $(date +"%Y-%m-%d")

## Changes

EOF
    
    # Add git log since last tag
    if [ -n "$previous_tag" ]; then
        echo "### Commits since $previous_tag:" >> "$changelog_file"
        echo "" >> "$changelog_file"
        git log --oneline "$previous_tag..HEAD" | sed 's/^/- /' >> "$changelog_file"
    else
        echo "### All commits:" >> "$changelog_file"
        echo "" >> "$changelog_file"
        git log --oneline | sed 's/^/- /' >> "$changelog_file"
    fi
    
    echo "" >> "$changelog_file"
    echo "---" >> "$changelog_file"
    echo "*Generated automatically by version-manager.sh*" >> "$changelog_file"
    
    print_success "Changelog generated: $changelog_file"
}

# Function to display version information
show_version_info() {
    print_info "📱 Current Version Information"
    echo ""
    
    # Get current versions
    local current_version_code=$(./gradlew -q printVersionCode 2>/dev/null || echo "Unknown")
    local current_version_name=$(./gradlew -q printVersionName 2>/dev/null || echo "Unknown")
    
    echo "Current Version Code: $current_version_code"
    echo "Current Version Name: $current_version_name"
    echo ""
    
    # Show git information
    if check_git_repo; then
        local latest_tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "No tags")
        local commit_count=$(git rev-list --count HEAD)
        local current_branch=$(git branch --show-current)
        
        echo "Git Information:"
        echo "  Latest Tag: $latest_tag"
        echo "  Commit Count: $commit_count"
        echo "  Current Branch: $current_branch"
        echo ""
    fi
    
    # Show suggested next versions
    echo "Suggested Next Versions:"
    echo "  Version Code (git-count): $(get_next_version_code git-count)"
    echo "  Version Code (timestamp): $(get_next_version_code timestamp)"
    echo "  Version Name (git-tag): $(get_next_version_name git-tag)"
    echo "  Version Name (patch): $(get_next_version_name semantic patch)"
}

# Function to perform version bump
bump_version() {
    local version_code_strategy="$1"
    local version_name_strategy="$2"
    local version_name_increment="$3"
    
    print_info "🚀 Version Bump Process"
    echo ""
    
    # Get next versions
    local next_version_code=$(get_next_version_code "$version_code_strategy")
    local next_version_name=$(get_next_version_name "$version_name_strategy" "$version_name_increment")
    
    print_info "Proposed versions:"
    echo "  Version Code: $next_version_code"
    echo "  Version Name: $next_version_name"
    echo ""
    
    # Validate versions
    if ! validate_version "$next_version_code" "$next_version_name"; then
        return 1
    fi
    
    # Check for conflicts
    if ! check_version_conflicts "$next_version_code" "$next_version_name"; then
        return 1
    fi
    
    # Confirm with user
    read -p "Proceed with version bump? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Version bump cancelled"
        return 0
    fi
    
    # Update build.gradle.kts
    update_build_gradle "$next_version_code" "$next_version_name"
    
    # Generate changelog
    local previous_tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
    generate_changelog "$next_version_name" "$previous_tag"
    
    # Create git tag
    read -p "Create git tag for this version? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        create_git_tag "$next_version_name"
    fi
    
    print_success "🎉 Version bump completed successfully!"
    echo ""
    print_info "Next steps:"
    echo "  1. Review the changes in app/build.gradle.kts"
    echo "  2. Test the build with new version"
    echo "  3. Commit changes to git"
    echo "  4. Push to trigger CI/CD pipeline"
}

# Main function
main() {
    print_info "📋 Android Version Manager"
    echo ""
    
    # Check if we're in the right directory
    if [ ! -f "app/build.gradle.kts" ]; then
        print_error "app/build.gradle.kts not found. Run this script from the project root."
        exit 1
    fi
    
    # Check git repository
    if ! check_git_repo; then
        print_warning "Not in a Git repository. Some features may not work."
    fi
    
    # Parse command line arguments
    case "${1:-info}" in
        "info"|"show")
            show_version_info
            ;;
        "bump")
            local version_code_strategy="${2:-git-count}"
            local version_name_strategy="${3:-semantic}"
            local version_name_increment="${4:-patch}"
            bump_version "$version_code_strategy" "$version_name_strategy" "$version_name_increment"
            ;;
        "help")
            echo "Usage: $0 [command] [options]"
            echo ""
            echo "Commands:"
            echo "  info, show                    Show current version information"
            echo "  bump [code_strategy] [name_strategy] [increment]"
            echo "                               Bump version with specified strategies"
            echo "  help                         Show this help message"
            echo ""
            echo "Version Code Strategies:"
            echo "  git-count                    Use git commit count (default)"
            echo "  timestamp                    Use timestamp-based code"
            echo "  increment                    Increment current version code"
            echo "  manual                       Manual input"
            echo ""
            echo "Version Name Strategies:"
            echo "  git-tag                      Use latest git tag"
            echo "  semantic                     Semantic versioning (default)"
            echo "  manual                       Manual input"
            echo ""
            echo "Semantic Increment Types:"
            echo "  major                        Increment major version"
            echo "  minor                        Increment minor version"
            echo "  patch                        Increment patch version (default)"
            echo ""
            echo "Examples:"
            echo "  $0 info                      Show version information"
            echo "  $0 bump                      Bump patch version with git-count"
            echo "  $0 bump git-count semantic minor"
            echo "                               Bump minor version with git-count"
            echo "  $0 bump timestamp manual     Use timestamp code, manual name"
            ;;
        *)
            print_error "Unknown command: $1"
            echo "Use '$0 help' for usage information"
            exit 1
            ;;
    esac
}

# Run main function
main "$@"