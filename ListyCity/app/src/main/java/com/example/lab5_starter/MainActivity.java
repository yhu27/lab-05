package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {
    private Button addCityButton;
    private Button deleteCityButton;
    private ListView cityListView;
    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private String selectedCityName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        cityListView = findViewById(R.id.listviewCities);
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");
        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Listen failed", error);
                Toast.makeText(this, "Listen failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (value == null) return;

            cityArrayList.clear();
            for (QueryDocumentSnapshot doc : value) {
                String name = doc.getString("name");
                String province = doc.getString("province");

                if (name == null) name = doc.getId();
                if (province == null) province = "";

                cityArrayList.add(new City(name, province));
            }
            cityArrayAdapter.notifyDataSetChanged();

            if (selectedCityName != null) {
                boolean stillExists = false;
                for (City c : cityArrayList) {
                    if (c.getName() != null && c.getName().equals(selectedCityName)) {
                        stillExists = true;
                        break;
                    }
                }
                if (!stillExists) selectedCityName = null;
            }
        });
        addCityButton.setOnClickListener(v -> {
            CityDialogFragment dialog = new CityDialogFragment();
            dialog.show(getSupportFragmentManager(), "Add City");
        });

        cityListView.setOnItemClickListener((parent, view, position, id) -> {
            City c = cityArrayAdapter.getItem(position);
            if (c == null) return;

            String name = c.getName() == null ? "" : c.getName().trim();
            if (name.isEmpty()) return;

            selectedCityName = name;
            Toast.makeText(this, "Selected: " + selectedCityName, Toast.LENGTH_SHORT).show();
        });
        cityListView.setOnItemLongClickListener((parent, view, position, id) -> {
            City city = cityArrayAdapter.getItem(position);
            CityDialogFragment dialog = CityDialogFragment.newInstance(city);
            dialog.show(getSupportFragmentManager(), "City Details");
            return true;
        });
        deleteCityButton.setOnClickListener(v -> {
            if (selectedCityName == null) {
                Toast.makeText(this, "Please tap a city to select it first.", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = selectedCityName.trim();
            if (name.isEmpty()) return;

            citiesRef.document(name).delete()
                    .addOnSuccessListener(v1 -> {
                        Toast.makeText(this, "Deleted: " + name, Toast.LENGTH_SHORT).show();
                        selectedCityName = null;
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }
    @Override
    public void addCity(City city) {
        if (city == null) return;

        String name = city.getName() == null ? "" : city.getName().trim();
        String prov = city.getProvince() == null ? "" : city.getProvince().trim();
        if (name.isEmpty()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("province", prov);

        citiesRef.document(name).set(data)
                .addOnSuccessListener(v -> Toast.makeText(this, "Saved: " + name, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    @Override
    public void updateCity(City city, String title, String year) {
        if (city == null) return;

        String oldName = city.getName() == null ? "" : city.getName().trim();
        String newName = title == null ? "" : title.trim();
        String prov = year == null ? "" : year.trim();

        if (newName.isEmpty()) return;

        if (!oldName.isEmpty() && !oldName.equals(newName)) {
            citiesRef.document(oldName).delete();
            if (selectedCityName != null && selectedCityName.equals(oldName)) {
                selectedCityName = newName;
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", newName);
        data.put("province", prov);

        citiesRef.document(newName).set(data)
                .addOnSuccessListener(v -> Toast.makeText(this, "Updated: " + newName, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}