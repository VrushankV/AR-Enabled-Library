package com.example.armodule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.widget.ImageView;

import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;



public class BarcodeScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ImageView mImageView;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    public static final String SCAN_DATA = "scan_data";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        //mImageView = findViewById(R.id.imageView);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission()){
                Toast.makeText(BarcodeScanner.this, "Permission is Granted ", Toast.LENGTH_LONG).show();
            }else{
                requestPermission();
            }
        }
    }

    private boolean checkPermission(){
        return ContextCompat.checkSelfPermission(BarcodeScanner.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permission[], int grantResults[]){
        switch(requestCode){
            case REQUEST_CAMERA:
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted){
                        Toast.makeText(BarcodeScanner.this,"Permission Granted ", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(BarcodeScanner.this, "Permission Denied ", Toast.LENGTH_LONG).show();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                                displayAlertMessage("You need to allow actions for both permissions", new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface,int i){
                                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                                    }

                                });
                                return;
                            }
                        }
                    }
                }
                break;

            }
        }


    @Override
    public void onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission()){
                if(scannerView == null){
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }else{
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        scannerView.stopCamera();
    }


    private void displayAlertMessage(String message, DialogInterface.OnClickListener listener) {

        new AlertDialog.Builder(BarcodeScanner.this).setMessage(message)
                .setPositiveButton("OK", listener).setNegativeButton("Cancel",null).create().show();
    }



    @Override
    public void handleResult(Result result) {

        final String scanResult = result.getText();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                //scannerView.resumeCameraPreview(BarcodeScanner.this);
                Intent intent = new Intent(BarcodeScanner.this,ARview.class);
                intent.putExtra(SCAN_DATA,scanResult);
                startActivity(intent);
            }
        });
        builder.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scanResult));
                startActivity(intent);
            }
        });
        builder.setMessage(scanResult);
        AlertDialog alert = builder.create();
        alert.show();

    }
}
