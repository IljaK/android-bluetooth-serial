package com.iljak.android_bluetooth_serial.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.iljak.android_bluetooth_serial.R;
import com.iljak.android_bluetooth_serial.bluetooth.BluetoothClientService;
import com.iljak.android_bluetooth_serial.bluetooth.BluetoothState;
import com.iljak.android_bluetooth_serial.broadcast.CommonBroadcastReceiver;
import com.iljak.android_bluetooth_serial.broadcast.IBroadcastReceiver;
import com.iljak.android_bluetooth_serial.model.BtDeviceDataModel;
import com.iljak.android_bluetooth_serial.service.CommonServiceBinder;

public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection, IBroadcastReceiver {

    private static final String LOG_TAG = BaseActivity.class.getSimpleName();
    protected ProgressBar progressBar;

    private BluetoothClientService btService;
    private CommonBroadcastReceiver btServiceReciver = new CommonBroadcastReceiver(this, btServiceIntentFilter());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        btServiceReciver.register();

        createService();
    }

    protected void createService()
    {
        Intent btClientServiceIntent = new Intent(this, BluetoothClientService.class);
        bindService(btClientServiceIntent, this, BIND_AUTO_CREATE);
    }

    public boolean connectToDevice(BtDeviceDataModel deviceData) {
        if (deviceData == null) return false;
        if (btService == null) return false;

        Toast.makeText(getApplicationContext(), "Connecting: " + deviceData.getName(), Toast.LENGTH_LONG).show();

        if (btService.isConnected()) {
            btService.disconnect();
        }

        return btService.createConnection(deviceData);
    }

    public void setScreenLocked(boolean isLocked) {
        if (isLocked) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public void disconnectBT()
    {
        if (btService != null) {
            btService.disconnect();
        }
    }

    @Override
    protected void onStart() {
        setScreenLocked(false);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        btServiceReciver.unregister();
        disconnectBT();
        unbindService(this);
        btService = null;
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    protected IntentFilter btServiceIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothClientService.BT_SERVICE_STATE);
        return intentFilter;
    }

    public BluetoothClientService getBtService() {
        return btService;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sendStringData(String data) {
        if (btService != null) {
            btService.sendStringData(data);
        }
    }

    @Override
    public void onNullBinding(ComponentName name) {
        Log.d(LOG_TAG, "onNullBinding");
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(LOG_TAG, "onServiceConnected");
        btService = (BluetoothClientService) ((CommonServiceBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LOG_TAG, "onServiceDisconnected");
        btService = null;
    }

    @Override
    public void onReceive(BroadcastReceiver receiver, Context context, Intent intent) {
        //Log.d(LOG_TAG, "onReceive: " + intent.getAction());
        if (receiver == btServiceReciver) {
            if (intent.getAction().equals(BluetoothClientService.BT_SERVICE_STATE)) {
                int state = intent.getIntExtra(BluetoothClientService.BT_SERVICE_EXTRA_DATA, 0);
                handleStateChanged(BluetoothState.getState(state));
            }
        }
    }

    protected void handleStateChanged(BluetoothState state) {
        Log.d(LOG_TAG, "Handle state: " + state.toString());
        /*
        switch (state) {
            case CONNECTION_FAILED:
                hideProgressBar();
                break;
            case CONNECTION_ESTABLISHED:
                //preferences.setDeviceAddress(bluetoothClient.getBtDeviceData().getAddress());
                openMainActivity();
                break;
            case CONNECTION_LOST:
                hideProgressBar();
                checkMainBTState();
                break;
        }
        */
    }
}
