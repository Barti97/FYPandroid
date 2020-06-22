//package com.example.fyp.controller;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.graphics.drawable.Drawable;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.core.app.ActivityCompat;
//
//import com.example.fyp.R;
//import com.example.fyp.service.IncidentReporting;
//import com.example.fyp.service.ServerRequests;
//
//import org.osmdroid.bonuspack.routing.OSRMRoadManager;
//import org.osmdroid.bonuspack.routing.Road;
//import org.osmdroid.bonuspack.routing.RoadManager;
//import org.osmdroid.util.GeoPoint;
//import org.osmdroid.views.MapView;
//import org.osmdroid.views.overlay.Marker;
//import org.osmdroid.views.overlay.Polyline;
//
//import java.util.ArrayList;
//
//public class MapViewController {
//
//    private Context ctx;
//    private MapView map = null;
//    private boolean hasRoute;
//    private boolean navigating;
//    private ArrayList<GeoPoint> waypoints;
//    private ServerRequests serverRequests;
//    private IncidentReporting incidentReporting;
//    private GeoPoint currentLocation;
//    private TextView remainingDist;
//    private float distanceToTravel;
//
//    public MapViewController(Context ctx, MapView map) {
//        this.ctx = ctx;
//        this.map = map;
//
//    }
//
//    public void checkLocalisation(){
//        MyLocationListener locationListener = new MyLocationListener();
//        LocationManager locationManager = (LocationManager) this.ctx.getSystemService(Context.LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this.ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this.ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        if (location != null) {
//            currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
//        }
//    }
//
//    public float drawRoute(GeoPoint startPoint, GeoPoint endPoint, boolean avoidIncidents, ServerRequests.DirectionsPreference preference) {
//
//        RoadManager roadManager = new OSRMRoadManager(this.ctx);
//
//        waypoints = (ArrayList<GeoPoint>) serverRequests.directionsRequest(startPoint, endPoint, avoidIncidents, preference);
//        if (isSimilarToCurrent(waypoints.get(0), 10)) {
//            waypoints.remove(0);
//        }
//        Log.d("w[0]", waypoints.get(0).toString());
//        Road road = roadManager.getRoad(waypoints);
//        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
//        clearRoute();
//        map.getOverlays().add(1, roadOverlay);
//        map.invalidate();
//
//        Drawable startingIcon = this.ctx.getResources().getDrawable(R.drawable.marker2_copy);
//        Marker startMarker = new Marker(map);
//        startMarker.setPosition(waypoints.get(0));
//        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        startMarker.setIcon(startingIcon);
//        startMarker.setPanToView(true);
//        startMarker.setTitle("Starting Point");
//        map.getOverlays().add(2, startMarker);
//
//        Drawable destinationIcon = this.ctx.getResources().getDrawable(R.drawable.marker2_copy);
//        Marker endMarker = new Marker(map);
//        endMarker.setPosition(waypoints.get(waypoints.size() - 1));
//        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        endMarker.setIcon(destinationIcon);
//        endMarker.setPanToView(true);
//        endMarker.setTitle("Destination Point");
//        map.getOverlays().add(3, endMarker);
//
//        hasRoute = true;
//
//        return calculateRemainingDistance();
//
//    }
//
//    @SuppressLint("RestrictedApi")
//    public void clearRoute() {
//        if (hasRoute) {
//            map.getOverlays().remove(1);
//            map.getOverlays().remove(1);
//            map.getOverlays().remove(1);
//            hasRoute = false;
//        }
//    }
//
//    public class MyLocationListener implements LocationListener {
//
//        public void onLocationChanged(Location location) {
//            displayMyCurrentLocationOverlay(location);
//        }
//
//        public void onProviderDisabled(String provider) {
//        }
//
//        public void onProviderEnabled(String provider) {
//        }
//
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//    }
//
//    public void displayMyCurrentLocationOverlay(Location location) {
//        if (currentLocation != null && location != null) {
//            GeoPoint newLocation = new GeoPoint(location);
//            speedometer.speedTo((float) (location.getSpeed() * (3.6)));
//            if (!isSimilarToCurrent(newLocation, 2)) {
//                currentLocation = newLocation;
//                Marker naviMarker = (Marker) map.getOverlays().get(0);
//                naviMarker.setPosition(currentLocation);
//                naviMarker.setRotation(location.getBearing());
//                naviMarker.setInfoWindow(null);
//
//                map.getController().animateTo(currentLocation);
//                speedometer.speedTo((float) (location.getSpeed() * (3.6)));
////                map.getController().setCenter(currentLocation);
//                if (waypoints != null) {
//                    Log.d("w size", Integer.toString(waypoints.size()));
//                    Log.d("next w", waypoints.get(0).toString());
//                    Toast.makeText(this.ctx, "w size" + waypoints.size(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(this.ctx, "next w" + waypoints.get(0).toString(), Toast.LENGTH_LONG).show();
//                }
//                if (waypoints != null && waypoints.size() > 0 && isSimilarToCurrent(waypoints.get(0), (int) (10 + (location.getSpeed() / 2)))) {
//                    Log.d("removing", "w[0]");
//                    Toast.makeText(this.ctx, "removing w[0]", Toast.LENGTH_LONG).show();
//                    waypoints.remove(0);
//                }
//                if (hasRoute && navigating) {
//                    float total = calculateRemainingDistance();
//                    setProgressBar(total);
//
//                }
//            }
//        }
//    }
//
//    public boolean isSimilarToCurrent(GeoPoint newLocation, int margin) {
//        double metre = 0.0000146;
//        double moe = margin * metre;
//        if (currentLocation.getLatitude() + moe <= newLocation.getLatitude() //100m    101m < 101.01m
//                || currentLocation.getLatitude() - moe >= newLocation.getLatitude() //100m 99m > 98.9m
//                || currentLocation.getLongitude() + moe <= newLocation.getLongitude()
//                || currentLocation.getLongitude() - moe >= newLocation.getLongitude()) {
//            return false;
//        }
//        return true;
//    }
//
//    private float calculateRemainingDistance() {
//        if (currentLocation != null && waypoints != null && waypoints.size() > 1) {
//            float results[] = new float[1];
//            Location.distanceBetween(
//                    currentLocation.getLatitude(),
//                    currentLocation.getLongitude(),
//                    waypoints.get(0).getLatitude(),
//                    waypoints.get(0).getLongitude(),
//                    results);
//            float total = results[0];
//            for (int i = 1; i < waypoints.size(); i++) {
//                Location.distanceBetween(
//                        waypoints.get(i - 1).getLatitude(),
//                        waypoints.get(i - 1).getLongitude(),
//                        waypoints.get(i).getLatitude(),
//                        waypoints.get(i).getLongitude(),
//                        results);
////                Log.d("r", Float.toString(results[0]));
//                total += results[0];
//            }
//            Log.d("total", Float.toString(total));
//
//
//            return total;
//        }
//
//        return -1f;
//    }
//
//
//}
