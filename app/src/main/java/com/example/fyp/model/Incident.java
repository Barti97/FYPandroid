package com.example.fyp.model;

import com.google.maps.model.LatLng;

public class Incident {

    private int incidentId;
    private String typeOfIncident;
    private double lat;
    private double lng;
    private int reportNumber;

    public Incident(String typeOfIncident, LatLng incidentCoordinates, int reportNumber) {
        this.typeOfIncident = typeOfIncident;
        this.reportNumber = reportNumber;

        if(incidentCoordinates != null) {
            this.lat = incidentCoordinates.lat;
            this.lng = incidentCoordinates.lng;
        } else {
            this.lat = 0.0;
            this.lng = 0.0;
        }
    }

    public String toString() {
        String output = "Incident: ";
        if (this.incidentId != 0) {
            output += this.incidentId;
        }
        output += "\n\tType: " + this.typeOfIncident + "\n\tCoordinates: " + this.lat + "," + this.lng ;
        return output;
    }

    public int getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(int incidentId) {
        this.incidentId = incidentId;
    }

    public String getTypeOfIncident() {
        return typeOfIncident;
    }

    public void setTypeOfIncident(String typeOfIncident) {
        this.typeOfIncident = typeOfIncident;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(int reportNumber) {
        this.reportNumber = reportNumber;
    }
//    public String drawPolygon() {
//
//        double metre = 0.0000146;
//
//        LatLng p1 = new LatLng(this.lat + (5 * metre), this.lng - (5 * metre)); // Top left
//        LatLng p2 = new LatLng(this.lat + (5 * metre), this.lng + (5 * metre)); // Top right
//        LatLng p3 = new LatLng(this.lat - (5 * metre), this.lng + (5 * metre)); // Bottom right
//        LatLng p4 = new LatLng(this.lat - (5 * metre), this.lng - (5 * metre)); // Bottom left
//
//        String p1s = latLngAsArray(p1);
//        String p2s = latLngAsArray(p2);
//        String p3s = latLngAsArray(p3);
//        String p4s = latLngAsArray(p4);
//
//        String polygon = "[" + p1s + ","  + p2s + "," + p3s + "," + p4s + "," + p1s + "]";
////		String polygon = p1+p2+p3+p4+p1;
//        return polygon;
//    }
//
//    private String latLngAsArray(LatLng coord) {
//        return "[" + coord.lng + "," + coord.lat + "]";
//    }


}

