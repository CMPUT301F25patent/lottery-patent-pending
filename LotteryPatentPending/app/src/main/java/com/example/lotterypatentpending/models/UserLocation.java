package com.example.lotterypatentpending.models;

/**
 * Represents a geographical point using latitude and longitude coordinates.
 * This class is typically used to store a user's last known location
 * for features like event proximity or check-in verification.
 */
public class UserLocation {
    /** The latitude coordinate, measured in degrees. */
    private double lat;
    /** The longitude coordinate, measured in degrees. */
    private double lng;

    /** * No-argument constructor required for Firebase object deserialization.
     */
    public UserLocation() {}

    /**
     * Constructs a new UserLocation with specified coordinates.
     * @param lat The latitude coordinate.
     * @param lng The longitude coordinate.
     */
    public UserLocation(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    /**
     * Gets the latitude coordinate.
     * @return The latitude value.
     */
    public double getLat() {
        return lat;
    }

    /**
     * Sets the latitude coordinate.
     * @param lat The new latitude value.
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * Gets the longitude coordinate.
     * @return The longitude value.
     */
    public double getLng() {
        return lng;
    }

    /**
     * Sets the longitude coordinate.
     * @param lng The new longitude value.
     */
    public void setLng(double lng) {
        this.lng = lng;
    }
}