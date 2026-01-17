package com.example.newtripdiary;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRActivity extends AppCompatActivity {

    private ImageView qrCodeIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qractivity);

        qrCodeIV = findViewById(R.id.idIVQrcode);

        // Get trip data from intent
        String tripName = getIntent().getStringExtra("tripName");
        String note = getIntent().getStringExtra("note");

        if (tripName == null) tripName = "";
        if (note == null) note = "";

        // Combine tripName and note into a single string
        String qrData = "Trip: " + tripName + "\nNotes: " + note;

        // Generate QR code automatically
        generateQRCode(qrData);
    }

    private void generateQRCode(String text) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeIV.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}
