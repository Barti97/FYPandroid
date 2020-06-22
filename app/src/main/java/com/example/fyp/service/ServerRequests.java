package com.example.fyp.service;

import android.util.Log;

import com.example.fyp.model.Incident;
import com.example.fyp.model.Route;
import com.example.fyp.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.osmdroid.util.GeoPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerRequests {

    public enum DirectionsPreference {
        SHORTEST,
        FASTEST,
    }

    private static ServerRequests instance = null;

    private String serverUrl = "";

    private ServerRequests() {
        serverUrl = "http://192.168.0.192:8080";
    }
//    private ServerRequests() { serverUrl = "http://192.168.43.190:8080"; }

    public static ServerRequests getInstance() {
        if (instance == null) {
            instance = new ServerRequests();
        }
        return instance;
    }

    public ArrayList<GeoPoint> directionsRequest(GeoPoint source, GeoPoint destination, boolean avoidPlaces, DirectionsPreference preference) {
        String pref = null;
        switch (preference) {
            case FASTEST:
                pref = "fastest";
                break;
            case SHORTEST:
                pref = "shortest";
                break;
            default:
                pref = "fastest";
                break;
        }
        String src = "{\"lat\":" + source.getLatitude() + ",\"lng\":" + source.getLongitude() + "}";
        String dest = "{\"lat\":" + destination.getLatitude() + ",\"lng\":" + destination.getLongitude() + "}";
        String res = HttpRequest.executePost(this.serverUrl + "/api/directions", "{\"start\":" + src + ", \"end\":" + dest + ", \"avoidIncidents\":" + avoidPlaces + ", \"preference\":\"" + pref + "\" }");
        Log.d("res", res);
        JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
        JsonArray coords = jsonObject.getAsJsonArray("result");
        return parseServerCoords(coords.toString());
    }

    public ArrayList<GeoPoint> parseServerCoords(String coordsList) {
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        JsonArray json = JsonParser.parseString(coordsList).getAsJsonArray();
        for (JsonElement j : json) {
            JsonArray c = j.getAsJsonArray();
            waypoints.add(new GeoPoint(c.get(1).getAsFloat(), c.get(0).getAsFloat()));
        }
        return waypoints;
    }

    public String getAllIncidents() {
        return HttpRequest.executeGet(this.serverUrl + "/api/incident/all");
    }

    public boolean saveIncident(String incidentType, GeoPoint location) {
        String res = HttpRequest.executePost(this.serverUrl + "/api/incident/save", "{\"type\":\"" + incidentType + "\", \"location\":{\"lat\":" + location.getLatitude() + ",\"lng\":" + location.getLongitude() + "}, \"userEmail\":\"admin@email.com\"}");
        return true;
    }

    public HashMap<String, GeoPoint> autocompletePlace(String searchPhrase) {
        String searchRes = HttpRequest.executePost(this.serverUrl + "/api/autocompleteAddress", "{\"address\":\"" + searchPhrase + "\"}");
        Log.d("autocomplete response", searchRes);
        return parseAutocompleteSearch(searchRes);
    }

    public HashMap<String, GeoPoint> parseAutocompleteSearch(String searchResult) {
        Log.d("searchResult", searchResult);
        HashMap<String, GeoPoint> addresses = new HashMap<>();
        JsonArray json = JsonParser.parseString(searchResult).getAsJsonArray();
        for (JsonElement je : json) {
            JsonObject jo = je.getAsJsonObject();
            String address = jo.get("address").getAsString();
            JsonObject coordsJson = jo.getAsJsonObject("coordinates");
            GeoPoint coords = new GeoPoint(coordsJson.get("lat").getAsFloat(), coordsJson.get("lng").getAsFloat());
            addresses.put(address, coords);
        }
        return addresses;
    }

    public User userAuth(String emailIn, String password) {
        User user = null;
        String res = HttpRequest.executePost(this.serverUrl + "/api/login", "{\"username\":\"" + emailIn + "\"}", emailIn, password);
        if (res != null) {
//            Log.d("res", res);
            JsonObject json = JsonParser.parseString(res).getAsJsonObject();
            String name = json.get("name").getAsString();
            String surname = json.get("surname").getAsString();
            String email = json.get("email").getAsString();
            String date = json.get("doB").getAsString();
            int phone = json.get("phoneNumber").getAsInt();
            user = new User(email, name, surname, phone, LocalDate.parse(date));
        }
        return user;
    }

    public void registerUser(User user) {
//        Log.d("user", user.toJSON());
        String res = HttpRequest.executePost(this.serverUrl + "/api/register", user.toJSON());
//        Log.d("res", res);
    }

    public Incident checkNearestIncident(GeoPoint currentPosition) {
        String res = HttpRequest.executePost(this.serverUrl + "/api/incident/proximity", "{\"lat\":" + currentPosition.getLatitude() + ", \"lng\":" + currentPosition.getLongitude() + "}");
//        Log.d("res", res);
        if (res != null) {
            return new Gson().fromJson(res, Incident.class);
        }
        return null;
    }

    public boolean updateIncident(Incident incident, boolean increase) {
        String updateRes = HttpRequest.executePost(this.serverUrl + "/api/incident/update", "{\"id\":" + incident.getIncidentId() + ",\"increase\":" + increase + "}");
        if (updateRes != null) {
//            Log.d("updateRes", updateRes);
            if (updateRes.equalsIgnoreCase("success")) {
                return true;
            }
        }
        return false;
    }

    public boolean saveRoute(GeoPoint start, GeoPoint end, String title, String email, String pswd) {
        String res = HttpRequest.executePost(this.serverUrl + "/api/route/save", "{\"start\":{\"lat\":" + start.getLatitude() + ",\"lng\":" + start.getLongitude() + "},\"end\":{\"lat\":" + end.getLatitude() + ",\"lng\":" + end.getLongitude() + "}, \"title\":\"" + title + "\"}", email, pswd);
        if (res != null && res.equalsIgnoreCase("success")) {
            return true;
        } else {
            return false;
        }
    }

    public HashMap<String, Route> favouriteRoutes(String email, String pswd) {
        String searchRes = HttpRequest.executePost(this.serverUrl + "/api/route/favourites", "", email, pswd);
        Log.d("autocomplete response", searchRes);
        return parseFavouritesResponse(searchRes);
    }

    public HashMap<String, Route> parseFavouritesResponse(String searchResult) {
        HashMap<String, Route> favourites = new HashMap<>();
        JsonArray json = JsonParser.parseString(searchResult).getAsJsonArray();
        for (JsonElement je : json) {
            Route route = new Gson().fromJson(je.toString(), Route.class);
            favourites.put(route.getTitle(), route);
        }
        return favourites;
    }

//    public static List<GeoPoint> getDirections(String source, String destination) {
//        List<GeoPoint> startFinishCoords = getStartFinishCoordinates(source, destination);
//        ArrayList<GeoPoint> waypoints = new ArrayList<>();
//        GeoPoint start = startFinishCoords.get(0);
//        GeoPoint end = startFinishCoords.get(1);
//        String resDirections = OpenRouteServiceAPI.executePost(
//                "https://api.openrouteservice.org/v2/directions/driving-car/geojson",
//                "{\"coordinates\":[[" + start.getLongitude() + "," + start.getLatitude() + "],[" + end.getLongitude() + "," + end.getLatitude() + "]]}");
//        Log.i("directions result", resDirections);
//        JsonObject jsonObject = JsonParser.parseString(resDirections).getAsJsonObject();
//        JsonObject features = jsonObject.getAsJsonArray("features").get(0).getAsJsonObject();
//        JsonObject geometry = features.getAsJsonObject("geometry");
//        JsonArray coords = geometry.getAsJsonArray("coordinates");
//        Log.i("server coords", coords.toString());
//        return parseServerCoords(coords.toString());
////        for (JsonElement j : coords) {
////            JsonArray c = j.getAsJsonArray();
////            waypoints.add(new GeoPoint(c.get(1).getAsFloat(), c.get(0).getAsFloat()));
////        }
////        return waypoints;
//    }

//    public static List<GeoPoint> getStartFinishCoordinates(String startingPoint, String destinationPoint) {
//        ArrayList<GeoPoint> coords = new ArrayList<>();
//        String startSearch = findPlace(startingPoint);
//        GeoPoint startPoint = parsePlaceSearch(startSearch);
//        coords.add(startPoint);
//        String endSearch = findPlace(destinationPoint);
//        GeoPoint endPoint = parsePlaceSearch(endSearch);
//        coords.add(endPoint);
//        return coords;
//    }

//    public static String findPlace(String searchPhrase) {
//        return HttpRequest.executeGet("https://api.openrouteservice.org/geocode/search?api_key=5b3ce3597851110001cf62487948acef386c48808312fcdaf6978e33&text=" + searchPhrase + "&boundary.country=IE");
//
//    }

//    public List<String> autocompletePlace(String searchPhrase) {
//        String searchRes = HttpRequest.executeGet("https://api.openrouteservice.org/geocode/autocomplete?api_key=5b3ce3597851110001cf62487948acef386c48808312fcdaf6978e33&text=" + searchPhrase + "&boundary.country=IE&layers=address,venue,neighbourhood");
//        return parseAutocompleteSearch(searchRes);
//    }

//    public List<String> parseAutocompleteSearch(String searchResult) {
//        List<String> addresses = new ArrayList<>();
//        Log.d("searchResult", searchResult);
//
//        JsonObject json = JsonParser.parseString(searchResult).getAsJsonObject();
//        JsonArray features = json.getAsJsonArray("features");
//
//        for (JsonElement jsonElement : features) {
//            JsonObject jsonObject = jsonElement.getAsJsonObject();
//            JsonElement label = jsonObject.getAsJsonObject("properties").get("label");
//            addresses.add(label.getAsString());
//        }
//        return addresses;
//    }
//
//    public static GeoPoint parsePlaceSearch(String searchResult) {
//        JsonObject json = JsonParser.parseString(searchResult).getAsJsonObject();
//        JsonArray features = json.getAsJsonArray("features");
//        JsonObject feature0 = features.get(0).getAsJsonObject();
//        JsonObject geometry = feature0.getAsJsonObject("geometry");
//        JsonArray coords = geometry.getAsJsonArray("coordinates");
//        return new GeoPoint(coords.get(1).getAsFloat(), coords.get(0).getAsFloat());
//    }
}
