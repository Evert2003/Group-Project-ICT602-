package com.example.newtripdiary;



import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

    private TextView textAlarmPrompt;
    private Button buttonstartSetDialog;
    private Button button;
    private EditText message;

    private static final int ALARM_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reminder);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textAlarmPrompt = findViewById(R.id.alarmprompt);
        buttonstartSetDialog = findViewById(R.id.startSetDialog);
        message = findViewById(R.id.editText);
        button = findViewById(R.id.button);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        buttonstartSetDialog.setOnClickListener(v -> {
            textAlarmPrompt.setText("");
            openDateTimePicker(); // Start with date picker
        });

        button.setOnClickListener(arg0 -> {
            new AlertDialog.Builder(ReminderActivity.this)
                    .setTitle("Back to Main Page?")
                    .setMessage("Click yes to continue!")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> finish())
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel())
                    .create()
                    .show();
        });
    }

    // Step 1: Open date picker
    private void openDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ReminderActivity.this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Step 2: Open time picker after date is chosen
                    openTimePickerDialog(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.setTitle("Select Date");
        datePickerDialog.show();
    }

    // Step 2: Time picker for the selected date
    private void openTimePickerDialog(Calendar selectedDate) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                ReminderActivity.this,
                (view, hourOfDay, minute) -> {
                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDate.set(Calendar.MINUTE, minute);
                    selectedDate.set(Calendar.SECOND, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);

                    // If the selected date-time is in the past, add 1 day
                    if (selectedDate.compareTo(Calendar.getInstance()) <= 0) {
                        selectedDate.add(Calendar.DATE, 1);
                    }

                    setAlarm(selectedDate);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false // 12-hour format
        );
        timePickerDialog.setTitle("Set Alarm Time");
        timePickerDialog.show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(Calendar targetCal) {
        String msgText = message.getText().toString();

        // Format date-time nicely
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d yyyy 'at' hh:mm a", Locale.getDefault());
        String formattedTime = sdf.format(targetCal.getTime());

        textAlarmPrompt.setText(
                "\n\n**************************************************\n" +
                        "Alarm is set for: " + formattedTime + "\n" +
                        "**************************************************\n" +
                        "Message: " + msgText
        );

        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        intent.putExtra("msg", msgText);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
        }
    }
}
