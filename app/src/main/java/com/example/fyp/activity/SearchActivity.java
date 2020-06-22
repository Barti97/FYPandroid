package com.example.fyp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.example.fyp.service.ServerRequests;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;


public class SearchActivity extends AppCompatActivity {

    private TextView address;
    private Button searchAddressBtn;
    private ListView addressList;
    private ServerRequests serverRequests;
    private HashMap<String, GeoPoint> addresses;
    private GeoPoint currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        serverRequests = ServerRequests.getInstance();

        address = findViewById(R.id.addressIn);
        searchAddressBtn = findViewById(R.id.searchAddressBtn);
        addressList = findViewById(R.id.addressList);

        addresses = new HashMap<String, GeoPoint>();
        currentLocation = getIntent().getParcelableExtra("location");
        addresses.put("Current Location", currentLocation);
        addressList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<>(addresses.keySet())));

        searchAddressBtn.setOnClickListener(view -> {
            if (!address.getText().toString().isEmpty()) {
                addresses = serverRequests.autocompletePlace(address.getText().toString());
                addresses.put("Current Location", currentLocation);
                addressList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<>(addresses.keySet())));
            }
        });

        addressList.setOnItemClickListener((adapterView, view, position, id) -> {
            Intent intent = new Intent();
            String listItem = (String) addressList.getItemAtPosition(position);
            intent.putExtra("address", listItem);
            intent.putExtra("coordinates", (Parcelable) addresses.get(listItem));
            setResult(RESULT_OK, intent);
            finish();
        });

    }
}
