package com.hackto.kali;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectThread extends Thread {
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectedThread connectedThread;
    private Handler handler;

    public ConnectThread(BluetoothDevice device, UUID uuid, BluetoothAdapter bluetoothAdapter, Handler handler) {
        this.device = device;
        this.handler = handler;
        this.bluetoothAdapter = bluetoothAdapter;

        try {
            socket =  device.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (Exception e) {
            // TODO: Handle Exception
        }
    }

    public void run() {
        bluetoothAdapter.cancelDiscovery();

        try {
            socket.connect();
        } catch (IOException connectException) {
            try {
                socket.close();
            } catch (IOException closeException) {
                // TODO: Handle case
                return;
            }
        }

        connectedThread = new ConnectedThread(socket, handler);
        connectedThread.start();
    }

    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            // TODO: Handle case
        }
    }
}