package com.zendrive.phonegap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

@SuppressLint("ApplySharedPref")
public class SharedPrefsManager {
    private SharedPreferences prefs;
    private static SharedPrefsManager sharedInstance;

    private static final String DRIVER_ID = "driverId";
    private static final String USER_ON_DUTY = "isUserOnDuty";
    private static final String PASSENGERS_IN_CAR = "passengersInCar";
    private static final String PASSENGERS_WAITING_FOR_PICKUP = "passengersWaitingForPickup";
    private static final String TRACKING_ID = "trackingId";
    private static final String ZENDRIVE_SETTINGS_ERRORS = "errorsFound";
    private static final String ZENDRIVE_SETTINGS_WARNINGS = "warningsFound";
    private static final String RETRY_ZENDRIVE_SETUP = "retry_zendrive_setup";

    public static synchronized SharedPrefsManager sharedInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new SharedPrefsManager(context);
        }
        return sharedInstance;
    }

    private SharedPrefsManager(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getDriverId() {
        return prefs.getString(DRIVER_ID, null);
    }

    public void setDriverId(String driverId) {
        prefs.edit().putString(DRIVER_ID, driverId).apply();
    }

    Boolean isUserOnDuty() {
        return prefs.getBoolean(USER_ON_DUTY, false);
    }

    void setIsUserOnDuty(boolean isUserOnDuty) {
        prefs.edit().putBoolean(USER_ON_DUTY, isUserOnDuty).apply();
    }

    Integer passengersInCar() {
        return prefs.getInt(PASSENGERS_IN_CAR, 0);
    }

    void setPassengersInCar(int passengersInCar) {
        prefs.edit().putInt(PASSENGERS_IN_CAR, passengersInCar).apply();
    }

    Integer passengersWaitingForPickup() {
        return prefs.getInt(PASSENGERS_WAITING_FOR_PICKUP, 0);
    }

    void setPassengersWaitingForPickup(int passengersWaitingForPickup) {
        prefs.edit().putInt(PASSENGERS_WAITING_FOR_PICKUP, passengersWaitingForPickup).apply();
    }

    String getTrackingId() {
        return prefs.getString(TRACKING_ID, null);
    }

    void setTrackingId(String trackingId) {
        prefs.edit().putString(TRACKING_ID, trackingId).apply();
    }

    public boolean isSettingsErrorFound() {
        return prefs.getBoolean(ZENDRIVE_SETTINGS_ERRORS, false);
    }

    public void setSettingsErrorsFound(boolean errorsFound) {
        prefs.edit().putBoolean(ZENDRIVE_SETTINGS_ERRORS, errorsFound).apply();
    }

    public boolean isSettingsWarningsFound() {
        return prefs.getBoolean(ZENDRIVE_SETTINGS_WARNINGS, false);
    }

    public void setSettingsWarningsFound(boolean warningsFound) {
        prefs.edit().putBoolean(ZENDRIVE_SETTINGS_WARNINGS, warningsFound).apply();
    }

    public boolean shouldRetryZendriveSetup() {
        return prefs.getBoolean(RETRY_ZENDRIVE_SETUP, false);
    }

    public void setRetryZendriveSetup(boolean retry) {
        prefs.edit().putBoolean(RETRY_ZENDRIVE_SETUP, retry).apply();
    }
}
