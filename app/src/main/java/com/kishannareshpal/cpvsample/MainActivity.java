package com.kishannareshpal.cpvsample;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.SeekBar;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.kishannareshpal.circularprogressview.CircularProgressView;
import com.kishannareshpal.circularprogressview.ProgressType;
import com.kishannareshpal.circularprogressview.StrokePlacement;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        CircularProgressView cpv = findViewById(R.id.cpv);
        SeekBar seekBar = findViewById(R.id.seekBar);
//        Button btn = findViewById(R.id.button);

        // the last color in the array is the starting color..
        int maxValue = 100;

        int[] gradientColors = new int[]{
                Color.parseColor("#ff9100"), // orange
                Color.parseColor("#ff1744") // red
        };

        cpv.setBackgroundColor(Color.parseColor("#ffcdd2")); // light red
        cpv.setProgressStrokeColor(gradientColors);
        cpv.setStrokePlacement(StrokePlacement.CENTER);



        cpv.setRange(maxValue);

        seekBar.setMax(maxValue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cpv.setProgressType(ProgressType.DETERMINATE);
                float determinedPercentage = CircularProgressView.calcProgressValuePercentageOf(progress, maxValue);
                cpv.setProgress(determinedPercentage, true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
//
//
//        btn.setOnClickListener((v) -> {
//            cpv.toggleIndeterminateAnimation();
//        });

    }

    private int color(@ColorRes int clr) {
        return ContextCompat.getColor(this, clr);
    }
}
