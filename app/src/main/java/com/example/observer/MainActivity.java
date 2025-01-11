package com.example.observer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.Context;
import android.Manifest;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.core.Context;

import java.util.List;

import android.location.LocationManager;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 5;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATIONS = 99;
    private static final int PERMISSIONS_REQUEST = 100;
    public static boolean SearchingOn = false;

    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address, tv_waypointCount, tv_deviceName;
    Switch sw_locationupdates, sw_gps;
    Button btn_newWaypoint, btn_showWaypointList, btn_showMap;
    //current location
    public static Location currentLocation;
    //list of saved locations
    List<Location> savedLocations;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    PendingIntent pendingIntent;
    public static FusedLocationProviderClient fusedLocationProviderClient;//client
    public static DatabaseReference reff;
    public static LocationData locData = new LocationData();

    public static FirebaseDatabase rootNode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        tv_waypointCount = findViewById(R.id.tv_waypointCount);
        tv_deviceName = findViewById(R.id.tv_deviceName);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);
        btn_newWaypoint = findViewById(R.id.btn_newWaypoint);
        btn_showWaypointList = findViewById(R.id.btn_showWaypointList);
        btn_showMap = findViewById(R.id.btn_showMap);

        reff = FirebaseDatabase.getInstance().getReference(Build.BRAND+" "+Build.DEVICE);

        locationRequest = LocationRequest.create();
        //як часто оновлюється локація
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //Check whether GPS tracking is enabled//

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            finish();
        }

        //Check whether this app has access to the location permission//

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If the location permission has been granted, then start the TrackerService//

        if (permission == PackageManager.PERMISSION_GRANTED) {
         //   startTrackerService();
        } else {

            //If the app doesn’t currently have access to the user’s location, then request access//

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUIValues(locationResult.getLastLocation());

            }
        };


        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        btn_newWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get location

                //add it to list
                MyApplication myApplication = (MyApplication) getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentLocation);
                reff.push().setValue(locData);
                Toast.makeText(myApplication, "Data sucsess", Toast.LENGTH_SHORT).show();
            }
        });

        btn_showWaypointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, deviceList.class);
                startActivity(i);
            }
        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    //most accurate(gps)
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Towers+WIFI");

                }
            }
        });
        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (sw_locationupdates.isChecked()) {
                    //tracking
                    startLocationUpdates();

                } else {
                    //not tracking
                    stopLocationUpdates();

                }
            }
        });

        updateGPS();
    }//oncreate end

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startLocationUpdates() {
        tv_updates.setText("Location is tracking");
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
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();
        if (!foregroundServiceRunning()){
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            SearchingOn=true;
            startForegroundService(serviceIntent);
            tv_deviceName.setText(
                    "" + Build.BRAND + " " + Build.DEVICE //brand+device
            );
        }
    }

    private void stopLocationUpdates() {
        tv_updates.setText("Disabled");
        tv_lat.setText("Disabled");
        tv_lon.setText("Disabled");
        tv_speed.setText("Disabled");
        tv_address.setText("Disabled");
        tv_accuracy.setText("Disabled");
        tv_altitude.setText("Disabled");
        tv_sensor.setText("Disabled");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
        SearchingOn=false;

    }
    public boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE );
        for (ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if (ForegroundService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {

        //If the permission has been granted...//

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //...then start the GPS tracking service//

        } else {

            //If the user denies the permission request, then display a toast with some more information//

            Toast.makeText(this, "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGPS() {
        //дозвіл користувача на відслідковування
        //отримання локації з клієнту
        //ОНОВЛЕННЯ ЮІ

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //дозволено
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // we got permissions, put the values of location into the UI components
                    updateUIValues(location);
                    currentLocation = location;
                    locData.setLatitude(location.getLatitude());
                    locData.setLongtitude(location.getLongitude());
                    locData.setAccuracy(location.getAccuracy());
                    locData.setAltitude(location.getAltitude());
                    locData.setSpeed(location.getSpeed());
                    locData.setAddress(tv_address.getText().toString());
                    locData.setUserDevice(Build.BRAND + " " + Build.DEVICE);
                    locData.setDate(reff.getKey());

                    reff.push().setValue(locData);
                    Toast.makeText(MainActivity.this, "This was by updateGPS", Toast.LENGTH_SHORT).show();

                }
            }

        });
        }
        else{
            //не дозволено
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATIONS);
            }
        }
    }

    private void updateUIValues(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.hasAltitude()));
        }
        else {
            tv_altitude.setText("N/a");
        }
        if (location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.hasSpeed()));
        }
        else {
            tv_speed.setText("N/a");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        }
        catch(Exception e){
            tv_address.setText("Unable to set address");
        }

        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        //show saved wp
        tv_waypointCount.setText(Integer.toString(savedLocations.size()));
    }

}