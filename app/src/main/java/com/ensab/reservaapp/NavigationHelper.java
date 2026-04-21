package com.ensab.reservaapp;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationHelper {

    public static void setSelectedItem(Activity activity, int navId) {
        int[] navIds = {R.id.navDiscover, R.id.navSaved, R.id.navBookings, R.id.navProfile};
        int[] indicatorIds = {R.id.indicatorDiscover, R.id.indicatorSaved, R.id.indicatorBookings, R.id.indicatorProfile};
        int[] iconIds = {R.id.ivDiscover, R.id.ivSaved, R.id.ivBookings, R.id.ivProfileNav};
        int[] textIds = {R.id.tvDiscover, R.id.tvSaved, R.id.tvBookings, R.id.tvProfileNav};

        for (int i = 0; i < navIds.length; i++) {
            View indicator = activity.findViewById(indicatorIds[i]);
            ImageView icon = activity.findViewById(iconIds[i]);
            TextView text = activity.findViewById(textIds[i]);

            if (indicator == null || icon == null || text == null) continue;

            if (navIds[i] == navId) {
                // ACTIVE STATE (Airbnb style)
                indicator.setVisibility(View.VISIBLE);
                icon.setColorFilter(Color.BLACK);
                text.setTextColor(Color.BLACK);
                text.setTypeface(null, Typeface.BOLD);
                icon.setAlpha(1.0f);
                text.setAlpha(1.0f);
            } else {
                // INACTIVE STATE
                indicator.setVisibility(View.INVISIBLE);
                icon.setColorFilter(Color.parseColor("#717171"));
                text.setTextColor(Color.parseColor("#717171"));
                text.setTypeface(null, Typeface.NORMAL);
                icon.setAlpha(0.6f);
                text.setAlpha(0.6f);
            }
        }
    }
}