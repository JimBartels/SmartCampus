package com.inc.bb.smartcampus;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import javax.annotation.Nullable;

public class CampusCar extends Fragment
        implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    ListView list;
    ArrayAdapter adapter;
    boolean taxiResponseReceived=false;
    TextView responseMessage;

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

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createBroadcastReceiverMotionplanningPath();
        Button locButton = (Button) getView().findViewById(R.id.requestbutton);
        Button cancelbutton = (Button) getView().findViewById(R.id.cancelbutton);
        locButton.setHapticFeedbackEnabled(true);
        Drawable buttondrawable2 = ContextCompat.getDrawable(getActivity(), R.drawable.buttonshapebefore);
        responseMessage = getView().findViewById(R.id.responseMessage);

        locButton.setBackground(buttondrawable2);
        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeTimeOutTimer();
                responseMessage.setText("Request sent");
            }
        });
        cancelbutton.setBackground(buttondrawable2);
        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRequestTaxi();
                responseMessage.setText("Request cancelled");
            }
        });
    }

    private void makeTimeOutTimer() {
        new CountDownTimer(10000, 1000) {

            @Override
            public void onTick(long l) {
                requestTaxi();
            }

            public void onFinish() {
                if(!taxiResponseReceived){
                    responseMessage.setText("Request timed out");
                }
                else{
                    responseMessage.setText("Request received");
                    taxiResponseReceived = false;
                }
                cancelRequestTaxi();
            }
        }.start();
    }

    private void requestTaxi() {
        Intent intent = new Intent();
        intent.setAction("CampusCar.REQUEST_TAXI");
        getActivity().sendBroadcast(intent);
    }

    private void cancelRequestTaxi() {
        Intent intent = new Intent();
        intent.setAction("CancelTaxiRequest");
        getActivity().sendBroadcast(intent);
    }

    private void createBroadcastReceiverMotionplanningPath() {
        BroadcastReceiver broadcastReceiverTaxiReceived = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                taxiResponseReceived = true;
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("OneM2MBackwardCommunications.SEND_MP_PATH");
        getActivity().registerReceiver(
                broadcastReceiverTaxiReceived, intentFilter);

    }
}