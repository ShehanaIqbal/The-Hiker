package com.example.hiker;

import android.app.Activity;
import android.content.Intent;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

import androidx.annotation.Nullable;

import static com.example.hiker.UtilHelper.getEncodedString;

public class CameraActivity extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 300;
    private static final int REQUEST_LOAD_IMAGE = 301 ;
    View buttonFromGallery,buttonFromCamera;
    ImageView imageView;
    Bitmap photo;
    String blob;  

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void selectImage() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        imageView = findViewById(R.id.image_preview);
        buttonFromCamera = findViewById(R.id.take_image_button);
        buttonFromGallery = findViewById(R.id.select_image_button);
    }


    @Override
    public void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            blob = getEncodedString(photo);
            Log.d("ff",blob);
            setResult(RESULT_OK,data);
            imageView.setImageBitmap(photo);
            finish();
        }
        if (requestCode == REQUEST_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {
            Log.d("URI",blob);

            Uri imageUri = (Uri) data.getData();
            Log.d("imageUri", String.valueOf(imageUri));

            try {
                photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
                blob = getEncodedString(photo);
                setResult(RESULT_OK,data);
                imageView.setImageBitmap(photo);
            } catch (IOException e) {
                e.printStackTrace();
            }

            finish();
        }
    }


    public void takeFromCamera(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    public void selectFromGallery(View view) {
        Intent cameraIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(cameraIntent, REQUEST_LOAD_IMAGE);
    }
}


//on activity calling camera activity

//call below
//Intent intent = new Intent(this, CameraActivity.class);
//
//startActivityForResult(intent,CAMERA_ACTIVITY);
//int CAMERA_ACTIVITY = 1;
//and override below
//@Override
//public void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CAMERA_ACTIVITY && resultCode == Activity.RESULT_OK) {
//        Bitmap photo = (Bitmap) data.getExtras().get("data");
//        String blob = getEncodedString(photo);
//        Log.d("ff", blob);
//        setResult(RESULT_OK, data);
//        }
//        }
