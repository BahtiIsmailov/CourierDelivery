package com.wb.logistics.utils.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

public class SharedWorkerImpl implements SharedWorker {

    @NonNull
    private final SharedPreferences preferences;

    public SharedWorkerImpl(@NonNull Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public boolean load(@NonNull String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    @Override
    public void save(@NonNull String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

}