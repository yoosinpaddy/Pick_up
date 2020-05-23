package com.yoosinpaddy.pickup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.yoosinpaddy.pickup.common.activities.Login;
import com.yoosinpaddy.pickup.common.utils.SharedPref;

public class UserTypeChooserActivity extends AppCompatActivity {

    TextView user,driver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_usertype_chooser);
        user=findViewById(R.id.user);
        driver=findViewById(R.id.driver);
        user.setOnClickListener(this::user);
        driver.setOnClickListener(this::driver);
    }
    public void user(View view){
        SharedPref.saveSharedPreference("userType","user", UserTypeChooserActivity.this);
        startActivity(new Intent(UserTypeChooserActivity.this, Login.class));
    }
    public void driver(View view){
        SharedPref.saveSharedPreference("userType","driver", UserTypeChooserActivity.this);
        startActivity(new Intent(UserTypeChooserActivity.this, Login.class));
    }
}
