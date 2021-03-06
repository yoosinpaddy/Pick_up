package com.yoosinpaddy.pickup.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;
import static com.yoosinpaddy.pickup.common.utils.Constants.shared_auth;

public class SharedPref {
    public static void saveSharedPreference(String key, String value, Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(shared_auth, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }
    @SuppressLint("ApplySharedPref")
    public static void saveSharedPreference(String key, Boolean value, Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(shared_auth, MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public static boolean isOnline(Context context){
        SharedPreferences sharedPreferences=context.getSharedPreferences(shared_auth,MODE_PRIVATE);
        return sharedPreferences.getBoolean("status",true);
    }
    public static void deleteAllSharedPreference(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(shared_auth, MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        @SuppressLint("SdCardPath")
        File sharedPreferenceFile = new File("/data/data/"+ context.getPackageName()+ "/shared_prefs/");
        File[] listFiles = sharedPreferenceFile.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                file.delete();
            }
        }
    }
    public static String getSharedPreference(String key, Context context){
        return context.getSharedPreferences(shared_auth, MODE_PRIVATE).getString(key, "");
    }
}
