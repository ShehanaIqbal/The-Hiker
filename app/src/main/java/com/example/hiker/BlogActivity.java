package com.example.hiker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.hiker.model.CommentSerializable;
import com.example.hiker.model.Hike;
import com.example.hiker.model.LatLangSerializable;
import com.example.hiker.utils.Utils;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.hiker.api.FirebaseApi;
import com.example.hiker.model.HikeSerializable;
import com.example.hiker.model.User;
import com.example.hiker.utils.ImageLoadTask;
import com.example.hiker.utils.MapperUtils;
import com.example.hiker.utils.SharedPrefUtils;
import com.example.hiker.utils.UserUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlogActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    List<LatLng> path;
    User user;
    private int ZOOM_LEVEL=15;
    private HikeSerializable hike;
    private List<CommentSerializable> commentSerializable;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

        user = SharedPrefUtils.getUserFromSP(BlogActivity.this);

//        TextView title = findViewById(R.id.title);
//        TextView distance = findViewById(R.id.distance);
//        ImageView img = findViewById(R.id.image);
//        ImageView likeButton = findViewById(R.id.likeButton);

        hike = (HikeSerializable) getIntent().getSerializableExtra("hike");
        if (hike == null) {
            Intent intent = new Intent(BlogActivity.this, ExploreActivity.class);
            startActivity(intent);
        } else {
            getComments();
            path = MapperUtils.convertToLatLang(hike.getPath());
//            title.setText(hike.getTitle());
//            distance.setText(hike.getDistance());
//            setLikeButtonColor(likeButton, UserUtils.userFav(user, hike.getTitle()));
            // set image
            //new ImageLoadTask(hike.getImage(), img).execute();

            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    private void createHorizontalCard(HikeSerializable hike,  List<CommentSerializable> commentSerializable) {
        LatLangSerializable firstPoint = hike.getPath().get(0);
        ArrayList<String> allImageURL = new ArrayList();
        ArrayList<String> allComments = new ArrayList();
        List<LatLangSerializable> allGeoPoints = hike.getPath();

//        allImageURL.addAll(Arrays.asList("https://destinationlesstravel.com/wp-content/uploads/2019/05/DSC_9600-1-1024x684-1024x684.jpg","https://destinationlesstravel.com/wp-content/uploads/2020/04/Ella-train.jpg.webp"));
//        allComments.addAll(Arrays.asList("asd","qwe"));
//        allGeoPoints.addAll(Arrays.asList(null,null));
        allComments.addAll(Collections.nCopies(path.size(), ""));
        allImageURL.addAll(Collections.nCopies(path.size(), null));

        LinearLayout linearLayout = findViewById(R.id.horizontal_linear);
        linearLayout.removeAllViews();
        for (int i=0;i<commentSerializable.size();i++) {
            GeoPoint commentGeoPoint = commentSerializable.get(i).getLocation();
            LatLangSerializable commentLatLang = new LatLangSerializable(commentGeoPoint.getLatitude(), commentGeoPoint.getLongitude());
            for (int j=0;j<allGeoPoints.size();j++) {
                if (Utils.getDistance(commentLatLang, allGeoPoints.get(j)) < 10) {
                    if(allComments.get(j) == "") {
                        allComments.set(j, commentSerializable.get(i).getComment());
                        allImageURL.set(j, commentSerializable.get(i).getImageUrl());
                        break;
                    }
                }
            }
        }
        double totalDistance = Utils.getHikeDistance(allGeoPoints);
        for (int i=0;i<hike.getPath().size();i++) {
            double completedDistance = Utils.getHikeDistance(allGeoPoints.subList(0, i+1));
//          completedDistance to string
            linearLayout.addView(horizontalCard(allImageURL.get(i), allComments.get(i),allGeoPoints.get(i) ,new DecimalFormat("#.0#").format(completedDistance),new DecimalFormat("#.0#").format(totalDistance), hike, linearLayout));
        }
    }

    private View horizontalCard(String url, String comment, LatLangSerializable geoPoint, String CompletedDistance , String TotalDistance, HikeSerializable hike, LinearLayout mainLayout) {
        LayoutInflater inflater = getLayoutInflater();
        View myLayout = inflater.inflate(R.layout.layout_blog_card, mainLayout, false);
        TextView title = myLayout.findViewById(R.id.title);
        TextView distance = myLayout.findViewById(R.id.distance);
        ImageView img = myLayout.findViewById(R.id.image);
        ImageView likeButton = myLayout.findViewById(R.id.likeButton);
        TextView tags   = myLayout.findViewById(R.id.tags);
        CardView cardView = myLayout.findViewById(R.id.card_view);

        tags.setText(hike.getTitle());
        title.setText(comment);

        distance.setText("Remaining"+ " " + CompletedDistance + "/" + TotalDistance + "m");
        setLikeButtonColor(likeButton, UserUtils.userFav(user, hike.getTitle()));
        likeButton.setVisibility(View.GONE);

        // set image
        if (url == null) {
            cardView.setVisibility(View.GONE);
        }else {
            new ImageLoadTask(url, img).execute();
        }
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

        myLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Clicked on " + comment , Toast.LENGTH_SHORT).show();
                Log.i("Clicked on", comment + " " + geoPoint.getLatitude() + " " + geoPoint.getLongitude());
                if (geoPoint != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()), ZOOM_LEVEL+5));
                }
            }
        });
        return myLayout;
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
        mMap = googleMap;
        try {
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
            if (!success) {
                Log.e("HikePath", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("HikePath", "Can't find style. Error: ", e);
        }

        Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(path));
        stylePolyline(polyline1);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path.get(0), ZOOM_LEVEL));
        mMap.setMyLocationEnabled(true);
        mMap.setOnPolylineClickListener(this);
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

    private void getComments(){
        FirebaseApi.getHikeComments(hike.getId()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    commentSerializable = queryDocumentSnapshots.toObjects(CommentSerializable.class);
                    for (CommentSerializable comment : commentSerializable) {
                        Log.i("Comment", comment.getComment() + " "+ comment.getLocation().getLatitude()+ " "+ comment.getLocation().getLongitude()+" " + comment.getImageUrl());
                    }
                    Log.i("Comment", "Success");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initCommentUi();
                        }
                    });
                }
                else {
                    commentSerializable= new ArrayList<>();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BlogActivity.this, "Issue loading comments", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                createHorizontalCard(hike,commentSerializable);
            }
        });
    }

    private void initCommentUi() {
        Log.d("HikePath", "initCommentUi: " + commentSerializable.size());
//        TODO : add bottom sheet to show comments with images
    }
}