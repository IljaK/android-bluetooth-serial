package com.iljak.android_bluetooth_serial.service;

import android.app.Service;
import android.os.Binder;

public class CommonServiceBinder extends Binder {

    private final Service service;

    public CommonServiceBinder(Service service) {
        super();
        this.service = service;
    }

    public Service getService() {
        return service;
    }


}
