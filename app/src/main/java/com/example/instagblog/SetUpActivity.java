package com.example.instagblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    private CircleImageView circleImageView;
    private EditText profileName_et;
    private Button save_btn;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;
    private String Uid;
    private Uri imgUri = null;
    private Toolbar setUpToolbar;
    private ProgressBar progressBar;
    private Boolean isPhotoSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        setUpToolbar = findViewById(R.id.set_up_toolbar);
        setSupportActionBar(setUpToolbar);
        getSupportActionBar().setTitle("Profile");

        progressBar =findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        circleImageView = findViewById(R.id.circleImageView);
        profileName_et =findViewById(R.id.set_up_profile_name_et);
        save_btn = findViewById(R.id.set_up_save_btn);

        auth =FirebaseAuth.getInstance();
        Uid = auth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference();

        firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users").document(Uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists()) // means if there data or not
                    {
                        String name = task.getResult().getString("name");
                        String imageUrl = task.getResult().getString("image");
                        imgUri = Uri.parse(imageUrl);

                        profileName_et.setText(name);
                        // to retrieve the image from the url require the glide library
                        Glide.with(SetUpActivity.this).load(imageUrl).into(circleImageView);
                    }
                }
            }
        });


        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   /*
                    now need to check the android version of the user's phone because earlier before
                    the marshmellow there was no need to rake permissions because permission are
                    granted by default ,but after the marshmellow we need to ask user for permission
                     */
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    //here we can check if the permission is granted or not
                    if(ContextCompat.checkSelfPermission(SetUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        //it means the permission is not granted so for that asking the permission
                        ActivityCompat.requestPermissions(SetUpActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else //it means the permission is granted
                    {
                        // start picker to get image for cropping and then use the image in cropping activity
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetUpActivity.this);
                    }
                }
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String name = profileName_et.getText().toString();
                /*
                create one child inside our storage as a profile picture and inside that it
                will have another child so that will be user id .jpg
                */
                StorageReference imgRef = storageReference.child("Profile_pics").child(Uid + ".jpg");
                if (isPhotoSelected) {
                    if (!name.isEmpty() && imgUri != null) {

                        imgRef.putFile(imgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //create one method for saving this details to the fireStore
                                            saveToFireStore(task, name, uri);
                                        }
                                    });

                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SetUpActivity.this, "Please Select picture and write your name", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    saveToFireStore(null,name,imgUri);
                }
            }
        });
    }

    private void saveToFireStore(Task<UploadTask.TaskSnapshot> task, String name, Uri downloadUri) {
     /*   if (task != null) // it means user want to upload both profile picture and name
        {
            //download url from the image
            imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    downloadUri = uri;

                }
            });
        }
        else
        {
            downloadUri  = imgUri;
        }
*/

        HashMap<String , Object> map = new HashMap<>();
        map.put("name",name);
        map.put("image",downloadUri.toString());

        firestore.collection("Users").document(Uid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity.this, "Profile Settings Save", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetUpActivity.this,MainActivity.class));
                }
                else
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imgUri = result.getUri();
                circleImageView.setImageURI(imgUri);

                isPhotoSelected = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this,result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }
}