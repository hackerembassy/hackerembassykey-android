package com.keimoger.hackerembassy.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class PreferencesUtil {

    private final SharedPreferences mAppSharedPrefs;

    public PreferencesUtil(Context c) {
        this.mAppSharedPrefs = c.getSharedPreferences("com.keimoger.hackerembassy_preferences", Context.MODE_PRIVATE);
    }

    //generate getters and setters for username, password and host
    public String getUsername() {
        return mAppSharedPrefs.getString("username", null);
    }

    public PreferencesUtil setUsername(String username) {
        mAppSharedPrefs.edit().putString("username", username).apply();
        return this;
    }

    public String getPassword() {
        return mAppSharedPrefs.getString("password", null);
    }

    public PreferencesUtil setPassword(String password) {
        mAppSharedPrefs.edit().putString("password", password).apply();
        return this;
    }

    public String getHost() {
        return mAppSharedPrefs.getString("host", null);
    }

    public PreferencesUtil setHost(String host) {
        mAppSharedPrefs.edit().putString("host", host).apply();
        return this;
    }

    public int getConnectionMethod() {
        return mAppSharedPrefs.getInt("connection_method", 0);
    }

    public void setConnectionMethod(int connectionMethod) {
        mAppSharedPrefs.edit().putInt("connection_method", connectionMethod).apply();
    }
}
