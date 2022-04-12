package com.iljak.android_bluetooth_serial.model;

import android.bluetooth.BluetoothDevice;

public class BtDeviceDataModel {
    private String name;
    private String address;
    private int type;

    public BtDeviceDataModel(String name, String address, int type) {
        this.name=name;
        this.address=address;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getType() {
        return type;
    }

    public boolean isBLE() {
        return type >= BluetoothDevice.DEVICE_TYPE_LE;
    }

    public boolean isClassic() {
        return type == BluetoothDevice.DEVICE_TYPE_CLASSIC || type == BluetoothDevice.DEVICE_TYPE_DUAL;
    }
}
