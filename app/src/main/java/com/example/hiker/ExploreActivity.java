package com.example.hiker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.hiker.api.FirebaseApi;
import com.example.hiker.model.Hike;
import com.example.hiker.model.User;
import com.example.hiker.utils.ImageLoadTask;
import com.example.hiker.utils.MapperUtils;
import com.example.hiker.utils.SharedPrefUtils;
import com.example.hiker.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    private List<Hike> filteredData;
    private List<Hike> allData;
    private List<Hike> favHikes;
    private List<Hike> popHikes;
    private List<Hike> hikes;
    private User user;

    private ProgressBar progressBar1;
    private ProgressBar progressBar2;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        // Show progress bar
        progressBar1 = findViewById(R.id.loading1);
        progressBar2 = findViewById(R.id.loading2);
        editText = findViewById(R.id.search);
        progressBar1.setVisibility(View.VISIBLE);
        progressBar2.setVisibility(View.VISIBLE);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filteredData = new ArrayList<>();
                for (Hike hike : allData) {
                    if (hike.getTitle().toLowerCase().startsWith(editable.toString())) {
                        filteredData.add(hike);
                    }
                }
                if (allData.size() > 0) {
                    initUI();
                }
            }
        });


        // Check login
        user = SharedPrefUtils.getUserFromSP(this);
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            getData();
        }
    }

    private void initUI() {
        favHikes = new ArrayList<>();
        popHikes = new ArrayList<>();
        hikes = new ArrayList<>();

        for (Hike hike : filteredData) {
            if (UserUtils.userFav(user, hike)) {
                favHikes.add(hike);
            } else if (hike.getPopular()) {
                popHikes.add(hike);
            } else {
                hikes.add(hike);
            }
        }

        TextView favText = findViewById(R.id.favouriteHikesText);
        if (favHikes.size() == 0) {
            favText.setVisibility(View.GONE);
        } else {
            favText.setVisibility(View.VISIBLE);
        }

        TextView popText = findViewById(R.id.popHikesText);
        if (popHikes.size() == 0) {
            popText.setVisibility(View.GONE);
        } else {
            popText.setVisibility(View.VISIBLE);
        }

        LinearLayout favLinearLayout = findViewById(R.id.fav_horizontal_linear);
        favLinearLayout.removeAllViews();
        for (Hike favHike : favHikes) {
            favLinearLayout.addView(horizontalViewCard(favHike, favLinearLayout));
        }

        LinearLayout popLinearLayout = findViewById(R.id.pop_horizontal_linear);
        popLinearLayout.removeAllViews();
        for (Hike popHike : popHikes) {
            popLinearLayout.addView(horizontalViewCard(popHike, popLinearLayout));
        }

        LinearLayout otherLinearLayout = findViewById(R.id.vertical_linear);
        otherLinearLayout.removeAllViews();
        for (Hike hike : hikes) {
            otherLinearLayout.addView(verticalViewCard(hike, otherLinearLayout));
        }

        progressBar1.setVisibility(View.GONE);
        progressBar2.setVisibility(View.GONE);
    }

    private void getData() {
        FirebaseApi.getAllHikes().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Hike> hikes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        hikes.add(document.toObject(Hike.class));
                    }
                    allData = hikes;
                    filteredData = hikes;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (allData.size() > 0) {
                                initUI();
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExploreActivity.this, "Please Restart the App", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private View verticalViewCard(Hike hike, LinearLayout mainLayout) {
        LayoutInflater inflater = getLayoutInflater();
        View myLayout = inflater.inflate(R.layout.item_explore_horizontal, mainLayout, false);
        TextView title = myLayout.findViewById(R.id.title);
        TextView distance = myLayout.findViewById(R.id.distance);
        ImageView img = myLayout.findViewById(R.id.image);
        CardView root = myLayout.findViewById(R.id.rootLayout);
        ImageView likeButton = myLayout.findViewById(R.id.likeButton);

        title.setText(hike.getTitle());
        distance.setText(hike.getDistance());
        setLikeButtonColor(likeButton, UserUtils.userFav(user, hike));

        // set image
        new ImageLoadTask(hike.getImage(), img).execute();

        View.OnClickListener cardClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExploreActivity.this, BlogActivity.class);
                intent.putExtra("hike", MapperUtils.convertToSerializable(hike));
                startActivity(intent);
            }
        };

        root.setOnClickListener(cardClick);
        img.setOnClickListener(cardClick);

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null) {
                    if (!UserUtils.userFav(user, hike)) {
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
        return myLayout;
    }

    private View horizontalViewCard(Hike hike, LinearLayout mainLayout) {
        LayoutInflater inflater = getLayoutInflater();
        View myLayout = inflater.inflate(R.layout.item_explore_carousel_card, mainLayout, false);
        TextView title = myLayout.findViewById(R.id.title);
        TextView distance = myLayout.findViewById(R.id.distance);
        ImageView img = myLayout.findViewById(R.id.image);
        LinearLayout root = myLayout.findViewById(R.id.rootLayout);
        ImageView likeButton = myLayout.findViewById(R.id.likeButton);

        title.setText(hike.getTitle());
        distance.setText(hike.getDistance());
        setLikeButtonColor(likeButton, UserUtils.userFav(user, hike));

        // set image
        new ImageLoadTask(hike.getImage(), img).execute();

        View.OnClickListener cardClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExploreActivity.this, BlogActivity.class);
                intent.putExtra("hike", MapperUtils.convertToSerializable(hike));
                startActivity(intent);
            }
        };

        root.setOnClickListener(cardClick);
        img.setOnClickListener(cardClick);

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null) {
                    if (!UserUtils.userFav(user, hike)) {
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

    public void onBackPressed(View view) {
        onBackPressed();
    }
}