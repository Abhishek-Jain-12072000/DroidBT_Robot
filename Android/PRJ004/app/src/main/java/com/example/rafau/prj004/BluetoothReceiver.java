package com.example.rafau.prj004;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.rafau.prj004.Events.BluetoothEvent;
import com.example.rafau.prj004.Events.DeviceDisconnectEvent;

import de.greenrobot.event.EventBus;

public class BluetoothReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothReceiver";
    private static final int FAIL = -1;

    public BluetoothReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        if (extras == null) return;
        if (action == "android.bluetooth.adapter.action.STATE_CHANGED") {
            EventBus.getDefault().post(new BluetoothEvent(extras.getInt("android.bluetooth.adapter.extra.STATE", FAIL)));
        } else if(action == "android.bluetooth.device.action.ACL_DISCONNECTED"){
            EventBus.getDefault().post(new DeviceDisconnectEvent());
        }
    }
}
