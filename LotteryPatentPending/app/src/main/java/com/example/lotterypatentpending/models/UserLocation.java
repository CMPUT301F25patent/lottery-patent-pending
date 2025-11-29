package com.example.lotterypatentpending.models;
/**
 * Lightweight container for a user's latitude/longitude pair.
 * Stored inside the User document as {@code location.lat} / {@code location.lng}.
 */
public class UserLocation {
    private double lat;
    private double lng;
    /** Required for Firestore deserialization. */
    public UserLocation() {}
    /**
     * Creates a new location with the given coordinates.
     *
     * @param lat latitude in degrees
     * @param lng longitude in degrees
     */
    public UserLocation(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }
    /** @return latitude in degrees */
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    /** @return longitude in degrees */
    public double getLng() {
        return lng;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }
}
