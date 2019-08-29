package com.example.dani.biketracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, DevicesFragment.OnHeadlineSelectedListener {

    private static String deviceMAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(), "devices").commit();
        }
        else {
            onBackStackChanged();
        }
    }

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof DevicesFragment) {
            DevicesFragment devicesFragment = (DevicesFragment) fragment;
            devicesFragment.setOnHeadlineSelectedListener(this);
        }
    }

    public void onArticleSelected(String device) {
        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article

        //Save MAC address
        this.deviceMAC = device;
        //Notify
        //Toast.makeText(this, "Device selected MAC address :" + device, Toast.LENGTH_SHORT).show();

        //Call next activity
        Intent LoginIntent = new Intent(this, LoginActivity.class);
        startActivity(LoginIntent);
        //getSupportFragmentManager()
        //        .beginTransaction()
        //        .hide(getSupportFragmentManager().findFragmentById(R.id.fragment))
        //        .commit();
    }

    public static String getMAC() {
        return deviceMAC;
    }

}
