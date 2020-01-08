package com.example.dani.biketracker;

import android.location.Location;

public class MyAcc {

    Location currentLocation = null;
    Location previousLocation = null;
    double myAcc;

    public MyAcc() {

    }

    public MyAcc(Location currentLocation) {
        this.currentLocation = currentLocation;
        this.myAcc = 0;
    }

    public void updateAcc(Location newLocation) {
        this.previousLocation = this.currentLocation;
        this.currentLocation = newLocation;
        if (previousLocation != null) {
            this.myAcc = 1000 * (currentLocation.getSpeed() - previousLocation.getSpeed()) / (currentLocation.getTime() - previousLocation.getTime());
        }
        else {
            this.myAcc = 0;
        }
    }

    public double getMyAcc() {
        return this.myAcc;
    }
}
