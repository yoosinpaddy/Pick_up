package com.yoosinpaddy.pickup.common.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoosinpaddy.pickup.R;
import com.yoosinpaddy.pickup.UserTypeChooserActivity;
import com.yoosinpaddy.pickup.common.utils.SharedPref;
import com.yoosinpaddy.pickup.driver.activities.MainActivity;
import com.yoosinpaddy.pickup.user.activities.AllRoutes;

public class Login extends AppCompatActivity {

    EditText emailEt,passwordEt;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListner;
    FirebaseUser mUser;
    String email, password;
    ProgressDialog dialog;
    private static final String TAG = "Login";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailEt=findViewById(R.id.emailEt);
        passwordEt=findViewById(R.id.passwordEt);

        dialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuthListner = firebaseAuth -> {
            if (mUser != null) {
                if (SharedPref.getSharedPreference("userType",Login.this).contentEquals("driver")){
                    startActivity(new Intent(Login.this, MainActivity.class));
                }else {
                    startActivity(new Intent(Login.this, AllRoutes.class));
                }
            }
            else
            {
                startActivity(new Intent(Login.this, UserTypeChooserActivity.class));
                Log.d(TAG,"AuthStateChanged:Logout");
            }

        };
    }
    public void login(View view){
        if (validate()){

            email = emailEt.getText().toString().trim();
            password = passwordEt.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, "Enter the correct Email", Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Enter the correct password", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.setMessage("Loging in please wait...");
            dialog.setIndeterminate(true);
            dialog.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    dialog.dismiss();

                    Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();

                } else {

                    dialog.dismiss();

                    SharedPref.saveSharedPreference("email",task.getResult().getUser().getEmail(),Login.this);
                    if (SharedPref.getSharedPreference("userType",Login.this).contentEquals("driver")){
                        startActivity(new Intent(Login.this, MainActivity.class));
                    }else {
                        startActivity(new Intent(Login.this, com.yoosinpaddy.pickup.user.activities.MainActivity.class));
                    }
                }
            });
        }
    }
    public void register(View view){
        startActivity(new Intent(this, Register.class));
    }
    private boolean validate(){
         if (emailEt.getText().toString().trim().contentEquals("")){
            emailEt.setError("Cannot be empty");
            emailEt.requestFocus();
            return false;
        }else if (passwordEt.getText().toString().trim().contentEquals("")){
            passwordEt.setError("Cannot be empty");
            passwordEt.requestFocus();
            return false;
        }else if (passwordEt.getText().toString().trim().length()<5){
            passwordEt.setError("Must be greater than 5");
            passwordEt.requestFocus();
            return false;
        }else if (!emailEt.getText().toString().contains("@")){
            emailEt.setError("Not a valid email");
            emailEt.requestFocus();
            return false;
        }
        return true;
    }
}
