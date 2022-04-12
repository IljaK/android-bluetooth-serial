package com.iljak.android_bluetooth_serial.activity.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.iljak.android_bluetooth_serial.activity.adapter.BtDeviceListAdapter;
import com.iljak.android_bluetooth_serial.broadcast.CommonBroadcastReceiver;
import com.iljak.android_bluetooth_serial.broadcast.IBroadcastReceiver;
import com.iljak.android_bluetooth_serial.model.BtDeviceDataModel;

import java.util.ArrayList;

public class BaseBTFragment extends Fragment implements IBroadcastReceiver {

    protected BtDeviceListAdapter btDeviceListAdapter;
    protected BluetoothAdapter bluetoothAdapter;
    protected BluetoothActivity bluetoothActivity;
    private CommonBroadcastReceiver btBroadcastReceiver;

    private boolean isActive = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        bluetoothActivity = (BluetoothActivity) getActivity();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btDeviceListAdapter = new BtDeviceListAdapter(bluetoothActivity.getApplicationContext(), new ArrayList<BtDeviceDataModel>());
        btBroadcastReceiver = new CommonBroadcastReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    public void onPause() {
        isActive = false;
        bluetoothActivity.unregisterReceiver(btBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        isActive = true;
        super.onResume();
        bluetoothActivity.registerReceiver(btBroadcastReceiver, btBroadcastReceiver.getIntentFilter());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onReceive(BroadcastReceiver receiver, Context context, Intent intent) {

    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        bluetoothActivity.unregisterReceiver(receiver);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return bluetoothActivity.registerReceiver(receiver, filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return bluetoothActivity.registerReceiver(receiver, filter, flags);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return bluetoothActivity.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        return bluetoothActivity.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags);
    }
}
