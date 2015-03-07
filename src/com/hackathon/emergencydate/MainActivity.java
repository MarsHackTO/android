package com.hackathon.emergencydate;

import java.io.IOException;
import java.io.*;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity implements OnClickListener {
	EditText phoneField;
	RadioButton radioText;
	RadioButton radioCall;
	SharedPreferences prefs;
	Button saveButton;
	BluetoothDevice mDevice;
	BluetoothAdapter mBluetoothAdapter;
	String bluetoothStatus = "Please pair with jewelry.";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v("AARDVARK", "setting sht");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		saveButton = (Button) findViewById(R.id.button1);
		saveButton.setOnClickListener(this);

		phoneField = (EditText) findViewById(R.id.editText1);
		radioText = (RadioButton) findViewById(R.id.radioText);
		radioCall = (RadioButton) findViewById(R.id.radioCall);

		prefs = getPreferences(MODE_PRIVATE);

		phoneField.setText(prefs.getString("phoneNumber", "(123) 456-7890"));
		String helpOption = prefs.getString("helpOption", "text");
		Log.v("AARDVARK", "first helpOption " + helpOption);
		if (helpOption.equals("text")) {
			Log.v("AARDVARK", "radioText.setChecked(true) ");
			radioText.setChecked(true);
		} else if (helpOption.equals("call")) {
			Log.v("AARDVARK", "radioCall.setChecked(true) ");
			radioCall.setChecked(true);
		}

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			bluetoothStatus = "Phone does not support Bluetooth.";
		} else if (!mBluetoothAdapter.isEnabled()) {
			bluetoothStatus = "Please enable Bluetooth.";
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 1);
		} else {
			//Get the Bluetooth module device
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					mDevice = device;
				}
			}
			bluetoothStatus = "Bluetooth connected.";
			
			Log.v("AARDVARK","Bluetook devicename = "+mDevice.getName());

		}
		
		TextView bluetoothStatusText = (TextView) findViewById(R.id.bluetoothStatus);
		bluetoothStatusText.setText(bluetoothStatus);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void saveSettings() {
		prefs = getPreferences(MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("phoneNumber", phoneField.getText().toString());

		if (radioText.isChecked()) {
			Log.v("AARDVARK", "radioText.isChecked()");
			editor.putString("helpOption", "text");
		} else if (radioCall.isChecked()) {
			Log.v("AARDVARK", "radioCall.isChecked()");
			editor.putString("helpOption", "call");
		}

		editor.commit();

		Log.v("AARDVARK", "ITW ORKED" + prefs.getString("phoneNumber", "fail"));

	}

	@Override
	public void onClick(View v) {
		Log.v("AARDVARK", v.getId() + "");
		if (v.getId() == R.id.button1) {
			saveSettings();
		}
	}
	
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
	        } catch (Exception e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	        mBluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        //manageConnectedSocket(mmSocket);
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	                        .sendToTarget();
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}

}
