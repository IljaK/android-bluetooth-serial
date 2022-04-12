package com.iljak.android_bluetooth_serial.bluetooth;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BTScanCallback extends ScanCallback {

    private final IBTScanCallback listener;

    public BTScanCallback(IBTScanCallback listener) {
        super();
        this.listener = listener;
    }
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        if (listener != null) listener.onScanResult(callbackType, result);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
        if (listener != null) listener.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        if (listener != null) listener.onScanFailed(errorCode);
    }
}
