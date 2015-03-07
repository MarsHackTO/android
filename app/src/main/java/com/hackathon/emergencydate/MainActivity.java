package com.hackathon.emergencydate;

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

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
		} else if (!mBluetoothAdapter.isEnabled()) {
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
			
			Log.v("AARDVARK","Bluetook devicename = "+mDevice.getName());

		}
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

}
