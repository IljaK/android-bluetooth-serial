package com.iljak.android_bluetooth_serial.activity.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.iljak.android_bluetooth_serial.R;
import com.iljak.android_bluetooth_serial.activity.adapter.BtDeviceListAdapter;
import com.iljak.android_bluetooth_serial.bluetooth.BTScanCallback;
import com.iljak.android_bluetooth_serial.bluetooth.IBTScanCallback;
import com.iljak.android_bluetooth_serial.model.BtDeviceDataModel;
import com.iljak.android_bluetooth_serial.timer.CommonTimer;
import com.iljak.android_bluetooth_serial.timer.ITimerCallback;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BTScanFragment extends BaseBTFragment implements AdapterView.OnItemClickListener, IBTScanCallback, ITimerCallback {

    private static final String LOG_TAG = BTScanFragment.class.getSimpleName();

    private ListView deviceList;
    private BTScanCallback btScanCallback;
    private CommonTimer mScanTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.tab_ble, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(LOG_TAG, "onViewCreated" );

        mScanTimer = new CommonTimer(this);
        btScanCallback = new BTScanCallback(this);

        deviceList = (ListView)view.findViewById(R.id.deviceListView);

        deviceList.setAdapter(btDeviceListAdapter);
        deviceList.setOnItemClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        btDeviceListAdapter.clear();
        Log.d(LOG_TAG, "onResume " );

        if (bluetoothAdapter == null) return;
        if (!bluetoothAdapter.isEnabled()) return;

        StartScan();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPause() {
        Log.d(LOG_TAG, "BTScanFragment onPause " );
        StopScan();
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent == deviceList) {
            BtDeviceListAdapter valueDatapter = (BtDeviceListAdapter) deviceList.getAdapter();
            BtDeviceDataModel deviceData = valueDatapter.getDataModel(position);

            connectToDevice(deviceData);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void connectToDevice(BtDeviceDataModel model)
    {
        if (model == null) return;

        StopScan();

        BluetoothActivity activity = (BluetoothActivity)getActivity();
        if (activity != null) {
            activity.connectToDevice(model);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart" );
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate" );
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop" );
    }

    @Override
    public void onDestroy() {
        StopScan();
        btDeviceListAdapter.clear();
        mScanTimer.stop();
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy" );
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        //Log.d(LOG_TAG, "onScanResult" );

        BluetoothDevice device = result.getDevice();

        if (result.isConnectable()) {
            if (btDeviceListAdapter.getDataModel(device.getAddress()) == null) {
                btDeviceListAdapter.add(new BtDeviceDataModel(device.getName(), device.getAddress(), device.getType()));
            }
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        Log.d(LOG_TAG, "onBatchScanResults" );
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.d(LOG_TAG, "onScanFailed " + Integer.toString(errorCode) );
    }

    @Override
    public void onTimerComplete(CommonTimer timer) {
        Log.d(LOG_TAG, "onTimerComplete" );
        StopScan();

    }

    @Override
    public void onTimerInterval(CommonTimer timer) {
        Log.d(LOG_TAG, "onTimerInterval" );
        //StopScan();
    }

    private void StartScan()
    {
        Log.d(LOG_TAG, "StartScan" );
        if (bluetoothActivity == null) return;
        if (bluetoothAdapter == null) return;

        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        if (scanner == null) return;

        btDeviceListAdapter.clear();
        bluetoothActivity.setScreenLocked(true);
        mScanTimer.schedule(5000, 5000, 1);
        scanner.startScan(btScanCallback);

    }

    private void StopScan()
    {
        Log.d(LOG_TAG, "StopScan" );
        if (bluetoothActivity != null) bluetoothActivity.setScreenLocked(false);
        if (mScanTimer != null) mScanTimer.stop();

        if (bluetoothAdapter == null) return;
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        if (scanner == null) return;
        bluetoothAdapter.getBluetoothLeScanner().stopScan(btScanCallback);
    }

    @Override
    public void onReceive(BroadcastReceiver receiver, Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = bluetoothAdapter.getState();

            switch (state) {
                case BluetoothAdapter.STATE_TURNING_OFF:
                    // The user bluetooth is turning off yet, but it is not disabled yet.
                    break;
                case BluetoothAdapter.STATE_OFF:
                    StopScan();
                    btDeviceListAdapter.clear();
                    // The user bluetooth is already disabled.
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    // The user bluetooth is turning off yet, but it is not disabled yet.
                    break;
                case BluetoothAdapter.STATE_ON:
                    StartScan();
                    // The user bluetooth is already disabled.
                    break;
            }
        }
    }
}
