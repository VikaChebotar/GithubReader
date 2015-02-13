package com.example.viktoria.githubreader.view;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.example.viktoria.githubreader.R;

import java.lang.reflect.Field;

/**
 * Application class of this App. Calls overrideFont() in onCreate() to set font for all views that extends TextView
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
       overrideFont(getApplicationContext(), getString(R.string.default_font), getString(R.string.font_name));
    }
    private void overrideFont(Context context, String defaultFontNameToOverride, String customFontFileNameInAssets) {
        try {
            final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);
            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {
            Log.e(MainActivity.TAG, getString(R.string.font_error));
        }
    }
}