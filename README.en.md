# WiwyMusic

<div align="center">

  ### Advanced YouTube Music Client with Material Design 3 for Android

  [![Latest Release](https://img.shields.io/github/v/release/angelanda023-prog/WiwyMusic?style=flat-square&logo=github&color=0D1117&labelColor=161B22)](https://github.com/angelanda023-prog/WiwyMusic/releases)
  [![License](https://img.shields.io/badge/License-GPLv3-2B3137?style=flat-square&logo=gnu&labelColor=161B22)](LICENSE)
  [![Android](https://img.shields.io/badge/Platform-Android%206.0+-3DDC84.svg?style=flat-square&logo=android&logoColor=white&labelColor=161B22)](https://www.android.com)
  [![Stars](https://img.shields.io/github/stars/angelanda023-prog/WiwyMusic?style=flat-square&logo=github&color=yellow&labelColor=161B22&cacheSeconds=21600)](https://github.com/angelanda023-prog/WiwyMusic/stargazers)
</div>

---

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Key Features](#key-features)
- [Installation](#installation)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)
- [Credits](#credits)
- [License](#license)

---

## Overview

**WiwyMusic** is a YouTube Music client for Android with its own account system, Premium plan, and cloud sync, built with a modern Material Design 3 interface.

### Key Benefits

- **Ad-free Experience**: Enjoy uninterrupted music streaming
- **Enhanced Performance**: Optimized for smooth playback and navigation
- **Customizable Interface**: Personalize your music experience
- **Offline Capabilities (Premium)**: Download and play music without an internet connection
- **Cloud Sync (Premium)**: Back up and sync your library across devices

> **Note**: WiwyMusic is an independent project and is not affiliated, sponsored, or endorsed by YouTube or Google.

---

## Technology Stack

<div align="center">

| Frontend | Backend | Development Tools |
|:--------:|:-------:|:----------------:|
| ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white) | ![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white) | ![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white) |
| ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white) | ![Cloudflare](https://img.shields.io/badge/Cloudflare-F38020?style=for-the-badge&logo=cloudflare&logoColor=white) | ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white) |
| ![Material Design 3](https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white) | | ![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white) |

</div>

---

## Key Features

### Core Functionality
<table>
<tr>
<th width="30%">Feature</th>
<th width="70%">Description</th>
</tr>
<tr>
<td><strong>🎵 Ad-free Playback</strong></td>
<td>Enjoy music without any advertising interruptions</td>
</tr>
<tr>
<td><strong>🔄 Background Playback</strong></td>
<td>Continue listening while using other applications</td>
</tr>
<tr>
<td><strong>🔍 Advanced Search</strong></td>
<td>Quickly find songs, videos, albums, and playlists</td>
</tr>
<tr>
<td><strong>👤 Own Account + Premium Plan</strong></td>
<td>Sign in with your WiwyMusic account and unlock Premium benefits</td>
</tr>
<tr>
<td><strong>📚 Library Management</strong></td>
<td>Organize and fully manage your music collection</td>
</tr>
<tr>
<td><strong>📱 Offline Mode (Premium)</strong></td>
<td>Download content for offline listening</td>
</tr>
<tr>
<td><strong>☁️ Cloud Sync (Premium)</strong></td>
<td>Back up and sync your library, favorites, and playlists across devices</td>
</tr>
</table>

### Audio Enhancement
<table>
<tr>
<th width="30%">Feature</th>
<th width="70%">Description</th>
</tr>
<tr>
<td><strong>🎤 Synchronized Lyrics</strong></td>
<td>View perfectly synchronized song lyrics</td>
</tr>
<tr>
<td><strong>⚡ Smart Silence Skip</strong></td>
<td>Automatically skip segments without audio</td>
</tr>
<tr>
<td><strong>🔊 Volume Normalization</strong></td>
<td>Balance sound levels between different tracks</td>
</tr>
<tr>
<td><strong>🎛️ Tempo & Pitch Control</strong></td>
<td>Adjust playback speed and pitch to preferences</td>
</tr>
</table>

### Personalization & Integration
<table>
<tr>
<th width="30%">Feature</th>
<th width="70%">Description</th>
</tr>
<tr>
<td><strong>🎨 Dynamic Theming</strong></td>
<td>Interface adapts to album artwork colors</td>
</tr>
<tr>
<td><strong>🌐 Multi-language Support</strong></td>
<td>Available in numerous languages for global users</td>
</tr>
<tr>
<td><strong>🚗 Android Auto Compatible</strong></td>
<td>Integration with vehicle infotainment systems</td>
</tr>
<tr>
<td><strong>🎯 Material Design 3</strong></td>
<td>Design aligned with Google's latest design guidelines</td>
</tr>
<tr>
<td><strong>🖼️ Artwork Export</strong></td>
<td>Save high-resolution album images</td>
</tr>
</table>

---

## Installation

### System Requirements

| Component | Minimum Requirement |
|:----------|:--------------------|
| Operating System | Android 6.0 (Marshmallow) or higher |
| Storage Space | 10 MB available |
| Network | Internet connection for streaming |
| RAM | 2 GB recommended |

### Installation from GitHub Releases

1. Navigate to the [Releases](https://github.com/angelanda023-prog/WiwyMusic/releases) section on GitHub
2. Download the `WiwyMusic.apk` file from the latest version
3. Enable "Install from unknown sources" in your device's security settings
4. Open the downloaded APK file to complete installation

> **Security Notice**: For security reasons, it is recommended to obtain the application exclusively through the official [Releases](https://github.com/angelanda023-prog/WiwyMusic/releases) of this repository. Avoid downloading APKs from unverified sources.

---

## Building from Source

### Prerequisites

<table>
<tr>
<th>Tool</th>
<th>Recommended Version</th>
<th>Purpose</th>
</tr>
<tr>
<td>Gradle</td>
<td>7.5 or higher</td>
<td>Build automation</td>
</tr>
<tr>
<td>Kotlin</td>
<td>1.7 or higher</td>
<td>Programming language</td>
</tr>
<tr>
<td>Android Studio</td>
<td>2022.1 or higher</td>
<td>IDE and development environment</td>
</tr>
<tr>
<td>JDK</td>
<td>21</td>
<td>Java runtime environment</td>
</tr>
<tr>
<td>Android SDK</td>
<td>API level 36</td>
<td>Android development tools</td>
</tr>
</table>

### Environment Setup

```bash
# Clone the repository
git clone https://github.com/angelanda023-prog/WiwyMusic.git

# Navigate to project directory
cd WiwyMusic
```

### Build Methods

#### Android Studio Build

1. Open Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate and select the WiwyMusic directory
4. Wait for project synchronization and indexing
5. Select Build → Build Bundle(s) / APK(s) → Build APK(s)

#### Command Line Build

```bash
# Build production release (universal flavor)
./gradlew assembleUniversalRelease

# Build debug version
./gradlew assembleUniversalDebug

# Run unit tests
./gradlew test
```

> **Note**: Compiled APK files will be located in the `app/build/outputs/apk/` directory.

---

## Contributing

### Code of Conduct

All participants in this project must adhere to our [Code of Conduct](CODE_OF_CONDUCT.md) that promotes an inclusive, respectful, and constructive environment.

### Development Workflow

1. **Issue Review**: Check [open issues](https://github.com/angelanda023-prog/WiwyMusic/issues) or create a new one describing the problem or feature
2. **Fork Repository**: Create a personal fork of the repository
3. **Feature Branch**: Create a branch for your feature (`git checkout -b feature/new-feature`)
4. **Implementation**: Implement changes following project coding conventions
5. **Testing**: Ensure code passes all tests (`./gradlew test`)
6. **Commit**: Make commits with descriptive messages (`git commit -m 'feat: add new feature'`)
7. **Push Changes**: Upload changes to your fork (`git push origin feature/new-feature`)
8. **Pull Request**: Open a PR detailing changes and referencing corresponding issue

> **Development Guidelines**: Review our [contribution guidelines](CONTRIBUTING.md) for detailed information about development process, code standards, and workflow.

---

## Credits

WiwyMusic is a fork of **[OpenTune](https://github.com/Arturo254/OpenTune)** (by Arturo Cervantes), distributed under the GNU General Public License v3.0. All credit for the original project and its architecture goes to its authors and contributors; the code here includes WiwyMusic-specific rebranding, backend, and integration work.

OpenTune, in turn, credits [ArchiveTune](https://github.com/koiverse/ArchiveTune) and [Vivi Music](https://github.com/vivizzz007/vivi-music) as inspiration.

---

## License

This project is a derivative of OpenTune and is distributed under the same terms:

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but **WITHOUT ANY WARRANTY**; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the [GNU General Public License](LICENSE) for more details.

<div align="center">

[![GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg?style=for-the-badge&logo=gnu&logoColor=white)](https://www.gnu.org/licenses/gpl-3.0)

</div>
