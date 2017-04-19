package com.example.rafau.prj004.Events;

/**
 * Created by rafau on 10.01.16.
 */
public class BluetoothReceiveEvent {
    private String message;

    public BluetoothReceiveEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
