package com.example.lyy.airsteward;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gjiazhe.multichoicescirclebutton.MultiChoicesCircleButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView quality_tv;
    private TextView pm_out_tv;
    private TextView aqi_out_tv;
    private TextView co_in_tv;
    private TextView pm_in_tv;
    private TextView degree_in_tv;

    private Toolbar mToolbar;
    private TextView mToolBarTextView;

    private LinearLayout smallLabel;

    private NotificationCompat.Builder notifyBuilder;
    private NotificationManager mNotificationManager;

    private static final int REQUEST_UI = 1;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initToolbar();
        show_cache();
        init_chooseItem();

    }


    private void init() {
        quality_tv = (TextView) findViewById(R.id.quality_tv);
        aqi_out_tv = (TextView) findViewById(R.id.aqi_out_tv);
        pm_out_tv = (TextView) findViewById(R.id.pm_out_tv);
        co_in_tv = (TextView) findViewById(R.id.co2_in_tv);
        pm_in_tv = (TextView) findViewById(R.id.pm_in_tv);
        degree_in_tv = (TextView) findViewById(R.id.degree_in_tv);

        smallLabel = (LinearLayout) findViewById(R.id.smallLabel);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolBarTextView = (TextView) findViewById(R.id.text_view_toolbar_title);
        mToolBarTextView.setText("空气管家");

        notifyBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        sharedPreferences = getSharedPreferences("isOpen", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (isNetworkAvailable()) {
            Toasty.success(MainActivity.this, "网络连接成功").show();
        } else {
            Toasty.warning(MainActivity.this, "请检查网络连接").show();
        }

        boolean isNotificationOpen = sharedPreferences.getBoolean("isNotificationOpen", false);
        if (isNotificationOpen) {
            setNotification();
            editor.putBoolean("isNotificationOpen", true);
            editor.apply();
        } else {
            cancelNotification();
            editor.putBoolean("isNotificationOpen", false);
            editor.apply();
        }

        smallLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                intent.putExtra("room_id", "1");
                startActivity(intent);
            }
        });
    }

    //显示缓存数据
    private void show_cache() {
        SharedPreferences pref = getSharedPreferences("data_to_mainPage", MODE_PRIVATE);
        String AQI = pref.getString("AQI", "");
        String PM = pref.getString("PM2.5", "");

        if (AQI.equals("")) {
            aqi_out_tv.setText("----");
        } else {
            aqi_out_tv.setText(AQI);
        }
        if (PM.equals("")) {
            pm_out_tv.setText("----");
        } else {
            pm_out_tv.setText(PM);
        }

        SharedPreferences valuesPref = getSharedPreferences("values", MODE_PRIVATE);
        String quality = valuesPref.getString("Quality", "");
        String CO2_In = valuesPref.getString("CO2", "");
        String PM_In = valuesPref.getString("PM", "");
        String Degree = valuesPref.getString("Temperature", "");
        if (quality.equals("")) {
            quality_tv.setText("----");
        } else {
            quality_tv.setText(quality);
        }
        if (CO2_In.equals("")) {
            co_in_tv.setText("----");
        } else {
            co_in_tv.setText(AQI);
        }
        if (PM_In.equals("")) {
            pm_in_tv.setText("----");
        } else {
            pm_in_tv.setText(PM);
        }
        if (Degree.equals("")) {
            degree_in_tv.setText("----");
        } else {
            degree_in_tv.setText(Degree + "°");
        }

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

    private void isVoicePermitted() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            start();
        }
    }

    private void init_chooseItem() {
        MultiChoicesCircleButton.Item item1 = new MultiChoicesCircleButton.Item("Weather", getResources().getDrawable(R.drawable.weather), 30);

        MultiChoicesCircleButton.Item item2 = new MultiChoicesCircleButton.Item("Voice", getResources().getDrawable(R.drawable.voice_icon), 90);

        MultiChoicesCircleButton.Item item3 = new MultiChoicesCircleButton.Item("Home", getResources().getDrawable(R.drawable.home), 150);

        List<MultiChoicesCircleButton.Item> buttonItems = new ArrayList<>();
        buttonItems.add(item1);
        buttonItems.add(item2);
        buttonItems.add(item3);

        MultiChoicesCircleButton multiChoicesCircleButton = (MultiChoicesCircleButton) findViewById(R.id.multiChoicesCircleButton);
        multiChoicesCircleButton.setButtonItems(buttonItems);

        multiChoicesCircleButton.setOnSelectedItemListener(new MultiChoicesCircleButton.OnSelectedItemListener() {
            @Override
            public void onSelected(MultiChoicesCircleButton.Item item, int index) {
                // Do something
                switch (item.getText()) {
                    case "Weather":
                        Intent intent1 = new Intent(MainActivity.this, WeatherIndexActivity.class);
                        startActivity(intent1);
                        break;
                    case "Home":
                        Intent intent2 = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent2);
                        break;
                    case "Voice":
                        isVoicePermitted();
                        break;
                    default:
                        break;
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
        mToolbar.setNavigationIcon(R.drawable.hvac);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HVACActivity.class);
                startActivity(intent);
            }
        });
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


    private void start() {
        Intent recognizerIntent = new Intent("com.baidu.action.RECOGNIZE_SPEECH");
        recognizerIntent.putExtra("sample", 16000); // 离线仅支持16000采样率
        recognizerIntent.putExtra("language", "cmn-Hans-CN"); // 离线仅支持中文普通话
        recognizerIntent.putExtra("prop", 20000); // 输入
        // recognizerIntent.put("...", "...") TODO 为recognizerIntent设置参数，支持的参数见本文档的“识别参数”一节
        // 为了支持离线识别能力，请参考“离线语音识别参数设置”一节
        startActivityForResult(recognizerIntent, REQUEST_UI);
    }

    private void identify(String action) {
        if (action.contains("开")) {
            if (action.contains("门")) {
                voice_control_action("开门");
            } else if (action.contains("窗")) {
                voice_control_action("打开窗户");
            } else if (action.contains("灯")) {
                voice_control_action("开灯");
            } else if (action.contains("风扇")) {
                voice_control_action("打开风扇");
            }
        } else if (action.contains("关")) {
            if (action.contains("门")) {
                voice_control_action("关门");
            } else if (action.contains("窗")) {
                voice_control_action("关闭窗户");
            } else if (action.contains("灯")) {
                voice_control_action("关灯");
            } else if (action.contains("风扇")) {
                voice_control_action("关闭风扇");
            }
        } else {
            voice_control_action(action);
        }
    }

    //用语音控制行为的方法
    private void voice_control_action(final String action) {
        switch (action) {
            case "打开窗户":
                sendRequestToControl("window1", "1");
                Toast.makeText(MainActivity.this, "窗户已打开", Toast.LENGTH_SHORT).show();
                break;
            case "关闭窗户":
                sendRequestToControl("window1", "0");
                Toast.makeText(MainActivity.this, "窗户已关闭", Toast.LENGTH_SHORT).show();
                break;
            case "开门":
                sendRequestToControl("door", "1");
                Toast.makeText(MainActivity.this, "门已打开", Toast.LENGTH_SHORT).show();
                break;
            case "关门":
                sendRequestToControl("door", "0");
                Toast.makeText(MainActivity.this, "门已关闭", Toast.LENGTH_SHORT).show();
                break;
            case "打开风扇":
                sendRequestToControl("fan1", "1");
                Toast.makeText(MainActivity.this, "风扇已打开", Toast.LENGTH_SHORT).show();
                break;
            case "关闭风扇":
                sendRequestToControl("fan1", "0");
                Toast.makeText(MainActivity.this, "风扇已关闭", Toast.LENGTH_SHORT).show();
                break;
            case "开灯":
                sendRequestToControl("lant1", "1");
                Toast.makeText(MainActivity.this, "灯已打开", Toast.LENGTH_SHORT).show();
                break;
            case "关灯":
                sendRequestToControl("lant1", "0");
                Toast.makeText(MainActivity.this, "灯已关闭", Toast.LENGTH_SHORT).show();
                break;
            case "查看天气":
                Intent intent1 = new Intent(MainActivity.this, WeatherIndexActivity.class);
                startActivity(intent1);
                break;
            case "设置":
                Intent intent2 = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent2);
                break;
            case "打开空调":
                Intent intent3 = new Intent(MainActivity.this, HVACActivity.class);
                startActivity(intent3);
                break;
            default:
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("未匹配到 " + "“" + action + "”")
                        .setContentText("您需要使用度娘吗？")
                        .setCancelText("不，谢谢！")
                        .setConfirmText("百度一下！")
                        .showCancelButton(true)
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.cancel();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                String URL = "http://www.baidu.com/s?wd=";
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(URL + action));
                                startActivity(intent);
                                sweetAlertDialog.cancel();
                            }
                        })
                        .show();
                break;
        }
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
                    //Toast.makeText(getApplicationContext(), responseData, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "type: " + responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ArrayList<String> results = data.getExtras().getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String str = results + "";
            String res = str.substring(str.indexOf("[") + 1, str.indexOf("]"));
            // data.get... TODO 识别结果包含的信息见本文档的“结果解析”一节

            identify(res);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getSharedPreferences("data_to_mainPage", MODE_PRIVATE);
        String AQI = pref.getString("AQI", "");
        String PM = pref.getString("PM2.5", "");
        aqi_out_tv.setText(AQI);
        pm_out_tv.setText(PM);

        SharedPreferences valuesPref = getSharedPreferences("values", MODE_PRIVATE);
        String quality = valuesPref.getString("Quality", "");
        String CO2_In = valuesPref.getString("CO2", "");
        String PM_In = valuesPref.getString("PM", "");
        String Degree = valuesPref.getString("Temperature", "");
        if (quality.equals("")) {
            quality_tv.setText("----");
        } else {
            quality_tv.setText(quality);
        }
        if (CO2_In.equals("")) {
            co_in_tv.setText("----");
        } else {
            co_in_tv.setText(CO2_In);
        }
        if (PM_In.equals("")) {
            pm_in_tv.setText("----");
        } else {
            pm_in_tv.setText(PM_In);
        }
        if (Degree.equals("")) {
            degree_in_tv.setText("----");
        } else {
            degree_in_tv.setText(Degree + "°");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    start();
                } else {
                    Toasty.error(MainActivity.this, "你还没有获取权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }


}
