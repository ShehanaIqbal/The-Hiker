package com.example.hiker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.example.hiker.api.FirebaseApi;
import com.example.hiker.model.Hike;
import com.example.hiker.model.HikeSerializable;
import com.example.hiker.model.LatLangSerializable;
import com.example.hiker.utils.MapperUtils;
import com.example.hiker.utils.SharedPrefUtils;

public class StartHikeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;

    private Button begin;
    private Button addMarker;
    private Button finish;
    private Button restart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_hike);

        isLocationPermissionGranted();
    }

    public void isLocationPermissionGranted() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initMap();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initMap();
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
            Toast.makeText(this, "Please Grant the Permission", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.e("New Location", location.getLatitude() + ", " + location.getLongitude());
                            SharedPrefUtils.addLocToOnGoingHike(getApplicationContext(), location.getLatitude(), location.getLongitude());

                            LatLng newPin = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(newPin));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPin, 5));
                        }
                    }
                });
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }

    private void initMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initLayout() {
        // get reference to all buttons and set the state
        begin = findViewById(R.id.begin);
        addMarker = findViewById(R.id.addMarker);
        finish = findViewById(R.id.finish);
        restart = findViewById(R.id.restart);

        if (SharedPrefUtils.onGoingHike(getApplicationContext()) == null) {
            resetPage();
        } else {
            drawMarkers(SharedPrefUtils.onGoingHike(getApplicationContext()));
            enableButtons();
        }

        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastLocation();
                enableButtons();
            }
        });

        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastLocation();
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSaveHikePopup();
            }
        });

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPage();
            }
        });
    }

    private void drawMarkers(HikeSerializable hike) {
        if (mMap !=  null) {
            LatLng newPin = null;
            for (LatLangSerializable location : hike.getPath()) {
                newPin = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(newPin));
            }
            if (newPin != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPin, 5));
            }
        }
    }

    private void enableButtons() {
        begin.setEnabled(false);
        addMarker.setEnabled(true);
        finish.setEnabled(true);
        restart.setEnabled(true);

        begin.setTextColor(getResources().getColor(R.color.shopSecondary));
        addMarker.setTextColor(getResources().getColor(R.color.teal_700));
        finish.setTextColor(getResources().getColor(R.color.teal_700));
        restart.setTextColor(getResources().getColor(R.color.teal_700));
    }

    private void resetPage() {
        SharedPrefUtils.deleteOnGoingHike(getApplicationContext());
        begin.setEnabled(true);
        addMarker.setEnabled(false);
        finish.setEnabled(false);
        restart.setEnabled(false);

        begin.setTextColor(getResources().getColor(R.color.teal_700));
        addMarker.setTextColor(getResources().getColor(R.color.shopSecondary));
        finish.setTextColor(getResources().getColor(R.color.shopSecondary));
        restart.setTextColor(getResources().getColor(R.color.shopSecondary));

        if (mMap != null) {
            mMap.clear();
        }
    }

    private void openSaveHikePopup() {
        HikeSerializable newHike = SharedPrefUtils.onGoingHike(getApplicationContext());

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_save_hike, null);
        final AlertDialog alertD = new AlertDialog.Builder(this).create();
        alertD.setTitle("Create a New Hike");
        EditText name = (EditText) promptView.findViewById(R.id.name);
        EditText distance = (EditText) promptView.findViewById(R.id.distance);
        EditText url = (EditText) promptView.findViewById(R.id.url);
        Button save = (Button) promptView.findViewById(R.id.save);
        Button back = (Button) promptView.findViewById(R.id.back);

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Hike hike = new Hike();
                hike.setTitle(name.getText().toString());
                hike.setDistance(distance.getText().toString() + " Miles");
                hike.setImage(url.getText().toString());
                hike.setFeatured(false);
                hike.setPopular(false);
                hike.setPath(MapperUtils.convertToGeoPoint(newHike.getPath()));
                saveHikeInFirebase(hike);
                alertD.cancel();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertD.cancel();
            }
        });
        alertD.setView(promptView);
        alertD.show();
    }

    private void saveHikeInFirebase(Hike hike) {
        FirebaseApi.saveHike(hike)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("saveHike", "DocumentSnapshot added with ID: " + documentReference.getId());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(StartHikeActivity.this, "Saved your Hike", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("saveHike", "Error adding document", e);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(StartHikeActivity.this, "Saving Failed", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));
            if (!success) {
                Log.e("HikePath", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("HikePath", "Can't find style. Error: ", e);
        }

        initLayout();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.9631884661419, -1.174854825044051), 5));
    }
}