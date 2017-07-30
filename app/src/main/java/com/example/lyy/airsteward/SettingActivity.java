package com.example.lyy.airsteward;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.suke.widget.SwitchButton;

public class SettingActivity extends AppCompatActivity implements SwitchButton.OnCheckedChangeListener {

    private static final String TAG = "SettingActivity";

    private boolean isOpen;

    private Toolbar mToolbar;

    private NotificationCompat.Builder notifyBuilder;
    private NotificationManager mNotificationManager;

    private SwitchButton toast_btn, door_btn1, door_btn2, door_btn3, fan_btn1, fan_btn2, light_btn1, light_btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        notifyBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        SharedPreferences pref = getSharedPreferences("isOpen", MODE_PRIVATE);

        isOpen = pref.getBoolean("isNotificationOpen", false);//第二个参数为默认值

        init();
        initToolbar();

        Log.d(TAG, "onCreate: " + isOpen);

        if (isOpen) {
            toast_btn.setChecked(true);
        } else {
            toast_btn.setChecked(false);
        }

    }

    // 添加常驻通知
    private void setNotification() {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contextIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        notifyBuilder.setContentTitle("室内空气状况:");
        notifyBuilder.setContentText("优");
        notifyBuilder.setSmallIcon(R.drawable.icon);
        notifyBuilder.setOngoing(true);
        notifyBuilder.setContentIntent(contextIntent);

        mNotificationManager.notify(1, notifyBuilder.build());

        isOpen = true;
    }

    // 取消通知
    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1);

        isOpen = false;
    }

    private void init() {
        toast_btn = (SwitchButton) findViewById(R.id.toast_btn);
        door_btn1 = (SwitchButton) findViewById(R.id.door_btn_1);
        door_btn2 = (SwitchButton) findViewById(R.id.door_btn_2);
        door_btn3 = (SwitchButton) findViewById(R.id.door_btn_3);
        fan_btn1 = (SwitchButton) findViewById(R.id.fan_btn_1);
        fan_btn2 = (SwitchButton) findViewById(R.id.fan_btn_2);
        light_btn1 = (SwitchButton) findViewById(R.id.light_btn_1);
        light_btn2 = (SwitchButton) findViewById(R.id.light_btn_2);

        toast_btn.setOnCheckedChangeListener(this);
    }

    private void initToolbar() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onCheckedChanged(SwitchButton switchButton, boolean b) {
        switch (switchButton.getId()) {
            case R.id.toast_btn:
                if (b) {
                    setNotification();
                } else {
                    cancelNotification();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " + isOpen);
        if (isOpen) {
            toast_btn.setChecked(true);
        } else {
            toast_btn.setChecked(false);
        }
    }
}
