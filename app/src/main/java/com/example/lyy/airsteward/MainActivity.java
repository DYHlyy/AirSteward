package com.example.lyy.airsteward;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //private TextView ASwitch;
    //private SwitchButton switchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=new Intent(MainActivity.this,WeatherIndexActivity.class);
        startActivity(intent);


        CardView cardView1 = (CardView) findViewById(R.id.room1);
        CardView cardView2 = (CardView) findViewById(R.id.room2);
        CardView cardView3 = (CardView) findViewById(R.id.room3);

        cardView1.setOnClickListener(this);
        cardView2.setOnClickListener(this);
        cardView3.setOnClickListener(this);

        if (isNetworkAvailable()) {
            Toasty.success(MainActivity.this, "网络连接成功").show();
        } else {
            Toasty.warning(MainActivity.this, "请检查网络连接").show();
        }
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.room1:
                Intent intent1 = new Intent(MainActivity.this, RoomActivity.class);
                intent1.putExtra("room_id", "1");
                startActivity(intent1);

                break;
            case R.id.room2:
                Intent intent2 = new Intent(MainActivity.this, RoomActivity.class);
                intent2.putExtra("room_id", "2");
                startActivity(intent2);

                break;
            case R.id.room3:
                Intent intent3 = new Intent(MainActivity.this, RoomActivity.class);
                intent3.putExtra("room_id", "3");
                startActivity(intent3);

                break;
            default:
                break;
        }
    }
}

