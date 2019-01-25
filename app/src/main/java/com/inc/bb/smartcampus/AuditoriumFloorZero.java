package com.inc.bb.smartcampus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class AuditoriumFloorZero extends AppCompatActivity {

    private static final String TAG = "auditorium 0 activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "This is floor zero ");
        setContentView(R.layout.activity_auditorium_zero);
    }
}