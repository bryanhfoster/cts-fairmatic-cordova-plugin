package com.zendrive.phonegap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.apache.cordova.BuildConfig;
import com.zendrive.R;
import com.zendrive.sdk.ZendriveOperationResult;

/**
 * Utility to create notifications to show to the user when the Zendrive SDK has
 * something interesting to report.
 */
public class NotificationUtility {
    public static final int FOREGROUND_MODE_NOTIFICATION_ID = 98;
    public static final int LOCATION_DISABLED_NOTIFICATION_ID = 99;
    public static final int LOCATION_PERMISSION_DENIED_NOTIFICATION_ID = 100;

    private static final String FOREGROUND_CHANNEL_KEY = "Foreground";
    private static final String SETTINGS_CHANNEL_KEY = "Settings";
    private static final String LOCATION_CHANNEL_KEY = "Location";
    private static NotificationManagerCompat notificationManager;

    public static Notification createWaitingForDriveNotification(Context context) {
        createNotificationChannels(context);
        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_KEY)
                .setPriority(NotificationCompat.PRIORITY_MIN).setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentText("Application is waiting for drive.")
                .setContentIntent(getNotificationClickIntent(context)).build();
    }

    public static Notification createMaybeInDriveNotification(Context context) {
        createNotificationChannels(context);
        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_KEY)
                .setPriority(NotificationCompat.PRIORITY_MIN).setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentText("Application is tracking your location.")
                .setContentIntent(getNotificationClickIntent(context)).build();
    }

    public static Notification createInDriveNotification(Context context) {
        createNotificationChannels(context);
        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_KEY)
                .setCategory(NotificationCompat.CATEGORY_SERVICE).setContentText("Application is tracking your location.")
                .setContentIntent(getNotificationClickIntent(context)).build();
    }

    private static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel lowPriorityNotificationChannel = new NotificationChannel(FOREGROUND_CHANNEL_KEY,
                    "Application trip tracking", NotificationManager.IMPORTANCE_MIN);
            lowPriorityNotificationChannel.setShowBadge(false);
            manager.createNotificationChannel(lowPriorityNotificationChannel);

            NotificationChannel defaultNotificationChannel = new NotificationChannel(SETTINGS_CHANNEL_KEY, "Problems",
                    NotificationManager.IMPORTANCE_HIGH);
            defaultNotificationChannel.setShowBadge(true);
            manager.createNotificationChannel(defaultNotificationChannel);

        }
    }

    private static PendingIntent getNotificationClickIntent(Context context) {
        Intent notificationIntent = new Intent(context.getApplicationContext(), NotificationActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent, 0);
    }

    public static Notification createLocationSettingDisabledNotification(Context context,
                                                                         ZendriveOperationResult settingsResult) {
        createNotificationChannels(context);
        if (BuildConfig.DEBUG && settingsResult.isSuccess()) {
            throw new AssertionError("Only expected failed settings result");
        }
        // TODO: use the result from the callback and show appropriate message and intent
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                callGPSSettingIntent, 0);

        return new NotificationCompat.Builder(context.getApplicationContext(), LOCATION_CHANNEL_KEY)
                .setContentTitle("Location is disabled")
                //.setTicker(context.getResources().getString(context.getResources().getIdentifier("R.string.location_disabled", "string", context.getPackageName())))
                .setContentText("The location is disabled.")
                //.setSmallIcon(context.getResources().getIdentifier("R.drawable.ic_notification", "drawable", context.getPackageName()))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_ERROR)
                .build();
    }
}