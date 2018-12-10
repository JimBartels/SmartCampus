package com.inc.bb.smartcampus;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Timer;

import javax.annotation.Nullable;

public class CampusCar extends Fragment
        implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    ListView list;
    ArrayAdapter adapter;


    /*(@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Use the current time as the default values for the picker

        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));




    }*/

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        Log.d("TimePicked", Integer.toString(hourOfDay));
        Log.d("TimePicked", Integer.toString(minute));

    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
    }

    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button locButton = (Button) getView().findViewById(R.id.carbutton);
        Drawable buttondrawable2 = ContextCompat.getDrawable(getActivity(),R.drawable.buttonshapebefore);
        locButton.setBackground(buttondrawable2);
        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((GpsActivity) getActivity()).CallCar();
            }
        });
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_campus_car, container, false);

        //TODO: Get Firebase Data


        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button locButton = (Button) getView().findViewById(R.id.requestbutton);
        Button cancelbutton = (Button) getView().findViewById(R.id.cancelbutton);
        locButton.setHapticFeedbackEnabled(true);
        Drawable buttondrawable2 = ContextCompat.getDrawable(getActivity(),R.drawable.buttonshapebefore);
        final TextView responseMessage = getView().findViewById(R.id.responseMessage);

        locButton.setBackground(buttondrawable2);
        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((GpsActivity) getActivity()).CallCar();
                makeTimeOutTimer2();
                makeTimeOutTimer();
                responseMessage.setText("Request sent");
            }
        });
        cancelbutton.setBackground(buttondrawable2);
        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((GpsActivity) getActivity()).cancelRequestTaxi();
                responseMessage.setText("Request cancelled");
            }
        });
    }

    private void makeTimeOutTimer2() {
        new CountDownTimer(5000, 1000) {

            @Override
            public void onTick(long l) {
                ((GpsActivity) getActivity()).CallCar();
            }

            public void onFinish() {
                ((GpsActivity) getActivity()).cancelRequestTaxi();
                ((GpsActivity) getActivity()).cancelRequestTaxi();
                ((GpsActivity) getActivity()).cancelRequestTaxi();
            }
        }.start();
    }

    private void makeTimeOutTimer() {
        new CountDownTimer(6000, 1000) {

            @Override
            public void onTick(long l) {

            }

            public void onFinish() {
                ((GpsActivity) getActivity()).cancelRequestTaxi();
            }
        }.start();
    }
}