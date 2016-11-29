package usi.justmove;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import usi.justmove.dataGathering.LocationDataService;

/**
 * Created by Luca Dotti on 22/11/16.
 */
public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener, MapFragment.OnFragmentInteractionListener, UsageFragment.OnFragmentInteractionListener {
    android.support.v7.widget.Toolbar toolbar;
    TabLayout tab;
    ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private final static int PERMISSION_ACCESS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tab = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new HomeFragment(), "Home");
        viewPagerAdapter.addFragment(new MapFragment(), "Map");
        viewPagerAdapter.addFragment(new UsageFragment(), "Usage");
        viewPager.setAdapter(viewPagerAdapter);
        tab.setupWithViewPager(viewPager);
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_LOCATION);
        }

        startService(new Intent(this, LocationDataService.class));
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
