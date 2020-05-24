package com.yoosinpaddy.pickup.driver.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yoosinpaddy.pickup.R;
import com.yoosinpaddy.pickup.common.activities.Login;
import com.yoosinpaddy.pickup.common.activities.SplashActivity;
import com.yoosinpaddy.pickup.common.models.Road;
import com.yoosinpaddy.pickup.common.utils.MyLocation;
import com.yoosinpaddy.pickup.common.utils.SharedPref;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.yoosinpaddy.pickup.common.utils.Constants.roads_base;
import static com.yoosinpaddy.pickup.common.utils.Constants.user_base;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    List<LatLng> singleAddPath = new ArrayList<>();
    AlertDialog.Builder builder;
    Boolean isBuilding = false;
    Boolean isShowing = false;
    Boolean isLocationEnabled = false;
    View topPanel, bottomPanel;
    FloatingActionButton addRoute;
    GoogleMap myMap;
    DatabaseReference mdatabase,mdatabase2;
    ProgressDialog mDialog;
    private static final String TAG = "MainActivity";
    Road r;
    public static final String CHANNEL_ID = "driverServiceChannel";
    MyLocation myLocation = new MyLocation();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_driver_main);
        checkPermission();
        createNotificationChannel();
        mDialog = new ProgressDialog(this);
        mdatabase = FirebaseDatabase.getInstance().getReference().child(roads_base);
        mdatabase2 = FirebaseDatabase.getInstance().getReference().child(user_base);
        topPanel = findViewById(R.id.topPanel);
        bottomPanel = findViewById(R.id.bottomPanel);
        addRoute = findViewById(R.id.addRoute);
        topPanel.setVisibility(View.GONE);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        builder = new AlertDialog.Builder(this);
        addRoute.setOnClickListener(this::addRoute);
        if (getIntent().getExtras()!=null){
            r=(Road) getIntent().getExtras().getSerializable("road");
            ((EditText)findViewById(R.id.name)).setText(r.getRoadName());
            singleAddPath=r.getPolylines();
        }
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        startService();
    }

    public void addRoute(View v) {
        if (isBuilding) {
            Toast.makeText(this, "Please finish adding the current road", Toast.LENGTH_SHORT).show();
        } else {
            builder.setMessage("Start building a road?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                isBuilding = true;
                singleAddPath = new ArrayList<>();
                topPanel.setVisibility(View.VISIBLE);
            }).setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
                topPanel.setVisibility(View.GONE);
            });
            builder.show();
        }
    }
    public void onlineOfflineRoute(View v1) {
        if (isOnline()) {
            Toast.makeText(this, "You are now offline", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((ImageView)v1).setImageDrawable(getDrawable(R.drawable.ic_nature_people_dull_24dp));
                isOnline(false);
            }
        } else {
            Toast.makeText(this, "You are now online", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((ImageView)v1).setImageDrawable(getDrawable(R.drawable.ic_nature_people_black_24dp));
                isOnline(true);
            }
        }
    }
    public boolean isOnline(){
        return SharedPref.isOnline(MainActivity.this);
    }
    public void isOnline(Boolean isonline){
        SharedPref.saveSharedPreference("status",isonline,MainActivity.this);
    }
    public void viewRoute(View v) {
        startActivity(new Intent(MainActivity.this, MyRoutes.class));
    }
    public void finishRoute(View v) {
        if (isBuilding) {
            if (singleAddPath.size() <= 1) {
                Toast.makeText(this, "Please add at least two points", Toast.LENGTH_SHORT).show();
            } else {
                savePoints();
            }
        } else {
            topPanel.setVisibility(View.GONE);
        }
    }
    private void savePoints() {
        Log.e(TAG, "savePoints: saving" );
        if(((EditText)findViewById(R.id.name)).getText().toString().trim().contentEquals("")){
            ((EditText)findViewById(R.id.name)).setError("Please fill the name");
            ((EditText)findViewById(R.id.name)).requestFocus();
        }else if(singleAddPath.size()<=1){
            Toast.makeText(this, "kindly ad at least 2 points", Toast.LENGTH_SHORT).show();
        }else{
            String roadName=((EditText)findViewById(R.id.name)).getText().toString().trim();
            FirebaseUser driverUid= FirebaseAuth.getInstance().getCurrentUser();
            if (driverUid!=null){
                String id;
                if (r==null){
                    id=String.valueOf(Calendar.getInstance().getTimeInMillis())+driverUid.getUid();
                }else {
                    id=r.getRoadId();
                }
                r=new Road(singleAddPath,driverUid.getUid(),id,roadName);
                uploadRoad(r,id);
            }else{
                Toast.makeText(this, "You must login to access this service", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, Login.class));
                        MainActivity.this.finish();
                    }
                },2000);
            }
        }
    }

    public void undoRoute(View v) {
        if (isBuilding) {
            if (singleAddPath.size() <= 1) {
                topPanel.setVisibility(View.GONE);
                isBuilding = false;
                singleAddPath = new ArrayList<>();
                myMap.clear();
            } else {
                singleAddPath.remove(singleAddPath.size() - 1);
                reDrawPoints();
                zoom(myMap,singleAddPath.get(singleAddPath.size()-1));
            }
        } else {
            topPanel.setVisibility(View.GONE);
        }
    }

    public void reDrawPoints() {
        if (myMap != null) {
            myMap.clear();
            getLine(singleAddPath);
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
            if (isBuilding){
                singleAddPath.add(latLng);
                moveZoom(myMap,latLng);
                new Handler().postDelayed(this::reDrawPoints,1500);
            }
        });
        if (singleAddPath!=null&&singleAddPath.size()>0){
            isBuilding=true;
            topPanel.setVisibility(View.VISIBLE);
            reDrawPoints();
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

    public void moveZoom(GoogleMap map, LatLng sydney) {

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());

        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 1500, null);

        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(sydney)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

    public void uploadRoad(Road road,String id){
        mDialog.setTitle("Saving...");
        mDialog.show();
        mdatabase=mdatabase.child(id);
        mdatabase.setValue(road).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDialog.dismiss();
                topPanel.setVisibility(View.GONE);
                (new AlertDialog.Builder(MainActivity.this)).setMessage("Success").setCancelable(true).show();
                if (myMap!=null){
                    myMap.clear();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                (new AlertDialog.Builder(MainActivity.this)).setMessage("Failed:"+e.getMessage()).setCancelable(true).show();
                if (myMap!=null){
                    myMap.clear();
                }

            }
        });
    }
    public void uploadLocation(Location location){
//        now done on a service
        /*FirebaseUser driverUid= FirebaseAuth.getInstance().getCurrentUser();
        if (driverUid!=null){
            String dr=driverUid.getUid();
            com.yoosinpaddy.pickup.common.models.LatLng latLng=new com.yoosinpaddy.pickup.common.models.LatLng(location.getLatitude(),location.getLongitude());
            DatabaseReference mdata3=FirebaseDatabase.getInstance().getReference().child(user_base);
            mdata3=mdata3.child(dr).child("location");
            mdata3.setValue(latLng).addOnSuccessListener(aVoid -> Log.e(TAG, "onLocationSuccess: " )).addOnFailureListener(e -> Log.e(TAG, "onLocationFailure: " ));
        }*/

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
                if (isLocationEnabled){
                    checkGps();
                    myLocation.getLocation(MainActivity.this, locationResult);
                    if (myMap!=null){
                        myMap.setMyLocationEnabled(true);
                        if (!isBuilding){

                        }
                    }
                }
                new Handler().postDelayed(this,5000);
            }
        },200);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationEnabled=true;
                    checkGps();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    isLocationEnabled=false;
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
            isLocationEnabled=true;
            checkGps();
        }
    }
    public void checkGps() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            if (!isShowing){
                isShowing=true;
                buildAlertMessageNoGps();
                new Handler().postDelayed(() -> isShowing=false,20000);
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
    MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
        @Override
        public void gotLocation(Location location){
            Log.e(TAG, "gotLocation: "+location.getLatitude() );
            if (isOnline()){
                uploadLocation(location);
                if (myMap!=null){
                    if (!isBuilding){
                        runOnUiThread(() -> moveZoom(myMap,new LatLng(location.getLatitude(),location.getLongitude())));
                    }
                }
            }
        }
    };

    public void logout(View v1) {
        (new AlertDialog.Builder(this)).setMessage("Are you sure you want to logout?").setPositiveButton("Yes", (dialog, which) -> {
            SharedPref.deleteAllSharedPreference(MainActivity.this);
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, SplashActivity.class));
            MainActivity.this.finish();

        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Driver Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setSound(null,null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
    public void startService() {
        Intent serviceIntent = new Intent(this, DriverService.class);
        serviceIntent.putExtra("inputExtra", "Updating users");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, DriverService.class);
        stopService(serviceIntent);
    }
}
