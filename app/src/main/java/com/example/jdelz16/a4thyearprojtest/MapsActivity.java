package com.example.jdelz16.a4thyearprojtest;

import java.text.DecimalFormat;
import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private TextView textView;

    private GoogleMap mMap;

    private LocationManager locationManager;

    private DatabaseHelper myDB;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private EditText editUserName, editDistancePref;
    private Spinner editAvailability, editExerciseType;
    private TextView mTextViewCountdown;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;

    private DatabaseReference refDatabase;

    private DatabaseReference mBuddyReqDB;

    private FirebaseUser mCurrent_user;

    private String buddy_state;

    private ArrayList<String> listOfUsers = new ArrayList<>();
   // private Set<String> hs = new HashSet<>();


    private double uLatt;
    private double uLongt;
    private String uExerciseType;
    private Button lookForBuddy, cancelBuddy, acceptBuddy, rateBuddy;
    private RatingBar rating;

    private List<Polyline> polylines;

    private ListView lv;

    private String user_id;
    //private String result;
    private ArrayList<String> result = new ArrayList<>();

    private String theTarget;
    private String targetBud;

    private long routeTimer;

    private double average;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(user_id != null){
            Log.d("FIRSTUSERID", user_id);
        }

        lv = (ListView) findViewById(R.id.listView_users);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        editUserName = (EditText) findViewById(R.id.edit_userName);
        editDistancePref = (EditText) findViewById(R.id.edit_distancePref);
        editAvailability = (Spinner) findViewById(R.id.available);
        editExerciseType = (Spinner) findViewById(R.id.exerciseType);
        lookForBuddy = (Button)findViewById(R.id.lookButton);
        cancelBuddy = (Button)findViewById(R.id.cancelButton);
        acceptBuddy = (Button)findViewById(R.id.acceptButton);
        rateBuddy = (Button)findViewById(R.id.submitRating);
        mTextViewCountdown = (TextView)findViewById(R.id.timer);
        rating = (RatingBar)findViewById(R.id.ratingBar);

        acceptBuddy.setVisibility(View.GONE);
        cancelBuddy.setVisibility(View.GONE);

        mBuddyReqDB = FirebaseDatabase.getInstance().getReference().child("buddyReq");

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        myDB = new DatabaseHelper(this, "", null, 9);

        polylines = new ArrayList<>();

        buddy_state = "not buddies";

        ArrayAdapter<String> myAdapterAvail = new ArrayAdapter<String>(MapsActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.availabilities));
        myAdapterAvail.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editAvailability.setAdapter(myAdapterAvail);

        ArrayAdapter<String> myAdapterEx = new ArrayAdapter<String>(MapsActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.exerciseTypes));
        myAdapterAvail.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editExerciseType.setAdapter(myAdapterEx);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //permission check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //set the username
        loadUserInformation();
        avgRating();

        //check if the network provider is enabled
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //get the latitude
                    double lat = location.getLatitude();
                    //get the longtitude
                    double lng = location.getLongitude();
                    //instantiate the class latitude and longtitude
                    LatLng latLng = new LatLng(lat, lng);

                    //insert the location to the database
                    myDB.insert_location(String.valueOf(lat), String.valueOf(lng));
                    Log.d("1st:", String.valueOf(lat));
                    //show lat and long
                    //textView.setText(myDB.list_locations());
                    //myDB.listsGPS(textView);
                    Log.d("2nd:", "Working");
                    //instantiate the class Geocoder

                    if(editUserName.getText().toString().matches("")){
                        Toast.makeText(MapsActivity.this, "Please enter a Name", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String uName = editUserName.getText().toString().trim();
                        String availability = editAvailability.getSelectedItem().toString();
                        String exerciseType = editExerciseType.getSelectedItem().toString();



                        UserInformation userInformation = new UserInformation();

                        userInformation.setUserName(uName);
                        userInformation.setAvailability(availability);
                        userInformation.setExerciseType(exerciseType);
                        userInformation.setLatitude(lat);
                        userInformation.setLongtitude(lng);
                        userInformation.setUniqueID(mCurrent_user.getUid().toString());
                        userInformation.setRatingAvg(average);

                        uLatt = lat;
                        uLongt = lng;
                        databaseReference.child("users").child(mCurrent_user.getUid()).setValue(userInformation);

                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        try {
                            List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
                            String str = addressList.get(0).getLocality() + ", ";
                            //str += addressList.get(0).getCountryName();
                            //mMap.addMarker(new MarkerOptions().position(latLng).title(str + " " + myDB.listsGPS()));
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            FirebaseUser firebaseUser = mAuth.getInstance().getCurrentUser();

            DatabaseReference ref = database.getReference();

            ref.child("users").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null) {
                        Log.d("Error Message", "Empty");
                    } else {
                        UserInformation loc = dataSnapshot.getValue(UserInformation.class);

                        //uLatt = loc.getLatitude();
                        //uLongt = loc.getLongtitude();
                        uExerciseType = loc.getExerciseType();

                        //Log.d("Current User", loc.getAvailability().toString());
                        //Log.d("Current Exercise Type", uExerciseType);

                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            /*mBuddyReqDB.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot ds) {
                    if(ds.exists()){
                        String requestType = ds.child(user_id).child("req_type").getValue(String.class);

                        if(requestType.equals("received")) {
                            acceptBuddy.setVisibility(View.VISIBLE);
                        } else if(requestType.equals("sent")) {
                            cancelBuddy.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(MapsActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });*/

        } else {
            Toast.makeText(MapsActivity.this, "Need internet connection!!", Toast.LENGTH_SHORT).show();
            /*
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //get the latitude
                    double lat = location.getLatitude();
                    //get the longtitude
                    double lng = location.getLongitude();
                    //instantiate the class latitude and longtitude
                    LatLng latLng = new LatLng(lat, lng);

                    //insert the location to the database
                    myDB.insert_location(String.valueOf(lat), String.valueOf(lng));
                    Log.d("3rd:", String.valueOf(lng));
                    //show lat and long
                    textView.setText(myDB.list_locations());
                    Log.d("4th:", "Working");

                    //database gps
                    String availability = editAvailability.getText().toString().trim();
                    String exerciseType = editExerciseType.getText().toString().trim();

                    UserInformation userInformation = new UserInformation();

                    FirebaseUser user = mAuth.getCurrentUser();

                    userInformation.setAvailability(availability);
                    userInformation.setExerciseType(exerciseType);
                    userInformation.setLatitude(lat);
                    userInformation.setLongtitude(lng);

                    databaseReference.child("users").child(user.getUid()).setValue(userInformation);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });*/
        }

        acceptBuddyRequest();

        lookForBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editUserName.getText().toString().matches("") ){
                    Toast.makeText(MapsActivity.this, "Please enter a Name", Toast.LENGTH_SHORT).show();
                } else {
                    searchForBuddy();
                }
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), lv.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
                String lvItem = lv.getItemAtPosition(position).toString();

                getUniqueID(lvItem);

                lv.setVisibility(View.INVISIBLE);
                mTextViewCountdown.setVisibility(View.INVISIBLE);

                /*if(buddy_state.equals("not buddies")) {
                    mBuddyReqDB.child(mCurrent_user.getUid()).child(user_id).child("req_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mBuddyReqDB.child(user_id).child(mCurrent_user.getUid()).child("req_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //buddy_state = "req_sent";
                                        cancelBuddy.setVisibility(View.VISIBLE);

                                        Toast.makeText(MapsActivity.this, "Success sending request", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(MapsActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }*/
            }
        });

        acceptBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refDatabase = FirebaseDatabase.getInstance().getReference().child("buddyReq");
                refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Log.d("CheckforID", mCurrent_user.getUid().toString());
                            theTarget = dataSnapshot.child("uniqueIdentifier").getValue(String.class);

                            for(final DataSnapshot ds : dataSnapshot.getChildren()){
                                String theUser = ds.child("requestType").getValue(String.class);

                                if(theUser.equals(mCurrent_user.getUid().toString())){
                                    ds.child("receiverReply").getRef().setValue("yes").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MapsActivity.this, "Stay and wait for your buddy!!", Toast.LENGTH_LONG).show();
                                            //mTextViewCountdown.setVisibility(View.VISIBLE);
                                            lv.setVisibility(textView.INVISIBLE);
                                            lookForBuddy.setVisibility(View.INVISIBLE);
                                            rateBuddy.setVisibility(View.VISIBLE);
                                            rating.setVisibility(View.VISIBLE);
                                            startTimer();
                                        }
                                    });
                                }
                            }
                        } else {
                            acceptBuddy.setVisibility(View.GONE);
                            mTextViewCountdown.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        mBuddyReqDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        String theUser = ds.child("uniqueIdentifier").getValue(String.class);
                        String reply = ds.child("receiverReply").getValue(String.class);
                        Rating rateUser = new Rating();

                        if(theUser.equals(mCurrent_user.getUid().toString()) && reply.equals("yes")){
                            targetBud = ds.child("requestType").getValue(String.class);
                            lv.setVisibility(textView.INVISIBLE);
                            lookForBuddy.setVisibility(View.INVISIBLE);
                            rateBuddy.setVisibility(View.VISIBLE);
                            rating.setVisibility(View.VISIBLE);

                            Toast.makeText(MapsActivity.this, "Go to your buddy!!", Toast.LENGTH_SHORT).show();
                            showRoute(targetBud);

                            Log.d("TIMER: " , String.valueOf(routeTimer));
                            ds.child("timer").getRef().setValue(routeTimer).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MapsActivity.this, "Timer changed", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rateBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBuddyReqDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                String theUser = ds.child("uniqueIdentifier").getValue(String.class);
                                String reply = ds.child("receiverReply").getValue(String.class);
                                Rating rateUser = new Rating();

                                if(theUser.equals(mCurrent_user.getUid().toString()) && reply.equals("yes")){
                                    targetBud = ds.child("requestType").getValue(String.class);

                                    rateUser.setRating(rating.getRating());
                                    rateUser.setUniqID(targetBud);
                                    databaseReference.child("ratings").child(targetBud).push().setValue(rateUser);

                                    avgRating();
                                    lookForBuddy.setVisibility(View.VISIBLE);
                                    rateBuddy.setVisibility(View.INVISIBLE);
                                    rating.setVisibility(View.INVISIBLE);
                                    acceptBuddy.setVisibility(View.INVISIBLE);

                                } else {
                                    rateUser();
                                    avgRating();
                                    lookForBuddy.setVisibility(View.VISIBLE);
                                    rateBuddy.setVisibility(View.INVISIBLE);
                                    rating.setVisibility(View.INVISIBLE);
                                    mTextViewCountdown.setVisibility(View.INVISIBLE);
                                    acceptBuddy.setVisibility(View.INVISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        cancelBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTextViewCountdown.setVisibility(View.INVISIBLE);
                databaseReference.child("buddyReq").child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        cancelBuddy.setVisibility(View.GONE);
                    }
                });

                mMap.clear();
                polylines.clear();
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

   }

    public void loadUserInformation() {
        //FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(mCurrent_user != null){
            if(mCurrent_user.getUid() != null) {
                //displayUserInfo.setText(firebaseUser.getUid());
                databaseReference.child("users").child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null){
                            Toast.makeText(MapsActivity.this, "You need to input your Name", Toast.LENGTH_SHORT).show();
                        } else {
                            UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);

                            editUserName.setText(userInformation.getUserName());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

   public void acceptBuddyRequest() {
        refDatabase = FirebaseDatabase.getInstance().getReference().child("buddyReq");
        refDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    Log.d("CheckforID", mCurrent_user.getUid().toString());

                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        Buddy_req budReq = ds.getValue(Buddy_req.class);

                        if(budReq.getRequestType().equals(mCurrent_user.getUid().toString())){
                            acceptBuddy.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    acceptBuddy.setVisibility(View.VISIBLE);
                                }
                            }, 3000);
                        }
                        else {
                            Toast.makeText(MapsActivity.this, "You sent a buddy request", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    acceptBuddy.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
   }

   public void rateUser() {
       refDatabase = FirebaseDatabase.getInstance().getReference().child("buddyReq");
       refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists()){

                   Log.d("CheckforID", mCurrent_user.getUid().toString());

                   for (DataSnapshot ds : dataSnapshot.getChildren()){
                       Buddy_req budReq = ds.getValue(Buddy_req.class);
                       Rating rateUser = new Rating();
                       String uniqueID;

                       if(budReq.getRequestType().equals(mCurrent_user.getUid().toString())){
                           rateUser.setRating(rating.getRating());
                           rateUser.setUniqID(budReq.getUniqueIdentifier().toString());
                           uniqueID = budReq.getUniqueIdentifier().toString();
                           databaseReference.child("ratings").child(uniqueID).push().setValue(rateUser);
                       }
                       else {
                           Log.d("Check rating", "Checked");
                       }
                   }
               } else {
                   acceptBuddy.setVisibility(View.GONE);
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
   }

   public void avgRating() {
       databaseReference.child("ratings").child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               double total = 0;
               double count = 0;

               if(dataSnapshot.exists()) {
                   for (DataSnapshot ds : dataSnapshot.getChildren()){
                      double rating = Double.parseDouble(ds.child("rating").getValue().toString());
                      total = total + rating;
                      count = count + 1;
                      average = total / count;
                      average = Math.round(average * 100);
                      average = average/100;

                       ds.child("averageRating").getRef().setValue(average);
                   }

                   final DatabaseReference newRef = FirebaseDatabase.getInstance().getReference().child("users");;
                   newRef.child(mCurrent_user.getUid()).child("ratingAvg").setValue(average);

                   Log.d("TheAvgRating", String.valueOf(average));
               } else {
                   Log.d("AverageRating", "Empty");
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
   }

   public void startTimer() {
       refDatabase = FirebaseDatabase.getInstance().getReference().child("buddyReq");
       refDatabase.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists()){

                   Log.d("CheckforID", mCurrent_user.getUid().toString());

                   for (DataSnapshot ds : dataSnapshot.getChildren()){
                       Buddy_req budReq = ds.getValue(Buddy_req.class);

                       if(budReq.getRequestType().equals(mCurrent_user.getUid().toString()) && budReq.getTimer() > 0 && budReq.receiverReply.equals("yes")){
                           Toast.makeText(MapsActivity.this, "Timer will be displayed", Toast.LENGTH_SHORT).show();

                           //countdowntimer start
                           //Double.valueOf(budReq.getTimer()).longValue()
                           Log.d("TimerBeforeCountDown: ", String.valueOf(budReq.getTimer()));
                           mCountDownTimer = new CountDownTimer(Double.valueOf(budReq.getTimer()).longValue(), 1000) {
                               @Override
                               public void onTick(long millisUntilFinished) {
                                    mTimeLeftInMillis = millisUntilFinished;
                                    updateCountDownText();
                               }

                               @Override
                               public void onFinish() {

                               }
                           }.start();

                           mTextViewCountdown.setVisibility(View.VISIBLE);
                       }
                       else {
                           Toast.makeText(MapsActivity.this, "Timer not ready...", Toast.LENGTH_SHORT).show();
                       }
                   }
               } else {
                   acceptBuddy.setVisibility(View.GONE);
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
   }

   public void updateCountDownText(){
       int minutes = (int) (mTimeLeftInMillis / 1000) / 60;     //minutes
       int seconds = (int) (mTimeLeftInMillis / 1000) % 60;     //seconds

       String timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
       mTextViewCountdown.setText(timeLeftFormatted);
   }

   public void searchForBuddy(){
       String prefDistance = editDistancePref.getText().toString().trim();

       mTextViewCountdown.setVisibility(textView.INVISIBLE);

       refDatabase = FirebaseDatabase.getInstance().getReference().child("users");
       refDatabase.orderByChild("availability").equalTo("yes").addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()) {
                   //refreshes the map
                   mMap.clear();
                   listOfUsers.clear();

                   for (DataSnapshot ds : dataSnapshot.getChildren()) {
                       UserInformation userInfo = ds.getValue(UserInformation.class);

                       if (userInfo.getExerciseType().equals(uExerciseType)) {
                           double mylat = userInfo.getLatitude();
                           double mylong = userInfo.getLongtitude();
                           String exTitle = ds.child("userName").getValue(String.class);
                           Double userRating = ds.child("ratingAvg").getValue(Double.class);

                           String forUNameList = ds.child("userName").getValue(String.class);

                           if(!listOfUsers.contains(forUNameList)){
                               listOfUsers.add(forUNameList);
                           }

                           Log.d("List of Users: ", listOfUsers.toString());

                           LatLng newLocation = new LatLng(mylat, mylong);

                           //arrayLatLng.add(newLocation);
                           //Log.d("ARRAY OF LATLNG", arrayLatLng.toString());

                           Log.d("New Location: ", newLocation.toString());


                           LatLng myLatLang = new LatLng(uLatt, uLongt);
                           Log.d("My Location: ", myLatLang.toString());

                           MarkerOptions markerOptions = new MarkerOptions();
                           markerOptions.position(newLocation);
                           markerOptions.title(exTitle + ", " + userRating);
                           markerOptions.visible(false);

                           Marker locationMarker = mMap.addMarker(markerOptions);

                           if (SphericalUtil.computeDistanceBetween(myLatLang, locationMarker.getPosition()) < 1000000) {
                               locationMarker.setVisible(true);
                           }

                           //LatLng sampleLatLang = new LatLng(53.291492, -6.3635647);

                           //getRouteToMarker(newLocation);

                           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLang, 11.2f));
                       }

                   }
               }

               ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,listOfUsers);
               adapter.notifyDataSetChanged();
               lv.setAdapter(adapter);
               lv.setVisibility(View.VISIBLE);
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
   }

   //get the unique ID of the clicked user
    public void getUniqueID(String listviewdata){
        refDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        refDatabase.orderByChild("userName").equalTo(listviewdata).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String result1;

                if (dataSnapshot.exists()) {
                    result.clear();
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        Buddy_req buddy_req = new Buddy_req();

                        if(ds.child("availability").getValue(String.class).equals("no")){
                            Toast.makeText(MapsActivity.this, "User not available.", Toast.LENGTH_SHORT).show();
                        } else {
                            result1 = ds.child("uniqueID").getValue(String.class);

                            showRoute(result1);
                            Log.d("TIMER: ", String.valueOf(routeTimer));

                            buddy_req.setRequestType(result1);
                            buddy_req.setUniqueIdentifier(mCurrent_user.getUid().toString());
                            buddy_req.setReceiverReply("no");
                            buddy_req.setTimer(routeTimer);

                            final String finalResult = result1;

                            Log.d("finalResult", finalResult);
                            databaseReference.child("buddyReq").child(mCurrent_user.getUid()).setValue(buddy_req).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    cancelBuddy.setVisibility(View.VISIBLE);

                                    refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    if(ds.child("uniqueID").getValue(String.class).equals(mCurrent_user.getUid().toString())) {
                                                        ds.child("availability").getRef().setValue("no");
                                                    }

                                                    if(ds.child("uniqueID").getValue(String.class).equals(finalResult)) {
                                                        ds.child("availability").getRef().setValue("no");
                                                    }
                                                }
                                            } else {
                                                Log.d("User change", "User changed");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            };
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToMarker(LatLng newLocation) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.WALKING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(uLatt, uLongt), newLocation)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        //long duration;
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(Color.RED);
            polyOptions.width(4);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();

            routeTimer = route.get(i).getDurationValue();

            refDatabase = FirebaseDatabase.getInstance().getReference().child("buddyReq");
            refDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            Log.d("CheckforID", mCurrent_user.getUid().toString());

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                Buddy_req budReq = ds.getValue(Buddy_req.class);

                                if (budReq.getRequestType().equals(mCurrent_user.getUid().toString())) {
                                    ds.child("timer").getRef().setValue(routeTimer).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MapsActivity.this, "Timer changed", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(MapsActivity.this, "Didnt Work", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        }

    }

    @Override
    public void onRoutingCancelled() {

    }

    public void showRoute(String buddy) {

        Toast.makeText(this, "Routing", Toast.LENGTH_SHORT).show();
        refDatabase.orderByChild("uniqueID").equalTo(buddy).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        UserInformation userInfo = ds.getValue(UserInformation.class);
                        double mylat = userInfo.getLatitude();
                        double mylong = userInfo.getLongtitude();

                        LatLng target = new LatLng(mylat, mylong);

                        getRouteToMarker(target);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

