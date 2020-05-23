package com.yoosinpaddy.pickup.common.models;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class LatLng implements Serializable {
    double latitude,longitude;
    @Nullable
    String id;

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
