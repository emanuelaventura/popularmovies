package com.emanuelaventura.popularmovies.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;

import com.emanuelaventura.popularmovies.R;

public class UiUtils {

    public static void showErrorSnackbar(View view, int resId) {
        showErrorSnackbar(view, resId, null);
    }

    public static void showErrorSnackbar(View view, int resId, Snackbar.Callback callback) {
        Snackbar snackbar = Snackbar.make(view, resId, Snackbar.LENGTH_LONG);
        if (callback != null)
            snackbar.addCallback(callback);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.error));
        snackbar.show();
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 180);
    }
}
