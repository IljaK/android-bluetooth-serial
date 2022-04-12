package com.iljak.android_bluetooth_serial.model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleItemDataModel {
    private LocalDateTime timeStamp;
    private String message;


    private boolean incoming;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ConsoleItemDataModel(String message, boolean incoming) {
        this.timeStamp = LocalDateTime.now();
        this.message = message;
        this.incoming = incoming;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isIncoming() {
        return incoming;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getFormattedTimeStamp() {
        return timeStamp.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss.SSS"));
    }
}
