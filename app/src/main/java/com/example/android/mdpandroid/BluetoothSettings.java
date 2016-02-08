package com.example.android.mdpandroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.lang.reflect.Array;
import java.util.Set;

/**
 * Created by srishtilal on 08/02/16.
 */
public class BluetoothSettings extends Activity {

    private final static int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    String[] items;
    ArrayAdapter<String> mArrayAdapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //oncreate method is to save the data in case u log out and u want to log in back the app again.
        setContentView(R.layout.bluetooth_activity);


        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }


    }


}

