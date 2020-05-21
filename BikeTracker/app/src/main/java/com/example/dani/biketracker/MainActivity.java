package com.example.dani.biketracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
            //getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(), "devices").commit();
        }
        else {
            onBackStackChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onArticleSelected("TEST_ADDRESS");
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
        //Initialize user
        this.deviceMAC = device;
        /*User usuario = new User();
        usuario.setMAC_ADDRESS(device);*/
        //Notify
        Toast.makeText(this, "Device selected MAC address :" + device, Toast.LENGTH_SHORT).show();
        //Call next activity
        Intent i = new Intent(this, UserConfigActivity.class);
        startActivity(i);
        //Finish
        //finish();
    }

    public static String getMAC() {
        return deviceMAC;
    }

}
