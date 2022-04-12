package com.iljak.android_bluetooth_serial.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;

public class StringUtil {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static byte[] getBytesWithZero(String string) {
        string += '\r';
        byte [] array = string.getBytes(StandardCharsets.US_ASCII);
        int zero = 0;
        array[array.length - 1] = (byte)(zero & 0xff);
        return array;
    }
}
