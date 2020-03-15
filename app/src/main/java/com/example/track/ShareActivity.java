package com.example.track;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

public class ShareActivity extends Activity {
    private ValueEventListener postListener;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    String cemail;
    String time;
    double latitude;
    double longitude;
    String phone;
    DatabaseReference rootRef, demoRef;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    //String userid=user.getUid();
    //DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
    String cname="";
    ShareActivity() {
     postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String email=dataSnapshot.child("admin").child("email").getValue().toString();
                 phone=dataSnapshot.child("admin").child("phoneno").getValue().toString();;
                Toast.makeText(getApplicationContext(), "retrieved phoneno",
                        Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        //database reference pointing to root of database
        /*rootRef = FirebaseDatabase.getInstance().getReference();

        //database reference pointing to demo node
        demoRef = rootRef.child("admin");
        demoRef.child("value").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                demoValue.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }




*/
        reference.addValueEventListener(postListener);
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        String userid=user.getUid();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef.child("Users");
        usersRef.orderByChild("userid").equalTo(userid).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    cname = ds.child("name").getValue().toString();
                    cemail = ds.child("email").getValue().toString();
                    time = ds.child("time").getValue().toString();
                    latitude = ds.child("latitude").getValue(Double.class);
                    longitude = ds.child("longitude").getValue(Double.class);
                }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });




    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (cname != null) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS)) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phone, null, "name:"+cname+" email:"+cemail+" latitude:"+latitude+" longitude:"+longitude+" time:"+time, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }
}