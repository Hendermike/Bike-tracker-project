package com.example.dani.biketracker;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    String USER_TYPE;
    String MAC_ADDRESS;

    public User(String USER_TYPE, String MAC_ADDRESS) {

        this.USER_TYPE = USER_TYPE;
        this.MAC_ADDRESS = MAC_ADDRESS;

    }

    public User(){

    }

    protected User(Parcel in) {
        USER_TYPE = in.readString();
        MAC_ADDRESS = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(USER_TYPE);
        dest.writeString(MAC_ADDRESS);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getMAC_ADDRESS() {
        return MAC_ADDRESS;
    }

    public String getUSER_TYPE() {
        return USER_TYPE;
    }

    public void setMAC_ADDRESS(String MAC_ADDRESS) {
        this.MAC_ADDRESS = MAC_ADDRESS;
    }

    public void setUSER_TYPE(String USER_TYPE) {
        this.USER_TYPE = USER_TYPE;
    }
}
