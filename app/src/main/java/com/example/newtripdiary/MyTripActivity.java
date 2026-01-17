package com.example.newtripdiary;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MyTripActivity extends AppCompatActivity {

    private String tripId;

    private EditText edtTripName, edtNote;
    private TextView txtDate;
    private Button btnPicture, btnSave, btnQR, btnGallery, btnDeleteTrip;

    private static final int REQUEST_CAPTURE_IMAGE = 101;
    private Uri photoUri;

    private DatabaseReference tripRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trip);

        tripId = getIntent().getStringExtra("tripId");
        if (tripId == null || tripId.isEmpty()) {
            Toast.makeText(this, "Trip not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtTripName = findViewById(R.id.edtTripName);
        edtNote = findViewById(R.id.edtNote);
        txtDate = findViewById(R.id.txtLocation); // you can rename to txtDate if needed
        btnPicture = findViewById(R.id.btnPicture);
        btnSave = findViewById(R.id.btnSave);
        btnQR = findViewById(R.id.btnQR);
        btnGallery = findViewById(R.id.btnGallery);
        btnDeleteTrip = findViewById(R.id.btnDeleteTrip);

        // Firebase reference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tripRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("trips")
                .child(tripId);

        loadTripDataFromFirebase();

        // 📸 TAKE PHOTO
        btnPicture.setOnClickListener(v -> takePhoto());

        // 🔳 QR button
        btnQR.setOnClickListener(v -> {
            String tripName = edtTripName.getText().toString().trim();
            String note = edtNote.getText().toString().trim();

            Intent i = new Intent(MyTripActivity.this, QRActivity.class);
            i.putExtra("tripName", tripName);
            i.putExtra("note", note);
            startActivity(i);
        });


        // 🖼 GALLERY button
        btnGallery.setOnClickListener(v -> {
            Intent i = new Intent(MyTripActivity.this, GalleryActivity.class);
            startActivity(i);
        });

        // 💾 SAVE TRIP
        btnSave.setOnClickListener(v -> saveTripToFirebase());

        // DELETE TRIP
        btnDeleteTrip.setOnClickListener(v -> confirmAndDeleteTrip());
    }

    private void loadTripDataFromFirebase() {
        tripRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Trip trip = snapshot.getValue(Trip.class);
                if (trip != null) {
                    edtTripName.setText(trip.getTripName());
                    edtNote.setText(trip.getNote());
                    txtDate.setText(trip.getTripDate());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MyTripActivity.this, "Failed to load trip data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTripToFirebase() {
        String name = edtTripName.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        if (name.isEmpty()) {
            edtTripName.setError("Trip name required");
            return;
        }

        String tripDate = txtDate.getText().toString();

        Trip trip = new Trip(tripId, name, note, tripDate);
        tripRef.setValue(trip).addOnSuccessListener(aVoid ->
                Toast.makeText(MyTripActivity.this, "Trip saved successfully", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(MyTripActivity.this, "Failed to save trip", Toast.LENGTH_SHORT).show()
        );
    }

    private void confirmAndDeleteTrip() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Trip")
                .setMessage("Are you sure you want to delete this trip? All notes and images will be removed.")
                .setPositiveButton("Yes", (dialog, which) -> deleteTripFromFirebase())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTripFromFirebase() {
        tripRef.removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(MyTripActivity.this, "Trip deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(MyTripActivity.this, "Failed to delete trip", Toast.LENGTH_SHORT).show()
        );
    }

    private void takePhoto() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "IMG_" + timeStamp + ".jpg";

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TripDiaryApp");

            photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (photoUri == null) throw new IOException("Failed to create MediaStore entry");

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create file for photo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            Set<String> images = new HashSet<>();
            images.add(photoUri.toString());
            getSharedPreferences("TRIP_IMAGES", MODE_PRIVATE).edit().putStringSet(tripId, images).apply();
            Toast.makeText(this, "Photo saved in TripDiaryApp folder", Toast.LENGTH_SHORT).show();
        }
    }
}
