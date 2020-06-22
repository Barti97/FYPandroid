package com.example.fyp.service;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.example.fyp.R;
import com.example.fyp.model.Incident;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class IncidentReporting {

    private static IncidentReporting instance = null;

    private ServerRequests serverRequests;

    private IncidentReporting() {
        this.serverRequests = ServerRequests.getInstance();
    }

    public static IncidentReporting getInstance() {
        if(instance == null) {
            instance = new IncidentReporting();
        }
        return instance;
    }

    public void placeMarker(Context ctx, MapView map, String incident, GeoPoint location){
        Drawable icon = null;

        if (incident.equals("Accident")) {
            icon = ResourcesCompat.getDrawable(ctx.getResources(), R.drawable.accident_icon, null);
        } else if (incident.equals("Breakdown")) {
            icon = ResourcesCompat.getDrawable(ctx.getResources(), R.drawable.purple_icon_new, null);
        } else if (incident.equals("Roadworks")) {
            icon = ResourcesCompat.getDrawable(ctx.getResources(), R.drawable.brown_icon_new, null);
        } else if (incident.equals("Closed Road")) {
            icon = ResourcesCompat.getDrawable(ctx.getResources(), R.drawable.red_icon_new, null);
        }

        Marker nodeMarker = new Marker(map);
        nodeMarker.setPosition(location);
        nodeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        nodeMarker.setIcon(icon);
        nodeMarker.setPanToView(true);
        nodeMarker.setTitle(incident);
        map.getOverlays().add(nodeMarker);

    }

    public void displayIncidents(Context ctx, MapView map){

        List<GeoPoint> allIncidents = new ArrayList<GeoPoint>();

        String res = serverRequests.getAllIncidents();

        JsonArray jsonObject = JsonParser.parseString(res).getAsJsonArray();
        for (JsonElement jsonElement : jsonObject) {
            JsonObject json = jsonElement.getAsJsonObject();
            String incident = json.get("typeOfIncident").getAsString();
            float lat = json.get("lat").getAsFloat();
            float lng = json.get("lng").getAsFloat();

            GeoPoint coord = new GeoPoint(lat, lng);
            placeMarker(ctx, map, incident, coord);
        }

    }

    public void showAlertDialog(Context ctx, Incident incident){
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        alert.setTitle("WARNING");
        alert.setMessage("You are approaching : "+ incident.getTypeOfIncident().toUpperCase());
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Toast.makeText(ctx, "Approved", Toast.LENGTH_SHORT).show();
                serverRequests.updateIncident(incident, true);
//                serverRequests.checkNearestIncident(currentPosition, true);
            }
        });
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Toast.makeText(ctx, "Declined", Toast.LENGTH_SHORT).show();
                serverRequests.updateIncident(incident, false);
//                serverRequests.updateIncident(currentPosition, false);
            }
        });
        alert.create().show();

    }


}
