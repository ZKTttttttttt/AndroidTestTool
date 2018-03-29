package com.zkt.testapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView btnCrash, btnLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCrash = (TextView) findViewById(R.id.btncrash);
        btnLogs = (TextView) findViewById(R.id.btnlogs);
        btnCrash.setOnClickListener(this);
        btnLogs.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btncrash:
                int i = 10 / 0;
                break;
            case R.id.btnlogs:
                android.util.Log.v("TestLogs", "VVVVVVVVVVVV");
                android.util.Log.d("TestLogs", "DDDDDDDDDDDD");
                android.util.Log.i("TestLogs", "IIIIIIIIIIII");
                android.util.Log.w("TestLogs", "WWWWWWWWWWWW");
                android.util.Log.e("TestLogs", "EEEEEEEEEEEE");
                break;
        }
    }
}
