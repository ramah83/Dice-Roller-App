# 🎲 Dice Roller

<div align="center">

![Dice Roller Logo](https://img.shields.io/badge/🎲-Dice%20Roller-red?style=for-the-badge&labelColor=black)

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

A modern Android dice rolling application with smooth 3D animations and detailed statistics

</div>

---

## 📋 Overview

Dice Roller is a modern Android application built with Kotlin and Jetpack Compose.  
It delivers an interactive dice rolling experience with clean UI, smooth animations, and comprehensive statistics for board games and casual gameplay.

---

## ✨ Features

### Core Functionality
- Roll 1 to 9 dice simultaneously
- Automatic calculation of sum and product
- Instant sharing of results
- Haptic feedback on roll

### UI & Design
- Smooth 3D dice animations
- Light and Dark mode support
- Responsive layout
- Material Design 3

### Statistics
- Total roll count
- Highest and lowest sum
- Average roll value
- Last 5 rolls history
- Automatic data persistence

---

## 📸 Screenshots

<div align="center">


</div>

---

## 🛠️ Tech Stack

| Technology | Description |
|-----------|-------------|
| Kotlin | Programming Language |
| Jetpack Compose | UI Framework |
| Material Design 3 | Design System |
| Android SDK | Platform |
| Compose Animation | Animations |

---

## 📱 Requirements

- Android 7.0+ (API 24)
- Target API 35
- Storage usage ~10 MB
- Vibration permission only

---

## 🚀 Installation

### For Users
1. Download APK from Releases
2. Enable installation from unknown sources
3. Install and enjoy

### For Developers

```bash
git clone https://github.com/yourusername/dice-roller.git
cd dice-roller
./gradlew assembleDebug
```

---

## 🎮 How to Use

1. Open the application
2. Tap ROLL DICE
3. Adjust dice count using + / -
4. Tap the result to share
5. View statistics and history

---

## 🏗️ Project Structure

```
app/
├── src/main/
│   ├── java/com/example/diceroller/MainActivity.kt
│   ├── res/
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

---

## 🎨 Customization

```kotlin
val bgColors = if (isDarkMode) {
    listOf(Color(0xFF2C2C2C), Color(0xFF1F1F1F), Color(0xFF0F0F0F))
} else {
    listOf(Color(0xFFFAF8F4), Color(0xFFF4EFE9), Color(0xFFEFE6DE))
}
```

---

## 🔮 Roadmap

- Additional dice types (D8, D10, D12, D20)
- Sound effects
- Cloud statistics sync
- Export statistics
- Multi-language support

---

## 👨‍💻 Developer

<div align="center">

Built with ❤️ using Kotlin and Jetpack Compose

[![LinkedIn](https://img.shields.io/badge/Mohamed%20Khaled-LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/mohamed-khaled-16547233a/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/yourusername)

</div>

---

## 📬 Contact

Email: rammohamed962@gmail.com  
LinkedIn: https://www.linkedin.com/in/mohamed-khaled-16547233a/
