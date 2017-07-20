package com.example.lyy.airsteward;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gjiazhe.multichoicescirclebutton.MultiChoicesCircleButton;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    private TextView voice_tv;
    private TextView degree_tv;

    private Toolbar mToolbar;
    private TextView mToolBarTextView;

    private static final int REQUEST_UI = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voice_tv = (TextView) findViewById(R.id.voice_tv);
        degree_tv = (TextView) findViewById(R.id.degree_tv);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolBarTextView = (TextView) findViewById(R.id.text_view_toolbar_title);
        mToolBarTextView.setText("空气管家");

        initToolbar();

        if (isNetworkAvailable()) {
            Toasty.success(MainActivity.this, "网络连接成功").show();
        } else {
            Toasty.warning(MainActivity.this, "请检查网络连接").show();
        }


        SharedPreferences pref = getSharedPreferences("degree_to_index", MODE_PRIVATE);
        String degree = pref.getString("degree", "");
        degree_tv.setText(degree);


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
                        start();
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
        mToolbar.setNavigationIcon(R.drawable.back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getSharedPreferences("degree_to_index", MODE_PRIVATE);
        String degree = pref.getString("degree", "");
        degree_tv.setText(degree);
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

    //用语音控制行为的方法
    private void action(String action) {
        switch (action) {
            case "打开窗户":
                //openWindow()
                Toast.makeText(MainActivity.this, "你说了打开窗户命令", Toast.LENGTH_SHORT).show();
                break;
            case "查看天气":
                Intent intent = new Intent(MainActivity.this, WeatherIndexActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ArrayList<String> results = data.getExtras().getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String str = results + "";
            String res = str.substring(str.indexOf("[") + 1, str.indexOf("]"));
            voice_tv.setText(res + "\n");
            // data.get... TODO 识别结果包含的信息见本文档的“结果解析”一节
            action(res);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting, menu);
        return true;
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
}
