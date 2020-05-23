package com.yoosinpaddy.pickup.common.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yoosinpaddy.pickup.UserTypeChooserActivity;
import com.yoosinpaddy.pickup.R;
import com.yoosinpaddy.pickup.common.models.User;
import com.yoosinpaddy.pickup.common.utils.SharedPref;
import com.yoosinpaddy.pickup.driver.activities.MainActivity;
import com.yoosinpaddy.pickup.user.activities.AllRoutes;

import java.util.Date;

import static com.yoosinpaddy.pickup.common.utils.Constants.user_base;

public class Register extends AppCompatActivity {


    EditText usernameEt,emailEt,passwordEt;
    FirebaseAuth mAuth;
    DatabaseReference mdatabase;
    private static final String TAG = "Register";
    String Name,Email,Password;
    ProgressDialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        usernameEt=findViewById(R.id.usernameEt);
        emailEt=findViewById(R.id.emailEt);
        passwordEt=findViewById(R.id.passwordEt);

        // for authentication using FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(this);
        mdatabase = FirebaseDatabase.getInstance().getReference().child(user_base);

    }

    public void login(View view) {
        startActivity(new Intent(this, Login.class));
    }
    public void register(View view) {
        mDialog.setTitle("Registering");
        mDialog.show();
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        /*if (user!=null){
            if (validate()){
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child(user_base).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Log.e(TAG, "onDataChange: it exists" );
                            OnAuth(user);
                        } else {
                            Log.e(TAG, "onDataChange: doesnt exist" );
                            mAuth.createUserWithEmailAndPassword(emailEt.getText().toString(),passwordEt.getText().toString()).addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    mDialog.dismiss();
                                    OnAuth(task.getResult().getUser());
                                }else{
                                    Log.e(TAG, "onComplete: "+task.getResult().toString() );
                                    Toast.makeText(Register.this,"error on creating user",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }else {*/

            mAuth.createUserWithEmailAndPassword(emailEt.getText().toString(),passwordEt.getText().toString()).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
//                        sendEmailVerification();
                    mDialog.dismiss();
                    OnAuth(task.getResult().getUser());
                    mDialog.dismiss();
//                        mAuth.signOut();
                }else{
                    mDialog.dismiss();
                    Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(Register.this,"error on creating user",Toast.LENGTH_SHORT).show();
                }
            });
//        }
    }
    private void OnAuth(FirebaseUser user) {

        createAnewUser(user.getUid());
    }

    private void createAnewUser(String uid) {
        User user = BuildNewUser(uid);
        mdatabase.child(uid).setValue(user).addOnCompleteListener(task -> {
            SharedPref.saveSharedPreference("email",user.getEmail(),Register.this);
            if (SharedPref.getSharedPreference("userType",Register.this).contentEquals("driver")){
                startActivity(new Intent(Register.this, MainActivity.class));
            }else {
                startActivity(new Intent(Register.this, AllRoutes.class));
            }
        }).addOnFailureListener(e -> Toast.makeText(Register.this, "Something went wrong", Toast.LENGTH_SHORT).show());
    }

    private User BuildNewUser(String uid){
        return new User(
                getDisplayName(),
                getUserEmail(),
                new Date().getTime(),
                uid
        );
    }

    public String getDisplayName() {
        return Name;
    }

    public String getUserEmail() {
        return Email;
    }
    private boolean validate() {
        if (usernameEt.getText().toString().trim().contentEquals("")) {
            usernameEt.setError("Cannot be empty");
            usernameEt.requestFocus();
            return false;
        } else if (emailEt.getText().toString().trim().contentEquals("")) {
            emailEt.setError("Cannot be empty");
            emailEt.requestFocus();
            return false;
        } else if (passwordEt.getText().toString().trim().contentEquals("")) {
            passwordEt.setError("Cannot be empty");
            passwordEt.requestFocus();
            return false;
        } else if (passwordEt.getText().toString().trim().length() < 5) {
            passwordEt.setError("Must be greater than 5");
            passwordEt.requestFocus();
            return false;
        } else if (usernameEt.getText().toString().trim().length() < 5) {
            usernameEt.setError("Not a valid name");
            usernameEt.requestFocus();
            return false;
        } else if (!emailEt.getText().toString().contains("@")) {
            emailEt.setError("Not a valid email");
            emailEt.requestFocus();
            return false;
        }
        return true;

    }



}
