# Documentation Technique - ReservaApp

## 🏗 Architecture (MVVM)
ReservaApp utilise l'architecture **Model-View-ViewModel** pour séparer proprement la logique métier de l'interface utilisateur.

-   **Model** : Classes POJO (`Hotel`, `User`, `Booking`) représentant les données Firestore.
-   **View** : Activités Android et fichiers Layout XML gérant l'interaction utilisateur.
-   **ViewModel** : Gère l'état de l'interface et communique avec les Repositories.
-   **Repository** : Centralise les appels vers Firebase Firestore.

## 📡 Services de Données

### Firebase Firestore
La base de données est structurée en 3 collections principales :
1.  `hotels` : Contient les informations, prix, et URLs d'images des établissements.
2.  `bookings` : Stocke les réservations liées à un `userId` et un `hotelId`.
3.  `users` : Profils utilisateurs incluant le `role` (admin/client) et la liste des `favorites`.

### Google Gemini AI
L'application intègre le modèle `gemini-flash-lite-latest` pour fournir un assistant de recherche intelligent.
-   **Flux** : Le `ChatViewModel` extrait tous les hôtels de Firestore, construit un prompt incluant cette liste, et demande à Gemini de recommander le meilleur hôtel selon la requête utilisateur.
-   **Parsing** : Les réponses incluent des tags `[HOTEL_ID:id]` qui sont détectés par l'application pour afficher dynamiquement un bouton "Voir Détails".

## 🛠 Composants Personnalisés

### ImageLoader
Un chargeur d'images "fait maison" utilisant :
-   Un **LruCache** pour stocker les Bitmaps en mémoire et éviter les téléchargements redondants.
-   Un **ExecutorService** (pool de threads) pour le téléchargement asynchrone.
-   Un mécanisme de **Tagging** pour éviter les erreurs d'affichage dans les listes recyclées (RecyclerView).

## 🛡 Sécurité et Rôles
Le système de rôles est géré par un champ `role` dans le profil utilisateur.
-   **Admin** : Accès au Dashboard, CRUD des hôtels, validation des réservations.
-   **Client** : Recherche, consultation, favoris et réservation.

## 🚀 Guide de Déploiement
1.  Activer **Email/Password Authentication** dans la console Firebase.
2.  Créer les collections `hotels` et `users` (ou laisser le `FirebaseHelper` les initialiser au premier lancement).
3.  Ajouter la clé `GEMINI_API_KEY` dans le fichier `local.properties`.
