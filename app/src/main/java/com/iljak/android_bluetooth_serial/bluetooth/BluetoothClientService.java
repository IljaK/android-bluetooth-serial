package com.iljak.android_bluetooth_serial.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.iljak.android_bluetooth_serial.model.BtDeviceDataModel;
import com.iljak.android_bluetooth_serial.service.CommonServiceBinder;
import com.iljak.android_bluetooth_serial.timer.CommonTimer;
import com.iljak.android_bluetooth_serial.timer.ITimerCallback;
import com.iljak.android_bluetooth_serial.util.StringUtil;

import java.nio.charset.StandardCharsets;

public class BluetoothClientService extends Service implements Handler.Callback, ITimerCallback {

    private static final String LOG_TAG = BluetoothClientService.class.getSimpleName();

    public final static String BT_SERVICE_STATE = "com.example.carmonitor.BT_SERVICE_STATE";
    public final static String BT_SERVICE_ACTION_UART_RX = "com.example.carmonitor.BT_SERVICE_ACTION_UART_RX";
    public final static String BT_SERVICE_ACTION_UART_TX_SENT = "com.example.carmonitor.BT_SERVICE_ACTION_UART_TX_SENT";
    public final static String BT_SERVICE_EXTRA_DATA = "com.example.carmonitor.BT_SERVICE_EXTRA_DATA";

    private final String messageSeparator = "\r\n";

    private BluetoothState btState = BluetoothState.NONE;

    private BTRFCConnectionHandler mBtClient;
    private BLEGattConnectionHandler mBleClient;

    private BtDeviceDataModel mBtDeviceData;
    private String messageStack = "";

    private final CommonServiceBinder serviceBinder = new CommonServiceBinder(this);
    private CommonTimer msgTimer = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mBleClient = new BLEGattConnectionHandler(this);
        msgTimer = new CommonTimer(this);
    }

    @Override
    public void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {

        if (msg.arg1 > 0) {
            handleUartRXData((byte[])msg.obj);
            return true;
        }

        btState = BluetoothState.getState(msg.what);
        handleStateChanged(btState);
        return true;
    }

    public boolean createConnection(BtDeviceDataModel deviceData)
    {
        if (mBtDeviceData != null) {
            if (isConnected() && deviceData.getAddress().equals(mBtDeviceData.getAddress())) {
                return false;
            }
        }

        if(mBtClient != null && mBtClient.IsActive()) {
            mBtClient.disconnect();
        }
        if (mBleClient != null && mBleClient.IsActive()) {
            mBleClient.disconnect();
        }

        mBtDeviceData = deviceData;

        if (deviceData.isBLE()) {
            return mBleClient.connect(deviceData.getAddress());
        } else {
            mBtClient = new BTRFCConnectionHandler(this);
            return mBtClient.connect(deviceData.getAddress());
        }
    }

    public boolean isConnected() {

        if(mBtClient != null && mBtClient.IsActive()) {
            return true;
        }
        if (mBleClient != null && mBleClient.IsActive()) {
            return true;
        }

        return false;
    }

    public void disconnect() {
        mBtDeviceData = null;

        if(mBtClient != null && mBtClient.IsActive()) {
            mBtClient.disconnect();
            mBtClient = null;
        }
        if (mBleClient != null && mBleClient.IsActive()) {
            mBleClient.disconnect();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sendStringData(String data)
    {
        if (data.length() > 0 && isConnected()) {

            if(mBtClient != null && mBtClient.IsActive()) {
                String msg = data + messageSeparator;
                if (mBtClient.write(msg.getBytes(StandardCharsets.US_ASCII))) {
                    handleUartSent(data);
                }
            }
            if (mBleClient != null && mBleClient.IsActive()) {
                mBleClient.sendTXUartData(StringUtil.getBytesWithZero(data), true);
            }
        }
    }

    public BluetoothState getBtState() {
        return btState;
    }

    public BtDeviceDataModel getBtDeviceData() { return mBtDeviceData; }
    public boolean isConnecting() {
        switch (btState) {
            case INITIALIZING:
            case CONNECTING:
                return true;
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public boolean onUnbind(Intent intent) {
        disconnect();
        return super.onUnbind(intent);
    }

    void handleStateChanged(BluetoothState btState) {
        this.btState = btState;
        final Intent intent = new Intent(BT_SERVICE_STATE);
        intent.putExtra(BT_SERVICE_EXTRA_DATA, btState.ordinal());
        sendBroadcast(intent);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void handleUartRXData(byte[] data) {
        if (data == null || data.length == 0) return;

        msgTimer.stop();
        messageStack += new String(data, StandardCharsets.US_ASCII);
        if (messageStack.contains(messageSeparator)) {
            String[] messages = messageStack.split(messageSeparator, -1);
            for (String message : messages) {
                dispatchRXMessage(message);
            }
            messageStack = messages[messages.length - 1];
        }

        if (messageStack.length() > 0) {
            msgTimer.schedule(1000, 1000, 1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void handleUartSent(byte[] data) {
        handleUartSent(new String(data, StandardCharsets.US_ASCII));
    }

    public void handleUartSent(String message) {
        final Intent intent = new Intent(BT_SERVICE_ACTION_UART_TX_SENT);
        intent.putExtra(BT_SERVICE_EXTRA_DATA, message);
        sendBroadcast(intent);
    }

    private void dispatchRXMessage(String message) {
        if (message.length() > 0) {
            Log.d(LOG_TAG, message);
            final Intent intent = new Intent(BT_SERVICE_ACTION_UART_RX);
            intent.putExtra(BT_SERVICE_EXTRA_DATA, message);
            sendBroadcast(intent);
        }
    }

    private String[] getMessageData(String msgType, String msg) {

        try {
            return msg.substring(msgType.length() + 1).split("\\|", -1);
        } catch (Exception ignored) {

        }
        return new String[0];
    }

    @Override
    public void onTimerComplete(CommonTimer timer) {
        if (timer == msgTimer) {
            dispatchRXMessage(messageStack);
            messageStack = "";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onTimerInterval(CommonTimer timer) {
        if (timer == msgTimer) {
        }
    }
}
