# PRIME ONE — Android GitHub Build

This repository wraps the latest PRIME ONE HTML interface in a Capacitor Android application.

## Build the APK with GitHub Actions
1. Create a new GitHub repository.
2. Extract this ZIP and upload **all files and folders** to the repository root (including the hidden `.github` folder).
3. Push to `main` or `master`, or open **Actions → Build Prime One Android APK → Run workflow**.
4. When the workflow succeeds, open its run and download the `prime-one-debug-apk` artifact. Extract it to get `app-debug.apk`.

## Included
- Latest PRIME ONE HTML as `www/index.html`.
- Capacitor Android wrapper configuration.
- Custom app launcher icon using the supplied The Last of Us Part I artwork, with density-specific Android icon resources.
- Picture-in-Picture (PiP) control in the video-player toolbar for local video playback.
- Native Android PiP plugin and manifest configuration (Android 8.0+).
- GitHub Actions workflow to generate a debug APK.
- Android manifest declarations for audio, images, video, notifications, microphone, Bluetooth and media playback.

## Permission note
Manifest declarations do not automatically grant access. Android runtime permission requests must be made by app code when each feature is used. The microphone is declared for voice features; ordinary music and video playback do not need microphone access. Android's system photo/file picker may grant selected-item access without broad gallery permission.

## Important feature limitation
This package wraps the current HTML app. Full access to every phone folder, native background wallpaper scheduling, and native media-library scanning require corresponding Android-native implementations and runtime permission handling; the manifest alone does not implement those features. The HTML wallpaper rotation works while the app is active, subject to Android lifecycle restrictions.

The APK is **not prebuilt** in this ZIP. GitHub Actions builds it and publishes the APK as an artifact.

## Picture-in-Picture
Open a local video in PRIME ONE and tap the PiP icon in the video toolbar to move playback into Android's floating window. PiP requires Android 8.0 (API 26) or later. Web/YouTube iframe playback is not currently routed into native PiP by this implementation.

## Media library and lock-screen upgrade
- Native Capacitor `LocalMusic` plugin queries Android MediaStore for audio and video files.
- The library scan reconciles removed MediaStore entries and imports newly discovered audio and video items.
- A public Android media notification uses MediaSession metadata and play/pause/previous/next actions; the existing web player receives lock-action events.
- Android 13+ requires the user to grant the Music and audio / Videos and notification permissions when prompted.
- This is a GitHub Actions source project; build the APK through the workflow after pushing the ZIP contents to GitHub.
