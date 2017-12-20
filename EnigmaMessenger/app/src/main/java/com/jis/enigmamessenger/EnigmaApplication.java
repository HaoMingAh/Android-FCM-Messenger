package com.jis.enigmamessenger;

import android.app.Application;
import android.content.ContextWrapper;

import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by JIS on 6/6/2017.
 */

public class EnigmaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize preference
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();


    }
}
