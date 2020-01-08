package com.example.dani.biketracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.widget.Button;

public class UserConfigActivity extends Activity {

    private static String USER_TYPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_userconfig);

        final Button leaderButton = findViewById(R.id.leader_button_id);
        leaderButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                USER_TYPE = "LEADER";// Code here executes on main thread after user presses button
                toLeaderConfigActivity();
            }
        });

        final Button follower0Button = findViewById(R.id.follower0_button_id);
        follower0Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                USER_TYPE = "FOLLOWER0";// Code here executes on main thread after user presses button
                toFollowerSpacingActivity();
            }
        });

        final Button follower1Button = findViewById(R.id.follower1_button_id);
        follower1Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                USER_TYPE = "FOLLOWER1";// Code here executes on main thread after user presses button
                toFollowerSpacingActivity();
            }
        });
    }

    public void toRecordingActivity() {
        Intent i = new Intent(this, RecordingActivity.class);
        startActivity(i);
    }

    public void toLeaderConfigActivity() {
        Intent i = new Intent(this, LeaderSpeed.class);
        startActivity(i);
    }

    public void toFollowerSpacingActivity() {
        Intent i = new Intent(this, FollowerSpacingActivity.class);
        startActivity(i);
    }

    public static String getUserType() {
        return USER_TYPE;
    }

}
