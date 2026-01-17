package com.example.newtripdiary;



import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST = 100;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        EditText edtSearch = findViewById(R.id.edtSearch);
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = v.getText().toString().trim();
            if (!query.isEmpty()) {
                searchNearby(query);
            }
            return true;
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        enableMyLocation();
    }

    // ENABLE BLUE DOT AND CENTER MAP
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            return;
        }

        mMap.setMyLocationEnabled(true);

        // Get last known location and move camera there
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Toast.makeText(this,
                "Selected: " + marker.getTitle(),
                Toast.LENGTH_SHORT).show();
        return false;
    }

    // SEARCH NEAR CURRENT MAP CENTER
    private void searchNearby(String keyword) {
        if (mMap == null) return;

        try {
            LatLng center = mMap.getCameraPosition().target;

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocationName(
                    keyword,
                    5,
                    center.latitude - 0.05,
                    center.longitude - 0.05,
                    center.latitude + 0.05,
                    center.longitude + 0.05
            );

            if (list == null || list.isEmpty()) {
                Toast.makeText(this,
                        "No places found nearby",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mMap.clear();

            for (Address a : list) {
                LatLng loc = new LatLng(a.getLatitude(), a.getLongitude());
                String title = a.getFeatureName() != null ? a.getFeatureName() : keyword;

                mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                        .position(loc)
                        .title(title));
            }

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(list.get(0).getLatitude(), list.get(0).getLongitude()), 14));

        } catch (Exception e) {
            Toast.makeText(this, "Search failed", Toast.LENGTH_SHORT).show();
        }
    }
}
