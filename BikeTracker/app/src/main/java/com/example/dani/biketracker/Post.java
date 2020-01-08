package com.example.dani.biketracker;

import java.text.DecimalFormat;

public class Post {

    private String lat;
    private String lon;

    private long GPSTimestamp;

    public double currentSpacing;

    public double userSpeed;
    public double leaderSpeed;

    public double acc;
    private double uk;

    //RTT
    public long sendTime;
    public long mySendTime;
    public long RTT;


    public Post() {
        this.lat = "0";
        this.lon = "0";
        this.userSpeed = 0;
        this.leaderSpeed = 0;
        this.acc = 0;
        this.uk = 0;
    }

    public Post(String LAT, String  LON, double userSpeed, double leaderSpeed, double acc, long GPSTimestamp) {
        this.lat = LAT;
        this.lon = LON;
        this.userSpeed = userSpeed;
        this.leaderSpeed = leaderSpeed;
        this.acc = acc;
        this.GPSTimestamp = GPSTimestamp;
    }

    public Post(String  LAT, String  LON, double userSpeed, double leaderSpeed, double acc, double u_k) {
        this.lat = LAT;
        this.lon = LON;
        this.userSpeed = userSpeed;
        this.leaderSpeed = leaderSpeed;
        this.acc = acc;
        this.uk = u_k;
    }

    public double getLeaderSpeed() {
        return leaderSpeed;
    }

    public void setCurrentSpacing(double currentSpacing) {
        this.currentSpacing = currentSpacing;
    }

    public void setSendTime(long time) {

        /*DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(0);
        String str = df.format(time);*/

        this.sendTime = time;
    }

    public void setTravelTime(long time) {

        /*DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(0);
        String str = df.format(time);*/

        this.RTT = time;
    }

    public long getSendTime() {
        return sendTime;
    }

    public String getLon() {
        return lon;
    }

    public String getLat() {
        return lat;
    }

    public void setMySendTime(long time) {
        this.mySendTime = time;
    }

    public void setGPSTimestamp(long time) {
        this.GPSTimestamp = time;
    }

    public void setUk(double u_k) {
        this.uk = u_k;
    }

    public long getGPSTimestamp() {
        return GPSTimestamp;
    }

    public double getUk() {
        return uk;
    }
}
