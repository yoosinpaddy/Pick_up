package com.yoosinpaddy.pickup.driver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yoosinpaddy.pickup.R;
import com.yoosinpaddy.pickup.common.activities.Login;
import com.yoosinpaddy.pickup.common.models.Road;
import com.yoosinpaddy.pickup.driver.adapters.MyRoutesAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.yoosinpaddy.pickup.common.utils.Constants.roads_base;

public class MyRoutes extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Road> myRoads=new ArrayList<>();
    FirebaseAuth firebaseAuth;
    DatabaseReference mdatabase;
    MyRoutesAdapter myRoutesAdapter;
    private static final String TAG = "MyRoutes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_routes);
        recyclerView=findViewById(R.id.routesRecycler);
        firebaseAuth=FirebaseAuth.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRoutesAdapter=new MyRoutesAdapter(myRoads,MyRoutes.this);
        recyclerView.setAdapter(myRoutesAdapter);
        getRoutes();
    }
    public void getRoutes(){
        if (firebaseAuth.getCurrentUser()==null){
            Toast.makeText(this, "You must login to access this service", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> {
                startActivity(new Intent(MyRoutes.this, Login.class));
                MyRoutes.this.finish();
            },2000);
        }else{

            String uId=firebaseAuth.getCurrentUser().getUid();
            mdatabase = FirebaseDatabase.getInstance().getReference().child(roads_base);
            mdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        for (DataSnapshot a:dataSnapshot.getChildren()) {
                            Log.e(TAG, "onDataChange: "+a.getValue() );
                            Road r=a.getValue(Road.class);
                            if (r != null && r.getDriverUid().contentEquals(uId)) {
                                myRoads.add(r);
                                myRoutesAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MyRoutes.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
