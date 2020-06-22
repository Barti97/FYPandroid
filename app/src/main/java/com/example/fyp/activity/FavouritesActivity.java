package com.example.fyp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.example.fyp.model.Route;
import com.example.fyp.service.ServerRequests;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;

public class FavouritesActivity extends AppCompatActivity {

    private ListView routeList;
    private ServerRequests serverRequests;

    private HashMap<String, Route> favourites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        String email = getIntent().getStringExtra("email");
        String pswd = getIntent().getStringExtra("pass");
        serverRequests = ServerRequests.getInstance();
        favourites = serverRequests.favouriteRoutes(email, pswd);

        routeList = findViewById(R.id.routeList);
        routeList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<>(favourites.keySet())));

        routeList.setOnItemClickListener((adapterView, view, position, id) -> {
            Intent intent = new Intent();
            String listItem = (String) routeList.getItemAtPosition(position);
            Route route = favourites.get(listItem);
            intent.putExtra("title", listItem);
            intent.putExtra("start", (Parcelable) new GeoPoint(route.getStart_lat(), route.getStart_lng()));
            intent.putExtra("end", (Parcelable) new GeoPoint(route.getEnd_lat(), route.getEnd_lng()));
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}
