package com.inc.bb.smartcampus;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TimePicker;

import java.util.Calendar;

public class CampusCar extends DialogFragment
                                implements TimePickerDialog.OnTimeSetListener {
    ListView list;
    ArrayAdapter adapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
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



   /* @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_campus_car, container, false);

        //TODO: Get Firebase Data


        return view;
    }
*/
}