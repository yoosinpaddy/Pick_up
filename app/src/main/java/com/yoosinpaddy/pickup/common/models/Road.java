package com.yoosinpaddy.pickup.common.models;




import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Road implements Serializable {
    String roadName;
    List<com.yoosinpaddy.pickup.common.models.LatLng> polylines;
    String driverUid;
    String roadId;

    public Road() {
    }
    // from firebase
    public Road(String roadName, List<com.yoosinpaddy.pickup.common.models.LatLng> polylines, String driverUid, String roadId) {
        this.roadName = roadName;
        this.driverUid = driverUid;
        this.roadId = roadId;
        this.polylines = polylines;
    }

    //to this
    private List<com.yoosinpaddy.pickup.common.models.LatLng> generate1(List<com.google.android.gms.maps.model.LatLng> polylines) {
        List<com.yoosinpaddy.pickup.common.models.LatLng> latLngs=new ArrayList<>();
        for (int i=0;i<polylines.size();i++){
            latLngs.add(new com.yoosinpaddy.pickup.common.models.LatLng(polylines.get(i).latitude,polylines.get(i).longitude));
        }
        return latLngs;
    }

    //to activity
    private List<com.google.android.gms.maps.model.LatLng> generate(List<com.yoosinpaddy.pickup.common.models.LatLng> polylines) {
        List<com.google.android.gms.maps.model.LatLng> latLngs=new ArrayList<>();
        for (int i=0;i<polylines.size();i++){
            latLngs.add(new com.google.android.gms.maps.model.LatLng(polylines.get(i).latitude,polylines.get(i).longitude));
        }
        return latLngs;
    }
    //from Main activity
    public Road(List<com.google.android.gms.maps.model.LatLng> polylines,  String driverUid, String roadId,String roadName) {
        this.roadName = roadName;
        this.polylines = generate1(polylines);
        this.driverUid = driverUid;
        this.roadId = roadId;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public List<com.google.android.gms.maps.model.LatLng> getPolylines() {
        return generate(polylines);
    }

    public void setPolylines(List<LatLng> polylines) {
        this.polylines = polylines;
    }

    public String getDriverUid() {
        return driverUid;
    }

    public void setDriverUid(String driverUid) {
        this.driverUid = driverUid;
    }

    public String getRoadId() {
        return roadId;
    }

    public void setRoadId(String roadId) {
        this.roadId = roadId;
    }
}
