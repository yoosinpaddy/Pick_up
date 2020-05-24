package com.yoosinpaddy.pickup.common.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.content.ContextCompat;

import com.yoosinpaddy.pickup.driver.activities.DriverService;

public class ConnectivityChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive( Context context, Intent intent )
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Intent serviceIntent = new Intent(context, DriverService.class);
            serviceIntent.putExtra("inputExtra", "Updating users");
            ContextCompat.startForegroundService(context, serviceIntent);
        } else {

            Intent serviceIntent = new Intent(context, DriverService.class);
            serviceIntent.putExtra("inputExtra", "Updating users");
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
