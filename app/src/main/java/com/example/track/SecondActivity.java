package com.example.track;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SecondActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    public static FirebaseAuth mAuth;
    Button signOutBtn, mapButton , shareloc;
    TextView textView;

    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;
    DatabaseReference mDatabase;
    public static LatLng myLocation;
    public static LocationTrack locationTrack;
    ValueEventListener dbListener;
    ArrayList<Loc> userList;
    //    ArrayList<String> paired;
    MyRecyclerViewAdapter adapter;
    Date d;
    SimpleDateFormat sdf;
    ProgressBar progressBar;




    class Loc{
        double latitude;
        double longitude;
        String name;
        String email;
        String time;
        public Loc(Double latitude, Double longitude, String name, String email, String time){
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.email = email;
            this.time = time;
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_second);
            setContentView(R.layout.content_second);
        signOutBtn = findViewById(R.id.signOutBtn);
        mapButton = findViewById(R.id.button2);
        shareloc=findViewById(R.id.share);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        locationTrack = new LocationTrack(SecondActivity.this);
        userList = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, userList);
        adapter.setClickListener(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);


        myLocation = new LatLng(locationTrack.getLatitude(), locationTrack.getLongitude());
        if (locationTrack.canGetLocation()) {
            if (!(locationTrack.getLatitude() == 0 || locationTrack.getLongitude() == 0)) {
                myLocation = new LatLng(locationTrack.getLatitude(), locationTrack.getLongitude());
                mDatabase.child(user.getUid()).child("latitude").setValue(myLocation.latitude);
                mDatabase.child(user.getUid()).child("longitude").setValue(myLocation.longitude);
                mDatabase.child(user.getUid()).child("name").setValue(user.getDisplayName());
                d = new Date();
                sdf = new SimpleDateFormat("dd-MM-yy   HH:mm:ss");
                mDatabase.child(user.getUid()).child("time").setValue(sdf.format(d));
                mDatabase.child(user.getUid()).child("email").setValue(user.getEmail());
            }
            else
                Toast.makeText(SecondActivity.this,"Getting location",Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(SecondActivity.this,"Cant get location",Toast.LENGTH_SHORT).show();
        //locationTrack.showSettingsAlert();
//        paired = new ArrayList<>();
//        paired.add("yogeshj939@gmail.com");
        dbListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                    Double lat = userSnapShot.child("latitude").getValue(Double.class);
                    Double lon = userSnapShot.child("longitude").getValue(Double.class);
                    String name = userSnapShot.child("name").getValue(String.class);
                    String email = userSnapShot.child("email").getValue(String.class);
                    String time = userSnapShot.child("time").getValue(String.class);
//                    for(String s: paired)
//                        if(s.equals(email)){
                    int f = 0;
                    for (Loc u : userList) {
                        if (u.email == email) {
                            u.latitude = lat;
                            u.longitude = lon;
                            u.time = time;
                            f = 1;
                            break;
                        }
                    }

                    if (f == 0) {
                        Loc user = new Loc(lat, lon, name, email,time);
                        userList.add(user);
                    }
//                    }





                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);


            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(dbListener);



//******************************************************************************
//        permissions.add(ACCESS_FINE_LOCATION);
//        permissions.add(ACCESS_COARSE_LOCATION);
//        permissionsToRequest = findUnAskedPermissions(permissions);
//        //get the permissions we have asked for before but are not granted..
//        //we will store this in a global list to access later.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (permissionsToRequest.size() > 0)
//                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
//        }




        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(SecondActivity.this, MainActivity.class));

                }

            }
        };



        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
            }
        });

        shareloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SecondActivity.this,ShareActivity.class));
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationTrack.canGetLocation()) {
                    if (!(locationTrack.getLatitude() == 0 || locationTrack.getLongitude() == 0)) {
                        myLocation = new LatLng(locationTrack.getLatitude(), locationTrack.getLongitude());
                        Bundle args = new Bundle();
                        args.putParcelable("my_loc", myLocation);
                        args.putString("name","You are here");
                        Intent i = new Intent(SecondActivity.this, MapsActivity.class);
                        i.putExtra("bundle", args);
                        startActivity(i);
                        mDatabase.child(user.getUid()).child("latitude").setValue(myLocation.latitude);
                        mDatabase.child(user.getUid()).child("longitude").setValue(myLocation.longitude);
                        mDatabase.child(user.getUid()).child("name").setValue(user.getDisplayName());
                        d = new Date();
                        sdf = new SimpleDateFormat("dd-MM-yy  HH:mm:ss");
                        mDatabase.child(user.getUid()).child("time").setValue(sdf.format(d));
                        mDatabase.child(user.getUid()).child("email").setValue(user.getEmail());

                        //mDatabase.child(user.getUid()).child("paired").setValue(paired);
                        //mDatabase.child(user.getUid()).setValue(user.getDisplayName());
//                        String s = " ";
//                        for(Loc l: userList)
//                            s = s+l.name+"\n";
//                        textView.setText(s);

                    }
                    else
                        Toast.makeText(SecondActivity.this,"Getting location",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(SecondActivity.this,"Cant location",Toast.LENGTH_SHORT).show();
                //locationTrack.showSettingsAlert();

            }
        });



    }

    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }

    @Override
    public void onItemClick(View view, int position) {
        Loc p = userList.get(position);
        LatLng myLocation = new LatLng(p.latitude,p.longitude);
        Bundle args = new Bundle();
        args.putParcelable("my_loc", myLocation);
        args.putString("name",p.name);
        Intent i = new Intent(SecondActivity.this, MapsActivity.class);
        i.putExtra("bundle", args);
        startActivity(i);
    }


}
