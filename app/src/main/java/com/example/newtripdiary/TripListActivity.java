package com.example.newtripdiary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TripListActivity extends AppCompatActivity {

    private ListView listTrip;
    private Button btnCreateTrip;
    private List<String> tripIds;
    private List<String> tripNames;
    private ArrayAdapter<String> adapter;

    private DatabaseReference tripsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        listTrip = findViewById(R.id.listTrip);
        btnCreateTrip = findViewById(R.id.btnCreateTrip);

        // Firebase reference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tripsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("trips");

        tripIds = new ArrayList<>();
        tripNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tripNames);
        listTrip.setAdapter(adapter);

        loadTripsFromFirebase();

        // LIST ITEM CLICK → OPEN MyTripActivity
        listTrip.setOnItemClickListener((parent, view, position, id) -> {
            String tripId = tripIds.get(position);
            Intent i = new Intent(TripListActivity.this, MyTripActivity.class);
            i.putExtra("tripId", tripId);
            startActivity(i);
        });

        // CREATE NEW TRIP
        btnCreateTrip.setOnClickListener(v -> {
            startActivity(new Intent(TripListActivity.this, CreateTripActivity.class));
        });
    }

    private void loadTripsFromFirebase() {
        tripsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tripIds.clear();
                tripNames.clear();

                for (DataSnapshot tripSnapshot : snapshot.getChildren()) {
                    String tripId = tripSnapshot.getKey();
                    Trip trip = tripSnapshot.getValue(Trip.class);
                    if (trip != null) {
                        tripIds.add(tripId);
                        tripNames.add(trip.getTripName());
                    }
                }

                adapter.notifyDataSetChanged();

                if (tripNames.isEmpty()) {
                    Toast.makeText(TripListActivity.this, "No trips found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TripListActivity.this, "Failed to load trips", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
