package com.example.dani.biketracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LeaderSpeed extends Activity {

    private static float desiredSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_leaderconfig);

        final Button leaderButton = findViewById(R.id.max_button_id);
        leaderButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                desiredSpeed = (float)5;// Code here executes on main thread after user presses button
                toRecordingActivity();
            }
        });

        final Button follower0Button = findViewById(R.id.mid_button_id);
        follower0Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                desiredSpeed = (float)4.16;// Code here executes on main thread after user presses button
                toRecordingActivity();
            }
        });

        final Button follower1Button = findViewById(R.id.min_button_id);
        follower1Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                desiredSpeed = (float)2.77;// Code here executes on main thread after user presses button
                toRecordingActivity();
            }
        });
    }

    public void toRecordingActivity() {
        Intent i = new Intent(this, RecordingActivity.class);
        startActivity(i);
    }

    public static float getLeaderSpeed() {
        return desiredSpeed;
    }

}
