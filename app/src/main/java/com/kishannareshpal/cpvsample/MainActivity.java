package com.kishannareshpal.cpvsample;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import com.kishannareshpal.circularprogressview.CircularProgressView;
import com.kishannareshpal.circularprogressview.StrokePlacement;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        CircularProgressView cpv = findViewById(R.id.cpv);

        int[] gradientColors = new int[] {
                color(R.color.light_blue_A400),
                color(R.color.blue_A700)
        };
        cpv.setStrokeColor(gradientColors);

        findViewById(R.id.control).setOnClickListener((btn) -> {
            cpv.start();
        });

    }

    private int color(@ColorRes int clr) {
        return ContextCompat.getColor(this, clr);
    }
}
