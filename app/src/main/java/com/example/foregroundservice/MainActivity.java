package com.example.foregroundservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button mBtnStartBackground, mBtnStopBackground;
    Button mBtnStartForeground, mBtnStopForeground;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnStartBackground = findViewById(R.id.buttonStartBackground);
        mBtnStopBackground = findViewById(R.id.buttonStopBackground);
        mBtnStartForeground = findViewById(R.id.buttonStartForeground);
        mBtnStopForeground = findViewById(R.id.buttonStopForeground);

        mBtnStartBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyService.class);
                intent.putExtra("text", "Hello");
                startService(intent);
            }
        });

        mBtnStopBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(MainActivity.this, MyService.class));
            }
        });

        mBtnStartForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MyService.class);
                startService(intent);
                bindService(intent, connection, Context.BIND_AUTO_CREATE);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (isMyServiceRunning(MyService.class)){
            Intent intent = new Intent(MainActivity.this, MyService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyService.MyBound myBoundService = (MyService.MyBound) service;
            MyService myService = myBoundService.getService();
            myService.setOnListenDuration(new MyService.OnListenDuration() {
                @Override
                public void onCurrentDuration(long time) {
                    Log.d("BBB",time + "");
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };
}
