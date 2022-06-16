package com.example.hiker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hiker.api.FirebaseApi;
import com.example.hiker.helper.ForegroundLocationUpdateService;
import com.example.hiker.model.CommentSerializable;
import com.example.hiker.model.Hike;
import com.example.hiker.model.HikeSerializable;
import com.example.hiker.model.LatLangSerializable;
import com.example.hiker.utils.MapperUtils;
import com.example.hiker.utils.SharedPrefUtils;
import com.example.hiker.utils.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.example.hiker.utils.SharedPrefUtils.getOnGoingHikeId;

public class StartHikeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;

    private Button begin;
    private Button autoTrack;
    private Button addMarker;
    private Button finish;
    private Button restart;
    private Button comment;
    LocationRequest mLocationRequest;
    private static final long UPDATE_INTERVAL = 1000;
    private static final long FASTEST_UPDATE_INTERVAL = 500;
    private static final int MAP_ZOOM = 15;
    private static final String TAG = StartHikeActivity.class.getSimpleName();
    private Uri photoPath;
    private Location mLocation;
    private String hikeId;

    // A reference to the service used to get location updates.
    private ForegroundLocationUpdateService mService =null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    int CAMERA_ACTIVITY = 111;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ForegroundLocationUpdateService.LocalBinder binder = (ForegroundLocationUpdateService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        hikeId = getOnGoingHikeId(this);
        setContentView(R.layout.activity_start_hike);
//         Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
        isLocationPermissionGranted();
    }
    @Override
    protected void onStart() {
        super.onStart();

        initLayout();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, ForegroundLocationUpdateService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(ForegroundLocationUpdateService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }
    @Override
    public void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_ACTIVITY && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            photoPath = Utils.getImageUri(getApplicationContext(), photo,UUID.randomUUID().toString());
            Log.d("ff", photoPath.toString());
        }
        else if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        photoPath = Utils.getImageUri(getApplicationContext(), photo,UUID.randomUUID().toString());
                        Log.d("ff", photoPath.toString());
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        photoPath = data.getData();
                    }
                    break;
            }
        }
    }

    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_start_hike),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(StartHikeActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(StartHikeActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    public void onLocationUpdated(Location location) {
        if (location != null) {
            Log.d("New Location", location.getLatitude() + ", " + location.getLongitude());
            SharedPrefUtils.addLocToOnGoingHike(getApplicationContext(),hikeId, location.getLatitude(), location.getLongitude());
            mLocation =location;
            LatLng newPin = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(newPin));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPin, MAP_ZOOM+5));
        }
    }

    public void isLocationPermissionGranted() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initMap();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                        onLocationUpdated(location);
                    }
                });
    }
    @SuppressLint("MissingPermission")

//    private void getLocationUpdateInBackground() {
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mLocationRequest = LocationRequest.create();
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
//    }

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location currentLocation = locationResult.getLastLocation();
            onLocationUpdated(currentLocation);
        }
    };

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
        autoTrack = findViewById(R.id.autoTrack);
        addMarker = findViewById(R.id.addMarker);
        finish = findViewById(R.id.finish);
        restart = findViewById(R.id.restart);
        comment = findViewById(R.id.comment);
        String status = Utils.getAutoTrackButtonTag(this.getApplicationContext());
        autoTrack.setTag(status);
        if (status=="1") {
            autoTrack.setText("Auto Track");
        } else {
            autoTrack.setText("Stop");
        }

        final View.OnClickListener onClickAutoTracking = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String status =(String) view.getTag();
                Log.d("AutoTrackTag", status);
                if (status=="1") { //autoTracking not started yet
                    Log.d("StartHikeActivity", "autoTrack start button clicked");
//                    getLocationUpdateInBackground();
                    mService.requestLocationUpdates();
                    autoTrack.setText("Stop");
                    view.setTag("0");
                    Utils.setAutoTrackButtonTag(getApplicationContext(), "0");
                }else{
                    Log.d("StartHikeActivity", "autoTrack stop button clicked");
//                    fusedLocationClient.removeLocationUpdates(mLocationCallback);
                    mService.removeLocationUpdates();
                    enableButtons();
                    autoTrack.setText("Auto Track");
                    Utils.setAutoTrackButtonTag(getApplicationContext(), "1");
                    view.setTag("1");
                }
            }
        } ;

        autoTrack.setOnClickListener(
                onClickAutoTracking
        );

        if (SharedPrefUtils.onGoingHike(getApplicationContext()) == null) {
            resetPage();
        } else {
            drawMarkers(SharedPrefUtils.onGoingHike(getApplicationContext()));
            enableButtons();
        }

        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("StartHikeActivity", "begin button clicked");
                getLastLocation();
                enableButtons();
            }
        });
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("StartHikeActivity", "comment button clicked");
                openCommentPopUp();
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
                mService.removeLocationUpdates();
                autoTrack.setTag("1");
                autoTrack.setText("Auto Track");
                Utils.setAutoTrackButtonTag(getApplicationContext(), "1");
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
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPin, MAP_ZOOM));
            }
        }
    }

    private void enableButtons() {
        begin.setEnabled(false);
        autoTrack.setEnabled(true);
        addMarker.setEnabled(true);
        finish.setEnabled(true);
        restart.setEnabled(true);
        comment.setEnabled(true);

        begin.setTextColor(getResources().getColor(R.color.shopSecondary));
        autoTrack.setTextColor(getResources().getColor(R.color.teal_700));
        addMarker.setTextColor(getResources().getColor(R.color.teal_700));
        finish.setTextColor(getResources().getColor(R.color.teal_700));
        restart.setTextColor(getResources().getColor(R.color.teal_700));
        comment.setTextColor(getResources().getColor(R.color.teal_700));
    }

    private void resetPage() {
        SharedPrefUtils.deleteOnGoingHike(getApplicationContext());
        Utils.setAutoTrackButtonTag( getApplicationContext(),"1");
        begin.setEnabled(true);
        autoTrack.setEnabled(false);
        addMarker.setEnabled(false);
        finish.setEnabled(false);
        restart.setEnabled(false);
        comment.setEnabled(false);

        begin.setTextColor(getResources().getColor(R.color.teal_700));
        autoTrack.setTextColor(getResources().getColor(R.color.shopSecondary));
        addMarker.setTextColor(getResources().getColor(R.color.shopSecondary));
        finish.setTextColor(getResources().getColor(R.color.shopSecondary));
        restart.setTextColor(getResources().getColor(R.color.shopSecondary));
        comment.setTextColor(getResources().getColor(R.color.shopSecondary));

        if (mMap != null) {
            mMap.clear();
        }
        autoTrack.setTag("1");
        autoTrack.setText("Auto Track");
        hikeId=getOnGoingHikeId(this);
        if (mService != null) {
            mService.removeLocationUpdates();
        }
    }

    public void addImage() {
        final CharSequence[] optionsMenu = {"Take Photo", "Choose from Gallery", "Cancel"}; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(StartHikeActivity.this);
        builder.setItems(optionsMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (optionsMenu[i].equals(optionsMenu[0])) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                } else if (optionsMenu[i].equals(optionsMenu[1])) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);
                } else if (optionsMenu[i].equals(optionsMenu[2])) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }
    private  void openCommentPopUp(){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_add_comment, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle("Add Comment");
        EditText comment = (EditText) promptView.findViewById(R.id.comment);
        Button addComment = (Button) promptView.findViewById(R.id.add);
        Button capture = (Button) promptView.findViewById(R.id.capture);
        Button back = (Button) promptView.findViewById(R.id.back);




        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("StartHikeActivity", "capture button clicked");
//                Intent intent = new Intent(StartHikeActivity.this, CameraActivity.class);
//                startActivityForResult(intent,CAMERA_ACTIVITY);
                addImage();
            }
        });
        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (comment.getText().toString().length() > 0) {
                    getLastLocation();
                    CommentSerializable commentSerializable = new CommentSerializable(comment.getText().toString(),hikeId,null, MapperUtils.convertToGeoPoint(mLocation));
                    if (photoPath != null) {
                        StorageReference ref = FirebaseApi.saveHikePictures();
                        ref.putFile(photoPath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        photoPath = uri;
                                        commentSerializable.setImageUrl(uri.toString());
                                        SharedPrefUtils.addCommentToOnGoingHike(getApplicationContext(), commentSerializable, hikeId);
                                        FirebaseApi.saveComment(commentSerializable);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("StartHikeActivity", "failed to upload image");
                                photoPath = null;
                                commentSerializable.setImageUrl(null);
                                SharedPrefUtils.addCommentToOnGoingHike(getApplicationContext(), commentSerializable, hikeId);
                                FirebaseApi.saveComment(commentSerializable);
                                Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        commentSerializable.setImageUrl(null);
                        SharedPrefUtils.addCommentToOnGoingHike(getApplicationContext(), commentSerializable, hikeId);
                        FirebaseApi.saveComment(commentSerializable);
                    }
                    comment.setText("");
                    photoPath= null;
                    alertDialog.dismiss();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment.setText("");
                photoPath = null;
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(promptView);
        alertDialog.show();
    }
    private void openSaveHikePopup() {
        HikeSerializable newHike = SharedPrefUtils.onGoingHike(getApplicationContext());
        String d = new DecimalFormat("#.0#").format(Utils.getHikeDistance(newHike.getPath()));

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_save_hike, null);
        final AlertDialog alertD = new AlertDialog.Builder(this).create();
        alertD.setTitle("Create a New Hike");
        EditText name = (EditText) promptView.findViewById(R.id.name);
        EditText distance = (EditText) promptView.findViewById(R.id.distance);
        EditText url = (EditText) promptView.findViewById(R.id.url);
        Button save = (Button) promptView.findViewById(R.id.save);
        Button back = (Button) promptView.findViewById(R.id.back);

        url.setVisibility(View.GONE);
        distance.setText(d);

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Hike hike = new Hike();
                hike.setId(hikeId);
                hike.setTitle(name.getText().toString());
                hike.setDistance(distance.getText().toString() + " m");
//                hike.setImage(url.getText().toString());
                hike.setFeatured(false);
                hike.setPopular(false);
                hike.setPath(MapperUtils.convertToGeoPoints(newHike.getPath()));
                saveHikeInFirebase(hike);
                resetPage();
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
        List<CommentSerializable> comments = SharedPrefUtils.getCommentsFromOnGoingHike(getApplicationContext(), hikeId);
        for (CommentSerializable c : comments) {
            if (c.getImageUrl() != null) {
                hike.setImage(c.getImageUrl());
                break;
            }
        }
        FirebaseApi.saveHike(hike);
        Toast.makeText(StartHikeActivity.this, "Saved your Hike", Toast.LENGTH_SHORT).show();
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d("saveHike", "DocumentSnapshot added with ID: " + documentReference.getId());
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(StartHikeActivity.this, "Saved your Hike", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("saveHike", "Error adding document", e);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(StartHikeActivity.this, "Saving Failed", Toast.LENGTH_SHORT).show();
//
//                            }
//                        });
//                    }
//                }
//                );
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
//        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(7.8731, 80.7718), MAP_ZOOM));
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(ForegroundLocationUpdateService.EXTRA_LOCATION);
            if (location != null) {
                onLocationUpdated(location);
                Toast.makeText(StartHikeActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}