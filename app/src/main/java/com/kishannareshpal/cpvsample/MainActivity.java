package com.kishannareshpal.cpvsample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

import com.kishannareshpal.circularprogressview.CircularProgressView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        CircularProgressView cpv = findViewById(R.id.cpv);
        cpv.setStrokeColor(ContextCompat.getColor(this, R.color.blue));
        cpv.setBackgroundColor(ContextCompat.getColor(this, R.color.black));

        findViewById(R.id.control).setOnClickListener((btn) -> {
            cpv.stop();
        });



    }
}
