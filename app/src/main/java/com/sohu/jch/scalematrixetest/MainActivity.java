package com.sohu.jch.scalematrixetest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;

public class MainActivity extends AppCompatActivity {


    GestureView gestureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestureView = (GestureView) findViewById(R.id.gesture_view);
        gestureView.initMatrix(640, 480);
    }

}
