package com.ensab.reservaapp.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ImageLoader est un chargeur d'images personnalisé léger.
 * Il utilise un cache mémoire (LruCache) et un pool de threads pour charger les images
 * de manière asynchrone sans bloquer l'interface utilisateur.
 */
public class ImageLoader {
    private static ImageLoader instance;
    private final LruCache<String, Bitmap> memoryCache; // Cache pour stocker les images déjà téléchargées
    private final ExecutorService executorService;      // Pool de threads pour le téléchargement
    private final Handler mainHandler;                  // Pour revenir sur le thread UI après téléchargement

    private ImageLoader() {
        // Calcul de la taille maximale du cache (1/8 de la mémoire disponible)
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        executorService = Executors.newFixedThreadPool(4); // 4 téléchargements simultanés max
        mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Singleton pour accéder au chargeur d'images partout dans l'app.
     */
    public static synchronized ImageLoader getInstance() {
        if (instance == null) {
            instance = new ImageLoader();
        }
        return instance;
    }

    /**
     * Charge une image depuis une URL dans un ImageView.
     * @param urlString URL de l'image
     * @param imageView Destination
     * @param placeholderResId Image par défaut en attendant le chargement
     */
    public void load(String urlString, ImageView imageView, int placeholderResId) {
        if (placeholderResId != 0) {
            imageView.setImageResource(placeholderResId);
        }

        if (urlString == null || urlString.isEmpty()) return;

        // Vérification si l'image est déjà en mémoire
        Bitmap cachedBitmap = memoryCache.get(urlString);
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        // Tag pour éviter que l'image ne s'affiche dans la mauvaise cellule (Recycling)
        imageView.setTag(urlString);
        executorService.execute(() -> {
            Bitmap bitmap = downloadBitmap(urlString);
            if (bitmap != null) {
                memoryCache.put(urlString, bitmap); // Mise en cache
                mainHandler.post(() -> {
                    // On ne met à jour que si l'URL est toujours celle demandée
                    if (urlString.equals(imageView.getTag())) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    /**
     * Télécharge le flux binaire de l'image depuis le web.
     */
    private Bitmap downloadBitmap(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}