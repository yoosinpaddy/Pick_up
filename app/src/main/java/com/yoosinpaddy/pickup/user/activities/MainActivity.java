package com.yoosinpaddy.pickup.user.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yoosinpaddy.pickup.R;
import com.yoosinpaddy.pickup.common.activities.Login;
import com.yoosinpaddy.pickup.common.activities.SplashActivity;
import com.yoosinpaddy.pickup.common.models.Road;
import com.yoosinpaddy.pickup.common.utils.MyLocation;
import com.yoosinpaddy.pickup.common.utils.SharedPref;

import java.util.ArrayList;
import java.util.List;

import static com.yoosinpaddy.pickup.common.utils.Constants.roads_base;
import static com.yoosinpaddy.pickup.common.utils.Constants.user_base;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    List<LatLng> singleAddPath = new ArrayList<>();
    AlertDialog.Builder builder;
    Boolean isFtime = true;
    Boolean isShowing = false;
    Boolean isLocationEnabled = false;
    View topPanel, bottomPanel;
    FloatingActionButton addRoute;
    GoogleMap myMap;
    DatabaseReference mdatabase, mdatabase2;
    ProgressDialog mDialog;
    private static final String TAG = "MainActivity";
    Road r;
    LatLng driverLocation;


    Marker userLocationMarker;
    Circle userLocationAccuracyCircle;
    MyLocation myLocation = new MyLocation();
    String driverId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_main);
        checkPermission();
        mDialog = new ProgressDialog(this);
        mdatabase = FirebaseDatabase.getInstance().getReference().child(roads_base);
        mdatabase2 = FirebaseDatabase.getInstance().getReference().child(user_base);
        topPanel = findViewById(R.id.topPanel);
        bottomPanel = findViewById(R.id.bottomPanel);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        builder = new AlertDialog.Builder(this);
        if (getIntent().getExtras() != null) {
            r = (Road) getIntent().getExtras().getSerializable("road");
            ((EditText) findViewById(R.id.name)).setText(r.getRoadName());
            singleAddPath = r.getPolylines();
        } else {
            startActivity(new Intent(MainActivity.this, AllRoutes.class));
            this.finish();
        }
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }


    public void logout(View v1) {
        (new AlertDialog.Builder(this)).setMessage("Are you sure you want to logout?").setPositiveButton("Yes", (dialog, which) -> {
            SharedPref.deleteAllSharedPreference(MainActivity.this);
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, SplashActivity.class));
            MainActivity.this.finish();

        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
    }

    public void viewRoute(View v) {
        startActivity(new Intent(MainActivity.this, AllRoutes.class));
    }

    public void reDrawPoints() {
        if (myMap != null) {
            myMap.clear();
            getLine(singleAddPath);
            if (driverLocation != null) {
                setUserLocationMarker(new LatLng(driverLocation.latitude, driverLocation.longitude));
            }
        } else {
            Toast.makeText(this, "Map not ready", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        myMap = map;
        LatLng sydney = new LatLng(-34, 151);
        zoom(map, sydney);
        myMap.setOnMapClickListener(latLng -> {
            /*if (isBuilding){
                singleAddPath.add(latLng);
                moveZoom(myMap,latLng);
                new Handler().postDelayed(this::reDrawPoints,1500);
            }*/
        });
        if (singleAddPath != null && singleAddPath.size() > 0) {
            reDrawPoints();
        }
    }

    private void setUserLocationMarker(LatLng latLng) {
        GoogleMap mMap = myMap;


        if (userLocationMarker == null) {
            //Create a new marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redcar));
//            markerOptions.rotation(location.getBearing());
            markerOptions.anchor((float) 0.5, (float) 0.5);
            userLocationMarker = mMap.addMarker(markerOptions);
            if (isFtime) {
                isFtime = false;
                zoom2(mMap, latLng);
            }
        } else {
            //use the previously created marker
            userLocationMarker.setPosition(latLng);
//            userLocationMarker.setRotation(location.getBearing());
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

            if (isFtime) {
                isFtime = false;
                zoom2(mMap, latLng);
            }
        }

        if (userLocationAccuracyCircle == null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.strokeWidth(4);
            circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
            circleOptions.fillColor(Color.argb(32, 255, 0, 0));
//            circleOptions.radius(location.getAccuracy());
            circleOptions.radius(100.0);
            userLocationAccuracyCircle = mMap.addCircle(circleOptions);
        } else {
            userLocationAccuracyCircle.setCenter(latLng);
//            userLocationAccuracyCircle.setRadius(location.getAccuracy());
            userLocationAccuracyCircle.setRadius(100.0);
        }
    }

    public void getLine(List<LatLng> path) {
        Polyline polyline = myMap.addPolyline((new PolylineOptions())
                .clickable(true)
                .addAll(path));

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(3);
        polyline.setColor(getResources().getColor(R.color.colorBlack));
        polyline.setJointType(JointType.ROUND);

        LatLng sydney = path.get(0);
        myMap.addMarker(new MarkerOptions().position(sydney).title("This is the start point"));
    }

    public void zoom(GoogleMap map, LatLng sydney) {

// Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());

// Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 3000, null);

// Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(sydney)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void zoom2(GoogleMap map, LatLng sydney) {

// Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());

// Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(17), 3000, null);

// Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(sydney)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void getDriverLocation() {
        DatabaseReference mdata3 = FirebaseDatabase.getInstance().getReference().child(user_base);
        mdata3 = mdata3.child(r.getDriverUid()).child("location");
        mdata3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                com.yoosinpaddy.pickup.common.models.LatLng latLng = dataSnapshot.getValue(com.yoosinpaddy.pickup.common.models.LatLng.class);
                if (latLng != null) {
                    driverLocation = new LatLng(latLng.getLatitude(), latLng.getLongitude());
                    setUserLocationMarker(new LatLng(driverLocation.latitude, driverLocation.longitude));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getDriverLocation();

                new Handler().postDelayed(this, 5000);
            }
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationEnabled = true;
                    checkGps();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    isLocationEnabled = false;
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to access your location", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(MainActivity.this::finish, 1500);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {//Can add more as per requirement
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        } else {
            isLocationEnabled = true;
            checkGps();
        }
    }

    public void checkGps() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (!isShowing) {
                isShowing = true;
                buildAlertMessageNoGps();
                new Handler().postDelayed(() -> isShowing = false, 20000);
            }
        }

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        Toast.makeText(MainActivity.this, "Permission denied to access your location", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(MainActivity.this::finish, 1500);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
