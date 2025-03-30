package com.example.lab8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private EditText randomCharacterEditText;
    private BroadcastReceiver broadcastReceiver;
    private Intent backgroundServiceIntent;
    private Intent foregroundServiceIntent;
    private boolean isReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        randomCharacterEditText = findViewById(R.id.editText_randomCharacter);

        try {
            backgroundServiceIntent = new Intent(this, Class.forName("com.example.lab8.services.RandomCharacterService"));
            foregroundServiceIntent = new Intent(this, Class.forName("com.example.lab8.services.MusicService"));
        } catch (ClassNotFoundException e) {
            Log.e("MainActivity", "Service classes not found", e);
            return;
        }

        Button btnStartBackground = findViewById(R.id.btn_start_background);
        Button btnStopBackground = findViewById(R.id.btn_stop_background);
        Button btnStartForeground = findViewById(R.id.btn_start_foreground);
        Button btnStopForeground = findViewById(R.id.btn_stop_foreground);

        View.OnClickListener clickListener = this::onClick;
        btnStartBackground.setOnClickListener(clickListener);
        btnStopBackground.setOnClickListener(clickListener);
        btnStartForeground.setOnClickListener(clickListener);
        btnStopForeground.setOnClickListener(clickListener);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                char data = intent.getCharExtra("randomCharacter", '?');
                randomCharacterEditText.setText(String.valueOf(data));
                Log.d("MainActivity", "Received character: " + data);
            }
        };
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_start_background) {
            startService(backgroundServiceIntent);
        } else if (id == R.id.btn_stop_background) {
            stopService(backgroundServiceIntent);
            randomCharacterEditText.getText().clear();
        } else if (id == R.id.btn_start_foreground) {
            ContextCompat.startForegroundService(this, foregroundServiceIntent);
        } else if (id == R.id.btn_stop_foreground) {
            stopService(foregroundServiceIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter("my.custom.action.tag.lab8");
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(broadcastReceiver, filter, RECEIVER_NOT_EXPORTED);
                } else {
                    registerReceiver(broadcastReceiver, filter);
                }
                isReceiverRegistered = true;
                Log.d("MainActivity", "Receiver registered successfully");
            } catch (Exception e) {
                Log.e("MainActivity", "Error registering receiver", e);
            }
        } else {
            Log.w("MainActivity", "Receiver already registered");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(broadcastReceiver);
                isReceiverRegistered = false;
                Log.d("MainActivity", "Receiver unregistered successfully");
            } catch (IllegalArgumentException e) {
                Log.w("MainActivity", "Receiver not registered or already unregistered", e);
            }
        }
    }
}
