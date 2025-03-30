package com.example.lab8.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.lab8.MainActivity;
import com.example.lab8.R;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private final String CHANNEL_ID = "music_channel";
    private final int NOTIFICATION_ID = 1;
    private final String TAG = "MusicService";
    private boolean isPaused = false;
    private Handler handler = new Handler();
    private Runnable progressUpdater;

    // Actions for notification buttons
    private static final String ACTION_PAUSE = "com.example.lab8.PAUSE";
    private static final String ACTION_RESUME = "com.example.lab8.RESUME";

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.sample);
        if (mediaPlayer == null) {
            Toast.makeText(this, "Failed to load music file", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.7f, 0.7f); // Set initial volume (0.0f to 1.0f)
        startProgressUpdater();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PAUSE:
                    pauseMusic();
                    return START_STICKY;
                case ACTION_RESUME:
                    resumeMusic();
                    return START_STICKY;
            }
        }

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            broadcastState("playing");
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Channel",
                    NotificationManager.IMPORTANCE_LOW); // Low importance to avoid sound
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        // Intent to return to MainActivity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Pause/Resume action
        Intent pauseIntent = new Intent(this, MusicService.class).setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent resumeIntent = new Intent(this, MusicService.class).setAction(ACTION_RESUME);
        PendingIntent resumePendingIntent = PendingIntent.getService(this, 0, resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build notification with actions and progress
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Service")
                .setContentText(isPaused ? "Paused" : "Playing music")
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        if (mediaPlayer != null) {
            int duration = mediaPlayer.getDuration();
            int currentPosition = mediaPlayer.getCurrentPosition();
            builder.setProgress(duration, currentPosition, false);
        }

        if (isPaused) {
            builder.addAction(R.drawable.ic_play, "Resume", resumePendingIntent);
        } else {
            builder.addAction(R.drawable.ic_pause, "Pause", pausePendingIntent);
        }

        return builder.build();
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
            updateNotification();
            broadcastState("paused");
            Toast.makeText(this, "Music Paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPaused = false;
            updateNotification();
            broadcastState("playing");
            Toast.makeText(this, "Music Resumed", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, buildNotification());
    }

    private void startProgressUpdater() {
        progressUpdater = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    updateNotification(); // Update progress bar
                }
                handler.postDelayed(this, 1000); // Update every second
            }
        };
        handler.post(progressUpdater);
    }

    private void broadcastState(String state) {
        Intent intent = new Intent("com.example.lab8.MUSIC_STATE");
        intent.putExtra("state", state);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(progressUpdater);
        broadcastState("stopped");
        Toast.makeText(this, "Music Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}