package com.iljak.android_bluetooth_serial.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

public interface IBroadcastReceiver {
    void onReceive(BroadcastReceiver receiver, Context context, Intent intent);

    void unregisterReceiver(BroadcastReceiver receiver);

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter);

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags);

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler);

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags);
}
