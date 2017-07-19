package com.example.lyy.airsteward;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import at.markushi.ui.CircleButton;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button weather;
    private Button home;

    private CircleButton voice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isNetworkAvailable()) {
            Toasty.success(MainActivity.this, "网络连接成功").show();
        } else {
            Toasty.warning(MainActivity.this, "请检查网络连接").show();
        }

        weather = (Button) findViewById(R.id.weather);
        voice = (CircleButton) findViewById(R.id.voice);
        home = (Button) findViewById(R.id.home);

        weather.setOnClickListener(this);
        voice.setOnClickListener(this);
        home.setOnClickListener(this);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.weather:
                Intent intent1 = new Intent(MainActivity.this, WeatherIndexActivity.class);
                startActivity(intent1);
                break;
            case R.id.voice:
                Intent intent2 = new Intent(MainActivity.this, VoiceActivity.class);
                startActivity(intent2);
                break;
            case R.id.home:
                Intent intent3 = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent3);
                break;
        }
    }
}
