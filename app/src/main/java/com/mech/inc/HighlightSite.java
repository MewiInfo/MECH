package com.mech.inc;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Felix Feldhofer on 08.01.2016.
 */
public class HighlightSite {

    private String name;
    private LatLng coordinates;
    private String[] tags;
    private String description;

    private boolean notifiedAlready = false;

    public boolean isNotifiedAlready() {
        return notifiedAlready;
    }

    public void setNotifiedAlready(boolean notifiedAlready) {
        this.notifiedAlready = notifiedAlready;
    }

    public String getName() {
        return name;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public String[] getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    //Returns true, if the distance between coordinates and currentLcoation is less than the number of meters specified by int distance.
    public Boolean distanceUnder(Location loc, float distance){

        Location loc2 = new Location("");
        loc2.setLatitude(coordinates.latitude);
        loc2.setLongitude(coordinates.longitude);


        if(loc.distanceTo(loc2) > distance){
            return false;
        }

        else{
            return true;
        }
    }

    //Returns true, if the distance between coordinates and currentLcoation is more than the number of meters specified by int distance.
    public Boolean distanceOver(Location loc, float distance){

        Location loc2 = new Location("");
        loc2.setLatitude(coordinates.latitude);
        loc2.setLongitude(coordinates.longitude);


        if(loc.distanceTo(loc2) < distance){
            return false;
        }

        else{
            return true;
        }
    }

    public HighlightSite(String name, LatLng coordinates, String[] tags, String description) {

        this.name = name;
        this.coordinates = coordinates;
        this.tags = tags;
        this.description = description;

    }
}
