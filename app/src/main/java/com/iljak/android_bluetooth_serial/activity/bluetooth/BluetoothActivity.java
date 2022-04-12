package com.iljak.android_bluetooth_serial.activity.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.iljak.android_bluetooth_serial.MainApplication;
import com.iljak.android_bluetooth_serial.R;
import com.iljak.android_bluetooth_serial.activity.BaseActivity;
import com.iljak.android_bluetooth_serial.activity.ConsoleActivity;
import com.iljak.android_bluetooth_serial.bluetooth.BluetoothState;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BluetoothActivity extends BaseActivity {

    private static final String LOG_TAG = BluetoothActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION = 2;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TabLayoutMediator tabMediator;
    private BTFragmentAdapter btFragmentAdapter;

    private BluetoothAdapter bluetoothAdapter;

    public BluetoothActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_bluetooth);

        super.onCreate(savedInstanceState);

        viewPager = (ViewPager2) findViewById(R.id.pageView);

        btFragmentAdapter = new BTFragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(btFragmentAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        tabMediator = new TabLayoutMediator(tabLayout, viewPager,
            new TabLayoutMediator.TabConfigurationStrategy() {
                @Override
                public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    //tab.setText("Tab " + (position + 1));
                }
            });
        tabMediator.attach();

        tabLayout.getTabAt(0).setText(getString(R.string.tab_bt_paired_label));
        tabLayout.getTabAt(1).setText(getString(R.string.tab_bt_scan_label));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            //finish apk
            finish();
        }
        checkPermissions();
    }

    public String[] retrievePermissions() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            //throw new RuntimeException ("This should have never happened.", e);
        }
        return null;
    }

    private void checkPermissions() {
        String[] appPermissions = retrievePermissions();

        if (appPermissions == null) {
            onPermissionCheckFailed(true);
            return;
        }

        for (String appPermission : appPermissions) {
            if (ActivityCompat.checkSelfPermission(this, appPermission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, appPermissions, REQUEST_PERMISSION);
                return;
            }
        }
        onPermissionCheckSuccess();
    }

    protected void onPermissionCheckSuccess() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    protected void onPermissionCheckFailed(boolean isCritical) {
        // TODO:
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                //onActivityBluetooth(resultCode, data);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(LOG_TAG, "onStart");

        if (getBtService() != null) {
            if (getBtService().isConnecting()) {
                setScreenLocked(true);
            } else if (getBtService().isConnected()) {
                openConsoleActivity();
            } else {
                connectToCachedDevice();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    public void connectToCachedDevice() {
        MainApplication application = (MainApplication) getApplication();
        // TODO:
        //BtDeviceListAdapter valueDatapter = (BtDeviceListAdapter) devicelist.getAdapter();
        //BtDeviceDataModel cachedDevice = valueDatapter.getDataModel(application.getPreferences().getDeviceAddress());
        //connectToDevice(cachedDevice);
    }

    private void openConsoleActivity()
    {
        Log.d(LOG_TAG, "openConsoleActivity");
        Context context = getApplicationContext();
        // Make an intent to start next activity.
        Intent intent = new Intent(context, ConsoleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);

        Toast.makeText(getApplicationContext(), "Connected: " + getBtService().getBtDeviceData().getName(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void handleStateChanged(BluetoothState state) {
        Log.d(LOG_TAG, "Handle state: " + state.toString());

        switch (state) {
            case INITIALIZING:
            case CONNECTING:
                setScreenLocked(true);
                break;

            case CONNECTION_FAILED:
            case CONNECTION_LOST:
                setScreenLocked(false);
                break;
            case CONNECTION_ESTABLISHED:
                openConsoleActivity();
                break;
        }
    }
}
