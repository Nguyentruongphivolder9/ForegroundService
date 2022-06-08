package com.example.foregroundservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
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
    private boolean isPlaying = true;
    private boolean isUnBind = false;

    private int RESUME_MUSIC_CODE = 0;
    private int PAUSE_MUSIC_CODE = 1;
    private OnListenDuration onListenDuration;

    private final class  ServiceHandler extends Handler {
        private MediaPlayer mediaPlayer;
        private int currentTime = 0;
        private Handler handler;

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case -1:
                    int resourceMusic = bundle.getInt("mp3");
                    startMp3(resourceMusic);
                    break;
                case 0:
                    resumeMp3();
                    break;
                case 1:
                    pauseMp3();
                    break;
            }
            if (!isUnBind){
                updateCurrenTime();
            }
        }

        private void disableUpdate() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()){
                handler.removeCallbacks(runnable);
            }
        }

        private void updateCurrenTime(){
            if (mediaPlayer != null){
                if (isPlaying) {
                    handler = new Handler();
                    handler.postDelayed(runnable,500);
                }
            }

        }
        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying() && mediaPlayer.getCurrentPosition() < mediaPlayer.getDuration()){
                    onListenDuration.onCurrentDuration(mediaPlayer.getCurrentPosition());
                }
                if (isPlaying){
                    handler.postDelayed(this,500);
                }
            }
        };

        private void pauseMp3() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                currentTime = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
                isPlaying = false;
                notificationManager.notify(1, makenotification("Music 1", "Hoài Lâm", false));
            }
        }

        private void resumeMp3() {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(currentTime);
                mediaPlayer.start();
                isPlaying = true;
                notificationManager.notify(1, makenotification("Music 1", "Hoài Lâm", true));

            }
        }

        private void startMp3(int resourceMusic) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = MediaPlayer.create(getApplicationContext(), resourceMusic);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    isPlaying = true;
                    mediaPlayer.start();
                }
            });
        }
    }

    public class MyBound extends Binder {

        MyService getService(){
            return  MyService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBound();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isUnBind = true;
        serviceHandler.disableUpdate();
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        isUnBind = false;
        serviceHandler.updateCurrenTime();
        super.onRebind(intent);
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
        startForeground(1, makenotification("Music 1","Hoài Lâm",isPlaying));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int requestCode = intent.getIntExtra("requestCode",-1);
        Message msg = serviceHandler.obtainMessage();
        Bundle bundle = new Bundle();

        switch (requestCode) {
            case -1:
                bundle.putInt("mp3", R.raw.rat_buon_hoai_lam);
                msg.what = -1;
                msg.setData(bundle);
                serviceHandler.sendMessage(msg);
                break;
            case 0 :
                msg.what = 0;
                serviceHandler.sendMessage(msg);
                break;
            case 1 :
                msg.what = 1;
                serviceHandler.sendMessage(msg);
                break;
        }
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public Notification makenotification(String title, String singer, boolean isPlaying) {
        Intent intentMusic = new Intent(this, MyService.class);
        intentMusic.putExtra("requestCode", isPlaying ? PAUSE_MUSIC_CODE : RESUME_MUSIC_CODE);

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                0,
                intentMusic,
                PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "CHANNLE_ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(singer);
        if (isPlaying) {
            notification.addAction(android.R.drawable.ic_media_pause, "Pause", pendingIntent);
        } else {
            notification.addAction(android.R.drawable.ic_media_play, "Play", pendingIntent);
        }

        return notification.build();
    }

    public void setOnListenDuration(OnListenDuration onListenDuration) {
        this.onListenDuration = onListenDuration;
    }

    interface OnListenDuration {
        void onCurrentDuration(long time);
    }

}
