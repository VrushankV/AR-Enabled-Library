package com.example.armodule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import static com.example.armodule.ARview.SHELF_VIEW;

public class ShelfDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String BookPos = getIntent().getStringExtra(SHELF_VIEW);
        Toast.makeText(this,BookPos,Toast.LENGTH_LONG).show();
        setContentView(new ShelfView(this,BookPos));
    }
}
