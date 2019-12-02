package com.music.playlist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.atomic.AtomicLong;

public class AskPermission extends AppCompatActivity {

    String TAG = "AskPermission";
    boolean isFirst = true;
    private final AtomicLong counter = new AtomicLong();
    private static final int READ_STORAGE_PERMISSION_CODE = 101;
    private static final int WRITE_STORAGE_PERMISSION_CODE = 101;
    private TextView permissionText;
    private Button startMainActivityButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();
    }

    void checkPermissions() {
        checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                READ_STORAGE_PERMISSION_CODE);

    }

    public void startMainActivity() {
        Intent i = new Intent(AskPermission.this,
                MainActivity.class);
        startActivity(i);
    }

    public void checkPermission(String permission, int requestCode)
    {
        Log.i(TAG, "Permission requested " + permission);
        if (ContextCompat.checkSelfPermission(AskPermission.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            setContentView(R.layout.loading_first_screen);
            permissionText = findViewById(R.id.permission_text);
            startMainActivityButton = findViewById(R.id.start_main_activity_button);
            isFirst = false;

            // Requesting the permission
            ActivityCompat.requestPermissions(AskPermission.this,
                    new String[]{permission},
                    requestCode);
        }
        else {
            startMainActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == WRITE_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AskPermission.this,
                        "Write Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                permissionText.setVisibility(View.INVISIBLE);
                startMainActivityButton.setVisibility(View.INVISIBLE);
                startMainActivity();
            }
            else {
                Toast.makeText(AskPermission.this,
                        "Write Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
                permissionText.setText(R.string.no_permission_text);
                startMainActivityButton.setText(R.string.re_request);
                startMainActivityButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkPermissions();
                    }
                });

            }
        }
    }
}
