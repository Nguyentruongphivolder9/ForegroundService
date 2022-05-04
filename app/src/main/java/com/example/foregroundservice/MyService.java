package com.example.foregroundservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private NotificationManager notificationManager;
    private Notification notification;
    private boolean isPlaying = false;
    private MediaPlayer mediaPlayer;

    private int PLAY_MUSIC_CODE = 0;
    private int PAUSE_MUSIC_CODE = 1;

    private final class  ServiceHandler extends Handler {
        private MediaPlayer mediaPlayer;

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0 : {
                    Bundle bundle = msg.getData();
                    int resourceMusic = bundle.getInt("mp3");
                    int id = bundle.getInt("id");
                    startMp3(resourceMusic);
                }
            }
        }

        private void startMp3(int resourceMusic) {
            if (mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = MediaPlayer.create(getApplicationContext(),resourceMusic);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("ServiceStarArguments", Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();

            serviceLooper = thread.getLooper();
            serviceHandler = new ServiceHandler(serviceLooper);
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            // Start foreground
        notification = makenotification("Music 1","Hoài Lâm",isPlaying);
        startForeground(1,notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
        if (isPlaying){
            Message msg = serviceHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt("mp3",R.raw.rat_buon_hoai_lam);
            bundle.putInt("id",startId);
            msg.what = 1;
            serviceHandler.sendMessage(msg);
        }
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public Notification makenotification(String title, String singer, boolean isPlaying) {
        Intent intentPlayMisic = new Intent(this, MyService.class);
        intentPlayMisic.putExtra("status",0);

        PendingIntent pendingIntentPlayMusic = PendingIntent.getService(
                this,
                PLAY_MUSIC_CODE,
                intentPlayMisic,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        Intent intentPauseMisic = new Intent(this, MyService.class);

        PendingIntent pendingIntentPauseMusic = PendingIntent.getService(
                this,
                PLAY_MUSIC_CODE,
                intentPlayMisic,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "CHANNLE_ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(singer)
                .addAction(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        isPlaying ? "Pause" : "Play",
                        isPlaying ? pendingIntentPauseMusic : pendingIntentPlayMusic
                );

        return notification.build();
    }
}
