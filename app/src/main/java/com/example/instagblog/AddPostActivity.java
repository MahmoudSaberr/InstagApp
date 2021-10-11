package com.example.instagblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {
    private Toolbar addPostToolbar;
    private Button addPost_btn;
    private EditText postCaption_et;
    private ImageView postImage_iv;
    private ProgressBar progressBar;
    private Uri postImgUri = null;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        addPostToolbar = findViewById(R.id.add_post_toolbar);
        setSupportActionBar(addPostToolbar);
        getSupportActionBar().setTitle("Add Post");

        progressBar = findViewById(R.id.post_progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        addPost_btn = findViewById(R.id.post_save_btn);
        postCaption_et = findViewById(R.id.post_caption_et);
        postImage_iv = findViewById(R.id.post_image_iv);


        auth =FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference();

        firestore = FirebaseFirestore.getInstance();

        postImage_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(3,2)
                        .setMinCropResultSize(512,512)
                        .start(AddPostActivity.this);
            }
        });

        addPost_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String caption = postCaption_et.getText().toString();
                if (!caption.isEmpty() && postImgUri != null)
                {
                    StorageReference postRef = storageReference
                                    .child("post_images")
                                    .child(FieldValue.serverTimestamp().toString() + ".jpg");
                    postRef.putFile(postImgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful())
                            {
                                postRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        HashMap<String,Object> postMap = new HashMap<>();
                                        postMap.put("image",uri.toString());
                                        postMap.put("user",currentUserId);
                                        postMap.put("caption",caption);
                                        postMap.put("time",FieldValue.serverTimestamp()); //time will be helpful when we will retrieve the data

                                        firestore.collection("posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful())
                                                {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(AddPostActivity.this, "Post Added Successfully!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(AddPostActivity.this,MainActivity.class));
                                                    finish();
                                                }
                                                else
                                                {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(AddPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                            else
                            {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(AddPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(AddPostActivity.this, "Please Add Image and Write Your Caption!", Toast.LENGTH_SHORT).show();
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
                postImgUri = result.getUri();
                postImage_iv.setImageURI(postImgUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this,result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}