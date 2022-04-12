package com.iljak.android_bluetooth_serial.bluetooth;

import android.bluetooth.le.ScanResult;

import java.util.List;

public interface IBTScanCallback {
    void onScanResult(int callbackType, ScanResult result);
    void onBatchScanResults(List<ScanResult> results);
    void onScanFailed(int errorCode);
}
