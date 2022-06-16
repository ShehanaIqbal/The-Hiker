package com.example.hiker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.hiker.api.FirebaseApi;
import com.example.hiker.model.Hike;
import com.example.hiker.adapter.CarouselAdapter;
import com.example.hiker.model.User;
import com.example.hiker.utils.ImageLoadTask;
import com.example.hiker.utils.SharedPrefUtils;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DiscreteScrollView.OnItemChangedListener<CarouselAdapter.ViewHolder> {

    private List<Hike> data;

    private TextView currentItemName;
    private TextView currentItemDistance;
    private ImageView profilePic;
    private DiscreteScrollView itemPicker;
    private InfiniteScrollAdapter<?> infiniteAdapter;
    private ProgressBar progressBar;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentItemName = findViewById(R.id.item_name);
        currentItemDistance = findViewById(R.id.item_distance);
        profilePic = findViewById(R.id.profile_image);
        context=getApplicationContext();
        // Check login
        User user = SharedPrefUtils.getUserFromSP(this);
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            if (!"".equals(user.getImage())) {
                new ImageLoadTask(user.getImage(), profilePic).execute();
            }
            // get all hikes from Firebase
            getData();

            // set listeners
            profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
                }
            });
        }

        // Show progress bar
        progressBar = findViewById(R.id.loading);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void initUI() {
        itemPicker = findViewById(R.id.item_picker);
        itemPicker.setOrientation(DSVOrientation.HORIZONTAL);
        itemPicker.addOnItemChangedListener(this);
        infiniteAdapter = InfiniteScrollAdapter.wrap(new CarouselAdapter(data));
        itemPicker.setAdapter(infiniteAdapter);
        itemPicker.setItemTransitionTimeMillis(150);
        itemPicker.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());
        onItemChanged(data.get(0));

        progressBar.setVisibility(View.GONE);
    }


    private void onItemChanged(Hike hike) {
        currentItemName.setText(hike.getTitle());
        currentItemDistance.setText(hike.getDistance());
    }

    private void getData() {
        FirebaseApi.getAllHikes().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Hike> hikes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Hike hike = document.toObject(Hike.class);
                        if (hike.getFeatured()) {
                            hikes.add(hike);
                        }
                    }
                    data = hikes;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initUI();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Please Restart the App", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onCurrentItemChanged(@Nullable CarouselAdapter.ViewHolder viewHolder, int adapterPosition) {
        int positionInDataSet = infiniteAdapter.getRealPosition(adapterPosition);
        onItemChanged(data.get(positionInDataSet));
    }

    public void exploreClicked(View view) {
        Intent intent = new Intent(MainActivity.this, ExploreActivity.class);
        startActivity(intent);
    }

    public void startHikeClicked(View view) {
        Intent intent = new Intent(MainActivity.this, StartHikeActivity.class);
        startActivity(intent);
    }

    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}