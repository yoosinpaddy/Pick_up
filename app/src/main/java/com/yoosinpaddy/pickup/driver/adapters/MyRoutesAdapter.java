package com.yoosinpaddy.pickup.driver.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yoosinpaddy.pickup.R;
import com.yoosinpaddy.pickup.common.models.Road;
import com.yoosinpaddy.pickup.driver.activities.MainActivity;

import java.util.List;

import static com.yoosinpaddy.pickup.common.utils.Constants.roads_base;

public class MyRoutesAdapter extends RecyclerView.Adapter<MyRoutesAdapter.MyRAViewHolder> {
    private List<Road> myRoads;
    private Context context;

    public MyRoutesAdapter(List<Road> myRoads, Context context) {
        this.myRoads = myRoads;
        this.context = context;
    }

    @NonNull
    @Override
    public MyRAViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.road_item, parent, false);
        return new MyRAViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRAViewHolder holder, int position) {
        Road r = myRoads.get(position);
        holder.roadName.setText(r.getRoadName());
        holder.delete.setOnClickListener(v -> deleteData(r));
        holder.edit.setOnClickListener(v -> editData(r));
    }

    @Override
    public int getItemCount() {
        return myRoads.size();
    }

    static class MyRAViewHolder extends RecyclerView.ViewHolder {
        TextView roadName;
        ImageView delete, edit;

        MyRAViewHolder(@NonNull View itemView) {
            super(itemView);
            roadName = itemView.findViewById(R.id.roadName);
            delete = itemView.findViewById(R.id.delete);
            edit = itemView.findViewById(R.id.edit);
        }
    }

    private void deleteData(Road r) {
        DatabaseReference mdatabase;
        ProgressDialog mDialog;
        mDialog = new ProgressDialog(context);
        mDialog.setTitle("Please wait");
        mdatabase = FirebaseDatabase.getInstance().getReference().child(roads_base);
        mdatabase=mdatabase.child(r.getRoadId());
        mdatabase.removeValue().addOnSuccessListener(aVoid -> {
            mDialog.dismiss();
            myRoads.remove(r);
            notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            mDialog.dismiss();
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        });
    }

    private void editData(Road r) {
        Intent intent= new Intent(context,MainActivity.class);
        intent.putExtra("road", r);
        context.startActivity(intent);

    }
}
