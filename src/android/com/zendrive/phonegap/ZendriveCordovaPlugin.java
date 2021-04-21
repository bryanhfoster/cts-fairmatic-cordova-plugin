package com.zendrive.phonegap;

import android.Manifest;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.util.Log;

import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveDriveDetectionMode;
import com.zendrive.sdk.ZendriveDriverAttributes;
import com.zendrive.sdk.ZendriveOperationCallback;
import com.zendrive.sdk.ZendriveOperationResult;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by chandan on 11/3/14.
 */
public class ZendriveCordovaPlugin extends CordovaPlugin {
    // ZendriveDriverAttributes dictionary keys
    private static final String kCustomAttributesKey = "customAttributes";
    private static final String kDriverAttributesKey = "driverAttributes";
    private static final String kDriveDetectionModeKey = "driveDetectionMode";

    private static final String TAG = "ZendriveCordovaPlugin";
    private static final String Config_PropertyName_DriverId = "driverId";
    private static final String Config_PropertyName_ApplicationKey = "applicationKey";

    private static CordovaInterface CORDOVA_INSTANCE;

    private static final String PERMISSION_DENIED_ERROR = "Location permission denied by user";
    private static final int LOCATION_PERMISSION_REQUEST = 42;

    private String [] permissions = { permission.ACCESS_FINE_LOCATION, permission.ACCESS_NETWORK_STATE, permission.ACCESS_WIFI_STATE,
            permission.INTERNET, permission.ACCESS_BACKGROUND_LOCATION, permission.ACCESS_COARSE_LOCATION, permission.WAKE_LOCK,
            permission.WAKE_LOCK, permission.ACTIVITY_RECOGNITION, permission.SYSTEM_ALERT_WINDOW, permission.RECEIVE_BOOT_COMPLETED,
            permission.RECEIVE_BOOT_COMPLETED, permission.USE_FULL_SCREEN_INTENT };

    private CallbackContext callbackContext;

    public android.content.Context OverrideContext = null;
    private android.content.Context getAppContextThroughApp() {
        return OverrideContext == null ? this.cordova.getActivity().getApplication().getApplicationContext() : OverrideContext;
    }
    private android.content.Context getAppContext() {
        return OverrideContext == null ? this.cordova.getActivity().getApplicationContext() : OverrideContext;
    }
    private android.content.Context getContext() {
        return OverrideContext == null ? this.cordova.getContext() : OverrideContext;
    }




    @Override
    protected synchronized void pluginInitialize() {
        super.pluginInitialize();
        if (CORDOVA_INSTANCE == null) {
            CORDOVA_INSTANCE = cordova;
        }
        ZendriveManager.init(getContext());
        requestPermissions();
    }

    static CordovaInterface getCordovaInstance() {
        return CORDOVA_INSTANCE;
    }

    public void manuallyInitCordovaPlugin() {
        this.pluginInitialize();
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        this.callbackContext = callbackContext;
        cordova.getThreadPool().execute(() -> {
            try {
                if (action.equals("setup")) {
                    setup(args);
                } else if (action.equals("teardown")) {
                    teardown(args);
                } else if (action.equals("startDrive")) {
                    startDrive(args);
                } else if (action.equals("getActiveDriveInfo")) {
                    getActiveDriveInfo();
                } else if (action.equals("stopDrive")) {
                    stopManualDrive(args);
                } else if (action.equals("startSession")) {
                    startSession(args);
                } else if (action.equals("stopSession")) {
                    stopSession(args);
                } else if (action.equals("setDriveDetectionMode")) {
                    setDriveDetectionMode(args);
                } else if (action.equals("setProcessStartOfDriveDelegateCallback")) {
                    ZendriveManager.getSharedInstance().setProcessStartOfDriveDelegateCallback(args,
                            callbackContext);
                } else if (action.equals("setProcessEndOfDriveDelegateCallback")) {
                    ZendriveManager.getSharedInstance().setProcessEndOfDriveDelegateCallback(args, callbackContext);
                } else if (action.equals("pickupPassenger")) {
                    pickupPassenger(callbackContext);
                } else if (action.equals("dropoffPassenger")) {
                    dropoffPassenger(callbackContext);
                } else if (action.equals("acceptPassengerRequest")) {
                    acceptPassengerRequest(callbackContext);
                } else if (action.equals("cancelPassengerRequest")) {
                    cancelPassengerRequest(callbackContext);
                } else if (action.equals("goOnDuty")) {
                    goOnDuty(callbackContext);
                } else if (action.equals("goOffDuty")) {
                    goOffDuty(callbackContext);
                } else if(action.equals("requestPermissions")) {
                    this.requestPermissions(generateRandom());
                }
                callbackContext.success(); // Thread-safe.
            } catch (JSONException e) {
                callbackContext.error(e.getMessage());
            }
        });

        return true;
    }
    protected int generateRandom(){
        Random rn = new Random();
        return rn.nextInt(1000000) + 1;
    }

    @Override
    public void requestPermissions(int requestCode)
    {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
        this.callbackContext.success();
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int grant : grantResults) {
            if (grant == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
    }

    private void setup(JSONArray args) throws JSONException {

        JSONObject configJsonObj = args.getJSONObject(0);
        if (configJsonObj == null) {
            callbackContext.error("Wrong configuration supplied");
            return;
        }

        String applicationKey = getApplicationKey(configJsonObj);
        String driverId = getDriverId(configJsonObj);

        Integer driveDetectionModeInt = null;
        if (hasValidValueForKey(configJsonObj, kDriveDetectionModeKey)) {
            driveDetectionModeInt = configJsonObj.getInt(kDriveDetectionModeKey);
        } else {
            callbackContext.error("Wrong drive detection mode supplied");
            return;
        }

        ZendriveDriveDetectionMode mode = this.getDriveDetectionModeFromInt(driveDetectionModeInt);
        ZendriveConfiguration configuration = new ZendriveConfiguration(applicationKey, driverId, mode);

        ZendriveDriverAttributes driverAttributes = this.getDriverAttrsFromJsonObject(configJsonObj);
        if (driverAttributes != null) {
            configuration.setDriverAttributes(driverAttributes);
        }

        // setup Zendrive SDK
        Zendrive.setup(
                this.getAppContext(),
                configuration,
                ZendriveCordovaBroadcastReceiver.class,
                ZendriveNotificationProviderImpl.class,
                zendriveOperationResult -> {
                    if (zendriveOperationResult.isSuccess()) {
                        callbackContext.success();
                    } else {
                        callbackContext.error("Zendrive setup failed");
                    }
                });
    }

    public void setup(final CallbackContext callbackContext, ZendriveConfiguration configuration, ZendriveDriverAttributes driverAttributes) throws JSONException {
        // setup Zendrive SDK
        Zendrive.setup(
                this.getAppContext(),
                configuration,
                ZendriveCordovaBroadcastReceiver.class,
                ZendriveNotificationProviderImpl.class,
                zendriveOperationResult -> {
                    if (zendriveOperationResult.isSuccess()) {
                        callbackContext.success();
                    } else {
                        callbackContext.error("Zendrive setup failed");
                    }
                });
    }

    private void requestPermissions() {
        if (cordova != null) {
            for (int i=0; i< permissions.length; i++) {
                cordova.requestPermission(this, LOCATION_PERMISSION_REQUEST, permissions[i]);
            }
        }
    }

    public void teardown(JSONArray args) throws JSONException {
        ZendriveManager.getSharedInstance().teardown(this.getAppContext(),
                callbackContext);
        callbackContext.success();
    }

    public void pickupPassenger(final CallbackContext callbackContext) {
        try {
            TripManager.sharedInstance(getAppContext()).pickupAPassenger(this.getAppContext());
            SuccessCallback(callbackContext);
        } catch (Throwable e) {
            ErrorCallback(callbackContext, "An unexpected error occurred during pickupPassenger.", e);
        }
    }

    public void dropoffPassenger(final CallbackContext callbackContext) {
        try {
            TripManager.sharedInstance(getAppContext()).dropAPassenger(this.getAppContext());
            SuccessCallback(callbackContext);
        } catch (Throwable e) {
            ErrorCallback(callbackContext, "An unexpected error occurred during dropoffPassenger.", e);
        }
    }

    public void acceptPassengerRequest(final CallbackContext callbackContext) {
        try {
            TripManager.sharedInstance(getAppContext()).acceptNewPassengerRequest(this.getAppContext());
            SuccessCallback(callbackContext);
        } catch (Throwable e) {
            ErrorCallback(callbackContext, "An unexpected error occurred during acceptPassengerRequest.", e);
        }
    }

    public void cancelPassengerRequest(final CallbackContext callbackContext) {
        try {
            TripManager.sharedInstance(getAppContext()).cancelARequest(this.getAppContext());
            SuccessCallback(callbackContext);
        } catch (Throwable e) {
            ErrorCallback(callbackContext, "An unexpected error occurred during cancelPassengerRequest.", e);
        }
    }

    public void goOnDuty(final CallbackContext callbackContext) {
        try {
            TripManager.sharedInstance(getAppContext()).goOnDuty(this.getAppContext());
            SuccessCallback(callbackContext);
        } catch (Throwable e) {
            ErrorCallback(callbackContext, "An unexpected error occurred during goOnDuty.", e);
        }
    }

    public void goOffDuty(final CallbackContext callbackContext) {
        try {
            TripManager.sharedInstance(getAppContext()).goOffDuty(this.getAppContext());
            SuccessCallback(callbackContext);
        } catch (Throwable e) {
            ErrorCallback(callbackContext, "An unexpected error occurred during goOffDuty.", e);
        }
    }


    private void startDrive(JSONArray args) throws JSONException {
        Zendrive.startDrive(getAppContextThroughApp(), args.getString(0),
                zendriveOperationResult -> {
                    if (zendriveOperationResult.isSuccess()) {
                        callbackContext.success();
                    } else {
                        callbackContext.error(zendriveOperationResult.getErrorMessage());
                    }
                });
    }

    private void getActiveDriveInfo() throws JSONException {
        JSONObject activeDriveInfoObject = ZendriveManager.getSharedInstance()
                .getActiveDriveInfo(this.getAppContext(), callbackContext);
        PluginResult result;
        if (activeDriveInfoObject != null) {
            result = new PluginResult(PluginResult.Status.OK, activeDriveInfoObject);
        } else {
            String resultStr = null;
            result = new PluginResult(PluginResult.Status.OK, resultStr);
        }
        result.setKeepCallback(false);
        callbackContext.sendPluginResult(result);
    }

    private void stopManualDrive(JSONArray args) throws JSONException {
        Zendrive.stopManualDrive(this.getAppContext(), //args.getString(0),
                zendriveOperationResult -> {
                    if (zendriveOperationResult.isSuccess()) {
                        callbackContext.success();
                    } else {
                        callbackContext.error(zendriveOperationResult.getErrorMessage());
                    }
                });
    }

    private void startSession(JSONArray args) throws JSONException {
        Zendrive.startSession(this.getAppContext(), args.getString(0));
    }

    private void stopSession(JSONArray args) throws JSONException {
        Zendrive.stopSession(this.getAppContext());
        callbackContext.success();
    }

    private void setDriveDetectionMode(JSONArray args) throws JSONException {
        Integer driveDetectionModeInt = args.getInt(0);
        if (driveDetectionModeInt == null) {
            callbackContext.error("Invalid Zendrive drive detection mode");
            return;
        }

        ZendriveDriveDetectionMode mode = this.getDriveDetectionModeFromInt(driveDetectionModeInt);
        Zendrive.setZendriveDriveDetectionMode(this.getAppContext(), mode,
                zendriveOperationResult -> {
                    if (zendriveOperationResult.isSuccess()) {
                        callbackContext.success();
                    } else {
                        callbackContext.error(zendriveOperationResult.getErrorMessage());
                    }
                });
    }

    private ZendriveDriveDetectionMode getDriveDetectionModeFromInt(Integer driveDetectionModeInt) {
        ZendriveDriveDetectionMode mode = driveDetectionModeInt == 1 ? ZendriveDriveDetectionMode.AUTO_OFF
                : ZendriveDriveDetectionMode.AUTO_ON;
        return mode;
    }

    private ZendriveDriverAttributes getDriverAttrsFromJsonObject(JSONObject configJsonObj) throws JSONException {
        Object driverAttributesObj = getObjectFromJSONObject(configJsonObj, kDriverAttributesKey);
        ZendriveDriverAttributes driverAttributes = null;
        if (null != driverAttributesObj && !JSONObject.NULL.equals(driverAttributesObj)) {
            JSONObject driverAttrJsonObj = (JSONObject) driverAttributesObj;
            driverAttributes = new ZendriveDriverAttributes();

            Object firstName = getObjectFromJSONObject(driverAttrJsonObj, "firstName");
            if (!isNull(firstName)) {
                try {
                    driverAttributes.setCustomAttribute("firstName", firstName.toString());
                } catch (Exception e) {
                }
            }

            Object lastName = getObjectFromJSONObject(driverAttrJsonObj, "lastName");
            if (!isNull(lastName)) {
                try {
                    driverAttributes.setCustomAttribute("lastName", lastName.toString());
                } catch (Exception e) {
                }
            }

            Object email = getObjectFromJSONObject(driverAttrJsonObj, "email");
            if (!isNull(email)) {
                try {
                    driverAttributes.setCustomAttribute("email", email.toString());
                } catch (Exception e) {
                }
            }

            Object group = getObjectFromJSONObject(driverAttrJsonObj, "group");
            if (!isNull(group)) {
                try {
                    driverAttributes.setGroup(group.toString());
                } catch (Exception e) {
                }
            }

            Object phoneNumber = getObjectFromJSONObject(driverAttrJsonObj, "phoneNumber");
            if (!isNull(phoneNumber)) {
                try {
                    driverAttributes.setCustomAttribute("phoneNumber", phoneNumber.toString());
                } catch (Exception e) {
                }
            }

            Object driverStartDateStr = getObjectFromJSONObject(driverAttrJsonObj, "driverStartDate");
            if (!isNull(driverStartDateStr)) {
                try {
                    Long driverStartDateTimestampInMillis = Long.parseLong(driverStartDateStr.toString()) * 1000;
                    Date driverStartDate = new Date(driverStartDateTimestampInMillis);
                    driverAttributes.setCustomAttribute("driverStartDate", driverStartDate.toString());
                } catch (Exception e) {
                }

            }

            if (hasValidValueForKey(driverAttrJsonObj, kCustomAttributesKey)) {
                JSONObject customAttrs = driverAttrJsonObj.getJSONObject(kCustomAttributesKey);
                Iterator<?> keys = customAttrs.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    Object value = getObjectFromJSONObject(customAttrs, key);
                    if (value instanceof String) {
                        try {
                            driverAttributes.setCustomAttribute(key, (String) value);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }

        return driverAttributes;
    }

    // UTILITY METHODS
    private Boolean isNull(Object object) {
        return ((object == null) || JSONObject.NULL.equals(object));
    }

    private Object getObjectFromJSONObject(JSONObject jsonObject, String key) throws JSONException {
        if (hasValidValueForKey(jsonObject, key)) {
            return jsonObject.get(key);
        }
        return null;
    }

    private Boolean hasValidValueForKey(JSONObject jsonObject, String key) {
        return (jsonObject.has(key) && !jsonObject.isNull(key));
    }

    private String getDriverId(JSONObject configJsonObj) throws JSONException {
        Object driverIdObj = getObjectFromJSONObject(configJsonObj, "driverId");
        String driverId = null;
        if (!isNull(driverIdObj)) {
            driverId = driverIdObj.toString();
        }
        return driverId;
    }

    private String getApplicationKey(JSONObject configJsonObj) throws JSONException {
        Object applicationKeyObj = getObjectFromJSONObject(configJsonObj, "applicationKey");
        String applicationKey = null;
        if (!isNull(applicationKeyObj)) {
            applicationKey = applicationKeyObj.toString();
        }
        return applicationKey;
    }


    // UTILITY METHODS
    private String getStringFromJson(JSONObject configJsonObj, String key) throws JSONException {
        Object valueObj = getObjectFromJSONObject(configJsonObj, key);
        String value = null;
        if (!isNull(valueObj)) {
            value = valueObj.toString();
        }
        return value;
    }

    private static ZendriveOperationCallback BuildCallback(final CallbackContext callbackContext, String errorMessage) {
        return new ZendriveOperationCallback() {
            @Override
            public void onCompletion(ZendriveOperationResult result) {
                HandleZendriveOperationResult(callbackContext, errorMessage, result);
            }
        };
    }

    private static void HandleZendriveOperationResult(final CallbackContext callbackContext, String errorMessage, ZendriveOperationResult result) {
        if (result.isSuccess()) {
            SuccessCallback(callbackContext);
        } else {
            ErrorCallback(callbackContext, errorMessage, result);
        }
    }

    private static void SuccessCallback(final CallbackContext callbackContext) {
        if (callbackContext != null) {
            final PluginResult result = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(result);
        } else {
            Log.d(TAG, "Success");
        }
    }

    private static void SuccessCallback(final CallbackContext callbackContext, String message) {
        if (callbackContext != null) {
            final PluginResult result = new PluginResult(PluginResult.Status.OK, message);
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
        } else {
            Log.d(TAG, "Success - " + message);
        }
    }

    private static void SuccessCallback(final CallbackContext callbackContext, JSONObject dataObject) {
        if (callbackContext != null) {
            final PluginResult result = new PluginResult(PluginResult.Status.OK, dataObject);
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
        } else {
            Log.d(TAG, dataObject.toString());
        }
    }

    private static void ErrorCallback(final CallbackContext callbackContext, String errorMessage, Throwable ex) {
        ErrorCallback(callbackContext, errorMessage
                .concat(" ").concat(ex.getMessage())
                .concat("\r\n").concat(getStackTraceString(ex)));
    }

    private static void ErrorCallback(final CallbackContext callbackContext, String errorMessage, ZendriveOperationResult result) {
        ErrorCallback(callbackContext, errorMessage
                .concat(" ").concat(result.getErrorCode().toString())
                .concat(" - ").concat(result.getErrorMessage()));
    }

    private static void ErrorCallback(final CallbackContext callbackContext, String errorMessage) {
        if (callbackContext != null) {
            final PluginResult result = new PluginResult(PluginResult.Status.ERROR, errorMessage);
            callbackContext.sendPluginResult(result);
        } else {
            Log.d(TAG, errorMessage);
        }
    }

    private static String getStackTraceString(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}