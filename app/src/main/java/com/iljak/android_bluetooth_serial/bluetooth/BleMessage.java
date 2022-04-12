package com.iljak.android_bluetooth_serial.bluetooth;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleMessage {

    private static final int PACKAGE_EXTRA_DATA_SIZE = 1;
    private static final int PACKAGE_MAX_CONTENT_SIZE = BLEGattConnectionHandler.MAX_BLE_MESSAGE_SIZE - PACKAGE_EXTRA_DATA_SIZE;

    private final ByteBuffer messageBuffer;
    private byte[] pendingPacket;

    private boolean respondSend;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BleMessage(byte[] data, boolean respondSend) {
        messageBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        this.respondSend = respondSend;
    }

    public byte[] getNextPacket() {
        int size = messageBuffer.capacity() - messageBuffer.position();
        if (messageBuffer.position() == 0) {
            size = messageBuffer.capacity();
        } else if (size > PACKAGE_MAX_CONTENT_SIZE) {
            size = PACKAGE_MAX_CONTENT_SIZE;
        }

        byte[] packet = new byte[size + PACKAGE_EXTRA_DATA_SIZE];
        packet[0] = (byte) ((byte) size & 0xff);

        // Clamp again because now we need package size
        if (size > PACKAGE_MAX_CONTENT_SIZE) size = PACKAGE_MAX_CONTENT_SIZE;
        messageBuffer.get(packet, PACKAGE_EXTRA_DATA_SIZE, size);

        pendingPacket = packet;

        return packet;
    }

    public byte[] getActualPacket() {
        return pendingPacket;
    }

    /*
    private void insertCheckSum(byte[] packet) {
        packet[0] = 0;
        for (int i = 2; i < packet.length; i++) {
            packet[0] += packet[i];
        }
        packet[0] = (byte)(packet[0] & 0xff);
    }*/

    /*
    public static boolean IsValidCheckSum(byte[] data) {
        int checkSum = 0;

        for (int i = 2; i < data.length; i++) {
            checkSum += data[i];
        }
        checkSum = (byte)(checkSum & 0xff);

        return data[0] == (byte)checkSum;
    }
    */

    public boolean isAllSent() {
        int pos = messageBuffer.position();
        int capacity = messageBuffer.capacity();
        return (messageBuffer.position() >= messageBuffer.capacity());
    }

    public byte[] getFullMessage() {
        int position = messageBuffer.position();
        messageBuffer.position(0);

        byte[] data = new byte[messageBuffer.capacity()];
        messageBuffer.get(data, 0, data.length);
        messageBuffer.position(position);

        return data;

    }

    public boolean isRespondSend() {
        return respondSend;
    }
}
