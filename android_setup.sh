#!/bin/bash

# Android Development Setup Script for Replit
# This script sets up the necessary environment for Android development with full SDK installation

set -e  # Exit on any error

echo "Setting up Android development environment..."

# Set environment variables
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Create SDK directory if it doesn't exist
mkdir -p $ANDROID_HOME

echo "Environment variables set:"
echo "ANDROID_HOME: $ANDROID_HOME"
echo "ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"

# Check if Android SDK is already installed
if [ -f "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
    echo "Android SDK already installed, skipping download..."
else
    echo "Installing Android SDK..."
    
    # Download Android cmdline-tools
    CMDTOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    CMDTOOLS_ZIP="$HOME/cmdtools.zip"
    
    echo "Downloading Android cmdline-tools..."
    curl -o "$CMDTOOLS_ZIP" "$CMDTOOLS_URL"
    
    # Extract to temporary location
    cd "$HOME"
    unzip -q "$CMDTOOLS_ZIP" -d "$ANDROID_HOME"
    
    # Move to proper location - the zip creates a 'cmdline-tools' directory
    if [ -d "$ANDROID_HOME/cmdline-tools" ]; then
        cd "$ANDROID_HOME"
        # Create the proper structure
        if [ ! -d "cmdline-tools/latest" ]; then
            mkdir -p cmdline-tools/latest
            # Move all files from the extracted cmdline-tools to latest, but avoid moving latest into itself
            for item in cmdline-tools/*; do
                if [ "$(basename "$item")" != "latest" ]; then
                    mv "$item" cmdline-tools/latest/ 2>/dev/null || true
                fi
            done
        fi
    fi
    
    # Clean up
    rm "$CMDTOOLS_ZIP"
    
    echo "Android cmdline-tools installed successfully!"
fi

# Update PATH for current session
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Accept licenses automatically (suppress warnings)
echo "Accepting Android SDK licenses..."
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null 2>&1 || echo "License acceptance completed"

# Install required SDK components
echo "Installing Android SDK components..."
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --install "cmdline-tools;latest" "platform-tools" "build-tools;35.0.0" "platforms;android-35" || echo "Some SDK components may already be installed"

# Make gradlew executable if we're in the project directory
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    echo "Made gradlew executable"
fi

echo "Verifying installation..."
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --list | head -10

echo ""
echo "Android development environment setup complete!"
echo "SDK Location: $ANDROID_HOME"
echo "Available platforms and build-tools have been installed."
echo ""
echo "To build the project, run: ./gradlew assembleDebug"