package com.example.dani.biketracker;

import android.location.Location;

public class EstAcc {

    Location currentLocation = null;
    Location previousLocation = null;
    double accelerationEstimation;

    public EstAcc() {

    }

    public EstAcc(Location currentLocation) {
        this.currentLocation = currentLocation;
        this.accelerationEstimation = 0;
    }

    public void update(Location newLocation) {
        this.previousLocation = this.currentLocation;
        this.currentLocation = newLocation;
        if (previousLocation != null) {
            this.accelerationEstimation = 1000 * (currentLocation.getSpeed() - previousLocation.getSpeed()) / (currentLocation.getTime() - previousLocation.getTime());
        }
        else {
            this.accelerationEstimation = 0;
        }
    }

    public double getEstimation() {
        return this.accelerationEstimation;
    }
}
