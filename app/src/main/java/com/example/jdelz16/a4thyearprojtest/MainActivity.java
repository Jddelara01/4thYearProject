package com.example.jdelz16.a4thyearprojtest;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by jdelz16 on 08/11/2017.
 */

public class MainActivity extends AppCompatActivity implements  SensorEventListener, StepListener {
    Button mapsBtn, start, stop, logout;
    TextView textView, distance, calories;

    //firebase objects
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    StepDetector simpleStepDetector;
    SensorManager sensorManager;
    Sensor accel;
    static final String TEXT_NUM_STEPS = "Number of Steps: ";
    static final String DISTANCE_TRAVELLED = "km";
    static final String CALORIES_BURNED = "calories burned";
    int numSteps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mapsBtn = (Button)findViewById(R.id.goMaps);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser fUser = mAuth.getCurrentUser();

        //database objects (firebase)
        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersdRef = databaseReference.child("users");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    //FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if(ds.equals(null)) {
                        Log.d("TAG", "nothing");

                    }else {
                        String avail = ds.child("availability").getValue(String.class);
                        //Log.d("TAG", avail);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        usersdRef.addListenerForSingleValueEvent(eventListener);

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        textView = (TextView)findViewById(R.id.count);
        distance = (TextView)findViewById(R.id.distance);
        calories = (TextView)findViewById(R.id.calBurned);

        logout = (Button)findViewById(R.id.logoutBtn);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);;


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                numSteps = 0;
                sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                sensorManager.unregisterListener(MainActivity.this);

            }
        });

        mapsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
                }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        loadUserInformation();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        textView.setText(numSteps + " " + "steps");

        distance.setText(String.format("%.2f", getDistanceRun(numSteps)) + " " +DISTANCE_TRAVELLED);
        calories.setText(String.format("%.2f", (numSteps*.045)) + " " +CALORIES_BURNED);
    }

    //function to determine the distance run in kilometers using average step length and number of steps
    public float getDistanceRun(long steps){
        float distance = (float)(steps*74)/(float)100000;
        return distance;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() == null) {
            finish();
            Intent intent =  new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public void loadUserInformation() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
    }

}
