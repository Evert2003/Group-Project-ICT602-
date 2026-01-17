package com.example.newtripdiary;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;

public class ImageViewActivity extends AppCompatActivity {

    ImageView fullImageView;
    Button deleteButton;
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        fullImageView = findViewById(R.id.fullImageView);
        deleteButton = findViewById(R.id.deleteButton);

        imagePath = getIntent().getStringExtra("imagePath");

        if (imagePath != null) {
            displayImageWithRotation(imagePath);
        }

        deleteButton.setOnClickListener(v -> confirmAndDeleteImage());
    }

    /**
     * Efficiently load the bitmap and rotate based on EXIF.
     */
    private void displayImageWithRotation(String path) {
        Bitmap bitmap = decodeSampledBitmapFromFile(path, 1080, 1080); // downsample large images
        String timestamp = "Unknown";

        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90); break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180); break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270); break;
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);

            String exifDate = exif.getAttribute(ExifInterface.TAG_DATETIME);
            if (exifDate != null) {
                timestamp = exifDate.replace(":", "-").replaceFirst(" ", " @ ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        fullImageView.setImageBitmap(bitmap);
        TextView timestampView = findViewById(R.id.imageTimestamp);
        timestampView.setText("Taken on: " + timestamp);
    }

    /**
     * Downsample image to avoid memory issues
     */
    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        // First decode bounds only
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void confirmAndDeleteImage() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", (dialog, which) -> deleteImage())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage() {
        File file = new File(imagePath);
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
