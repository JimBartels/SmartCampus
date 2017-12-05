package com.inc.bb.smartcampus;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

/**
 * Created by s163310 on 12-11-2017.
 */

public class MyTextWatcher implements TextWatcher {

    EditText editText, anotherEditText;
    View myView;
    boolean textwatcherCheck;

    // Your constructor
    public MyTextWatcher(EditText editText, EditText anotherEditText, View myView) {
        this.editText = editText;
        this.myView = myView;
        this.anotherEditText = anotherEditText;
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        textwatcherCheck = checkIfBothChanged();

        }

    private boolean checkIfBothChanged() {
        if(editText.getText().toString().length()>0){
            if(anotherEditText.getText().toString().length()>0){
                return true;
            }}
        return false;}


    @Override
    public void afterTextChanged(Editable s){}
}