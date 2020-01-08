package com.example.dani.biketracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FollowerSpacingActivity extends Activity {

    private static double desiredSpacing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_followerconfig);

        final Button maxButton = findViewById(R.id.max_button_id);
        maxButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                desiredSpacing = 8;// Code here executes on main thread after user presses button
                toRecordingActivity();
            }
        });

        final Button midButton = findViewById(R.id.mid_button_id);
        midButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                desiredSpacing = 5;;// Code here executes on main thread after user presses button
                toRecordingActivity();
            }
        });

        final Button minButton = findViewById(R.id.min_button_id);
        minButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                desiredSpacing = 2;// Code here executes on main thread after user presses button
                toRecordingActivity();
            }
        });
    }

    public void toRecordingActivity() {
        Intent i = new Intent(this, RecordingActivity.class);
        startActivity(i);
    }

    public static double getDesiredSpacing() {
        return desiredSpacing;
    }

}
