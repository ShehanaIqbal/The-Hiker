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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.List;

public class BlogActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    List<LatLng> path;
    User user;
    private int ZOOM_LEVEL=15;
    private HikeSerializable hike;
    private List<CommentSerializable> commentSerializable;
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
            String[] allImageURL ={"https://static.saltinourhair.com/wp-content/uploads/2016/11/23155637/Things-to-do-Ella-Sri-Lanka-Nine-arch-bridge-view-2.jpg","https://destinationlesstravel.com/wp-content/uploads/2019/05/DSC_9600-1-1024x684-1024x684.jpg","https://destinationlesstravel.com/wp-content/uploads/2020/04/Ella-train.jpg.webp"};
            String[] allComments = {"asd","qwe",""};
            createHorizontalCard(hike, allImageURL, allComments);
            //new ImageLoadTask(hike.getImage(), img).execute();

            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
//
//            likeButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (user != null) {
//                        if (!UserUtils.userFav(user, hike.getTitle())) {
//                            FirebaseApi.saveUserLike(hike.getTitle(), user).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void unused) {
//                                    Log.i("Firebase Update", "User liked a post");
//                                    User updatedUser = UserUtils.updateLikes(hike.getTitle(), user);
//                                    user = SharedPrefUtils.saveUserInSP(updatedUser, getApplicationContext());
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(getApplicationContext(), "Marked as Favorite", Toast.LENGTH_SHORT).show();
//                                            setLikeButtonColor(likeButton, true);
//                                        }
//                                    });
//                                }
//                            });
//                        } else {
//                            FirebaseApi.saveUserDislike(hike.getTitle(), user).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void unused) {
//                                    Log.i("Firebase Update", "User unliked a post");
//                                    User updatedUser = UserUtils.updateDislikes(hike.getTitle(), user);
//                                    user = SharedPrefUtils.saveUserInSP(updatedUser, getApplicationContext());
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(getApplicationContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
//                                            setLikeButtonColor(likeButton, false);
//                                        }
//                                    });
//                                }
//                            });
//                        }
//                    }
//                }
//            });
//
        }
    }

    private void createHorizontalCard(HikeSerializable hike, String[] allImageURL, String[] allComments) {
        LinearLayout linearLayout = findViewById(R.id.horizontal_linear);
        linearLayout.removeAllViews();
        for (int i=0;i<allImageURL.length;i++) {
            linearLayout.addView(horizontalCard(allImageURL[i], allComments[i], hike, linearLayout));
        }
    }

    private View horizontalCard(String url, String comment, HikeSerializable hike, LinearLayout mainLayout) {
        LayoutInflater inflater = getLayoutInflater();
        View myLayout = inflater.inflate(R.layout.layout_blog_card, mainLayout, false);
        TextView title = myLayout.findViewById(R.id.title);
        TextView distance = myLayout.findViewById(R.id.distance);
        ImageView img = myLayout.findViewById(R.id.image);
        ImageView likeButton = myLayout.findViewById(R.id.likeButton);

        title.setText(hike.getTitle());
        distance.setText(comment);
        setLikeButtonColor(likeButton, UserUtils.userFav(user, hike.getTitle()));

        // set image
        new ImageLoadTask(url, img).execute();

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
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path.get(0), ZOOM_LEVEL));
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

    private void getComments(){
        FirebaseApi.getHikeComments(hike.getId()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    commentSerializable = queryDocumentSnapshots.toObjects(CommentSerializable.class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initCommentUi();
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BlogActivity.this, "Issue loading comments", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void initCommentUi() {
        Log.d("HikePath", "initCommentUi: " + commentSerializable.size());
//        TODO : add bottom sheet to show comments with images
    }
}