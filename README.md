# ReservaApp 🏨✨

A premium Android application for hotel and restaurant reservations in Morocco, designed with a focus on high-end user experience and modern architectural principles.

## 🚀 Overview
ReservaApp provides a seamless booking experience, featuring an AI-powered smart assistant, real-time data synchronization, and a refined UI that respects system design guidelines.

## 🛠 Tech Stack & Architecture
- **Language:** Java (Android)
- **Architecture:** MVVM (Model-View-ViewModel)
- **UI Components:** ViewBinding, Material Design 3, Edge-to-Edge UI.
- **Backend:** Firebase (Authentication, Cloud Firestore).
- **AI Integration:** Google Gemini AI (Generative AI SDK) for smart hotel search.
- **Image Loading:** Custom `ImageLoader` implementation.

## ✨ Key Features
- **Smart Search AI:** An integrated chatbot powered by Gemini to help users find the perfect stay.
- **Real-time Sync:** Instant updates for favorites (Wishlist) and bookings using Firestore SnapshotListeners.
- **Advanced Filtering:** Sort hotels by price, rating, or luxury status (1300+ DH).
- **Secure Auth:** Support for Google Sign-In and Email/Password authentication.
- **Polished UX:** Full "Edge-to-Edge" support with `WindowInsets` handling to prevent UI overlap with system bars.

## 📦 Project Structure
- `view/`: Activities and Adapters (ViewBinding enabled).
- `viewmodel/`: Business logic and UI state management.
- `repository/`: Data abstraction layer for Firebase Firestore.
- `model/`: Data classes (Hotel, Booking, ChatMessage, etc.).
- `util/`: Helper classes for Navigation and Image loading.

## ⚙️ Setup & Configuration

### 1. Prerequisites
- Android Studio Ladybug or newer.
- A Firebase Project.
- A Google Gemini API Key.

### 2. Configuration
1. **Firebase:** Download your `google-services.json` from the Firebase Console and place it in the `app/` directory.
2. **API Keys:** Add your Gemini API key to your `local.properties` file:
   ```properties
   GEMINI_API_KEY=your_actual_key_here
   ```
3. **Google Sign-In:** 
   - Generate your local SHA-1 fingerprint using the Gradle `signingReport` task.
   - Add the SHA-1 to your Firebase Project Settings.
   - Download the updated `google-services.json` if necessary.

## 👥 Collaborators
This is an academic project developed by:
- **Sahraoui Youness**
- **Maataoui Belabbes Mohammed**

---
*Note: This project was developed for academic purposes. No license is attached.*
