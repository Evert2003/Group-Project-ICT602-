package com.example.newtripdiary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScannerActivity extends AppCompatActivity {

    private TextView txtResult;
    private Button btnScanQR;

    private final ActivityResultLauncher<Intent> qrLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                IntentResult intentResult = IntentIntegrator.parseActivityResult(
                        result.getResultCode(),
                        result.getData()
                );
                if (intentResult != null) {
                    String contents = intentResult.getContents();
                    if (contents != null) {
                        txtResult.setText(contents);
                    } else {
                        Toast.makeText(QRScannerActivity.this, "Scan canceled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        txtResult = findViewById(R.id.txtResult);
        btnScanQR = findViewById(R.id.btnScanQR);

        btnScanQR.setOnClickListener(v -> startQRScan());
    }

    private void startQRScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a QR code");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.setBarcodeImageEnabled(true);
        qrLauncher.launch(integrator.createScanIntent());
    }
}
