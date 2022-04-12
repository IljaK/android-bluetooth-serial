package com.iljak.android_bluetooth_serial.bluetooth;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;

public class BleMessageManager {

    private ArrayList<BleMessage> uartMessageBuffer = new ArrayList<BleMessage>();
    private boolean isTransfering = false;

    public BleMessageManager() {

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void addMessage(byte[] data, boolean respondSend) {
        uartMessageBuffer.add(new BleMessage(data, respondSend));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public byte[] getData() {
        if (uartMessageBuffer.size() == 0) {
            return null;
        }

        if (isTransfering) {
            return uartMessageBuffer.get(0).getActualPacket();
        }
        isTransfering = true;
        return uartMessageBuffer.get(0).getNextPacket();
    }

    public boolean getTransferring() {
        return isTransfering;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void transferSuccess() {
        isTransfering = false;
    }

    public void clear() {
        uartMessageBuffer.clear();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean isMessageTransfered() {
        if (uartMessageBuffer.size() == 0) {
            return true;
        }
        return uartMessageBuffer.get(0).isAllSent();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public byte[] getFullMessage() {
        if (uartMessageBuffer.size() == 0) {
            return null;
        }
        return uartMessageBuffer.get(0).getFullMessage();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean needRespond() {
        return uartMessageBuffer.get(0).isRespondSend();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean isValidTransfer(byte[] data) {
        byte[] current = getData();
        return Arrays.equals(data, current);
    }

    public void flushActual() {
        if (uartMessageBuffer.size() == 0) return;
        uartMessageBuffer.remove(0);
    }
}
