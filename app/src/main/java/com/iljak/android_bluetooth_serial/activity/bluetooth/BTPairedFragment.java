
package com.iljak.android_bluetooth_serial.activity.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.iljak.android_bluetooth_serial.R;
import com.iljak.android_bluetooth_serial.activity.adapter.BtDeviceListAdapter;
import com.iljak.android_bluetooth_serial.broadcast.IBroadcastReceiver;
import com.iljak.android_bluetooth_serial.model.BtDeviceDataModel;

import java.util.Set;

public class BTPairedFragment extends BaseBTFragment implements AdapterView.OnItemClickListener, IBroadcastReceiver {

    private static final String LOG_TAG = BTPairedFragment.class.getSimpleName();
    
    private ListView devicelist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.tab_bluetooth, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate " );
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(LOG_TAG, "onViewCreated" );

        devicelist = (ListView)view.findViewById(R.id.deviceListView);
        devicelist.setAdapter(btDeviceListAdapter);
        devicelist.setOnItemClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume " );

        updatePairedDeviceList();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updatePairedDeviceList()
    {
        if (btDeviceListAdapter == null) return;

        btDeviceListAdapter.clear();

        if (bluetoothAdapter == null) return;

        if (!bluetoothAdapter.isEnabled()) return;

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                btDeviceListAdapter.add(new BtDeviceDataModel(device.getName(), device.getAddress(), device.getType())); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(bluetoothActivity.getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent == devicelist) {
            BtDeviceListAdapter valueDatapter = (BtDeviceListAdapter) devicelist.getAdapter();
            BtDeviceDataModel deviceData = valueDatapter.getDataModel(position);

            connectToDevice(deviceData);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void connectToDevice(BtDeviceDataModel model)
    {
        if (model == null) return;

        BluetoothActivity activity = (BluetoothActivity)getActivity();
        if (activity != null) {
            activity.connectToDevice(model);
        }
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
                    updatePairedDeviceList();
                    // The user bluetooth is already disabled.
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    // The user bluetooth is turning off yet, but it is not disabled yet.
                    break;
                case BluetoothAdapter.STATE_ON:
                    updatePairedDeviceList();
                    // The user bluetooth is already disabled.
                    break;
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated" );
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart" );
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause" );
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop" );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy" );
    }

}