package com.dohoailam.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dohoailam.Adapter.UserAdapter;
import com.dohoailam.model.User;
import com.dohoailam.mychat.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<User> mUsers = new ArrayList<>();
    Timer timer = new Timer();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);



        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //readUsers(mMap);



        timer.scheduleAtFixedRate(new TimerTask() {
                                      @Override
                                      public void run() {
                                          readUsers(mMap);
                                      }
                                  },
                0, 60000);   // 1000 Millisecond  = 1 second




    }


    //ad option menu cho fragment
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.option_menu_maptype, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.mnuHD:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.mnuNE:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mnuNM:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mnuSL:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mnuTR:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void readUsers(final GoogleMap mMap) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //if(search_users.getText().toString().equals("")) {
                    mUsers.clear();
                    mMap.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null) {
                            throw new AssertionError();
                        }
                        if (firebaseUser == null) {
                            throw new AssertionError();
                        }

                        mUsers.add(user);
                        double longlng = Double.parseDouble(user.getLonglng());
                        double latlng = Double.parseDouble(user.getLatlng());
                        LatLng sydney = new LatLng(latlng, longlng);

                        if (user.getId().equals(firebaseUser.getUid())) {
                            Marker marker =  MapFragment.this.mMap.addMarker(
                                    new MarkerOptions()
                                            .position(sydney)
                                            .title(user.getUsername())
                                            .snippet(user.getUsername())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            );
                            marker.showInfoWindow();
                        }else if(user.getStatus().equals("online"))
                        {
                            Marker marker =  MapFragment.this.mMap.addMarker(
                                    new MarkerOptions()
                                            .position(sydney)
                                            .title(user.getUsername())
                                            .snippet(user.getUsername())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            );
                            marker.showInfoWindow();
                        }else
                        {
                            Marker marker =  MapFragment.this.mMap.addMarker(
                                    new MarkerOptions()
                                            .position(sydney)
                                            .title(user.getUsername())
                                            .snippet(user.getUsername())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            );
                            marker.showInfoWindow();
                        }





                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                        //}
                    }
                    // Toast.makeText(getContext(),"tong user " + mUsers.size(),Toast.LENGTH_LONG).show();
                      //  Log.e("tong user " , mUsers.size() + "");

                }
            //}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}