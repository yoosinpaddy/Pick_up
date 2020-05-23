package com.yoosinpaddy.pickup.common.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.yoosinpaddy.pickup.R;
import com.yoosinpaddy.pickup.UserTypeChooserActivity;
import com.yoosinpaddy.pickup.common.utils.SharedPref;
import com.yoosinpaddy.pickup.driver.activities.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splashactivity);
        new Handler().postDelayed(this::checkPermission, 2700);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {//Can add more as per requirement
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        } else {
            proceed();
        }
    }

    public void proceed() {

        if (SharedPref.getSharedPreference("userType", SplashActivity.this).contentEquals("")) {
            Log.e(TAG, "onCreate: usertype");
            startActivity(new Intent(SplashActivity.this, UserTypeChooserActivity.class));
            finish();

        } else if (SharedPref.getSharedPreference("userType", SplashActivity.this).contentEquals("driver")) {
            Log.e(TAG, "onCreate: driver");
            if (SharedPref.getSharedPreference("email", SplashActivity.this).contentEquals("")) {
                Log.e(TAG, "onCreate: login");
                startActivity(new Intent(SplashActivity.this, Login.class));
                finish();

            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();

            }
        } else {
            Log.e(TAG, "onCreate: user");
            if (SharedPref.getSharedPreference("email", SplashActivity.this).contentEquals("")) {
                Log.e(TAG, "onCreate: login");
                startActivity(new Intent(SplashActivity.this, Login.class));
                finish();

            } else {
                startActivity(new Intent(SplashActivity.this, com.yoosinpaddy.pickup.user.activities.MainActivity.class));
                finish();

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    proceed();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(SplashActivity.this, "Permission denied to access your location", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(SplashActivity.this::finish, 1500);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
