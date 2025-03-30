package com.example.lab8.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import java.util.Random;

public class RandomCharacterService extends Service {
    private Handler handler;
    private Random random;
    private final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private final String TAG = "RandomCharacterService";
    private Runnable characterGenerator;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        random = new Random();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Background Service Started", Toast.LENGTH_SHORT).show();

        characterGenerator = new Runnable() {
            @Override
            public void run() {
                char randomChar = alphabet[random.nextInt(alphabet.length)];
                Log.d(TAG, "Generated: " + randomChar);
                Intent broadcastIntent = new Intent("my.custom.action.tag.lab8");
                broadcastIntent.putExtra("randomCharacter", randomChar);
                sendBroadcast(broadcastIntent);
                handler.postDelayed(this, 1000); // Every 1 second
            }
        };
        handler.post(characterGenerator);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(characterGenerator);
        Toast.makeText(this, "Background Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}