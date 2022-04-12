package com.iljak.android_bluetooth_serial.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.textfield.TextInputEditText;
import com.iljak.android_bluetooth_serial.R;
import com.iljak.android_bluetooth_serial.activity.adapter.ConsoleItemDataAdapter;
import com.iljak.android_bluetooth_serial.model.ConsoleItemDataModel;
import com.iljak.android_bluetooth_serial.bluetooth.BluetoothClientService;
import com.iljak.android_bluetooth_serial.broadcast.CommonBroadcastReceiver;
import com.iljak.android_bluetooth_serial.broadcast.IBroadcastReceiver;

import java.util.ArrayList;
import java.util.Objects;

public class ConsoleActivity extends BaseActivity implements View.OnClickListener, IBroadcastReceiver {

    private static final String LOG_TAG = ConsoleActivity.class.getSimpleName();

    private ListView consoleMessageList;
    private TextInputEditText commandInputText;
    private Button sendButton;

    private CommonBroadcastReceiver btUartReceiver = new CommonBroadcastReceiver(this, btUartIntentFilter());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_console);
        super.onCreate(savedInstanceState);
        btUartReceiver.register();

        consoleMessageList = (ListView)findViewById(R.id.messageListView);
        ConsoleItemDataAdapter consoleItemAdapter = new ConsoleItemDataAdapter(this, new ArrayList<ConsoleItemDataModel>());
        consoleMessageList.setAdapter(consoleItemAdapter);

        commandInputText = (TextInputEditText)findViewById(R.id.inputText);
        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
    }



    @Override
    public void onResume() {
        super.onResume();
        /*
        boolean autoScroll = mainActivity.getSharedPreferences().getBoolean("setting_auto_scroll", false);
        if (autoScroll) {
            consoleMessageList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        } else {
            consoleMessageList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        }
        */
    }

    @Override
    public void onDestroy() {
        btUartReceiver.unregister();
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        if (v == sendButton) {

            String inputText = Objects.requireNonNull(commandInputText.getText()).toString();

            if (inputText.length() > 0) {
                sendStringData(inputText);
                commandInputText.setText("");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addConsoleMessage(String message, boolean incoming)
    {
        ConsoleItemDataModel messageModel = new ConsoleItemDataModel(message, incoming);
        ConsoleItemDataAdapter adapter = (ConsoleItemDataAdapter)consoleMessageList.getAdapter();
        adapter.add(messageModel);
        if (adapter.getCount() > 1000) {
            adapter.remove(adapter.getItem(0));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(BroadcastReceiver receiver, Context context, Intent intent) {
        //Log.d(LOG_TAG, "onReceive: " + intent.getAction());
        if (receiver == btUartReceiver) {
            String message = intent.getStringExtra(BluetoothClientService.BT_SERVICE_EXTRA_DATA);
            if (intent.getAction().equals(BluetoothClientService.BT_SERVICE_ACTION_UART_RX)) {
                addConsoleMessage(message, true);
            }
            else if (intent.getAction().equals(BluetoothClientService.BT_SERVICE_ACTION_UART_TX_SENT)) {
                addConsoleMessage(message, false);
            }
        }
    }

    private static IntentFilter btUartIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothClientService.BT_SERVICE_ACTION_UART_RX);
        intentFilter.addAction(BluetoothClientService.BT_SERVICE_ACTION_UART_TX_SENT);
        return intentFilter;
    }
}
