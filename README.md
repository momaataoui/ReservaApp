# ReservaApp - Luxe Escape

ReservaApp is a premium hotel and restaurant reservation application for Android, designed with a focus on high-end user experience and modern design principles.

## 🚀 Features

- **User Authentication**: Secure sign-up, login, and password reset using Firebase Authentication.
- **Dynamic Profile**: Personalized user profile with the ability to update name, phone number, and profile picture (via Gallery or Camera).
- **Cloud Database**: Real-time data synchronization using Cloud Firestore.
- **Premium Design**:
  - Clean, minimal UI inspired by Airbnb and LinkedIn.
  - Custom bottom navigation with dynamic selection indicators.
  - High-end transitions (Fade and Scale-up) between screens.
  - Optimized system bar handling (Dark icons on white background).
- **Cloud Storage**: Profile images are securely stored and served via Firebase Storage and Glide.

## 🛠 Tech Stack

- **Language**: Java
- **UI Architecture**: XML Layouts with ViewBinding
- **Backend**: Firebase (Auth, Firestore, Storage)
- **Image Loading**: Glide
- **Tools**: Android Studio, Git

## 📦 Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/momaataoui/ReservaApp.git
   ```

2. **Firebase Configuration**:
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.ensab.reservaapp`.
   - Download the `google-services.json` file and place it in the `app/` directory of the project.
   - Enable **Authentication** (Email/Password), **Firestore Database**, and **Storage**.

3. **Build the project**:
   - Open the project in Android Studio.
   - Sync Gradle files.
   - Run the app on an emulator or a physical device.

## 📁 Project Structure

- `app/src/main/java/com/ensab/reservaapp/`: Java source code.
  - `LoginActivity.java`: Handles user login.
  - `SignUpActivity.java`: Handles new user registration.
  - `ProfileActivity.java`: User profile management.
  - `ChoiceActivity.java`: Main dashboard (Explore).
  - `FirebaseHelper.java`: Utility class for Firestore operations.
  - `NavigationHelper.java`: Manages bottom navigation bar states.
- `app/src/main/res/layout/`: UI XML definitions.
- `app/src/main/res/anim/`: Custom transition animations.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
