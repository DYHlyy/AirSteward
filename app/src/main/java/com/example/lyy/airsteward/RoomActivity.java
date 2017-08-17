package com.example.lyy.airsteward;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyy.airsteward.card.CardItem;
import com.example.lyy.airsteward.card.CardPagerAdapter;
import com.example.lyy.airsteward.card.ShadowTransformer;
import com.githang.statusbar.StatusBarCompat;
import com.yalantis.phoenix.PullToRefreshView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rorbin.q.radarview.RadarData;
import rorbin.q.radarview.RadarView;

public class RoomActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "RoomActivity";

    private TextView textView;

    private ViewPager mViewPager;

    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;

    private PullToRefreshView mPullToRefreshView;
    private RadarView mRadarView;
    private List<Float> values = new ArrayList<>();
    private List<String> items = new ArrayList<>();
    private List<String> dataInfo = new ArrayList<>();

    private Toolbar mToolbar;
    private TextView mToolBarTextView;

    private String PM, Temperature, Humidity, CO2, CH2O, Gas;

    private float PM_degree, Temp_degree, Hum_degree, CO2_degree, CH2O_degree;

    private int red = Color.rgb(255, 0, 0);
    private int green = Color.rgb(0, 128, 0);

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private NotificationCompat.Builder notifyBuilder;
    private NotificationManager mNotificationManager;

    private boolean isNotificationOpen;

    RadarData data = new RadarData(values);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        int color = Color.rgb(20, 157, 163);
        StatusBarCompat.setStatusBarColor(this, color, true);

        sharedPreferences = getSharedPreferences("isOpen", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        isNotificationOpen = sharedPreferences.getBoolean("isNotificationOpen", false);

        init();
        choose();
        initToolbar();

        if (isNotificationOpen) {
            setNotification();
            editor.putBoolean("isNotificationOpen", true);
            editor.apply();
        } else {
            cancelNotification();
            editor.putBoolean("isNotificationOpen", false);
            editor.apply();
        }
//        if (isNetworkAvailable()) {
//            //Toast.makeText(this, "联网成功", Toast.LENGTH_SHORT).show();
//        } else {
//            textView.setText("----");
//            //Toast.makeText(this, "联网失败", Toast.LENGTH_SHORT).show();
//        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        mCardShadowTransformer.enableScaling(b);
    }

    private void init() {
        notifyBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        sharedPreferences = getSharedPreferences("values", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        final Intent intent = getIntent();

        mRadarView = (RadarView) findViewById(R.id.radarView);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        textView = (TextView) findViewById(R.id.eTV);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolBarTextView = (TextView) findViewById(R.id.text_view_toolbar_title);

        mRadarView.setEmptyHint("没有数据，下拉刷新");

        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isNetworkAvailable()) {
                            radarViewData(PM, CH2O, CO2, Temperature, Humidity);
                            sendRequestWithOkHttp(intent.getStringExtra("room_id"));
                            if (isNotificationOpen) {
                                setNotification();
                            }
                        } else {
                            Toasty.warning(RoomActivity.this, "请检查网络连接").show();
                        }
                        mPullToRefreshView.setRefreshing(false);
                    }
                }, 1000);
            }
        });
    }

    private void setNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contextIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String PM = sharedPreferences.getString("PM", "");
        String Quailty = sharedPreferences.getString("Quality", "");

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

    private boolean isNetworkAvailable() {

        //得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //去进行判断网络是否连接

        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }


    private void radarViewData(String PM, String CH2O, String CO2, String Temperature, String Humidity) {
        dataInfo.clear();
        items.clear();
        values.clear();
        Collections.addAll(dataInfo, PM, CH2O, CO2, Temperature, Humidity);
        Collections.addAll(items, "PM2.5", "CH2O", "CO2", "Temperature", "Humidity");
        Collections.addAll(values, PM_degree, CH2O_degree, CO2_degree, Temp_degree, Hum_degree);
        data.setValueText(dataInfo);
        data.setValueTextEnable(true);
        data.setValueTextSize(10);
        mRadarView.addData(data);
        mRadarView.setVertexText(items);
    }

    private void choose() {
        Intent intent = getIntent();
        switch (intent.getStringExtra("room_id")) {
            case "1":
                mToolBarTextView.setText("房间1");
                sendRequestWithOkHttp("1");
                break;
            case "2":
                mToolBarTextView.setText("房间2");
                sendRequestWithOkHttp("2");
                break;
            case "3":
                mToolBarTextView.setText("房间3");
                sendRequestWithOkHttp("3");
                break;

        }
    }

    private void sendRequestWithOkHttp(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("id", id)
                            .add("key", "BChiSsTqV9jHnnE7")
                            .build();
                    Request request = new Request.Builder()
                            .url("http://123.207.182.24/sam/api/app/sam_getsit.php")
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    showResponse(responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showResponse(final String jsonData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //在这里进行UI操作
                try {
                    JSONObject object = new JSONObject(jsonData);
                    PM = object.getString("pm2.5");
                    Temperature = object.getString("temperature");
                    Humidity = object.getString("humidity");
                    CO2 = object.getString("co2");
                    CH2O = object.getString("ch2o");
                    Gas = object.getString("landfillGas");

                    editor.putString("Temperature", Temperature);
                    editor.putString("CO2", CO2);
                    editor.putString("PM", PM);
                    editor.putString("Gas", Gas);
                    editor.putString("Quality", "");
                    editor.apply();

                    Temp_degree = Float.parseFloat(Temperature);
                    Hum_degree = Float.parseFloat(Humidity);
                    PM_degree = Float.parseFloat(PM) / 2;
                    CH2O_degree = Float.parseFloat(CH2O) * 500;
                    CO2_degree = Float.parseFloat(CO2) / 25;


                    radarViewData(PM, CH2O, CO2, Temperature, Humidity);

                    mCardAdapter = new CardPagerAdapter();
                    mCardAdapter.addCardItem(new CardItem("Temperature", Temperature + "℃", (int) Temp_degree));
                    mCardAdapter.addCardItem(new CardItem("Humidity", Humidity + "%", (int) Hum_degree));
                    mCardAdapter.addCardItem(new CardItem("PM2.5", PM + "μg/m³", (int) PM_degree));
                    mCardAdapter.addCardItem(new CardItem("CH2O", CH2O + "mg/m³", (int) CH2O_degree));
                    mCardAdapter.addCardItem(new CardItem("CO2", CO2 + "ppm", (int) CO2_degree));

                    int Gas_degree;
                    String security;
                    if (Gas.equals("0")) {
                        Gas_degree = 0;
                        security = "安全";
                    } else {
                        Gas_degree = 100;
                        security = "危险";
                    }

                    mCardAdapter.addCardItem(new CardItem("Gas", security, Gas_degree));

                    mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);

                    mViewPager.setAdapter(mCardAdapter);
                    mViewPager.setPageTransformer(false, mCardShadowTransformer);
                    mViewPager.setOffscreenPageLimit(3);

                    if (CO2_degree > 60 || Temp_degree > 40 || Hum_degree > 60 || PM_degree > 60 || Gas_degree == 100) {
                        Toasty.error(RoomActivity.this, "警告！浓度已超标").show();
                        textView.setText("危险");
                        textView.setTextColor(red);
                        editor.putString("Quality", "差");
                        editor.apply();
                    } else {
                        textView.setText("安全");
                        textView.setTextColor(green);
                        editor.putString("Quality", "优");
                        editor.apply();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initToolbar() {

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mToolbar.setNavigationIcon(R.drawable.back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Toast.makeText(this, "You click Share Button", Toast.LENGTH_SHORT).show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
