package com.inc.bb.smartcampus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MetaforumActivity extends AppCompatActivity {
    private static final String TAG = "Metaforum activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metaforum);
    }

    public void onClick_Meta_0(View view){
        Log.d(TAG, "This is on Click listener floor zero ");

        //startActivity(new Intent(this, MetaforumFloorZero.class));

    }
}
