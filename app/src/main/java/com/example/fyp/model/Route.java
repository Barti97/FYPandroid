package com.example.fyp.model;

import com.google.maps.model.LatLng;

public class Route {

    private int routeId;
    private String title;
    private double start_lat;
    private double start_lng;
    private double end_lat;
    private double end_lng;
    private User owner;

    public Route(String title, LatLng start, LatLng end, User owner) {
        this.title = title;
        if(start != null) {
            this.start_lat = start.lat;
            this.start_lng = start.lng;
        } else {
            this.start_lat = 0.0;
            this.start_lng = 0.0;
        }

        if(end != null) {
            this.end_lat = end.lat;
            this.end_lng = end.lng;
        } else {
            this.end_lat = 0.0;
            this.end_lng = 0.0;
        }
        this.owner = owner;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getStart_lat() {
        return start_lat;
    }

    public void setStart_lat(double start_lat) {
        this.start_lat = start_lat;
    }

    public double getStart_lng() {
        return start_lng;
    }

    public void setStart_lng(double start_lng) {
        this.start_lng = start_lng;
    }

    public double getEnd_lat() {
        return end_lat;
    }

    public void setEnd_lat(double end_lat) {
        this.end_lat = end_lat;
    }

    public double getEnd_lng() {
        return end_lng;
    }

    public void setEnd_lng(double end_lng) {
        this.end_lng = end_lng;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
