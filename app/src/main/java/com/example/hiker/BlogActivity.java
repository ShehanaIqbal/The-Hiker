package com.example.hiker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.hiker.api.FirebaseApi;
import com.example.hiker.model.HikeSerializable;
import com.example.hiker.model.User;
import com.example.hiker.utils.ImageLoadTask;
import com.example.hiker.utils.MapperUtils;
import com.example.hiker.utils.SharedPrefUtils;
import com.example.hiker.utils.UserUtils;

import java.util.Arrays;
import java.util.List;

public class BlogActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    List<LatLng> path;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

        user = SharedPrefUtils.getUserFromSP(BlogActivity.this);

        TextView title = findViewById(R.id.title);
        TextView distance = findViewById(R.id.distance);
        ImageView img = findViewById(R.id.image);
        ImageView likeButton = findViewById(R.id.likeButton);

        HikeSerializable hike = (HikeSerializable) getIntent().getSerializableExtra("hike");
        if (hike == null) {
            Intent intent = new Intent(BlogActivity.this, ExploreActivity.class);
            startActivity(intent);
        } else {
            path = MapperUtils.convertToLatLang(hike.getPath());
            title.setText(hike.getTitle());
            distance.setText(hike.getDistance());
            setLikeButtonColor(likeButton, UserUtils.userFav(user, hike.getTitle()));
            // set image
            new ImageLoadTask(hike.getImage(), img).execute();

            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (user != null) {
                        if (!UserUtils.userFav(user, hike.getTitle())) {
                            FirebaseApi.saveUserLike(hike.getTitle(), user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.i("Firebase Update", "User liked a post");
                                    User updatedUser = UserUtils.updateLikes(hike.getTitle(), user);
                                    user = SharedPrefUtils.saveUserInSP(updatedUser, getApplicationContext());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Marked as Favorite", Toast.LENGTH_SHORT).show();
                                            setLikeButtonColor(likeButton, true);
                                        }
                                    });
                                }
                            });
                        } else {
                            FirebaseApi.saveUserDislike(hike.getTitle(), user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.i("Firebase Update", "User unliked a post");
                                    User updatedUser = UserUtils.updateDislikes(hike.getTitle(), user);
                                    user = SharedPrefUtils.saveUserInSP(updatedUser, getApplicationContext());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
                                            setLikeButtonColor(likeButton, false);
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void setLikeButtonColor(ImageView likeButton, boolean contains) {
        if (contains) {
            ImageViewCompat.setImageTintList(likeButton,
                    ColorStateList.valueOf(
                            ContextCompat.getColor(getApplicationContext(), R.color.haloRed)));
        } else {
            ImageViewCompat.setImageTintList(likeButton,
                    ColorStateList.valueOf(
                            ContextCompat.getColor(getApplicationContext(), R.color.white)));
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
            if (!success) {
                Log.e("HikePath", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("HikePath", "Can't find style. Error: ", e);
        }

        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(path));
        stylePolyline(polyline1);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path.get(0), 7));
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnPolylineClickListener(this);
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }

    private void stylePolyline(Polyline polyline) {
        polyline.setStartCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.ic_adjust_black), 10));

        polyline.setEndCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.ic_bullseye_black), 10));
        polyline.setWidth(10);
        polyline.setColor(Color.BLACK);

        PatternItem DOT = new Dot();
        PatternItem GAP = new Gap(20);
        List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

        polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        polyline.setJointType(JointType.ROUND);
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}