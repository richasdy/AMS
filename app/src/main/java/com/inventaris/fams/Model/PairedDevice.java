package com.inventaris.fams.Model;

/**
 * Created by mwildani on 23/08/2017.
 */

public class PairedDevice {
    private String deviceName, deviceHardwareAddress;

    public PairedDevice(String deviceName, String deviceHardwareAddress) {
        this.deviceName = deviceName;
        this.deviceHardwareAddress = deviceHardwareAddress;
    }

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
}
