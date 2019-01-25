package com.inc.bb.smartcampus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AuditoriumActivity extends AppCompatActivity {

    private static final String TAG = "auditorium activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auditorium);
    }



    public void onClick_0(View view) {

        //Action for auditorium 0
        Log.d(TAG, "This is on Click listener floor zero ");

        //Launches floor zero activity
        startActivity(new Intent(this,  AuditoriumFloorZero.class));
    }
}
