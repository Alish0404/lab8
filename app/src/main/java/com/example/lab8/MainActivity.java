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
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private EditText randomCharacterEditText;
    private ImageView musicIcon;
    private TextView musicStatus;
    private BroadcastReceiver broadcastReceiver;
    private Intent backgroundServiceIntent;
    private Intent foregroundServiceIntent;
    private boolean isReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        randomCharacterEditText = findViewById(R.id.editText_randomCharacter);
        musicIcon = findViewById(R.id.music_icon);
        musicStatus = findViewById(R.id.music_status);

        // Initialize service intents
        try {
            backgroundServiceIntent = new Intent(this, Class.forName("com.example.lab8.services.RandomCharacterService"));
            foregroundServiceIntent = new Intent(this, Class.forName("com.example.lab8.services.MusicService"));
        } catch (ClassNotFoundException e) {
            Log.e("MainActivity", "Service classes not found", e);
            return;
        }

        // Set up buttons
        Button btnStartBackground = findViewById(R.id.btn_start_background);
        Button btnStopBackground = findViewById(R.id.btn_stop_background);
        Button btnStartForeground = findViewById(R.id.btn_start_foreground);
        Button btnStopForeground = findViewById(R.id.btn_stop_foreground);

        View.OnClickListener clickListener = this::onClick;
        btnStartBackground.setOnClickListener(clickListener);
        btnStopBackground.setOnClickListener(clickListener);
        btnStartForeground.setOnClickListener(clickListener);
        btnStopForeground.setOnClickListener(clickListener);

        // Define BroadcastReceiver
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("my.custom.action.tag.lab8".equals(action)) {
                    char data = intent.getCharExtra("randomCharacter", '?');
                    randomCharacterEditText.setText(String.valueOf(data));
                    Log.d("MainActivity", "Received character: " + data);
                } else if ("com.example.lab8.MUSIC_STATE".equals(action)) {
                    String state = intent.getStringExtra("state");
                    if (state != null) {
                        switch (state) {
                            case "playing":
                                musicIcon.setVisibility(View.VISIBLE);
                                musicStatus.setText("Music Playing");
                                break;
                            case "paused":
                                musicIcon.setVisibility(View.VISIBLE);
                                musicStatus.setText("Music Paused");
                                break;
                            case "stopped":
                                musicIcon.setVisibility(View.GONE);
                                musicStatus.setText("Music Stopped");
                                break;
                        }
                        Log.d("MainActivity", "Music state: " + state);
                    }
                }
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
            IntentFilter filter = new IntentFilter();
            filter.addAction("my.custom.action.tag.lab8"); // For RandomCharacterService
            filter.addAction("com.example.lab8.MUSIC_STATE"); // For MusicService
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
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