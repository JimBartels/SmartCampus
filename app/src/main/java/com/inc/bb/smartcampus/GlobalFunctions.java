/*
 * Created by Jim Bartels, lead developer and co-owner of Beams on 9/27/18 2:45 PM
 * Copyright (c) 2018. All rights reserved.
 *
 * Last modified on 9/27/18 2:45 PM
 *
 */

package com.inc.bb.smartcampus;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

public class GlobalFunctions extends AppCompatActivity {
    Context mContext;

    // constructor
    public GlobalFunctions(Context context) {
        this.mContext = context;
    }

    public int dpToPixels(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}