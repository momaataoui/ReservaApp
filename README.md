# ReservaApp - Luxe Escape 🏨

ReservaApp est une application Android premium de réservation d'hôtels et de restaurants au Maroc, conçue avec un accent particulier sur l'expérience utilisateur haut de gamme et les principes de design modernes.

---

## 📢 État d'Avancement (Côté Client)

Toute la base client et l'infrastructure de sécurité ont été refactorisées et sécurisées.

### ✅ Réalisations Récentes
- **Architecture Propre** : Migration vers une structure MVVM avec des packages organisés (`data`, `model`, `view`, `viewmodel`).
- **Sécurité API** : La clé Gemini AI a été retirée du code source et est maintenant gérée via `local.properties` et `BuildConfig`.
- **Logique de Prix** : Passage à la monnaie **DH (Dirham)** avec des tarifs réalistes (900 DH - 1500 DH) et calcul automatique des suppléments.
- **Synchronisation Firestore** : 
    - Le modèle `Booking` inclut désormais le `hotelId` pour permettre le suivi admin.
    - La **Wishlist** est entièrement synchronisée avec Firestore et gère les erreurs de données.
- **Rôles Utilisateurs** : Le système de connexion détecte désormais si l'utilisateur est un `admin` ou un `user` via Firestore.

---

## 🛠 Instructions pour le Collaborateur (Partie Admin)

Bienvenue sur le projet ! Voici comment démarrer et ce qu'il reste à accomplir.

### 1. Configuration Initiale
Avant de compiler, assure-toi d'avoir ces deux fichiers :
1. **`google-services.json`** : À placer dans le dossier `app/` (indispensable pour Firebase).
2. **`local.properties`** : Ajoute la clé API Gemini (demande-la moi en privé) à la fin du fichier :
   ```properties
   GEMINI_API_KEY=VOTRE_CLE_ICI
   ```
   *Puis fais un "Sync Project with Gradle Files".*

### 2. Accès au mode Admin
Pour tester tes futures pages :
1. Connecte-toi sur l'app avec ton compte.
2. Dans la console Firebase (Collection `users`), change ton champ `role` de `"user"` à `"admin"`.
3. La `LoginActivity` détectera automatiquement ce changement.

### 3. Tes Prochaines Étapes 🚀
L'infrastructure est prête, tu peux maintenant te concentrer sur :
- **Dashboard Admin** : Créer une `AdminActivity` pour centraliser la gestion.
- **Gestion des Hôtels** : Interface pour Ajouter / Modifier / Supprimer des hôtels dans la collection `hotels`.
- **Suivi des Réservations** : Liste de toutes les réservations (Collection `bookings`). Utilise le champ `hotelId` pour filtrer ou afficher le nom de l'hôtel.
- **Statistiques** : (Optionnel) Afficher le revenu total généré en DH.

---

## 📁 Structure du Projet (Refactorisée)

- `data/` : `FirebaseHelper`, `GeminiService` (Logique de données).
- `model/` : `Hotel`, `Booking`, `ChatMessage` (Modèles de données).
- `repository/` : `HotelRepository` (Abstraction Firestore).
- `view/activity/` : Activités classées par flux (Auth, Liste, Détails).
- `view/adapter/` : Adaptateurs pour RecyclerView.
- `viewmodel/` : Logique métier et gestion d'état (MVVM).

## 📄 Licence

Ce projet est sous licence MIT.
