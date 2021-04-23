package com.zendrive.phonegap;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveDriveDetectionMode;
import com.zendrive.sdk.ZendriveDriverAttributes;
import com.zendrive.sdk.ZendriveOperationCallback;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.sdk.insurance.ZendriveInsurance;

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

    private static final String [] permissions = { permission.ACCESS_FINE_LOCATION, permission.ACCESS_NETWORK_STATE, permission.ACCESS_WIFI_STATE,
            permission.INTERNET, permission.ACCESS_COARSE_LOCATION, permission.WAKE_LOCK,
            permission.WAKE_LOCK, permission.SYSTEM_ALERT_WINDOW, permission.RECEIVE_BOOT_COMPLETED,
            permission.RECEIVE_BOOT_COMPLETED };

    private CallbackContext callbackContext;

    public android.content.Context OverrideContext = null;

    static CordovaInterface getCordovaInstance() {
        return CORDOVA_INSTANCE;
    }

    public android.content.Context getAppContextThroughApp() {
        return OverrideContext == null ? this.cordova.getActivity().getApplication().getApplicationContext() : OverrideContext;
    }

    public android.content.Context getAppContext() {
        return OverrideContext == null ? this.cordova.getActivity().getApplicationContext() : OverrideContext;
    }

    public android.content.Context getContext() {
        return OverrideContext == null ? this.cordova.getContext() : OverrideContext;
    }

    @Override
    protected synchronized void pluginInitialize() {
        super.pluginInitialize();
        if (CORDOVA_INSTANCE == null) {
            CORDOVA_INSTANCE = cordova;
        }
        //TODO: this checks the version of the app to be over lollipop
        ZendriveManager.init(getContext());

        // this used to be "requestPermission"
        if (cordova != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cordova.requestPermission(this, LOCATION_PERMISSION_REQUEST, permission.ACCESS_FINE_LOCATION);
        }
    }

    public void manuallyInitializePlugin() {
        pluginInitialize();
    }

    private void requestAppPermissions()
    {
        if (cordova != null) {
            PermissionHelper.requestPermissions(this, 0, permissions);
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for(int r:grantResults)
        {
            if(r == PackageManager.PERMISSION_DENIED)
            {
                if (this.callbackContext != null)
                    this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {

        // store the callback context so that the other methods can call it.
        this.callbackContext = callbackContext;

        cordova.getThreadPool().execute(() -> {
            try {
                if (action.equals("setup")) {
                    setup(args);
                } else if (action.equals("teardown")) {
                    teardown();
                } else if (action.equals("startDrive")) {
                    startDrive(args);
                } else if (action.equals("getActiveDriveInfo")) {
                    getActiveDriveInfo();
                } else if (action.equals("stopDrive")) {
                    stopManualDrive();
                } else if (action.equals("startSession")) {
                    startSession(args);
                } else if (action.equals("stopSession")) {
                    stopSession();
                } else if (action.equals("setDriveDetectionMode")) {
                    setDriveDetectionMode(args);
                } else if (action.equals("setProcessStartOfDriveDelegateCallback")) {
                    ZendriveManager.getSharedInstance().setProcessStartOfDriveDelegateCallback(args,
                            callbackContext);
                } else if (action.equals("setProcessEndOfDriveDelegateCallback")) {
                    ZendriveManager.getSharedInstance().setProcessEndOfDriveDelegateCallback(args, callbackContext);
                } else if (action.equals("pickupPassenger")) {
                    pickupPassenger();
                } else if (action.equals("dropoffPassenger")) {
                    dropoffPassenger();
                } else if (action.equals("acceptPassengerRequest")) {
                    acceptPassengerRequest();
                } else if (action.equals("cancelPassengerRequest")) {
                    cancelPassengerRequest();
                } else if (action.equals("goOnDuty")) {
                    goOnDuty();
                } else if (action.equals("goOffDuty")) {
                    goOffDuty();
                } else if(action.equals("requestPermissions")) {
                    this.requestAppPermissions();
                }

                SuccessCallback(); // Thread-safe.
            } catch (Throwable e) {
                this.ErrorCallback("Unexpected error in exec.", e);
            }
        });

        return true;
    }

    private void setup(JSONArray args) throws JSONException {

        JSONObject configJsonObj = args.getJSONObject(0);
        if (configJsonObj == null) {
            ErrorCallback("Wrong configuration supplied");
            return;
        }

        String applicationKey = getStringFromJson(configJsonObj, Config_PropertyName_ApplicationKey);
        String driverId = getStringFromJson(configJsonObj, Config_PropertyName_DriverId);

        Integer driveDetectionModeInt = null;
        if (hasValidValueForKey(configJsonObj, kDriveDetectionModeKey)) {
            driveDetectionModeInt = configJsonObj.getInt(kDriveDetectionModeKey);
        } else {
            ErrorCallback("Wrong drive detection mode supplied");
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
                BuildCallback("Zendrive setup failed"));
    }

    public void setup(final CallbackContext callbackContext, ZendriveConfiguration configuration, ZendriveDriverAttributes driverAttributes) throws JSONException {
        // setup Zendrive SDK
        Zendrive.setup(
                this.getAppContext(),
                configuration,
                ZendriveCordovaBroadcastReceiver.class,
                ZendriveNotificationProviderImpl.class,
                BuildCallback("Zendrive setup failed"));
    }

    public void teardown() throws JSONException {
        ZendriveManager.getSharedInstance().teardown(this.getAppContext(),
                callbackContext);
        SuccessCallback();
    }

    public void goOnDuty() throws JSONException {
        ZendriveInsurance.startPeriod1(this.getAppContext(),
                BuildCallback("Zendrive goOnDuty failed"));
    }

    public void goOffDuty() throws JSONException {
        // clear tracking id if not already cleared.
        ZendriveManager.getSharedInstance().setTrackingId(null);
        ZendriveInsurance.stopPeriod(this.getAppContext(),
                BuildCallback("Zendrive goOffDuty failed"));
    }

    public void acceptPassengerRequest() throws JSONException {
        String trackingId = ZendriveManager.getSharedInstance().generateTrackingKey();
        ZendriveInsurance.startDriveWithPeriod2(
                this.getAppContext(),
                trackingId,
                BuildCallback("Zendrive acceptPassengerRequest failed"));
    }

    public void pickupPassenger() throws JSONException {
        String trackingId = ZendriveManager.getSharedInstance().generateTrackingKeyIfNull();
        ZendriveInsurance.startDriveWithPeriod3(
                this.getAppContext(),
                trackingId,
                BuildCallback("Zendrive acceptPassengerRequest failed"));
    }

    public void cancelPassengerRequest() throws JSONException {
        // clear tracking id and go back to period 1
        ZendriveInsurance.startPeriod1(this.getAppContext(),
                BuildCallback("Zendrive cancelPassengerRequest failed"));
    }

    public void dropoffPassenger() throws JSONException {
        // clear tracking id and go back to period 1
        ZendriveInsurance.startPeriod1(this.getAppContext(),
                BuildCallback("Zendrive dropoffPassenger failed"));
    }

    public void startDrive(JSONArray args) throws JSONException {
        Zendrive.startDrive(getAppContextThroughApp(), args.getString(0),
                BuildCallback("Zendrive startDrive failed"));
    }

    public void getActiveDriveInfo() throws JSONException {
        JSONObject activeDriveInfoObject = ZendriveManager.getSharedInstance()
                .getActiveDriveInfo(this.getAppContext());
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

    public void stopManualDrive() throws JSONException {
        Zendrive.stopManualDrive(this.getAppContext(),
                BuildCallback("Zendrive stopManualDrive failed"));
    }

    public void startSession(JSONArray args) throws JSONException {
        Zendrive.startSession(this.getAppContext(), args.getString(0));
        if (callbackContext != null) callbackContext.success();
    }

    public void stopSession() throws JSONException {
        Zendrive.stopSession(this.getAppContext());
        if (callbackContext != null) callbackContext.success();
    }

    public void setDriveDetectionMode(JSONArray args) throws JSONException {
        Integer driveDetectionModeInt = args.getInt(0);
        ZendriveDriveDetectionMode mode = this.getDriveDetectionModeFromInt(driveDetectionModeInt);
        Zendrive.setZendriveDriveDetectionMode(this.getAppContext(), mode,
                BuildCallback("Zendrive setDriveDetectionMode failed"));
    }

    public ZendriveDriveDetectionMode getDriveDetectionModeFromInt(Integer driveDetectionModeInt) {
        switch (driveDetectionModeInt) {
            case 0: return ZendriveDriveDetectionMode.AUTO_ON;
            case 1: return ZendriveDriveDetectionMode.AUTO_OFF;
            default: return ZendriveDriveDetectionMode.INSURANCE;
        }
    }

    public ZendriveDriverAttributes getDriverAttrsFromJsonObject(JSONObject configJsonObj) throws JSONException {
        Object driverAttributesObj = getObjectFromJSONObject(configJsonObj, kDriverAttributesKey);
        ZendriveDriverAttributes driverAttributes = null;
        if (null != driverAttributesObj && !JSONObject.NULL.equals(driverAttributesObj)) {
            JSONObject driverAttrJsonObj = (JSONObject) driverAttributesObj;
            driverAttributes = new ZendriveDriverAttributes();

            Object firstName = getObjectFromJSONObject(driverAttrJsonObj, "firstName");
            if (!isNull(firstName)) {
                try {
                    driverAttributes.setCustomAttribute("firstName", firstName.toString());
                } catch (Exception ignored) {
                }
            }

            Object lastName = getObjectFromJSONObject(driverAttrJsonObj, "lastName");
            if (!isNull(lastName)) {
                try {
                    driverAttributes.setCustomAttribute("lastName", lastName.toString());
                } catch (Exception ignored) {
                }
            }

            Object email = getObjectFromJSONObject(driverAttrJsonObj, "email");
            if (!isNull(email)) {
                try {
                    driverAttributes.setCustomAttribute("email", email.toString());
                } catch (Exception ignored) {
                }
            }

            Object group = getObjectFromJSONObject(driverAttrJsonObj, "group");
            if (!isNull(group)) {
                try {
                    driverAttributes.setGroup(group.toString());
                } catch (Exception ignored) {
                }
            }

            Object phoneNumber = getObjectFromJSONObject(driverAttrJsonObj, "phoneNumber");
            if (!isNull(phoneNumber)) {
                try {
                    driverAttributes.setCustomAttribute("phoneNumber", phoneNumber.toString());
                } catch (Exception ignored) {
                }
            }

            Object driverStartDateStr = getObjectFromJSONObject(driverAttrJsonObj, "driverStartDate");
            if (!isNull(driverStartDateStr)) {
                try {
                    Long driverStartDateTimestampInMillis = Long.parseLong(driverStartDateStr.toString()) * 1000;
                    Date driverStartDate = new Date(driverStartDateTimestampInMillis);
                    driverAttributes.setCustomAttribute("driverStartDate", driverStartDate.toString());
                } catch (Exception ignored) {
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
                        } catch (Exception ignored) {
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

    private String getStringFromJson(JSONObject configJsonObj, String key) throws JSONException {
        Object valueObj = getObjectFromJSONObject(configJsonObj, key);
        String value = null;
        if (!isNull(valueObj)) {
            value = valueObj.toString();
        }
        return value;
    }

    private ZendriveOperationCallback BuildCallback(String errorMessage) {
        return result -> HandleZendriveOperationResult(errorMessage, result);
    }

    private void HandleZendriveOperationResult(String errorMessage, ZendriveOperationResult result) {
        if (result.isSuccess()) {
            SuccessCallback();
        } else {
            ErrorCallback(errorMessage, result);
        }
    }

    private void SuccessCallback() {
        if (this.callbackContext != null) {
            final PluginResult result = new PluginResult(PluginResult.Status.OK);
            this.callbackContext.sendPluginResult(result);
        } else {
            Log.d(TAG, "Success");
        }
    }

    private void SuccessCallback(String message) {
        if (this.callbackContext != null) {
            final PluginResult result = new PluginResult(PluginResult.Status.OK, message);
            result.setKeepCallback(false);
            this.callbackContext.sendPluginResult(result);
        } else {
            Log.d(TAG, "Success - " + message);
        }
    }

    private void SuccessCallback(JSONObject dataObject) {
        if (this.callbackContext != null) {
            final PluginResult result = new PluginResult(PluginResult.Status.OK, dataObject);
            result.setKeepCallback(false);
            this.callbackContext.sendPluginResult(result);
        } else {
            Log.d(TAG, dataObject.toString());
        }
    }

    private void ErrorCallback(String errorMessage, Throwable ex) {
        ErrorCallback(errorMessage
                .concat(" ").concat(ex.getMessage())
                .concat("\r\n").concat(getStackTraceString(ex)));
    }

    private void ErrorCallback(String errorMessage, ZendriveOperationResult result) {
        ErrorCallback(errorMessage
                .concat(" ").concat(result.getErrorCode().toString())
                .concat(" - ").concat(result.getErrorMessage()));
    }

    private void ErrorCallback(String errorMessage) {
        if (this.callbackContext != null) {
            final PluginResult result = new PluginResult(PluginResult.Status.ERROR, errorMessage);
            this.callbackContext.sendPluginResult(result);
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