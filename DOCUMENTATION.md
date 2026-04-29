# ReservaApp Project Documentation

This document provides a comprehensive overview of the **ReservaApp** architecture, its current state of development, and a guide to the codebase.

---

## 🏛️ Architecture Overview
The app follows the **MVVM (Model-View-ViewModel)** architectural pattern, ensuring a clean separation of concerns and making the code easier to test and maintain.

*   **View**: Handles the UI and user interactions. (Activities in `view.activity`, Adapters in `view.adapter`, and XML layouts).
*   **ViewModel**: Acts as a bridge between the View and the Repository. It holds the UI state and handles business logic. (Classes in `viewmodel`).
*   **Repository**: The "Single Source of Truth." It manages data operations from Firestore and Firebase Auth. (`repository/HotelRepository.java`).
*   **Model**: Simple POJO (Plain Old Java Objects) classes representing data entities. (Classes in `model`).
*   **Data**: External service integrations, specifically the AI features. (`data/GeminiService.java`).

---

## 📊 Feature Status

### ✅ Fully Functional (Production Ready)
*   **User Authentication**: Full Login, Sign Up, and Logout flow using Firebase Auth.
*   **Real-Time Wishlist**: Adding/removing hotels from favorites updates across all screens instantly via `addSnapshotListener`.
*   **Booking Management**: Users can view their bookings, cancel them, or remove them from history with real-time Firestore sync.
*   **Dynamic Hotel Discovery**: The main "Explore" feed fetches and displays hotels directly from the database.
*   **Navigation**: Centralized navigation handling via `NavigationHelper` to prevent redundant code.

### ⚠️ Semi-Functional (Integration in Progress)
*   **AI Concierge (Gemini 1.5 Flash)**: 
    *   *Works*: Context-aware chatting, hotel suggestions, and conversation memory.
    *   *Issue*: Subject to free-tier API rate limits. Needs UI streaming for better UX.
*   **Search & Filters**: 
    *   *Works*: City-based search and basic sorting (Price/Rating).
    *   *Needs*: Advanced date-range filtering and guest count selection.
*   **Hotel Details**: 
    *   *Works*: Dynamic data loading and image gallery.
    *   *Needs*: Integration of a robust calendar picker for reservations.

### ❌ Not Functional (UI Placeholders / Mockups)
*   **Side Menu (Drawer)**: The icon exists but triggers a "Coming Soon" message.
*   **Smart Search Activity**: Layout exists but isn't yet plugged into the main discovery flow.
*   **Profile Editing**: User details are displayed but currently read-only.
*   **Notifications**: Visual badges are hardcoded; no real push notification system is implemented yet.

---

## 📂 Codebase Structure

- `app/src/main/java/com/ensab/reservaapp/`
    - `data/`: AI Service integrations (Gemini).
    - `model/`: Data structures (`Hotel.java`, `Booking.java`, `ChatMessage.java`).
    - `repository/`: Data fetching logic (`HotelRepository.java`).
    - `util/`: Helper classes (`NavigationHelper.java`).
    - `view/`: 
        - `activity/`: Android Activities (Screens).
        - `adapter/`: RecyclerView Adapters for lists.
    - `viewmodel/`: Logic for UI components.

---

## 🛠️ Setup & Maintenance
1.  **Firebase**: Requires a valid `google-services.json` in the `/app` folder.
2.  **API Keys**: The Gemini API Key is stored in `local.properties` as `GEMINI_API_KEY`.
3.  **Database**: Uses Cloud Firestore with specific collections: `hotels`, `users`, and `bookings`.
