package com.yoosinpaddy.pickup.user.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yoosinpaddy.pickup.R;
import com.yoosinpaddy.pickup.common.activities.Login;
import com.yoosinpaddy.pickup.common.models.Road;

import java.util.ArrayList;
import java.util.List;

import static com.yoosinpaddy.pickup.common.utils.Constants.roads_base;

public class AllRoutes extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Road> myRoads=new ArrayList<>();
    FirebaseAuth firebaseAuth;
    DatabaseReference mdatabase;
    AllRoutesAdapter myRoutesAdapter;
    private static final String TAG = "MyRoutes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_routes);
        if (getActionBar()!=null)
            getActionBar().setTitle("Select the route");
        recyclerView=findViewById(R.id.routesRecycler);
        firebaseAuth=FirebaseAuth.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRoutesAdapter=new AllRoutesAdapter(myRoads, AllRoutes.this);
        recyclerView.setAdapter(myRoutesAdapter);
        getRoutes();
    }
    public void getRoutes(){
        if (firebaseAuth.getCurrentUser()==null){
            Toast.makeText(this, "You must login to access this service", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> {
                startActivity(new Intent(AllRoutes.this, Login.class));
                AllRoutes.this.finish();
            },2000);
        }else{
            mdatabase = FirebaseDatabase.getInstance().getReference().child(roads_base);
            mdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        for (DataSnapshot a:dataSnapshot.getChildren()) {
                            Log.e(TAG, "onDataChange: "+a.getValue() );
                            Road r=a.getValue(Road.class);
                            if (r != null) {
                                myRoads.add(r);
                                myRoutesAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(AllRoutes.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
