package com.example.lyy.airsteward;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        CardView cardView1 = (CardView) findViewById(R.id.room1);
        CardView cardView2 = (CardView) findViewById(R.id.room2);
        CardView cardView3 = (CardView) findViewById(R.id.room3);

        cardView1.setOnClickListener(this);
        cardView2.setOnClickListener(this);
        cardView3.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.room1:
                Intent intent1 = new Intent(HomeActivity.this, RoomActivity.class);
                intent1.putExtra("room_id", "1");
                startActivity(intent1);
                break;
            case R.id.room2:
                Intent intent2 = new Intent(HomeActivity.this, RoomActivity.class);
                intent2.putExtra("room_id", "2");
                startActivity(intent2);
                break;
            case R.id.room3:
                Intent intent3 = new Intent(HomeActivity.this, RoomActivity.class);
                intent3.putExtra("room_id", "3");
                startActivity(intent3);
                break;
            default:
                break;
        }
    }
}

