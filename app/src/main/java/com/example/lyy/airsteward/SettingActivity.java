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
import android.widget.Toast;

import com.suke.widget.SwitchButton;

public class SettingActivity extends AppCompatActivity implements SwitchButton.OnCheckedChangeListener {

    private static final String TAG = "SettingActivity";

    private Toolbar mToolbar;

    private NotificationCompat.Builder notifyBuilder;
    private NotificationManager mNotificationManager;

    private SwitchButton toast_btn, door_btn1, window_btn1, window_btn2, fan_btn1, fan_btn2, light_btn1, light_btn2;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        init();
        initToolbar();
    }

    // 添加常驻通知
    private void setNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contextIntent = PendingIntent.getActivity(this, 0, intent, 0);

        SharedPreferences pref = getSharedPreferences("values", MODE_PRIVATE);
        String PM = pref.getString("PM", "");
        String Quailty = pref.getString("Quality", "");

        notifyBuilder.setContentTitle("室内空气状况: " + Quailty);
        notifyBuilder.setContentText("PM2.5浓度: " + PM + "μg/m³");
        notifyBuilder.setSmallIcon(R.drawable.icon);
        notifyBuilder.setOngoing(true);
        notifyBuilder.setContentIntent(contextIntent);
        mNotificationManager.notify(1, notifyBuilder.build());
    }

    // 取消通知
    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    private void init() {
        notifyBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        toast_btn = (SwitchButton) findViewById(R.id.toast_btn);
        door_btn1 = (SwitchButton) findViewById(R.id.door_btn_1);
        window_btn1 = (SwitchButton) findViewById(R.id.window_btn_1);
        window_btn2 = (SwitchButton) findViewById(R.id.window_btn_2);
        fan_btn1 = (SwitchButton) findViewById(R.id.fan_btn_1);
        fan_btn2 = (SwitchButton) findViewById(R.id.fan_btn_2);
        light_btn1 = (SwitchButton) findViewById(R.id.light_btn_1);
        light_btn2 = (SwitchButton) findViewById(R.id.light_btn_2);

        toast_btn.setOnCheckedChangeListener(this);

        sharedPreferences = getSharedPreferences("isOpen", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        boolean isNotificationOpen = sharedPreferences.getBoolean("isNotificationOpen", false);
        if (isNotificationOpen) {
            setNotification();
            toast_btn.setChecked(isNotificationOpen);
            editor.putBoolean("isNotificationOpen", true);
            editor.apply();
        } else {
            cancelNotification();
            toast_btn.setChecked(isNotificationOpen);
            editor.putBoolean("isNotificationOpen", false);
            editor.apply();
        }
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
                    editor.putBoolean("isNotificationOpen", true);
                    editor.apply();
                } else {
                    cancelNotification();
                    editor.putBoolean("isNotificationOpen", false);
                    editor.apply();
                }
                break;
            default:
                break;
        }
    }

}
