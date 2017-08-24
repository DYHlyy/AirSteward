package com.example.lyy.airsteward;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.suke.widget.SwitchButton;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "SettingActivity";

    private NotificationCompat.Builder notifyBuilder;
    private NotificationManager mNotificationManager;

    private SwitchButton toast_btn, door_btn1, window_btn1, window_btn2, fan_btn1, fan_btn2, light_btn1, light_btn2;

    private SharedPreferences.Editor editor;

    private FloatingActionButton floatingActionButton;
    private FABProgressCircle fabProgressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sendRequestToGetState();
        init();
        initToolbar();

        chooseSwitch();
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

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
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

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        fabProgressCircle = (FABProgressCircle) findViewById(R.id.fabProgressCircle);

        checkIsOpen();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabProgressCircle.show();
                sendRequestToGetState();
                checkIsOpen();
                fabProgressCircle.beginFinalAnimation();
            }
        });
    }

    private void checkIsOpen() {
        SharedPreferences sharedPreferences = getSharedPreferences("isOpen", MODE_PRIVATE);
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

        String isDoorOpen = sharedPreferences.getString("door", "");
        String isWin1Open = sharedPreferences.getString("win1", "");
        String isWin2Open = sharedPreferences.getString("win2", "");
        String isLight1Open = sharedPreferences.getString("light1", "");
        String isLight2Open = sharedPreferences.getString("light2", "");
        String isFan1Open = sharedPreferences.getString("fan1", "");
        String isFan2Open = sharedPreferences.getString("fan2", "");

        if (isDoorOpen.equals("1")) {
            door_btn1.setChecked(true);
        } else {
            door_btn1.setChecked(false);
        }
        if (isFan1Open.equals("1")) {
            fan_btn1.setChecked(true);
        } else {
            fan_btn1.setChecked(false);
        }
        if (isLight1Open.equals("1")) {
            light_btn1.setChecked(true);
        } else {
            light_btn1.setChecked(false);
        }
        if (isWin1Open.equals("1")) {
            window_btn1.setChecked(true);
        } else {
            window_btn1.setChecked(false);
        }
        if (isFan2Open.equals("1")) {
            fan_btn2.setChecked(true);
        } else {
            fan_btn2.setChecked(false);
        }
        if (isLight2Open.equals("1")) {
            light_btn2.setChecked(true);
        } else {
            light_btn2.setChecked(false);
        }
        if (isWin2Open.equals("1")) {
            window_btn2.setChecked(true);
        } else {
            window_btn2.setChecked(false);
        }
    }

    private void sendRequestToGetState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("id", "1")
                            .add("key", "BChiSsTqV9jHnnE7")
                            .build();
                    Request request = new Request.Builder()
                            .url("http://123.207.182.24/sam/api/app/sam_getnowsta.php")
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    saveState(responseData);
                    Log.d(TAG, "type: " + responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendRequestToControl(final String type, final String order) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("id", "1")
                            .add("key", "BChiSsTqV9jHnnE7")
                            .add("type", type)
                            .add("order", order)
                            .build();
                    Request request = new Request.Builder()
                            .url("http://123.207.182.24/sam/api/app/sam_order.php")
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d(TAG, "type: " + responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void chooseSwitch() {
        toast_btn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton switchButton, boolean b) {
                if (b) {
                    setNotification();
                    editor.putBoolean("isNotificationOpen", true);
                    editor.apply();
                } else {
                    cancelNotification();
                    editor.putBoolean("isNotificationOpen", false);
                    editor.apply();
                }
            }
        });

        door_btn1.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton switchButton, boolean b) {
                if (b) {
                    editor.putString("door", "1");
                    editor.apply();
                    sendRequestToControl("door", "1");
                    Toast.makeText(getApplicationContext(), "门已打开", Toast.LENGTH_SHORT).show();
                } else {
                    editor.putString("door", "0");
                    editor.apply();
                    sendRequestToControl("door", "0");
                    Toast.makeText(getApplicationContext(), "门已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

        window_btn1.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton switchButton, boolean b) {
                if (b) {
                    editor.putString("window1", "1");
                    editor.apply();
                    sendRequestToControl("window1", "1");
                    Toast.makeText(getApplicationContext(), "窗户已打开", Toast.LENGTH_SHORT).show();
                } else {
                    editor.putString("window1", "0");
                    editor.apply();
                    sendRequestToControl("window1", "0");
                    Toast.makeText(getApplicationContext(), "窗户已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fan_btn1.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton switchButton, boolean b) {
                if (b) {
                    editor.putString("fan1", "1");
                    editor.apply();
                    sendRequestToControl("fan1", "1");
                    Toast.makeText(getApplicationContext(), "风扇已打开", Toast.LENGTH_SHORT).show();
                } else {
                    editor.putString("fan1", "0");
                    editor.apply();
                    sendRequestToControl("fan1", "0");
                    Toast.makeText(getApplicationContext(), "风扇已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

        light_btn1.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton switchButton, boolean b) {
                if (b) {
                    editor.putString("light1", "1");
                    editor.apply();
                    sendRequestToControl("lant1", "1" +
                            "");
                    Toast.makeText(getApplicationContext(), "灯已打开", Toast.LENGTH_SHORT).show();
                } else {
                    editor.putString("light1", "0");
                    editor.apply();
                    sendRequestToControl("lant1", "0");
                    Toast.makeText(getApplicationContext(), "灯已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveState(String jsonData) {
        try {
            JSONObject object = new JSONObject(jsonData);
            String light1 = object.getString("lant1");
            String light2 = object.getString("lant2");
            String fan1 = object.getString("fan1");
            String fan2 = object.getString("fan2");
            String win1 = object.getString("window1");
            String win2 = object.getString("window2");
            String door = object.getString("door");

            editor.putString("light1", light1);
            editor.putString("light2", light2);
            editor.putString("fan1", fan1);
            editor.putString("fan2", fan2);
            editor.putString("win1", win1);
            editor.putString("win2", win2);
            editor.putString("door", door);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        sendRequestToGetState();
        checkIsOpen();
    }
}
