package com.iljak.android_bluetooth_serial.timer;

public interface ITimerCallback {
    void onTimerComplete(CommonTimer timer);
    void onTimerInterval(CommonTimer timer);
}
