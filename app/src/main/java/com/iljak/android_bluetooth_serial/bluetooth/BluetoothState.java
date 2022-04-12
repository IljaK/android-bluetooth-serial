package com.iljak.android_bluetooth_serial.bluetooth;

public enum BluetoothState {

    NONE,
    INITIALIZING,
    CONNECTING,
    CONNECTION_ESTABLISHED,
    CONNECTION_FAILED,
    CONNECTION_LOST;

    private static BluetoothState[] values = null;

    public static BluetoothState getState(int value) {
        if (values == null) values = BluetoothState.values();
        for (BluetoothState bluetoothState : values) {
            if (bluetoothState.ordinal() == value) return bluetoothState;
        }
        return BluetoothState.NONE;
    }

}
