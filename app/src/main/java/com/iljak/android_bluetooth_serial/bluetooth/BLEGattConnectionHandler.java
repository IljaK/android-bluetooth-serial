package com.iljak.android_bluetooth_serial.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEGattConnectionHandler extends BluetoothGattCallback {
    
    private static final String LOG_TAG = BLEGattConnectionHandler.class.getSimpleName();

    public final static UUID ESP32_SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E"); // UART service UUID

    public final static UUID UART_SERVER_TX_CHARACTERISTICS = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public final static UUID UART_SERVER_RX_CHARACTERISTICS = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    public final static UUID TEMPERATURE_STATE_UUID = UUID.fromString("6E400004-B5A3-F393-E0A9-E50E24DCCA9E");
    public final static UUID BATTERY_STATE_UUID = UUID.fromString("6E400005-B5A3-F393-E0A9-E50E24DCCA9E");

    public final static UUID MEMORY_STATE_UUID = UUID.fromString("6E400006-B5A3-F393-E0A9-E50E24DCCA9E");

    public final static int MAX_BLE_MESSAGE_SIZE = 20;
    private final BleMessageManager bleTxManager = new BleMessageManager();

    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothClientService mBtService;

    private BluetoothGatt mBluetoothGatt;
    private volatile boolean isActive = false;

    public BLEGattConnectionHandler(BluetoothClientService btService) {
        mBtService = btService;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean connect(final String address) {
        Log.d(LOG_TAG, "connect " + address);
        mBtService.handleStateChanged(BluetoothState.CONNECTING);

        if (mBluetoothAdapter == null || address == null) {
            isActive = false;
            mBtService.handleStateChanged(BluetoothState.CONNECTION_FAILED);
            return false;
        }

        BluetoothDevice device = null;

        // Previously connected device.  Try to reconnect.
        if (mBluetoothGatt != null) {
            Log.d(LOG_TAG, "connect reuse GATT");

            device = mBluetoothGatt.getDevice();

            if (device != null && address.equals(device.getAddress())) {
                if (mBluetoothGatt.connect()) {
                    Log.d(LOG_TAG, "GATT reconnect...");
                    isActive = true;
                    mBtService.handleStateChanged(BluetoothState.CONNECTING);
                    return true;
                }
                Log.d(LOG_TAG, "GATT reconnect failed!");
                mBtService.handleStateChanged(BluetoothState.CONNECTION_FAILED);
                isActive = false;
                return false;
            } else {
                disconnect();
            }
        }

        device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.d(LOG_TAG, "getRemoteDevice failed!");
            isActive = false;
            mBtService.handleStateChanged(BluetoothState.CONNECTION_FAILED);
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.

        mBluetoothGatt = device.connectGatt(mBtService, false, this);
        Log.d(LOG_TAG, "connectGatt...");
        isActive = true;
        mBtService.handleStateChanged(BluetoothState.CONNECTING);
        return true;
    }

    public void disconnect() {
        Log.d(LOG_TAG, "disconnect");
        isActive = false;
        if (mBluetoothGatt == null) return;
        bleTxManager.clear();
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Log.d(LOG_TAG, "onConnectionStateChange status: " + Integer.toString(status) + " newState: " + Integer.toString(newState));

        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                mBluetoothGatt.discoverServices();
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
            case BluetoothProfile.STATE_DISCONNECTED:
                mBtService.handleStateChanged(BluetoothState.CONNECTION_LOST);
                isActive = false;
                break;
        }
    }

    private void setCharacteristcsDesctiptor(BluetoothGatt gatt)
    {
        Log.d(LOG_TAG, "setCharacteristcsDesctiptor");


        for (BluetoothGattService service : gatt.getServices()) {

            Log.d(LOG_TAG, "Service: " + service.getUuid());

            if (service.getUuid().equals(ESP32_SERVICE_UUID)) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {

                    Log.d(LOG_TAG, "characteristic: " + characteristic.getUuid().toString());

                    if (characteristic.getUuid().equals(UART_SERVER_RX_CHARACTERISTICS)) continue;

                    gatt.setCharacteristicNotification(characteristic, true);
                    /*
                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        Log.d(LOG_TAG, "descriptor: " + descriptor.getUuid().toString());
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }*/
                }
            }

        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        Log.d(LOG_TAG, "onServicesDiscovered status: " + Integer.toString(status));
        if (status == BluetoothGatt.GATT_SUCCESS) {
            setCharacteristcsDesctiptor(gatt);
            isActive = true;
            mBtService.handleStateChanged(BluetoothState.CONNECTION_ESTABLISHED);
        } else {

        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        Log.d(LOG_TAG, "onCharacteristicRead " + characteristic.getUuid());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        Log.d(LOG_TAG, "onCharacteristicChanged " + characteristic.getUuid());

        if (characteristic.getUuid().equals(UART_SERVER_TX_CHARACTERISTICS)) {
            mBtService.handleUartRXData(characteristic.getValue());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        Log.d(LOG_TAG, "onCharacteristicWrite " + characteristic.getUuid());

        if (status == BluetoothGatt.GATT_SUCCESS && bleTxManager.isValidTransfer(characteristic.getValue())) {
            gatt.executeReliableWrite();
            bleTxManager.transferSuccess();

            if (bleTxManager.isMessageTransfered()) {
                if (bleTxManager.needRespond()) {
                    mBtService.handleUartSent(bleTxManager.getFullMessage());
                }
                bleTxManager.flushActual();
            }

        } else {
            //Log.d(LOG_TAG, "abortReliableWrite");
            gatt.abortReliableWrite();
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);

        //Log.d(LOG_TAG, "onReliableWriteCompleted: " + status);

        sendUartPacket();
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        //Log.d(LOG_TAG, "onMtuChanged");
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        //Log.d(LOG_TAG, "onDescriptorRead " + descriptor.getUuid());
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        //Log.d(LOG_TAG, "onDescriptorWrite " + descriptor.getUuid());
    }

    private void sendUartPacket() {

        byte[] data = bleTxManager.getData();

        if (data == null) return;

        if (mBluetoothGatt == null) return;

        BluetoothGattService service = mBluetoothGatt.getService(ESP32_SERVICE_UUID);

        if (service == null) return;

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UART_SERVER_RX_CHARACTERISTICS);

        if (characteristic == null) return;

        characteristic.setValue(data);
        mBluetoothGatt.beginReliableWrite();
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sendTXUartData(byte[] data, boolean respondSend) {
        if (mBluetoothGatt == null) return;

        BluetoothDevice device = mBluetoothGatt.getDevice();

        if (device == null) return;

        if (!isActive) return;

        bleTxManager.addMessage(data, respondSend);
        if (!bleTxManager.getTransferring()) {
            sendUartPacket();
        }

    }

    public boolean IsActive() {
        return isActive;
    }
}
