package com.shockwavegames.qr.generator.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PermissionDialogActivity extends AppCompatActivity {

    Button settingsBtn;
    String type="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_dialog);
        settingsBtn=findViewById(R.id.settings);
        type = getIntent().getExtras().getString("permissionType");
        int stringId=getApplicationInfo().labelRes;
        String applicationName=(stringId==0)?getApplicationInfo().nonLocalizedLabel.toString():getString(stringId);

        switch (type) {
            case "Contacts_Read":
                ((TextView)findViewById(R.id.settingsTV)).setText(applicationName+" Requires Contact Permission To Autofill Fields");
                break;
            case "Camera_Permission":
                ((TextView)findViewById(R.id.settingsTV)).setText(applicationName+" Requires Camera Permission To Scan QR Code");
                break;
        }
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (type){
            case "Contacts_Read":
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
                break;
            case "Camera_Permission":
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
                break;
        }
    }
}