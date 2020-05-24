package com.yoosinpaddy.pickup.driver.activities;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yoosinpaddy.pickup.R;
import com.yoosinpaddy.pickup.common.utils.MyLocation;
import com.yoosinpaddy.pickup.common.utils.SharedPref;

import static com.yoosinpaddy.pickup.common.utils.Constants.user_base;
import static com.yoosinpaddy.pickup.driver.activities.MainActivity.CHANNEL_ID;

public class DriverService extends Service {

    Boolean isShowing = false;
    MyLocation myLocation = new MyLocation();
    private static final String TAG = "DriverService";
    Notification notification;
    public DriverService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: ");
        String input ="Updating users";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Pickup Location")
                .setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setSound(null)
                .build();

        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkGps();
                myLocation.getLocation(getApplicationContext(), locationResult);
                    /*if (myMap!=null){
                        myMap.setMyLocationEnabled(true);
                        if (!isBuilding){

                        }
                    }*/
                new Handler().postDelayed(this, 5000);
            }
        }, 1000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = new Intent(this, DriverService.class);
        serviceIntent.putExtra("inputExtra", "Updating users");
        ContextCompat.startForegroundService(this, serviceIntent);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void checkGps() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (!isShowing) {
                isShowing = true;
                buildAlertMessageNoGps();
                new Handler().postDelayed(() -> isShowing = false, 20000);
            }
        }

    }

    MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            Log.e(TAG, "gotLocation: " + location.getLatitude());
            if (isOnline()) {
                startForeground(1, notification);
                uploadLocation(location);
                /*if (myMap!=null){
                    if (!isBuilding){
                        runOnUiThread(() -> moveZoom(myMap,new LatLng(location.getLatitude(),location.getLongitude())));
                    }
                }*/
            }
        }
    };

    public void uploadLocation(Location location) {
        FirebaseUser driverUid = FirebaseAuth.getInstance().getCurrentUser();
        if (driverUid != null) {
            String dr = driverUid.getUid();
            com.yoosinpaddy.pickup.common.models.LatLng latLng = new com.yoosinpaddy.pickup.common.models.LatLng(location.getLatitude(), location.getLongitude());
            DatabaseReference mdata3 = FirebaseDatabase.getInstance().getReference().child(user_base);
            mdata3 = mdata3.child(dr).child("location");
            mdata3.setValue(latLng).addOnSuccessListener(aVoid -> Log.e(TAG, "onLocationSuccess: ")).addOnFailureListener(e -> Log.e(TAG, "onLocationFailure: "));
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
                        Toast.makeText(getApplicationContext(), "Permission denied to access your location", Toast.LENGTH_SHORT).show();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isOnline() {
        return SharedPref.isOnline(getApplicationContext());
    }

    public void isOnline(Boolean isonline) {
        SharedPref.saveSharedPreference("status", isonline, getApplicationContext());
    }
}
