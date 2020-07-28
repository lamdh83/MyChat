package com.dohoailam.mychat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.dohoailam.Fragment.ChatsFragment;
import com.dohoailam.Fragment.MapFragment;
import com.dohoailam.Fragment.ProfileFragment;
import com.dohoailam.Fragment.UsersFragment;
import com.dohoailam.model.Chat;
import com.dohoailam.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    //https://www.youtube.com/watch?v=WsyJlFjJkyE&list=PLzLFqCABnRQftQQETzoVMuteXzNiXmnj8&index=5

    CircleImageView profile_image_tool;
    TextView username_tool;



    FirebaseUser firebaseUser;
    DatabaseReference reference;

    LatLng curLatLng =new LatLng(-34, 151);
    LatLng lastLatlng = new LatLng(0,0);
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username_tool = findViewById(R.id.username_tool);
        profile_image_tool = findViewById(R.id.profile_image_tool);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Màn Hình Chính");



        //AUTO LOGIN LUU usercurrent
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());


        //getgcurrentlocation
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        updateLocationTask();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username_tool.setText(user.getUsername());
                if(user.getImageURL().equals("default") || user.getImageURL().isEmpty())
                {
                    profile_image_tool.setImageResource(R.mipmap.ic_launcher);
                }else {
                    //Glide.with(MainActivity.this).load(user.getImageURL()).into(profile_image_tool);
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image_tool);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });







        final TabLayout tabLayout = findViewById(R.id.tablayout);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unread = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if( chat.getReceiver().equals(firebaseUser.getUid()) && !chat.getIsseen())
                    {
                        unread++;
                    }
                }

                if (unread == 0)
                {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                }else
                {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "(" + unread + ") Chats");
                }

                viewPagerAdapter.addFragment(new UsersFragment(),"Users");
                viewPagerAdapter.addFragment(new MapFragment(),"Map");
                viewPagerAdapter.addFragment(new ProfileFragment(),"Profile");

                viewPager.setAdapter(viewPagerAdapter);

                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm)
        {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String titile)
        {
            fragments.add(fragment);
            titles.add(titile);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.mnuLogout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this,StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void status (String status)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        //updateLocationTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
        //updateLocationTask();
    }


    private void getgcurrentlocation()
    {

        try {
            getLastLocation();
            //toad("Toa do: " + curLatLng.longitude + " / " + curLatLng.latitude);
            //Log.e("GET LOCATION:" ,  SphericalUtil.computeDistanceBetween(curLatLng, lastLatlng) + "");
            double km = SphericalUtil.computeDistanceBetween(curLatLng, lastLatlng);
            if (km > 0.5 ) {
                lastLatlng = curLatLng;
                //Log.e("GET LOCATION:" ,  lastLatlng + " / " + curLatLng);
                //update len firebase
                reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("latlng", curLatLng.latitude + "");
                hashMap.put("longlng", curLatLng.longitude + "");

                reference.updateChildren(hashMap);
            }
        }catch (Exception ex)
        {
//            Toast.makeText(this,ex.toString(),Toast.LENGTH_LONG).show();
            //Log.e("LOI LOCATION:" , ex.toString());
        }
    }
    /////////GETCURRENTLOCATION
    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    curLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                                    //toad(curLatLng.latitude + " / " + curLatLng.longitude);
                                }
                            }
                        }
                );
            } else {
                //y/c bat GPS
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                //chuyen den GPS
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            curLatLng = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());


        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(MainActivity.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    public void toad(String s)
    {
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }




    private void updateLocationTask()
    {
        timer.scheduleAtFixedRate(new TimerTask() {
                                      @Override
                                      public void run() {
                                          getgcurrentlocation();
                                      }
                                  },
                0, 60000);   // 1000 Millisecond  = 1 second
    }
}