# 🏃‍♂️ FitFlows - Fitness Tracking App

FitFlows is a comprehensive Android fitness tracking application built with Java and Firebase. It helps users manage their fitness goals, log workouts, track daily habits, and analyze their progress with detailed analytics.

## ✨ Features

### 🔐 **User Authentication**
- Secure user registration and login with Firebase Authentication
- Email verification workflow
- Password strength validation
- User profile management
- Secure session handling with automatic logout

### 🎯 **Goal Management**
- Create custom fitness goals with targets and deadlines
- Track progress with visual progress bars
- Categorize goals (Weight Loss, Muscle Gain, Endurance, Strength, Flexibility)
- Set deadlines and monitor remaining time

### 💪 **Workout Logging**
- Log various types of workouts (Cardio, Strength, Flexibility, Sports)
- Track duration, calories burned, and exercise details
- View workout history and statistics
- Link workouts to specific goals

### ✅ **Daily Habits & Streaks**
- Create and track daily, weekly, or custom frequency habits
- Build and maintain streaks with gamification
- Visual streak indicators (🔥 current, 👑 longest)
- Habit completion tracking with progress analytics

### 📊 **Analytics Dashboard**
- Comprehensive progress statistics
- Weekly and monthly overviews
- Goal completion rates
- Habit compliance tracking
- Visual charts and progress indicators

### 🏠 **Personalized Dashboard**
- User-specific welcome screen with profile info
- Quick stats overview
- Today's habits at a glance
- Active goals summary
- Recent workout highlights

## 🚀 **Getting Started**

### **Prerequisites**
- Android Studio Arctic Fox or newer
- Android SDK 29+
- Java 11
- Firebase project setup

### **Setup Instructions**

1. **Clone the Repository**
2. **Firebase Setup**
   - Create a new Firebase project at Firebase Console
   - Enable Authentication, Realtime Database, and Analytics
   - Download google-services.json and place it in app/ directory
   - Replace the placeholder google-services.json with your actual configuration

3. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the flowfits directory and open it

4. **Build and Run**
   - Let Gradle sync complete
   - Connect an Android device or start an emulator
   - Click "Run" or press Ctrl+R

## 🏗️ **Architecture**

### **MVVM Pattern**
- **Model**: Data classes (User, Goal, Workout, Habit, HabitLog)
- **View**: Activities and Fragments with XML layouts
- **ViewModel**: Business logic and UI state management

### **Tech Stack**
- **Language**: Java
- **UI**: XML layouts with Material Design 3
- **Backend**: Firebase (Realtime Database, Authentication, Analytics)
- **Architecture**: MVVM with Repository Pattern
- **Navigation**: Bottom Navigation with Fragment navigation

## 📱 **App Status**

### ✅ **Completed**
- **🔧 Core Architecture**: MVVM pattern with repository structure
- **🔥 Firebase Integration**: Realtime Database for all data operations
- **🔐 User Authentication**: Complete login/register system with email verification
- **📱 Modern UI**: Material Design 3 with bottom navigation
- **🎯 Goal Management**: Full CRUD operations with progress tracking
- **✅ Habit Tracking**: Complete system with streak management and logging
- **👤 User Profiles**: Personalized dashboard with user information
- **📊 Data Models**: Comprehensive models for User, Goal, Habit, HabitLog
- **🛠️ Repository Layer**: GoalRepository, HabitRepository, UserRepository
- **🎨 UI Components**: Activities, fragments, adapters with real-time updates
- **✅ Build Success**: App compiles and builds successfully

### 🔄 **Next Steps**
- **💪 Workout Module**: Complete workout logging functionality
- **📊 Analytics**: Charts and detailed progress visualization
- **📱 Notifications**: Push notifications for habit reminders
- **🎨 UI Polish**: Enhanced animations and user experience
- **🔧 Optimization**: Performance improvements and data sync optimization

---

**Built with ❤️ using Android Studio and Firebase** 