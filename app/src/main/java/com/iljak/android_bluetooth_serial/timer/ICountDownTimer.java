package com.iljak.android_bluetooth_serial.timer;

public interface ICountDownTimer {
    void onTick(long millisUntilFinished);
    void onFinish();
}
