package com.iljak.android_bluetooth_serial.timer;

import android.os.CountDownTimer;

public class CommonTimerTask extends CountDownTimer {
    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */

    private final ICountDownTimer callback;
    private final boolean isPeriodic;

    public CommonTimerTask(ICountDownTimer callback, long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        this.callback = callback;
        isPeriodic = true;
    }

    public CommonTimerTask(ICountDownTimer callback, long millisInFuture) {
        super(millisInFuture, millisInFuture);
        this.callback = callback;
        isPeriodic = false;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (callback != null) {
            callback.onTick(millisUntilFinished);
        }
    }

    @Override
    public void onFinish() {
        if (callback != null) {
            callback.onFinish();
        }
    }

    public boolean isPeriodic() {
        return isPeriodic;
    }
}
