package com.example.hiker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.hiker.api.FirebaseApi;
import com.example.hiker.model.User;
import com.example.hiker.utils.ImageLoadTask;
import com.example.hiker.utils.SharedPrefUtils;
import com.example.hiker.utils.Utils;

public class EditProfileActivity extends AppCompatActivity {

    TextView email;
    TextView password;
    TextView name;
    ProgressBar progressBar;
    ImageView profilePic;

    private FirebaseAuth mAuth;

    private Uri filePath;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        progressBar = findViewById(R.id.loading);
        profilePic = findViewById(R.id.profile_image);

        user = SharedPrefUtils.getUserFromSP(this);
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            initUI();
        }

        progressBar.setVisibility(View.INVISIBLE);
    }

    private void initUI() {
        email.setText(user.getEmail());
        name.setText(user.getName());
        if (!"".equals(user.getImage())) {
            new ImageLoadTask(user.getImage(), profilePic).execute();
        }
    }

    public void discardChangesClicked(View view) {
        startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
    }

    public void saveChangesClicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        String _password = password.getText().toString();

        if (_password.equals("")) {
            updateProfile();
        } else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            user.updatePassword(_password).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("EditProfileActivity", "updateUserWithEmail:success");
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    updateProfile();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("EditProfileActivity", "updateUserWithEmail:failure", e);
                    Toast.makeText(EditProfileActivity.this, "Updating user failed.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private void updateProfile() {
        if (filePath != null && !filePath.equals(user.getImage())) {
            StorageReference ref = FirebaseApi.saveProfilePicture();
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Image uploaded successfully
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String profileUrl = uri.toString();
                            saveUser(profileUrl);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Error, Image not uploaded
                    Toast.makeText(EditProfileActivity.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveUser(filePath != null ? filePath.toString() : "");
        }
    }

    private void saveUser(String filePath) {
        String _name = name.getText().toString();
        String _email = email.getText().toString();

        user.setName(_name);
        user.setEmail(_email);
        user.setImage(filePath);

        FirebaseApi.updateUser(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("updateUser", user.getId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPrefUtils.saveUserInSP(user, EditProfileActivity.this);
                        progressBar.setVisibility(View.INVISIBLE);
                        startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("updateUser", "Error adding document", e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditProfileActivity.this, "Updating user failed.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    public void onProfilePicClicked(View view) {
        final CharSequence[] optionsMenu = {"Take Photo", "Choose from Gallery", "Cancel"}; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        profilePic.setImageBitmap(selectedImage);
                        filePath = Utils.getImageUri(getApplicationContext(), selectedImage, "hiker-dp");
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        filePath = data.getData();
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                profilePic.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }

    public void logoutClicked(View view) {
        SharedPrefUtils.saveUserInSP(null, getApplicationContext());
        mAuth.signOut();
        startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
    }
}