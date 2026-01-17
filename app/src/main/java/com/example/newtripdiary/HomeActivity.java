package com.example.newtripdiary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private Button btnReminder, btnMaps, btnTrips, btnQRScanner, btnAbout, btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title programmatically (optional)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("       Main Page");
        }

        mAuth = FirebaseAuth.getInstance();

        // Check if user is logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        btnReminder = findViewById(R.id.btnReminder);
        btnMaps = findViewById(R.id.btnMaps);
        btnTrips = findViewById(R.id.btnTrips);
        btnQRScanner = findViewById(R.id.btnQRScanner);
        btnAbout = findViewById(R.id.btnAbout);
        btnLogout = findViewById(R.id.btnLogout);

        btnReminder.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ReminderActivity.class)));

        btnMaps.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MapsActivity.class)));

        btnTrips.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, TripListActivity.class)));

        btnAbout.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, AboutActivity.class)));

        btnQRScanner.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, QRScannerActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }
}
