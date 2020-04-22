package com.xyy.customview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatSeekBar seekBar = findViewById(R.id.seekbar);
        final RingPercentageView percentageView = findViewById(R.id.view_ring);
        final TextView tvValue = findViewById(R.id.tv_value);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("seekbar", "progress:" + progress);
                percentageView.setPercentData(progress);
                tvValue.setText("Value:" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }
}
