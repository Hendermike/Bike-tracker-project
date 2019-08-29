package com.example.dani.biketracker;

interface SerialListener {
    void onSerialConnect();
    void onSerialConnectError (Exception e);
    void onSerialRead(byte[] data);
    void onSerialIoError      (Exception e);
}
