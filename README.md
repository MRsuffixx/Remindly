# 🎂 Remindly

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="120" alt="Remindly Logo"/>
</p>

<p align="center">
  <b>Never forget birthdays, anniversaries, and special days again!</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Language"/>
  <img src="https://img.shields.io/badge/Min%20SDK-26-yellow.svg" alt="Min SDK"/>
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-purple.svg" alt="Architecture"/>
</p>

---

## 📱 About

**Remindly** is a modern Android application designed to help you remember important dates in your life. Whether it's birthdays, wedding anniversaries, or national holidays, Remindly ensures you never miss a special moment.

### ✨ Key Features

- 🎂 **Birthday Reminders** - Track birthdays for family, friends, and even pets
- 💍 **Anniversary Tracking** - Wedding, relationship, engagement, and more
- 🇹🇷 **Turkish Holidays** - Pre-loaded with Turkish national and religious holidays
- ⏰ **Flexible Reminders** - Get notified same day, 1, 3, 7, 14, or 30 days before
- 🌙 **Dark/Light Theme** - Choose your preferred appearance
- 💾 **Backup & Restore** - Export and import your data
- 🎨 **Beautiful UI** - Modern Material 3 design with colorful timeline

---

## 📸 Screenshots

| Home Screen | Add Event | Settings |
|:-----------:|:---------:|:--------:|
| Dashboard with upcoming events | Create new reminders | Customize your experience |

---

## 🏗️ Architecture

Remindly follows **MVVM (Model-View-ViewModel)** pattern combined with **Clean Architecture** principles:

```
app/
├── data/                    # Data Layer
│   ├── local/
│   │   ├── dao/            # Room DAOs
│   │   ├── database/       # Room Database
│   │   └── entity/         # Database Entities
│   └── repository/         # Repository Implementations
│
├── di/                      # Dependency Injection (Hilt)
│
├── domain/                  # Domain Layer
│   ├── model/              # Domain Models
│   ├── repository/         # Repository Interfaces
│   └── usecase/            # Use Cases
│
├── notification/            # Notification System
│
├── presentation/            # Presentation Layer
│   ├── addevent/           # Add/Edit Event Screen
│   ├── home/               # Home Dashboard
│   ├── navigation/         # Navigation Graph
│   └── settings/           # Settings Screen
│
└── ui/theme/               # Theming (Colors, Typography)
```

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Design System** | Material 3 |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Hilt |
| **Database** | Room |
| **Preferences** | DataStore |
| **Background Work** | WorkManager |
| **Navigation** | Navigation Compose |
| **Async** | Kotlin Coroutines + Flow |

---

## 📦 Event Categories

### 🎂 Birthdays
- Personal Birthday
- Children's Birthday
- Sibling Birthday
- Relative Birthday
- Pet Birthday

### 💍 Anniversaries
- Wedding Anniversary
- Relationship Anniversary
- Dating Anniversary
- Engagement Anniversary
- Promise Anniversary
- Graduation Day
- Work Anniversary
- First Day of Work
- House Anniversary
- Family Anniversary

### 👨‍👩‍👧 Family
- Mother's Day
- Father's Day

### 🇹🇷 Turkish Holidays
- Ramazan Bayramı (Eid al-Fitr)
- Kurban Bayramı (Eid al-Adha)
- New Year's Eve
- Valentine's Day
- Teachers' Day (November 24)
- April 23 - National Sovereignty and Children's Day
- May 19 - Commemoration of Atatürk, Youth and Sports Day
- August 30 - Victory Day
- October 29 - Republic Day

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35
- Kotlin 2.0.21

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/Remindly.git
   ```

2. **Open in Android Studio**
   - File → Open → Select the Remindly folder

3. **Sync Gradle**
   - Wait for Gradle sync to complete

4. **Run the app**
   - Select an emulator or connected device
   - Click Run (▶️) or press `Shift + F10`

### Build APK

```bash
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📋 Requirements

| Requirement | Version |
|-------------|---------|
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |
| Compile SDK | 35 |

---

## 🔔 Permissions

| Permission | Purpose |
|------------|---------|
| `POST_NOTIFICATIONS` | Show reminder notifications |
| `RECEIVE_BOOT_COMPLETED` | Reschedule reminders after device restart |
| `SCHEDULE_EXACT_ALARM` | Schedule precise reminder times |
| `VIBRATE` | Vibrate on notification |
| `WAKE_LOCK` | Keep device awake for notifications |

---

## 🎨 Theming

Remindly supports three theme modes:

- **System** - Follows device theme
- **Light** - Always light theme
- **Dark** - Always dark theme

The app uses a warm, friendly color palette:
- Primary: `#FF6B6B` (Coral Red)
- Secondary: `#4ECDC4` (Teal)
- Accent colors for different event types

---

## 💾 Data Management

### Backup
1. Go to Settings → Backup
2. Click "Kopyala" (Copy) to copy JSON data to clipboard
3. Save the data in a secure location

### Restore
1. Go to Settings → Restore
2. Paste your backup JSON data
3. Click "İçe Aktar" (Import)

---

## 🗺️ Roadmap

- [ ] Cloud Sync (Google Drive / Firebase)
- [ ] Widget Support
- [ ] Recurring Custom Events
- [ ] Contact Integration
- [ ] Multi-language Support
- [ ] Gift Ideas Integration
- [ ] Calendar Export (ICS)

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Remindly** - Made with ❤️

---

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system
- [Hilt](https://dagger.dev/hilt/) - Dependency injection
- [Room](https://developer.android.com/training/data-storage/room) - Database persistence

---

<p align="center">
  ⭐ Star this repo if you find it helpful!
</p>
