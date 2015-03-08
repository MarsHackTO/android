package com.hackto.kali;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private final String LOG_TAG = "AARDVARK";
    private final String SETTING_PHONE_NUMBER_KEY = "phoneNumber";
    private final String SETTING_HELP_OPTION_KEY = "helpOption";
    private final String SETTING_PHONE_NUMBER_DEFAULT = "(123) 456-7890";
    private final String SETTING_HELP_OPTION_DEFAULT = "text";

    public EditText phoneField;
    public RadioButton radioText;
    public RadioButton radioCall;
    public Button saveButton;

    public SharedPreferences savedSettings;
    public BluetoothAdapter blueToothAdapter;
    public List<BluetoothDevice> pairedDevices;
    public ConnectThread connectThread;
    public Handler handler;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedSettings = getPreferences(MODE_PRIVATE);
        saveButton = (Button) findViewById(R.id.button1);
        phoneField = (EditText) findViewById(R.id.editText1);
        radioText = (RadioButton) findViewById(R.id.radioText);
        radioCall = (RadioButton) findViewById(R.id.radioCall);

        saveButton.setOnClickListener(this);

        loadSettings();

//        receiver = buildBroadCastReceiver();
//        registerReceiver();

        handler = buildHandler();

        blueToothAdapter = BluetoothAdapter.getDefaultAdapter();
        initBluetooth();

        connectThread = new ConnectThread(getFirstDevice(), getFirstDeviceUUID(), blueToothAdapter, handler);
        startConnectThread();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button1) {
            saveSettings(view);
        }
    }

    public void saveSettings(View view) {
        savedSettings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = savedSettings.edit();

        editor.putString(SETTING_PHONE_NUMBER_KEY, phoneField.getText().toString());

        String helpOption = radioText.isChecked() ? "text" : "call";
        editor.putString(SETTING_HELP_OPTION_KEY, helpOption);

        editor.apply();
    }

    public void loadSettings() {
        loadHelpOption();

        String savedPhoneNumber = getPhoneNumber();
        loadPhoneNumber(savedPhoneNumber);
    }

    public void loadHelpOption() {
        String helpOption = getHelpOption();

        if (helpOption.equals("text")) {
            setTextOption();
        } else if (helpOption.equals("call")) {
            setCallOption();
        }
    }

    public String getHelpOption() {
        return savedSettings.getString(SETTING_HELP_OPTION_KEY, SETTING_HELP_OPTION_DEFAULT);
    }

    public void setTextOption() {
        radioText.setChecked(true);
    }

    public void setCallOption() {
        radioCall.setChecked(true);
    }

    public void loadPhoneNumber(String phoneNumber) {
        phoneField.setText(phoneNumber);
    }

    public String getPhoneNumber() {
        return savedSettings.getString(SETTING_PHONE_NUMBER_KEY, SETTING_PHONE_NUMBER_DEFAULT);
    }

    public void initBluetooth() {
        if (!hasBluetoothCapabilities()) {
            // TODO: Set status
        } else if (!isBluetoothEnabled()) {
            promptToEnableBluetooth();
        } else {
            updatePairedDevices();
        }
    }

    public boolean hasBluetoothCapabilities() {
        return blueToothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return blueToothAdapter.isEnabled();
    }

    public void promptToEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
    }

    public void updatePairedDevices() {
        pairedDevices = new ArrayList<BluetoothDevice>(blueToothAdapter.getBondedDevices());
    }

    public BluetoothDevice getFirstDevice() {
        return pairedDevices.get(0);
    }

    public UUID getFirstDeviceUUID() {
        return getFirstDevice().getUuids()[0].getUuid();
    }

    public void startConnectThread() {
        connectThread.start();
    }

    public Handler buildHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte[] writeBuf = (byte[]) msg.obj;
                int begin = (int) msg.arg1;
                int end = (int) msg.arg2;

                switch (msg.what) {
                    case 1:
                        String writeMessage = new String(writeBuf);
                        writeMessage = writeMessage.substring(begin, end);
                        break;
                }
            }
        };
    }

    public BroadcastReceiver buildBroadCastReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    pairedDevices.add(device);
                }
            }
        };
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }
}
