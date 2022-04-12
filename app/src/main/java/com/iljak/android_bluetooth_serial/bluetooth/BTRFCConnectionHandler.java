package com.iljak.android_bluetooth_serial.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class BTRFCConnectionHandler extends Thread {

    private final Handler mHandler;
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String connectedRequest = "+CONN:SUCCESS\r\n";
    private BluetoothDevice mBtDevice;

    private BluetoothAdapter mBluetooth;
    private BluetoothSocket mSocket;

    private InputStream mInStream;
    private OutputStream mOutStream;

    private volatile boolean isActive = false;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public BTRFCConnectionHandler(Handler.Callback handlerCallback) {
        mHandler = new Handler(Objects.requireNonNull(Looper.myLooper()), handlerCallback);
        mBluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void run() {

        try {
            mSocket = mBtDevice.createInsecureRfcommSocketToServiceRecord(mUUID);//create a RFCOMM (SPP) connection
        } catch (IOException e) {
            failedToConnect(e);
            return;
        }

        try {
            mBluetooth.cancelDiscovery();
            mSocket.connect();
        } catch (IOException e) {
            failedToConnect(e);
            return;
        }

        try {
            mInStream = mSocket.getInputStream();
        } catch (IOException e) {
            failedToConnect(e);
            return;
        }
        try {
            mOutStream = mSocket.getOutputStream();
        } catch (IOException e) {
            failedToConnect(e);
            return;
        }

        Message connectMsg = mHandler.obtainMessage(BluetoothState.CONNECTION_ESTABLISHED.ordinal());
        connectMsg.sendToTarget();

        write(connectedRequest.getBytes(StandardCharsets.US_ASCII));

        // Keep listening to the InputStream until an exception occurs.
        while (isActive) {
            if (mSocket == null) break;
            if (!mSocket.isConnected()) break;

            try {
                // Read from the InputStream.
                if (mInStream.available() > 0) {


                    int available = mInStream.available();
                    byte[] mBuffer = new byte[available];

                    available = mInStream.read(mBuffer, 0, available);
                    if (available > 0) {
                        // Send the obtained bytes to the UI activity.
                        //Log.d("RFC", new String(mBuffer, StandardCharsets.US_ASCII));
                        Message streamMsg = mHandler.obtainMessage(-1, available, -1, mBuffer);
                        streamMsg.sendToTarget();
                    }
                }
            } catch (IOException e) {
                Message sendExp = mHandler.obtainMessage(BluetoothState.CONNECTION_LOST.ordinal());
                sendExp.sendToTarget();
                isActive = false;
                break;
            }
        }
        disconnect();
    }

    private synchronized void failedToConnect(IOException e)
    {
        disconnect();
        Message msg = mHandler.obtainMessage(BluetoothState.CONNECTION_FAILED.ordinal());
        msg.sendToTarget();
    }

    // Call this from the main activity to send data to the remote device.
    public synchronized boolean write(byte[] bytes) {
        try {
            if (mOutStream != null) {
                mOutStream.write(bytes);
                return true;
            }
        } catch (IOException e) {
            // Send a failure message back to the activity.
        }
        Message writeErrorMsg = mHandler.obtainMessage(BluetoothState.CONNECTION_LOST.ordinal());
        writeErrorMsg.sendToTarget();
        disconnect();
        return false;
    }

    // Call this method from the main activity to shut down the connection.
    public synchronized void disconnect() {
        isActive = false;
        if (mInStream != null) {
            try {
                mInStream.close();
            } catch (IOException e) {
                Log.e("BT Thread", "Could not close the connect socket", e);
            }
            mInStream = null;
        }
        if (mOutStream != null) {
            try {
                mOutStream.close();
            } catch (IOException e) {
                Log.e("BT Thread", "Could not close the connect socket", e);
            }
            mOutStream = null;
        }
        mBluetooth = null;
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e("BT Thread", "Could not close the connect socket", e);
            }
            mSocket = null;
        }
    }

    public boolean IsActive() {
        return isActive;
    }

    public boolean connect(String address) {
        Message msg = mHandler.obtainMessage(BluetoothState.INITIALIZING.ordinal());
        msg.sendToTarget();

        if (mBluetooth == null) {
            failedToConnect(null);
            return false;
        }

        mBtDevice = mBluetooth.getRemoteDevice(address);

        if (mBtDevice == null) {
            failedToConnect(null);
            return false;
        } else {
            msg = mHandler.obtainMessage(BluetoothState.CONNECTING.ordinal());
            msg.sendToTarget();
            isActive = true;
            start();
        }
        return true;
    }
}