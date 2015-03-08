package com.hackto.kali;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private BluetoothSocket socket;
    private InputStream inStream;
    private OutputStream outStream;
    private Handler handler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;

        try {
            inStream= socket.getInputStream();
            outStream = socket.getOutputStream();
        } catch (IOException e) {
            // TODO: Handle Case
        }
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int begin = 0;
        int bytes = 0;

        while (true) {
            try {
                bytes += inStream.read(buffer, bytes, buffer.length - bytes);

                for (int i = begin; i < bytes; i++) {
                    if (buffer[i] == "#".getBytes()[0]) {
                        handler.obtainMessage(1, begin, i, buffer).sendToTarget();
                        begin = i + 1;
                        if (i == bytes - 1) {
                            bytes = 0;
                            begin = 0;
                        }
                    }
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            outStream.write(bytes);
        } catch (IOException e) {
            // TODO: Handle Case
        }
    }

    public void cancel() {
        try {
            inStream.close();
            outStream.close();
            socket.close();
        } catch (IOException e) {
            // TODO: Handle Case
        }
    }
}