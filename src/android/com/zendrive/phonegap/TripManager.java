package com.zendrive.phonegap;

import android.content.Context;

public class TripManager {

    public class State {
        private boolean isUserOnDuty;
        private int passengersWaitingForPickup;
        private int passengersInCar;
        private String trackingId;

        public boolean isUserOnDuty() {
            return isUserOnDuty;
        }

        public int getPassengersWaitingForPickup() {
            return passengersWaitingForPickup;
        }

        public int getPassengersInCar() {
            return passengersInCar;
        }

        String getTrackingId() {
            return trackingId;
        }

        State(boolean isUserOnDuty, int passengersWaitingForPickup,
              int passengersInCar, String trackingId) {
            this.isUserOnDuty = isUserOnDuty;
            this.passengersWaitingForPickup = passengersWaitingForPickup;
            this.passengersInCar = passengersInCar;
            this.trackingId = trackingId;
        }

        State(State another) {
            this.isUserOnDuty = another.isUserOnDuty;
            this.passengersWaitingForPickup = another.passengersWaitingForPickup;
            this.passengersInCar = another.passengersInCar;
            this.trackingId = another.trackingId;
        }
    }

    private static TripManager sharedInstance;

    public static synchronized TripManager sharedInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new TripManager(context);
        }
        return sharedInstance;
    }

    private State state;
    private TripManager(Context context) {
        SharedPrefsManager sharedPrefsManager = SharedPrefsManager.sharedInstance(context);
        state = new State(sharedPrefsManager.isUserOnDuty(),
                sharedPrefsManager.passengersWaitingForPickup(),
                sharedPrefsManager.passengersInCar(),
                sharedPrefsManager.getTrackingId());
    }

    public synchronized void acceptNewPassengerRequest(Context context) {
        state.passengersWaitingForPickup += 1;
        SharedPrefsManager.sharedInstance(context)
                .setPassengersWaitingForPickup(state.passengersWaitingForPickup);
        updateTrackingIdIfNeeded(context);
        ZendriveManager.getSharedInstance().updateZendriveInsurancePeriod(context);
    }

    public synchronized void pickupAPassenger(Context context) {
        state.passengersWaitingForPickup -= 1;
        SharedPrefsManager.sharedInstance(context)
                .setPassengersWaitingForPickup(state.passengersWaitingForPickup);

        state.passengersInCar += 1;
        SharedPrefsManager.sharedInstance(context)
                .setPassengersInCar(state.passengersInCar);
        updateTrackingIdIfNeeded(context);
        ZendriveManager.getSharedInstance().updateZendriveInsurancePeriod(context);
    }

    public synchronized void cancelARequest(Context context) {
        state.passengersWaitingForPickup -= 1;
        SharedPrefsManager.sharedInstance(context)
                .setPassengersWaitingForPickup(state.passengersWaitingForPickup);
        updateTrackingIdIfNeeded(context);
        ZendriveManager.getSharedInstance().updateZendriveInsurancePeriod(context);
    }

    public synchronized void dropAPassenger(Context context) {
        state.passengersInCar -= 1;
        SharedPrefsManager.sharedInstance(context).setPassengersInCar(state.passengersInCar);
        updateTrackingIdIfNeeded(context);
        ZendriveManager.getSharedInstance().updateZendriveInsurancePeriod(context);
    }

    public synchronized void goOnDuty(Context context) {
        state.isUserOnDuty = true;
        SharedPrefsManager.sharedInstance(context).setIsUserOnDuty(state.isUserOnDuty);
        updateTrackingIdIfNeeded(context);
        ZendriveManager.getSharedInstance().updateZendriveInsurancePeriod(context);
    }

    public synchronized void goOffDuty(Context context) {
        state.isUserOnDuty = false;
        SharedPrefsManager.sharedInstance(context).setIsUserOnDuty(state.isUserOnDuty);
        updateTrackingIdIfNeeded(context);
        ZendriveManager.getSharedInstance().updateZendriveInsurancePeriod(context);
    }

    private void updateTrackingIdIfNeeded(Context context) {
        if (state.passengersWaitingForPickup > 0 || state.passengersInCar > 0) {
            // We need trackingId
            if (state.trackingId == null) {
                state.trackingId = ((Long)System.currentTimeMillis()).toString();
                SharedPrefsManager.sharedInstance(context).setTrackingId(state.trackingId);
            }
        }
        else {
            state.trackingId = null;
            SharedPrefsManager.sharedInstance(context).setTrackingId(state.trackingId);
        }
    }

    public synchronized State getTripManagerState() {
        return new State(state);
    }
}
