package com.example.fyp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.fyp.R;
//import com.example.fyp.controller.MapViewController;
import com.example.fyp.model.Incident;
import com.example.fyp.model.User;
import com.example.fyp.service.IncidentReporting;
import com.example.fyp.service.ServerRequests;
import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MapView map = null;

    private double loadZoomlevel = 13.0;

    private TextView srcIN;
    private TextView destIN;
    private Button findButton;
    private FloatingActionButton start;

    private DrawerLayout drawer;

    private ServerRequests serverRequests;
    private IncidentReporting incidentReporting;

    private GeoPoint src;
    private GeoPoint dest;

    private boolean hasRoute;
    private boolean navigating;

    private Switch activeRoutingSwitch;
    private Switch avoidIncidentsSwitch;
    private boolean activeRouting;
    private boolean avoidIncidents;
    private Spinner routePreference;
    private List<String> preferences;
    private ServerRequests.DirectionsPreference preference;

    private FloatingActionButton startNav;
    private AwesomeSpeedometer speedometer;
    private LinearLayout searchLayout;
    private ProgressBar progressBar;
    private LinearLayout navLayout;
    private TextView remainingDist;
    private TextView navDestination;

    private GeoPoint currentLocation;

    private CheckBox saveRoute;

    private TextView userNameSurname;
    private TextView userEmail;
    private User user;

    private Timer timer;

    private float distanceToTravel;

    private ArrayList<GeoPoint> waypoints;

    private Incident latestSeenIncident;

//    private MapViewController mapViewController;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);

        readLayouts();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        user = new User();
        Bundle bundle = getIntent().getExtras();
        user.setName(bundle.getString("name"));
        user.setSurname(bundle.getString("surname"));
        user.setEmail(bundle.getString("email"));
        user.setPassword(bundle.getString("pass"));


        String welcome = ("Welcome: " + user.getName() + " " + user.getSurname());
        userNameSurname.setText(welcome);
        userEmail.setText(user.getEmail());

        preferences = new ArrayList<String>();
        preferences.add("Fastest (Time)");
        preferences.add("Shortest (Distance)");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, preferences);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routePreference.setAdapter(adapter);
        routePreference.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        preference = ServerRequests.DirectionsPreference.FASTEST;
                        break;
                    case 1:
                        preference = ServerRequests.DirectionsPreference.SHORTEST;
                        break;
                    default:
                        preference = ServerRequests.DirectionsPreference.FASTEST;
                        break;
                }
                Toast.makeText(MapActivity.this, "Route Preference : " + ((TextView) view).getText().toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        speedometer.setVisibility(View.GONE);

        serverRequests = ServerRequests.getInstance();

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(true);
        map.setMinZoomLevel(3.0);
        map.setScrollableAreaLimitLatitude(MapView.getTileSystem().getMaxLatitude(), MapView.getTileSystem().getMinLatitude(), 0);

//        mapViewController = new MapViewController(this, map);

        MyLocationListener locationListener = new MyLocationListener();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        }

//        mapViewController.checkLocalisation();

        IMapController mapController = map.getController();

        mapController.setZoom(loadZoomlevel);

        GeoPoint startPoint = currentLocation;
        mapController.setCenter(startPoint);

        Drawable naviIcon = getResources().getDrawable(R.drawable.ic_navigation);
        Marker naviMarker = new Marker(map);
        naviMarker.setPosition(startPoint);
        naviMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        naviMarker.isFlat();
        naviMarker.setInfoWindow(null);
        naviMarker.setIcon(naviIcon);
        naviMarker.setPanToView(true);
//        naviMarker.setTitle("Starting Point");
        map.getOverlays().add(0, naviMarker);

        incidentReporting = IncidentReporting.getInstance();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                incidentReporting.displayIncidents(MapActivity.this, map);
                Log.d("Do", "Request incidents");
            }
        }, 0, 90000);


        src = null;
        dest = null;
        hasRoute = false;

        srcIN.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Intent in = new Intent(this, SearchActivity.class);
                in.putExtra("location", (Parcelable) currentLocation);
                startActivityForResult(in, 1);
            }
            return true;
        });

        destIN.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Intent in = new Intent(this, SearchActivity.class);
                startActivityForResult(in, 2);
            }
            return true;
        });
//
        activeRoutingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean check = isChecked;
            if (check) {
                Toast.makeText(this, "ERROR 501", Toast.LENGTH_LONG).show();
//                    item.setChecked(false);
//                activeRouting = true;
            } else {
                Toast.makeText(this, "ERROR 501", Toast.LENGTH_LONG).show();
//                activeRouting = false;
            }
            buttonView.setChecked(check);
        });

        avoidIncidentsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean check = isChecked;
            if (check) {
                Toast.makeText(this, "Avoid Incidents", Toast.LENGTH_LONG).show();
//                    item.setChecked(false);
                avoidIncidents = true;
            } else {
                Toast.makeText(this, "Don't Avoid Incidents", Toast.LENGTH_LONG).show();
                avoidIncidents = false;
            }
            buttonView.setChecked(check);
        });

        saveRoute.setClickable(false);

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                preference = String.valueOf(routePreference.getSelectedItem()).toLowerCase().split(" ")[0];
                if (src != null && dest != null) {
                    drawRoute(src, dest, avoidIncidents, preference);
                    saveRoute.setClickable(true);
                    saveRoute.setButtonDrawable(R.drawable.star);
                }
            }
        });

        saveRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRoute.setButtonDrawable(R.drawable.star_down);
                String title = srcIN.getText().toString() + " -> " + destIN.getText().toString();
                serverRequests.saveRoute(src, dest, title, user.getEmail(), user.getPassword());
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                Toast.makeText(MapActivity.this, "Start Navigation", Toast.LENGTH_LONG).show();
                map.getController().setZoom(17.0);
                map.getController().setCenter(startPoint);
                searchLayout.setVisibility(View.GONE);
                speedometer.setVisibility(View.VISIBLE);
                start.setVisibility(View.GONE);
                navLayout.setVisibility(View.VISIBLE);
                navDestination.setText(destIN.getText().toString());
                navigating = true;

                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Incident incident = serverRequests.checkNearestIncident(currentLocation);
                                if (incident != null) {
                                    if ((latestSeenIncident == null) || (latestSeenIncident != null && incident.getIncidentId() != latestSeenIncident.getIncidentId())) {
                                        incidentReporting.showAlertDialog(MapActivity.this, incident);
                                    }
                                }
                                latestSeenIncident = incident;
                                Log.d("Do", "Checked for nearest incident");
                            }
                        });
                    }
                }, 0, 10000);
            }
        });

    }

    private void readLayouts() {
        drawer = findViewById(R.id.drawer_layout);

        searchLayout = findViewById(R.id.search_layout);
        navLayout = findViewById(R.id.navLayout);
        navLayout.setVisibility(View.GONE);

        speedometer = findViewById(R.id.speedometer);

        navDestination = findViewById(R.id.navDestinationView);
        remainingDist = findViewById(R.id.remainingDistView);
        progressBar = findViewById(R.id.progressBar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        userNameSurname = headerView.findViewById(R.id.userNameHeader);
        userEmail = headerView.findViewById(R.id.userEmailHeader);

        activeRoutingSwitch = navigationView.getMenu().findItem(R.id.activeRoutingItem).getActionView().findViewById(R.id.switchItem);
        avoidIncidentsSwitch = navigationView.getMenu().findItem(R.id.avoidIncidentsItem).getActionView().findViewById(R.id.switchItem);

        routePreference = navigationView.getMenu().findItem(R.id.routePreference).getActionView().findViewById(R.id.dropdown_item);

        map = (MapView) findViewById(R.id.map);

        srcIN = findViewById(R.id.srcIN);
        destIN = findViewById(R.id.destIN);

        saveRoute = (CheckBox) findViewById(R.id.starRoute);
        findButton = findViewById(R.id.searchPlaces);

        start = (FloatingActionButton) findViewById(R.id.startNavBtm);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                String strEditText = data.getStringExtra("address");
                srcIN.setText(strEditText);
                src = (GeoPoint) data.getParcelableExtra("coordinates");
            } else if (requestCode == 2) {
                String strEditText = data.getStringExtra("address");
                destIN.setText(strEditText);
                dest = (GeoPoint) data.getParcelableExtra("coordinates");
            } else if (requestCode == 3) {
                String title = data.getStringExtra("title");
                if (title.contains(" -> ")) {
                    srcIN.setText(title.split(" -> ")[0]);
                    destIN.setText(title.split(" -> ")[1]);
                    src = data.getParcelableExtra("start");
                    dest = data.getParcelableExtra("end");
                }
            }
        }
    }

    public void drawRoute(GeoPoint startPoint, GeoPoint endPoint, boolean avoidIncidents, ServerRequests.DirectionsPreference preference) {

//        distanceToTravel = mapViewController.drawRoute(startPoint, endPoint, avoidIncidents, preference);
//        progressBar.setMax((int) distanceToTravel);
//        setProgressBar(distanceToTravel);
        RoadManager roadManager = new OSRMRoadManager(this);

        waypoints = (ArrayList<GeoPoint>) serverRequests.directionsRequest(startPoint, endPoint, avoidIncidents, preference);
        if (isSimilarToCurrent(waypoints.get(0), 10)) {
            waypoints.remove(0);
        }
        Log.d("w[0]", waypoints.get(0).toString());
        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        clearRoute();
        map.getOverlays().add(1, roadOverlay);
        map.invalidate();

        Drawable startingIcon = getResources().getDrawable(R.drawable.marker2_copy);
        Marker startMarker = new Marker(map);
        startMarker.setPosition(waypoints.get(0));
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(startingIcon);
        startMarker.setPanToView(true);
        startMarker.setTitle("Starting Point");
        map.getOverlays().add(2, startMarker);

        Drawable destinationIcon = getResources().getDrawable(R.drawable.marker2_copy);
        Marker endMarker = new Marker(map);
        endMarker.setPosition(waypoints.get(waypoints.size() - 1));
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setIcon(destinationIcon);
        endMarker.setPanToView(true);
        endMarker.setTitle("Destination Point");
        map.getOverlays().add(3, endMarker);

        distanceToTravel = calculateRemainingDistance();
        progressBar.setMax((int) distanceToTravel);
        setProgressBar(distanceToTravel);

        hasRoute = true;

    }

    @SuppressLint("RestrictedApi")
    private void clearRoute() {
        if (hasRoute) {
            map.getOverlays().remove(1);
            map.getOverlays().remove(1);
            map.getOverlays().remove(1);
            hasRoute = false;
        }
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBackPressed() {
        if (navigating && !hasRoute) {
            speedometer.setVisibility(View.GONE);
            searchLayout.setVisibility(View.VISIBLE);
            start.setVisibility(View.VISIBLE);
            map.getController().setZoom(loadZoomlevel);
            saveRoute.setButtonDrawable(R.drawable.star);
            navLayout.setVisibility(View.GONE);
            timer.cancel();
            navigating = false;
        } else if (hasRoute) {
            new AlertDialog.Builder(this)
                    .setTitle("Stop Navigation")
                    .setMessage("Are you sure you want to clear route?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
//                            mapViewController.clearRoute();
                            clearRoute();
                            if (navigating) {
                                speedometer.setVisibility(View.GONE);
                                searchLayout.setVisibility(View.VISIBLE);
                                start.setVisibility(View.VISIBLE);
                                map.getController().setZoom(loadZoomlevel);
                                saveRoute.setButtonDrawable(R.drawable.star);
                                navLayout.setVisibility(View.GONE);
                                navigating = false;
                            }
                        }
                    }).create().show();
        } else {
            MapActivity.super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        IncidentReporting ir = IncidentReporting.getInstance();
        String title = "";
        GeoPoint location = null;
        switch (item.getItemId()) {
            case R.id.accident:
                title = "Accident";
                location = currentLocation;
                incidentReporting.placeMarker(MapActivity.this, map, title, location);
                serverRequests.saveIncident(title, location);
                return (true);
            case R.id.breakdown:
                title = "Breakdown";
                location = currentLocation;
                incidentReporting.placeMarker(MapActivity.this, map, title, location);
                serverRequests.saveIncident(title, location);
                return (true);
            case R.id.road_works:
                title = "Roadworks";
                location = currentLocation;
                incidentReporting.placeMarker(MapActivity.this, map, title, location);
                serverRequests.saveIncident(title, location);
                return (true);
            case R.id.closed_road:
                title = "Closed Road";
                location = currentLocation;
                incidentReporting.placeMarker(MapActivity.this, map, title, location);
                serverRequests.saveIncident(title, location);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_LONG).show();
                this.onBackPressed();
//                startActivity(new Intent(this, MainActivity.class));
//                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_routes:
                Toast.makeText(this, "Favourites", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, FavouritesActivity.class);
                intent.putExtra("email", user.getEmail());
                intent.putExtra("pass", user.getPassword());
                startActivityForResult(intent, 3);
//                startActivity(new Intent(this, MainActivity.class));
//                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.logout:
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            displayMyCurrentLocationOverlay(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private void displayMyCurrentLocationOverlay(Location location) {
        if (currentLocation != null && location != null) {
            GeoPoint newLocation = new GeoPoint(location);
            speedometer.speedTo((float) (location.getSpeed() * (3.6)));
            if (!isSimilarToCurrent(newLocation, 2)) {
                currentLocation = newLocation;
                Marker naviMarker = (Marker) map.getOverlays().get(0);
                naviMarker.setPosition(currentLocation);
                naviMarker.setRotation(location.getBearing());
                naviMarker.setInfoWindow(null);

                map.getController().animateTo(currentLocation);
                speedometer.speedTo((float) (location.getSpeed() * (3.6)));
//                map.getController().setCenter(currentLocation);
                if (waypoints != null) {
                    Log.d("w size", Integer.toString(waypoints.size()));
                    Log.d("next w", waypoints.get(0).toString());
//                    Toast.makeText(MapActivity.this, "w size" + waypoints.size(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(MapActivity.this, "next w" + waypoints.get(0).toString(), Toast.LENGTH_LONG).show();
                }
                if (waypoints != null && waypoints.size() > 0 && isSimilarToCurrent(waypoints.get(0), (int) (10 + (location.getSpeed() / 2)))) {
                    Log.d("removing", "w[0]");
//                    Toast.makeText(MapActivity.this, "removing w[0]", Toast.LENGTH_LONG).show();
                    waypoints.remove(0);
                }
                if (hasRoute && navigating) {
                    float total = calculateRemainingDistance();
                    setProgressBar(total);

                }
            }
        }
    }

    private boolean isSimilarToCurrent(GeoPoint newLocation, int margin) {
        double metre = 0.0000146;
        double moe = margin * metre;
        if (currentLocation.getLatitude() + moe <= newLocation.getLatitude() //100m    101m < 101.01m
                || currentLocation.getLatitude() - moe >= newLocation.getLatitude() //100m 99m > 98.9m
                || currentLocation.getLongitude() + moe <= newLocation.getLongitude()
                || currentLocation.getLongitude() - moe >= newLocation.getLongitude()) {
            return false;
        }
        return true;
    }

    private float calculateRemainingDistance() {
        if (currentLocation != null && waypoints != null && waypoints.size() > 1) {
            float results[] = new float[1];
            Location.distanceBetween(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    waypoints.get(0).getLatitude(),
                    waypoints.get(0).getLongitude(),
                    results);
            float total = results[0];
            for (int i = 1; i < waypoints.size(); i++) {
                Location.distanceBetween(
                        waypoints.get(i - 1).getLatitude(),
                        waypoints.get(i - 1).getLongitude(),
                        waypoints.get(i).getLatitude(),
                        waypoints.get(i).getLongitude(),
                        results);
//                Log.d("r", Float.toString(results[0]));
                total += results[0];
            }
            Log.d("total", Float.toString(total));


            return total;
        }

        return -1f;
    }

    private void setProgressBar(float total){
        progressBar.setProgress((int)(distanceToTravel - total));
        remainingDist.setText(Float.toString(total));
    }
}

//Start progressing
//                new Thread(new Runnable() {
//                    public void run() {
//                        while (progressStatusCounter < 100) {
//                            progressStatusCounter += 2;
//                            progressHandler.post(new Runnable() {
//                                public void run() {
//                                    progressBar.setProgress(progressStatusCounter);
//                                    //Status update in textview
//                                    textView.setText("Status: " + progressStatusCounter + "/" + androidProgressBar.getMax());
//                                }
//                            });
//                            try {
//                                Thread.sleep(300);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }).start();
