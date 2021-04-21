package com.zendrive.phonegap;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zendrive.sdk.ZendriveSettingError;
//import com.zendrive.sdk.*;
import com.zendrive.sdk.AccidentInfo;
import com.zendrive.sdk.AnalyzedDriveInfo;
import com.zendrive.sdk.DriveResumeInfo;
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.EstimatedDriveInfo;
import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveBroadcastReceiver;
import com.zendrive.sdk.ZendriveSettingWarning;
import com.zendrive.sdk.ZendriveSettings;
import com.zendrive.sdk.ZendriveSettingsCallback;

public class ZendriveCordovaBroadcastReceiver extends ZendriveBroadcastReceiver {

    @Override
    public void onDriveStart(Context context, DriveStartInfo startInfo) {
        ZendriveManager.getSharedInstance().onDriveStart(startInfo);
    }

    @Override
    public void onDriveResume(Context context, DriveResumeInfo resumeInfo) {
        ZendriveManager.getSharedInstance().onDriveResume(resumeInfo);
    }

    @Override
    public void onDriveEnd(Context context, EstimatedDriveInfo estimatedDriveInfo) {
        ZendriveManager.getSharedInstance().onDriveEnd(estimatedDriveInfo);
    }

    @Override
    public void onDriveAnalyzed(Context context, AnalyzedDriveInfo analyzedDriveInfo) {
        ZendriveManager.getSharedInstance().onDriveAnalyzed(analyzedDriveInfo);
    }

    @Override
    public void onAccident(Context context, AccidentInfo accidentInfo) {
        ZendriveManager.getSharedInstance().onAccident(accidentInfo);
    }

    @Override
    public void onZendriveSettingsConfigChanged(Context context, boolean errorsFound, boolean warningsFound) {
        //have separate logic to persist the returned errorsFound and warningsFound flag for logging
        if (errorsFound || warningsFound) {
            checkZendriveSettings(context);
        }
    }

    public void checkZendriveSettings(final Context context) {
        Zendrive.getZendriveSettings(context, new ZendriveSettingsCallback() {
            @Override
            public void onComplete(@Nullable ZendriveSettings zendriveSettings) {
                if (zendriveSettings == null) {
                    // The callback returns NULL if SDK is not setup.
                    return;
                }
                // Handle errors in the dotted areas
                for (ZendriveSettingError error : zendriveSettings.errors) {
                    switch (error.type) {
                        case POWER_SAVER_MODE_ENABLED: {
                            Log.d("Tag", "Error " + error.type.toString() );
                            break;
                        }
                        case BACKGROUND_RESTRICTION_ENABLED: {
                            Log.d("Tag", "Error " + error.type.toString() );
                            break;
                        }
                        case GOOGLE_PLAY_SETTINGS_ERROR: {
                            Log.d("Tag", "Error " + error.type.toString() );
                            break;
                        }
                        case LOCATION_PERMISSION_DENIED: {
                            Log.d("Tag", "Error " + error.type.toString() );
                            break;
                        }
                        case LOCATION_SETTINGS_ERROR: {
                            Log.d("Tag", "Error " + error.type.toString() );
                            break;
                        }
                        case WIFI_SCANNING_DISABLED: {
                            Log.d("Tag", "Error " + error.type.toString() );
                            break;
                        }
                    }
                }

                // Handle warnings
                for (ZendriveSettingWarning warning : zendriveSettings.warnings) {
                    switch (warning.type) {
                        case POWER_SAVER_MODE_ENABLED: {
                            Log.d("Tag", "Warning " + warning.type.toString() );
                            break;
                        }
                    }
                }
            }
        });
    }

}
