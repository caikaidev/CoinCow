#!/bin/bash

# Build Analysis Script
# This script analyzes build output for optimization opportunities

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

# Function to format file size
format_size() {
    local size=$1
    if [ $size -gt 1048576 ]; then
        echo "$(($size / 1048576)) MB"
    elif [ $size -gt 1024 ]; then
        echo "$(($size / 1024)) KB"
    else
        echo "$size bytes"
    fi
}

# Function to analyze APK/AAB size
analyze_app_size() {
    local build_type="$1"
    local output_dir="app/build/outputs"
    
    print_info "📦 Analyzing $build_type build size..."
    
    if [ "$build_type" = "debug" ]; then
        local apk_file="$output_dir/apk/debug/app-debug.apk"
        if [ -f "$apk_file" ]; then
            local size=$(stat -f%z "$apk_file" 2>/dev/null || stat -c%s "$apk_file" 2>/dev/null)
            echo "  Debug APK: $(format_size $size)"
        else
            print_warning "Debug APK not found. Run './gradlew assembleDebug' first."
        fi
    elif [ "$build_type" = "release" ]; then
        local aab_file="$output_dir/bundle/release/app-release.aab"
        local apk_file="$output_dir/apk/release/app-release.apk"
        
        if [ -f "$aab_file" ]; then
            local size=$(stat -f%z "$aab_file" 2>/dev/null || stat -c%s "$aab_file" 2>/dev/null)
            echo "  Release AAB: $(format_size $size)"
        fi
        
        if [ -f "$apk_file" ]; then
            local size=$(stat -f%z "$apk_file" 2>/dev/null || stat -c%s "$apk_file" 2>/dev/null)
            echo "  Release APK: $(format_size $size)"
        fi
        
        if [ ! -f "$aab_file" ] && [ ! -f "$apk_file" ]; then
            print_warning "Release builds not found. Run './gradlew bundleRelease' or './gradlew assembleRelease' first."
        fi
    fi
}

# Function to analyze ProGuard mapping
analyze_proguard_mapping() {
    local mapping_file="app/build/outputs/mapping/release/mapping.txt"
    
    print_info "🔍 Analyzing ProGuard obfuscation..."
    
    if [ ! -f "$mapping_file" ]; then
        print_warning "ProGuard mapping file not found. Build release version first."
        return
    fi
    
    local total_classes=$(grep -c "^[a-zA-Z]" "$mapping_file" || echo "0")
    local obfuscated_classes=$(grep -c " -> [a-z]:" "$mapping_file" || echo "0")
    local obfuscation_rate=0
    
    if [ $total_classes -gt 0 ]; then
        obfuscation_rate=$((obfuscated_classes * 100 / total_classes))
    fi
    
    echo "  Total classes: $total_classes"
    echo "  Obfuscated classes: $obfuscated_classes"
    echo "  Obfuscation rate: $obfuscation_rate%"
    
    if [ $obfuscation_rate -lt 50 ]; then
        print_warning "Low obfuscation rate. Consider reviewing ProGuard rules."
    else
        print_success "Good obfuscation rate achieved."
    fi
}

# Function to analyze resource shrinking
analyze_resource_shrinking() {
    local resources_file="app/build/outputs/mapping/release/resources.txt"
    
    print_info "📱 Analyzing resource shrinking..."
    
    if [ ! -f "$resources_file" ]; then
        print_warning "Resource shrinking report not found."
        return
    fi
    
    local removed_resources=$(grep -c "Removed unused resource" "$resources_file" || echo "0")
    echo "  Removed unused resources: $removed_resources"
    
    if [ $removed_resources -gt 0 ]; then
        print_success "Resource shrinking is working effectively."
    else
        print_warning "No unused resources removed. Check resource usage."
    fi
}

# Function to analyze method count
analyze_method_count() {
    local build_type="$1"
    
    print_info "🔢 Analyzing method count..."
    
    # This is a simplified analysis - in practice you'd use tools like dexcount-gradle-plugin
    local apk_file=""
    if [ "$build_type" = "debug" ]; then
        apk_file="app/build/outputs/apk/debug/app-debug.apk"
    else
        apk_file="app/build/outputs/apk/release/app-release.apk"
    fi
    
    if [ ! -f "$apk_file" ]; then
        print_warning "APK file not found for method count analysis."
        return
    fi
    
    # Extract DEX files and count methods (simplified)
    local temp_dir=$(mktemp -d)
    unzip -q "$apk_file" "*.dex" -d "$temp_dir" 2>/dev/null || true
    
    local dex_count=$(find "$temp_dir" -name "*.dex" | wc -l)
    echo "  DEX files: $dex_count"
    
    if [ $dex_count -gt 1 ]; then
        print_warning "Multiple DEX files detected. Consider method count optimization."
    else
        print_success "Single DEX file - good method count optimization."
    fi
    
    rm -rf "$temp_dir"
}

# Function to analyze build performance
analyze_build_performance() {
    print_info "⚡ Build Performance Analysis"
    
    # Check Gradle daemon status
    if ./gradlew --status | grep -q "IDLE"; then
        print_success "Gradle daemon is running (good for build performance)"
    else
        print_warning "Gradle daemon not running. Consider starting it for better performance."
    fi
    
    # Check for parallel builds
    if grep -q "org.gradle.parallel=true" gradle.properties 2>/dev/null; then
        print_success "Parallel builds enabled"
    else
        print_warning "Consider enabling parallel builds in gradle.properties"
    fi
    
    # Check for build cache
    if grep -q "org.gradle.caching=true" gradle.properties 2>/dev/null; then
        print_success "Build cache enabled"
    else
        print_warning "Consider enabling build cache for faster builds"
    fi
}

# Function to generate optimization recommendations
generate_recommendations() {
    print_info "💡 Optimization Recommendations"
    echo ""
    
    echo "📋 Size Optimization:"
    echo "  • Use vector drawables instead of PNG when possible"
    echo "  • Enable resource shrinking in release builds"
    echo "  • Consider using WebP format for images"
    echo "  • Remove unused dependencies"
    echo ""
    
    echo "🚀 Performance Optimization:"
    echo "  • Enable R8 full mode for better optimization"
    echo "  • Use ProGuard rules to keep only necessary code"
    echo "  • Enable parallel builds and build cache"
    echo "  • Consider using baseline profiles for runtime performance"
    echo ""
    
    echo "🔒 Security Optimization:"
    echo "  • Enable code obfuscation in release builds"
    echo "  • Remove debug information from release builds"
    echo "  • Use string obfuscation for sensitive strings"
    echo "  • Enable certificate pinning for network security"
    echo ""
    
    echo "📦 Distribution Optimization:"
    echo "  • Use Android App Bundle (AAB) for Play Store"
    echo "  • Enable dynamic delivery for large features"
    echo "  • Configure proper ABI and density splits"
    echo "  • Optimize for different screen sizes"
}

# Function to run comprehensive build analysis
run_comprehensive_analysis() {
    local build_type="${1:-both}"
    
    print_info "🔍 Comprehensive Build Analysis"
    echo ""
    
    # Build performance analysis
    analyze_build_performance
    echo ""
    
    # Size analysis
    if [ "$build_type" = "debug" ] || [ "$build_type" = "both" ]; then
        analyze_app_size "debug"
        analyze_method_count "debug"
    fi
    
    if [ "$build_type" = "release" ] || [ "$build_type" = "both" ]; then
        analyze_app_size "release"
        analyze_method_count "release"
        analyze_proguard_mapping
        analyze_resource_shrinking
    fi
    
    echo ""
    generate_recommendations
}

# Function to compare build sizes
compare_builds() {
    print_info "📊 Build Size Comparison"
    echo ""
    
    local debug_apk="app/build/outputs/apk/debug/app-debug.apk"
    local release_apk="app/build/outputs/apk/release/app-release.apk"
    local release_aab="app/build/outputs/bundle/release/app-release.aab"
    
    if [ -f "$debug_apk" ] && [ -f "$release_apk" ]; then
        local debug_size=$(stat -f%z "$debug_apk" 2>/dev/null || stat -c%s "$debug_apk" 2>/dev/null)
        local release_size=$(stat -f%z "$release_apk" 2>/dev/null || stat -c%s "$release_apk" 2>/dev/null)
        local reduction=$((100 - (release_size * 100 / debug_size)))
        
        echo "Debug APK:   $(format_size $debug_size)"
        echo "Release APK: $(format_size $release_size)"
        echo "Size reduction: $reduction%"
        
        if [ $reduction -gt 30 ]; then
            print_success "Excellent size optimization achieved!"
        elif [ $reduction -gt 15 ]; then
            print_success "Good size optimization."
        else
            print_warning "Consider additional optimization techniques."
        fi
    fi
    
    if [ -f "$release_aab" ]; then
        local aab_size=$(stat -f%z "$release_aab" 2>/dev/null || stat -c%s "$release_aab" 2>/dev/null)
        echo "Release AAB: $(format_size $aab_size)"
    fi
}

# Main function
main() {
    print_info "🔧 Android Build Analyzer"
    echo ""
    
    # Check if we're in the right directory
    if [ ! -f "app/build.gradle.kts" ]; then
        print_error "app/build.gradle.kts not found. Run this script from the project root."
        exit 1
    fi
    
    case "${1:-analyze}" in
        "analyze"|"full")
            run_comprehensive_analysis "${2:-both}"
            ;;
        "size")
            analyze_app_size "${2:-both}"
            ;;
        "compare")
            compare_builds
            ;;
        "proguard")
            analyze_proguard_mapping
            ;;
        "performance")
            analyze_build_performance
            ;;
        "recommendations")
            generate_recommendations
            ;;
        "help")
            echo "Usage: $0 [command] [options]"
            echo ""
            echo "Commands:"
            echo "  analyze, full [debug|release|both]  Run comprehensive analysis"
            echo "  size [debug|release|both]           Analyze app size only"
            echo "  compare                             Compare debug vs release sizes"
            echo "  proguard                            Analyze ProGuard obfuscation"
            echo "  performance                         Analyze build performance"
            echo "  recommendations                     Show optimization recommendations"
            echo "  help                                Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 analyze                          Full analysis of both builds"
            echo "  $0 size release                     Analyze release build size"
            echo "  $0 compare                          Compare debug and release sizes"
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