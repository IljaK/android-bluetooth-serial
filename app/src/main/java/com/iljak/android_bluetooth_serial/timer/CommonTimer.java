package com.iljak.android_bluetooth_serial.timer;

import android.os.SystemClock;

public class CommonTimer implements ICountDownTimer {

    private final ITimerCallback mTimerCallback;
    private CommonTimerTask countDownTimer;

    private long startTS = 0;
    private long iterationStartTS = 0;

    private long totalDuration = 0;
    private long periodDuration = 0;

    public CommonTimer(ITimerCallback callback)
    {
        mTimerCallback = callback;
    }

    /*
     startDelay – delay in milliseconds before task is to be executed.
     repeatDelay – time in milliseconds between successive task executions.
     repeatCount - iterations amount
      */
    public void schedule(long startDelay, long repeatDelay, long repeatCount)
    {
        if (startDelay == 0 && repeatDelay == 0 && repeatCount == 0) {
            return;
        }
        if (startDelay < 0 || repeatDelay < 0 || repeatCount < 0) {
            return;
        }

        periodDuration = repeatDelay;
        totalDuration = startDelay + repeatDelay * repeatCount;
        if (startDelay > 0 && repeatCount == 0) {
            totalDuration = 0;
        }

        stop();
        startTS = SystemClock.elapsedRealtime();
        iterationStartTS = SystemClock.elapsedRealtime();
        if (startDelay > 0) {
            countDownTimer = new CommonTimerTask(this, startDelay);
            countDownTimer.start();
        } else {
            startPeriodic();
        }

    }

    /*
    startDelay – delay in milliseconds before task is to be executed.
    repeatDelay – time in milliseconds between successive task executions.
     */
    public void schedule(long startDelay, long repeatDelay) {
        schedule(startDelay, repeatDelay, 0);
    }

    /*
    repeatDelay – period in milliseconds between task executions.
     */
    public void schedule(long startDelay) {
        schedule(startDelay, 0, 0);
    }

    public void stop() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = null;
    }

    public long elapsedTotal() {
        if (isCompleted()) return 0;
        return SystemClock.elapsedRealtime() - startTS;
    }

    public long elapsedIteration() {
        if (isCompleted()) return 0;
        return SystemClock.elapsedRealtime() - iterationStartTS;
    }

    public float progressTotal() {
        if (totalDuration == 0) return 0;
        return Math.min((float)((double)elapsedTotal() / (double)totalDuration), 1.0f);
    }

    public boolean isCompleted() {
        return countDownTimer == null;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (countDownTimer.isPeriodic()) {
            iterationStartTS = SystemClock.elapsedRealtime();
            mTimerCallback.onTimerInterval(this);
        }
    }

    @Override
    public void onFinish() {
        if (countDownTimer.isPeriodic() || periodDuration == 0) {
            stop();
            mTimerCallback.onTimerComplete(this);
        } else {
            startPeriodic();
        }
    }

    private void startPeriodic() {
        if (periodDuration == 0) {
            return;
        }
        long duration = totalDuration;
        if (duration == 0) {
            duration = Long.MAX_VALUE;
        }

        stop();
        countDownTimer = new CommonTimerTask(this, duration, periodDuration);
        countDownTimer.start();
    }
}
