package me.drblau.fumailapp.util.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class Settings {

    //SharedPreferences
    public static final String PREFS_NAME = "loginData";
    public static final String PREFS_MAIL = "Email";
    public static final String mailDefault = null;
    public static final String PREFS_PASSWORD = "Password";
    public static final String PREFS_USERNAME = "Username";
    public static final String usernameDefault = "FU Berlin Mail App User";
    public static final String PREFS_STAY_LOGGED_IN = "Keep_Login";
    public static final String PREFS_IV = "IV";

    private SharedPreferences settings;

    private byte[] iv = null;
    private static String usernameRes;
    private static String mailRes;
    private static String passRes;
    private static boolean keepLoggedInRes;

    public Settings(Activity activity) {

        settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if(settings.getString(PREFS_IV, null) != null) {
            iv = Base64.decode(settings.getString(PREFS_IV, null), Base64.DEFAULT);
        }

        usernameRes = settings.getString(PREFS_USERNAME, usernameDefault);
        mailRes = settings.getString(PREFS_MAIL, mailDefault);
        passRes = settings.getString(PREFS_PASSWORD, "password");
        keepLoggedInRes = settings.getBoolean(PREFS_STAY_LOGGED_IN, true);
    }

    public void delete() {
        settings.edit().clear().apply();
    }

    public byte[] getIv() {
        return iv;
    }

    public String getUsername() {
        return usernameRes;
    }

    public String getPassword() {
        return passRes;
    }

    public String getMail() {
        return mailRes;
    }

    public boolean getLoggedIn() {
        return keepLoggedInRes;
    }


    /**
     *
     * @param key Preferences Key. Ideally called with Setting.PREFS_KEY
     * @param content Content to be inserted (note that iv should be encoded into a string)
     */
    public void update(String key, Object content) {
        if(content instanceof String) {
            settings.edit().putString(key, (String) content).apply();
        }
        else if(content instanceof Boolean) {
            settings.edit().putBoolean(key, (boolean) content).apply();
        }
    }
}
