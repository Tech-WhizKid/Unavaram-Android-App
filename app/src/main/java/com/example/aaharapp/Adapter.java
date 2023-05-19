package com.example.aaharapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.uviewholder> {

    ArrayList<model> datalist;
    FirebaseAuth fAuth= FirebaseAuth.getInstance();
    public String userID = fAuth.getCurrentUser().getUid();
    public String uid;
    FirebaseFirestore db;

    CollectionReference collectionRef = FirebaseFirestore.getInstance().collection("Backup");

    DocumentReference dcref = collectionRef.document();

    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    Query query = collectionRef.whereEqualTo("userId", userId);
    public Adapter(ArrayList<model> datalist) {
        this.datalist = datalist;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Adapter.uviewholder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sigro, parent, false);
        return new Adapter.uviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.uviewholder holder, @SuppressLint("RecyclerView") int position) {
        holder.tname.setText(datalist.get(position).getName());
        holder.ttype.setText(datalist.get(position).getType());
        holder.tdescription.setText(datalist.get(position).getDescription());

    }



    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class uviewholder extends RecyclerView.ViewHolder
    {
        TextView tname,ttype,tdescription;
        Button delet;
        public uviewholder(@NonNull View itemView) {
            super(itemView);
            tname = itemView.findViewById(R.id.uname);
            ttype = itemView.findViewById(R.id.utype);
            tdescription = itemView.findViewById(R.id.udescription);
        }
    }
}
