package com.inc.bb.smartcampus;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class CampusCar extends Fragment {
    ListView list;
    ArrayAdapter adapter;

    @Override
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

}