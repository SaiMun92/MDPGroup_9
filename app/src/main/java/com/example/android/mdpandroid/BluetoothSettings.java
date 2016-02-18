package com.example.android.mdpandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by srishtilal on 08/02/16.
 */
public class BluetoothSettings extends ListActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //oncreate method is to save the data in case u log out and u want to log in back the app again.
        setContentView(R.layout.bluetooth_activity);
        Switch bluetoothSwitch = (Switch) findViewById(R.id.BluetoothSwitch);
        final   ArrayAdapter<String> mDeviceList
        = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1);

        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mBluetoothAdapter == null) {
                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                getApplicationContext());

                        // set title
                        alertDialogBuilder.setTitle(R.string.dialog_title);

                        // set dialog message
                        alertDialogBuilder
                                .setMessage(R.string.dialog_message)
                                .setCancelable(false)
                                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // if this button is clicked, close
                                        // current activity
                                        BluetoothSettings.this.finish();
                                    }
                                });


                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();
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
                            mDeviceList.add(device.getName() + "\n" + device.getAddress());
                            setListAdapter(mDeviceList);

                        }
                    }



                } else {
                    // The toggle is disabled
                }
            }
        });





    }


}

