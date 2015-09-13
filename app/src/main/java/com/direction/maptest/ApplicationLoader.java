package com.direction.maptest;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Hananideen on 13/9/2015.
 */
public class ApplicationLoader extends Application {

    public static final String Settings_PREFS_NAME = "SettingsFile";

    private static SharedPreferences Settings;
    private static SharedPreferences.Editor SettingsEditor;
    private static ApplicationLoader Instance;
    private static Context mAppContext;

    // global server IP address
    private static final String IP = "http://mynetsys.com/";


    @Override
    public void onCreate() {
        super.onCreate();


        init();

    }

    public void init(){
        Instance = this;
        Instance.setAppContext(getApplicationContext());
        //initialize the singleton.
        VolleySingleton.init();
        Settings = getSharedPreferences(Settings_PREFS_NAME, MODE_PRIVATE);
        SettingsEditor = Settings.edit();

    }

    public static ApplicationLoader getInstance(){
        return Instance;
    }
    public static Context getContext(){
        return mAppContext;
    }
    public void setAppContext(Context context){
        mAppContext = context;
    }
    public static String getIp(String fname) {
        return IP+fname;
    }
    public SharedPreferences getSettingPrefFile() {return Settings;}
    public SharedPreferences.Editor getSettingsPrefFileEditor() {return SettingsEditor;}
}