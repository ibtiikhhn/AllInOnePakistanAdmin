package com.codies.allinonepakistanadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements StoreClickListener {

    public static final String TAG = "HOMEACTIVITY";
    RecyclerView storesRV;
    StoreAdapter storeAdapter;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    List<Store> storeList;

    FloatingActionButton addBT;

    AppCompatSpinner firstSpinner;
    String selectedCategory = "";
    String[] array = {"Top Stores",  "Fashion", "Grocery", "Recharge", "Baby & Kids","Medicine", "Food", "Movie", "Furniture", "Gifts", "Home Services"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        addBT = findViewById(R.id.addBT);
        storesRV = findViewById(R.id.storeRV);
        storeAdapter = new StoreAdapter(this, this);
        storesRV.setLayoutManager(new GridLayoutManager(this, 2));
        storesRV.setAdapter(storeAdapter);

        firstSpinner = findViewById(R.id.spinner2);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, array);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_background_text);
        firstSpinner.setAdapter(spinnerArrayAdapter);

        firstSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = array[position];
                getStores(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        storeList = new ArrayList<>();
//        getStores(selectedCategory);

        addBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, NewStoreActivity.class));
            }
        });
    }

    void getStores(String category) {
        databaseReference.child(category).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storeList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Store store = dataSnapshot.getValue(Store.class);
                    storeList.add(store);
                    Log.i(TAG, "onDataChange: "+store.getStoreName());
                }
                storeAdapter.setList(storeList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void storeOnClick(Store store) {
        databaseReference.child(store.storeCategory).child(store.storeName).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                Toast.makeText(HomeActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}