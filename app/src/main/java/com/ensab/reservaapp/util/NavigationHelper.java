package com.ensab.reservaapp.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.ensab.reservaapp.R;

public class NavigationHelper {

    public static void setSelectedItem(Activity activity, int selectedId) {
        // Mise à jour visuelle de la barre de navigation
        updateNavItem(activity, R.id.navDiscover, R.id.ivDiscover, R.id.tvDiscover, selectedId == R.id.navDiscover);
        updateNavItem(activity, R.id.navBookings, R.id.ivBookings, R.id.tvBookings, selectedId == R.id.navBookings);
        updateNavItem(activity, R.id.navSaved, R.id.ivSaved, R.id.tvSaved, selectedId == R.id.navSaved);
        updateNavItem(activity, R.id.navProfile, R.id.ivProfileNav, R.id.tvProfileNav, selectedId == R.id.navProfile);
    }

    private static void updateNavItem(Activity activity, int containerId, int iconId, int textId, boolean isSelected) {
        View container = activity.findViewById(containerId);
        ImageView icon = activity.findViewById(iconId);
        TextView text = activity.findViewById(textId);

        if (container != null && icon != null && text != null) {
            int color = isSelected ? ContextCompat.getColor(activity, R.color.black) : ContextCompat.getColor(activity, android.R.color.darker_gray);
            icon.setColorFilter(color);
            text.setTextColor(color);
            container.setBackgroundResource(isSelected ? R.drawable.nav_item_bg_selected : 0);
        }
    }

    // Nouvelle méthode pour une navigation ultra-rapide
    public static void fastNavigate(Activity currentActivity, Class<?> targetClass) {
        fastNavigate(currentActivity, targetClass, false);
    }

    public static void fastNavigate(Activity currentActivity, Class<?> targetClass, boolean finishCurrent) {
        if (currentActivity.getClass().equals(targetClass)) return;

        Intent intent = new Intent(currentActivity, targetClass);
        // FLAG_ACTIVITY_NO_ANIMATION : Supprime l'animation de transition pour un effet instantané
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        currentActivity.startActivity(intent);

        // Supprime l'animation de sortie
        currentActivity.overridePendingTransition(0, 0);

        if (finishCurrent) {
            currentActivity.finish();
        }
    }
}
