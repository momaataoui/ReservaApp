# ReservaApp - Luxe Escape

ReservaApp est une application Android premium de réservation d'hôtels et de restaurants, conçue avec un accent particulier sur l'expérience utilisateur haut de gamme et les principes de design modernes.

## 🚀 Fonctionnalités

- **Authentification Sécurisée** : Inscription (avec nom, email, téléphone), connexion et réinitialisation de mot de passe via Firebase Auth.
- **Expérience Visuelle Premium** :
  - Design épuré et minimaliste (inspiré par Airbnb/LinkedIn).
  - **Correction Dark Mode** : Interface d'authentification optimisée pour rester élégante et lisible quel que soit le mode du système.
  - Bouton de connexion sociale (Google) personnalisé.
  - Transitions fluides (Fade & Scale-up) entre les écrans.
- **Gestion de Profil Dynamique** : Profil utilisateur complet avec synchronisation en temps réel (nom, téléphone) et gestion d'image de profil (Galerie/Appareil photo).
- **Base de Données Cloud** : Synchronisation en temps réel via Cloud Firestore.
- **Navigation Intuitive** : Barre de navigation personnalisée avec indicateurs de sélection dynamiques.

## 🛠 Stack Technique

- **Langage** : Java
- **Architecture UI** : XML Layouts avec Material Components (Material 3)
- **Backend** : Firebase (Authentication, Firestore, Storage)
- **Chargement d'Images** : Glide
- **Outils** : Android Studio, Gradle, Git

## 📦 Installation & Configuration

1. **Cloner le dépôt** :
   ```bash
   git clone https://github.com/momaataoui/ReservaApp.git
   ```

2. **Configuration Firebase** :
   - Créez un projet sur la [Console Firebase](https://console.firebase.google.com/).
   - Ajoutez une application Android avec le nom de package `com.ensab.reservaapp`.
   - Téléchargez le fichier `google-services.json` et placez-le dans le répertoire `app/`.
   - Activez **Authentication** (Email/Password, Google), **Firestore Database**, et **Storage**.

3. **Compiler le projet** :
   - Ouvrez le projet dans Android Studio.
   - Synchronisez les fichiers Gradle.
   - Lancez l'application sur un émulateur ou un appareil physique.

## 📁 Structure du Projet

- `app/src/main/java/com/ensab/reservaapp/` : Source Java.
  - `LoginActivity.java` : Gestion de la connexion.
  - `SignUpActivity.java` : Enregistrement des nouveaux utilisateurs.
  - `ForgotPasswordActivity.java` : Récupération de compte.
  - `ProfileActivity.java` : Gestion du profil utilisateur.
  - `ChoiceActivity.java` : Tableau de bord principal (Exploration).
- `app/src/main/res/layout/` : Définitions d'interface XML (Optimisées pour la cohérence visuelle).
- `app/src/main/res/values/` : Thèmes et couleurs (Forçage du style White-Premium).

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.
