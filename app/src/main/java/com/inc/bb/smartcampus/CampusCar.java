package com.inc.bb.smartcampus;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class CampusCar extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    int hourofday = 0;
    int minuteofday = 0;
    TextView TimeView;

    public TextView timetextview;
    public ListView placelistview;
    public ArrayAdapter<String> adapter;
    public String[] buildings = {"Vertigo", "Flux", "Metaforum", "Atlas", "Auditorium"};

    @Override
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


    }


    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        Log.d("TimePicked", Integer.toString(hourOfDay));
        Log.d("TimePicked", Integer.toString(minute));
        hourofday = hourOfDay;
        minuteofday = minute;
        Log.d("TimePickedChanged", Integer.toString(minuteofday));
        Log.d("TimePickedChanged", Integer.toString(hourofday));
        ((GpsActivity) getActivity()).changeDialogFragmentTimeTextView(hourOfDay, minute);

    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Let Fragment enter the screen
        View view = inflater.inflate(R.layout.fragment_campus_car, container, false);

        //Assign default values to TextViews
        View timetextview = view.findViewById(R.id.TimeView);
        //((TextView)timetextview).setText("CHOOSE TIME ...");
        View placetextview = view.findViewById(R.id.PlaceView);
        //((TextView)placetextview).setText("CHOOSE DESTINATION ...");

        //Identify the listview for places
        placelistview = (ListView) view.findViewById(R.id.placeListView);

        //Create and set adapter to listview
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, buildings);
        placelistview.setAdapter(adapter);


        // Set an item click listener for ListView
        placelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item text from ListView
                String selectedItem = (String) parent.getItemAtPosition(position);
                Log.d("selected item:", selectedItem);

                //Change indicated location textview
                ((GpsActivity) getActivity()).changeDialogFragmentPlaceTextView(selectedItem);

            }
        });

        //Button click listener in the fragment
        Button confirmationbutton = (Button) view.findViewById(R.id.ConfirmationButton);
        confirmationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ConfirmationButtonClick", "success");
                ((GpsActivity) getActivity()).DatetoMillis();

            }
        });

        //Button click listener for placebutton
        Button placebutton = (Button) view.findViewById(R.id.placeButton);
        placebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("PlaceButtonClick", "success");

            }

        });


        //TODO: Get Firebase Data


        return view;
    }




}