package com.inventaris.fams.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mwildani on 23/08/2017.
 */

public class PairedDevice implements Parcelable{
    private String deviceName, deviceHardwareAddress;

    public PairedDevice(String deviceName, String deviceHardwareAddress) {
        this.deviceName = deviceName;
        this.deviceHardwareAddress = deviceHardwareAddress;
    }

    protected PairedDevice(Parcel in) {
        deviceName = in.readString();
        deviceHardwareAddress = in.readString();
    }

    public static final Creator<PairedDevice> CREATOR = new Creator<PairedDevice>() {
        @Override
        public PairedDevice createFromParcel(Parcel in) {
            return new PairedDevice(in);
        }

        @Override
        public PairedDevice[] newArray(int size) {
            return new PairedDevice[size];
        }
    };

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceHardwareAddress() {
        return deviceHardwareAddress;
    }

    public void setDeviceHardwareAddress(String deviceHardwareAddress) {
        this.deviceHardwareAddress = deviceHardwareAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceName);
        dest.writeString(deviceHardwareAddress);
    }
}
