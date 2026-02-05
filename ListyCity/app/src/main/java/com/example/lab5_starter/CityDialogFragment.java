package com.example.lab5_starter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class CityDialogFragment extends DialogFragment {

    interface CityDialogListener {
        void updateCity(City city, String title, String year);
        void addCity(City city);
    }

    private CityDialogListener listener;

    public static CityDialogFragment newInstance(City city) {
        Bundle args = new Bundle();
        args.putSerializable("City", city);

        CityDialogFragment fragment = new CityDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Log.d("CITYDBG", "onAttach context=" + context.getClass().getName());

        if (context instanceof CityDialogListener) {
            listener = (CityDialogListener) context;
        } else {
            throw new RuntimeException("MainActivity must implement CityDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = getLayoutInflater().inflate(R.layout.fragment_city_details, null);
        EditText editCityName = view.findViewById(R.id.edit_city_name);
        EditText editProvince = view.findViewById(R.id.edit_province);

        Bundle bundle = getArguments();
        City city = null;
        if (Objects.equals(getTag(), "City Details") && bundle != null) {
            city = (City) bundle.getSerializable("City");
            if (city != null) {
                editCityName.setText(city.getName());
                editProvince.setText(city.getProvince());
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        City finalCity = city;

        return builder
                .setView(view)
                .setTitle(Objects.equals(getTag(), "City Details") ? "City Details" : "Add City")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", (dialog, which) -> {

                    Log.d("CITYDBG", "Continue clicked, tag=" + getTag());
                    Log.d("CITYDBG", "listener is null? " + (listener == null));

                    String name = editCityName.getText().toString().trim();
                    String prov = editProvince.getText().toString().trim();
                    if (name.isEmpty()) {
                        Log.d("CITYDBG", "Empty city name, ignore");
                        return;
                    }
                    if (finalCity != null) {
                        listener.updateCity(finalCity, name, prov);
                    } else {
                        listener.addCity(new City(name, prov));
                    }
                })
                .create();
    }
}