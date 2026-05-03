# 🏨 ReservaApp - Système de Réservation Hôtelière Intelligent

ReservaApp est une plateforme Android haut de gamme dédiée à la réservation hôtelière. Elle combine une expérience utilisateur raffinée avec un assistant de recherche propulsé par l'Intelligence Artificielle (**Google Gemini**), le tout orchestré par une architecture **MVVM** robuste et une synchronisation en temps réel via **Firebase**.

---

## 🚀 Vision du Projet
Offrir une interface moderne qui simplifie la découverte de séjours de luxe tout en fournissant aux administrateurs des outils de gestion puissants et des analyses de données en temps réel.

## ✨ Fonctionnalités Clés

### 👤 Expérience Client
- **Assistant Smart Search** : Un chatbot IA (Gemini 2.0 Flash) qui analyse vos besoins pour recommander l'hôtel parfait.
- **Gestion des Favoris** : Sauvegarde instantanée des coups de cœur avec synchronisation cloud.
- **Flux de Réservation** : Processus simplifié avec calcul automatique des coûts et suivi du statut.
- **Exploration Avancée** : Galerie d'images haute résolution, cartes interactives et filtres dynamiques.

### 🛡 Console Administrateur
- **Dashboard Statistique** : Visualisation du revenu total, du nombre de réservations et des performances par établissement.
- **Gestion des Ressources** : Module CRUD complet pour administrer le catalogue d'hôtels.
- **Workflow de Réservation** : Système de validation des demandes clients (Confirmation/Annulation).
- **Contrôle des Accès** : Gestion fine des rôles et des comptes utilisateurs.

## 🏗 Architecture & Stack Technique
- **Architecture** : MVVM (Model-View-ViewModel) pour une séparation stricte des préoccupations.
- **Base de données** : Google Cloud Firestore (Base NoSQL temps réel).
- **Authentification** : Firebase Auth (Sécurisation des sessions).
- **Intelligence Artificielle** : Google Generative AI SDK (Modèle Gemini Flash).
- **Performance** : ImageLoader personnalisé avec cache mémoire (LruCache) et multi-threading.

## 🛠 Installation & Configuration
1. **Firebase** : Téléchargez le fichier `google-services.json` depuis votre console Firebase et placez-le dans le dossier `app/`.
2. **API Gemini** : 
   - Générez une clé sur [Google AI Studio](https://aistudio.google.com/).
   - Ajoutez-la dans votre fichier `local.properties` : `GEMINI_API_KEY=votre_cle_ici`.
3. **Build** : Compilez le projet avec Android Studio (Graddle 8.0+ recommandé).

---
*Projet Académique de Fin de Module - 2026*
*Développé avec passion pour l'innovation mobile.*
