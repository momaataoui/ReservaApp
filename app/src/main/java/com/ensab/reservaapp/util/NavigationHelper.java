package com.ensab.reservaapp.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.ensab.reservaapp.R;

/**
 * NavigationHelper est une classe utilitaire centralisant la logique de navigation de l'application.
 * Elle gère l'état visuel de la barre de navigation personnalisée et les transitions entre activités.
 */
public class NavigationHelper {

    /**
     * Met à jour l'apparence des icônes et textes de la barre de navigation selon l'écran actif.
     */
    public static void setSelectedItem(Activity activity, int selectedId) {
        updateNavItem(activity, R.id.navDiscover, R.id.ivDiscover, R.id.tvDiscover, selectedId == R.id.navDiscover);
        updateNavItem(activity, R.id.navBookings, R.id.ivBookings, R.id.tvBookings, selectedId == R.id.navBookings);
        updateNavItem(activity, R.id.navSaved, R.id.ivSaved, R.id.tvSaved, selectedId == R.id.navSaved);
        updateNavItem(activity, R.id.navProfile, R.id.ivProfileNav, R.id.tvProfileNav, selectedId == R.id.navProfile);
    }

    /**
     * Applique les couleurs et les arrière-plans aux éléments d'un item de navigation.
     */
    private static void updateNavItem(Activity activity, int containerId, int iconId, int textId, boolean isSelected) {
        View container = activity.findViewById(containerId);
        ImageView icon = activity.findViewById(iconId);
        TextView text = activity.findViewById(textId);

        if (container != null && icon != null && text != null) {
            int color = isSelected ? ContextCompat.getColor(activity, R.color.black) : ContextCompat.getColor(activity, android.R.color.darker_gray);
            icon.setColorFilter(color);
            text.setTextColor(color);
            // Applique un cercle de fond si l'item est sélectionné
            container.setBackgroundResource(isSelected ? R.drawable.nav_item_bg_selected : 0);
        }
    }

    /**
     * Navigue vers une activité cible.
     */
    public static void fastNavigate(Activity currentActivity, Class<?> targetClass) {
        fastNavigate(currentActivity, targetClass, false);
    }

    /**
     * Effectue une transition instantanée vers l'activité cible.
     * @param finishCurrent Si vrai, ferme l'activité actuelle.
     */
    public static void fastNavigate(Activity currentActivity, Class<?> targetClass, boolean finishCurrent) {
        // Évite de naviguer vers la même page
        if (currentActivity.getClass().equals(targetClass)) return;

        Intent intent = new Intent(currentActivity, targetClass);
        // FLAG_ACTIVITY_NO_ANIMATION : Supprime l'animation pour un effet "Single Page Application"
        // FLAG_ACTIVITY_REORDER_TO_FRONT : Réutilise une instance existante si possible
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        currentActivity.startActivity(intent);

        // Désactivation des animations natives d'Android pour plus de fluidité
        currentActivity.overridePendingTransition(0, 0);

        if (finishCurrent) {
            currentActivity.finish();
        }
    }
}
