package com.dohoailam.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.dohoailam.Fragment.MapFragment;
import com.dohoailam.model.User;
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

public class MapUserActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private List<User> mUsers = new ArrayList<>();
    Timer timer = new Timer();
    String userid;
    Intent intent;
    Spinner spinnerMapType;
    String arr[]={
            "NORMAL",
            "HYBRID",
            "NONE",
            "SATELLITE",
            "TERRAIN"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_user);
        intent = getIntent();
        userid = intent.getStringExtra("userid");
        spinnerMapType = findViewById(R.id.spinnermapType);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ArrayAdapter<String> adapter=new ArrayAdapter<String>
                (
                        this,
                        android.R.layout.simple_spinner_item,
                        arr
                );
        //phải gọi lệnh này để hiển thị danh sách cho Spinner
        adapter.setDropDownViewResource
                (android.R.layout.simple_list_item_single_choice);
        //Thiết lập adapter cho Spinner
        spinnerMapType.setAdapter(adapter);
        //thiết lập sự kiện chọn phần tử cho Spinner
        spinnerMapType.setOnItemSelectedListener(new MyProcessEvent());




    }
    //Class tạo sự kiện
    private class MyProcessEvent implements
            AdapterView.OnItemSelectedListener
    {
        //Khi có chọn lựa thì vào hàm này
        public void onItemSelected(AdapterView<?> arg0,
                                   View arg1,
                                   int arg2,
                                   long arg3) {
            //arg2 là phần tử được chọn trong data source

            Log.e("CHON",(arr[arg2] ));
            switch (arr[arg2] )
            {
                case "HYBRID":
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                case "NONE":
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    break;
                case "NORMAL":
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case "SATELLITE":
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case "TERRAIN":
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
            }
        }
        //Nếu không chọn gì cả
        public void onNothingSelected(AdapterView<?> arg0) {

        }

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
                        Marker marker =  mMap.addMarker(
                                new MarkerOptions()
                                        .position(sydney)
                                        .title(user.getUsername())
                                        .snippet(user.getUsername())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        );
                        marker.showInfoWindow();
                        //Log.e("firebaseUser.getUid()",firebaseUser.getUid() + "");
                    }else if (user.getId().equals(userid))
                    {
                        Marker marker = mMap.addMarker(
                                new MarkerOptions()
                                        .position(sydney)
                                        .title(user.getUsername())
                                        .snippet(user.getUsername())
                        );
                        marker.showInfoWindow();
                        //Log.e("userid",userid + "");
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