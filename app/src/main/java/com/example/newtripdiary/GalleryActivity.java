package com.example.newtripdiary;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_IMAGE_VIEW = 100;

    private GridView gridView;
    private ImageAdapter adapter;
    private ArrayList<String> imagePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        gridView = findViewById(R.id.gridView);
        imagePaths = new ArrayList<>();

        // Check and request storage permission
        if (hasStoragePermission()) {
            loadImages();
            setupAdapter();
        } else {
            requestStoragePermission();
        }

        // Handle click on image thumbnails
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedImagePath = imagePaths.get(position);
            Intent intent = new Intent(GalleryActivity.this, ImageViewActivity.class);
            intent.putExtra("imagePath", selectedImagePath);
            startActivityForResult(intent, REQUEST_IMAGE_VIEW);
        });
    }

    /** Check if the app has storage permission */
    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6-12
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Pre-Android 6 automatically granted
    }

    /** Request storage permission at runtime */
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }

    /** Handle the result of permission request */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImages();
                setupAdapter();
            } else {
                Toast.makeText(this, "Storage permission is required to show images", Toast.LENGTH_LONG).show();
            }
        }
    }

    /** Load all JPG images from /Pictures/MyApp */
    private void loadImages() {
        imagePaths.clear();
        File picturesDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TripDiaryApp");

        if (picturesDir.exists() && picturesDir.isDirectory()) {
            File[] files = picturesDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getAbsolutePath().toLowerCase().endsWith(".jpg")) {
                        imagePaths.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    /** Set up the GridView adapter */
    private void setupAdapter() {
        adapter = new ImageAdapter(this, imagePaths);
        gridView.setAdapter(adapter);
    }

    /** Refresh gallery after returning from ImageViewActivity */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_VIEW && resultCode == RESULT_OK) {
            loadImages();
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }
}
