package com.example.newtripdiary;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CreateTripActivity extends AppCompatActivity {

    private EditText edtTripName, edtNotes;
    private Button btnSaveTrip;

    private DatabaseReference tripsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        edtTripName = findViewById(R.id.edtTripName);
        edtNotes = findViewById(R.id.edtNotes); // New EditText for notes
        btnSaveTrip = findViewById(R.id.btnSaveTrip);

        // Firebase reference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tripsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("trips");

        btnSaveTrip.setOnClickListener(v -> saveTripToFirebase());
    }

    private void saveTripToFirebase() {
        String tripName = edtTripName.getText().toString().trim();
        String notes = edtNotes.getText().toString().trim();

        if (tripName.isEmpty()) {
            edtTripName.setError("Trip name required");
            return;
        }

        // Generate unique trip ID
        String tripId = UUID.randomUUID().toString();

        // Get current date & time
        String tripDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        // Create a trip object
        Trip trip = new Trip(tripId, tripName, notes, tripDate);

        // Save to Firebase
        tripsRef.child(tripId).setValue(trip)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateTripActivity.this, "Trip saved successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after saving
                })
                .addOnFailureListener(e -> Toast.makeText(CreateTripActivity.this, "Failed to save trip", Toast.LENGTH_SHORT).show());
    }
}
