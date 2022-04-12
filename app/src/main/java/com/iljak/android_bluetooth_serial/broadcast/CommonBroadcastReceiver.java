package com.iljak.android_bluetooth_serial.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class CommonBroadcastReceiver extends BroadcastReceiver {

    private final IBroadcastReceiver receiver;
    private final IntentFilter intentFilter;
    private boolean isRegistered = false;

    public CommonBroadcastReceiver(IBroadcastReceiver receiver, IntentFilter intentFilter) {
        super();
        this.receiver = receiver;
        this.intentFilter = intentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (receiver != null) receiver.onReceive(this, context, intent);
    }

    public IntentFilter getIntentFilter() {
        return intentFilter;
    }

    public void register() {
        if (!isRegistered) {
            isRegistered = true;
            receiver.registerReceiver(this, intentFilter);
        }
    }

    public void unregister() {
        if (isRegistered) {
            isRegistered = false;
            receiver.unregisterReceiver(this);
        }
    }

    public boolean getIsRegistered() {
        return isRegistered;
    }
}
