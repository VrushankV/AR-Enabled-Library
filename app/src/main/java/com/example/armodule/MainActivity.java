package com.example.armodule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void BarcodeScanner(View view){
        Intent intent = new Intent(this, BarcodeScanner.class);
        startActivity(intent);
    }

    public void ShowBookPath(View view){
        Intent intent = new Intent(this, ARview.class);
        startActivity(intent);
    }

}
