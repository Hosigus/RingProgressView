package com.hosigus.ringprogressview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RingProgressView rpv = findViewById(R.id.rpv);
        Button btn = findViewById(R.id.btn);
        final EditText et = findViewById(R.id.et);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et.getText().toString().isEmpty()) {
                    rpv.setRingCount(Integer.parseInt(et.getText().toString()));
                }
                int count = rpv.getRingCount();
                List<Float> floatList = new ArrayList<>();
                List<String> strList = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    floatList.add((float) Math.random());
                    strList.add("说明");
                }
                rpv.setProgressList(floatList);
                rpv.setExplainList(strList);
                rpv.reDraw();
            }
        });
    }
}
