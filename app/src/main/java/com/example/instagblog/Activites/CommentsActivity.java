package com.example.instagblog.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.instagblog.Adapter.CommentsAdapter;
import com.example.instagblog.Model.Comments;
import com.example.instagblog.Model.Users;
import com.example.instagblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private EditText comment_et;

    private FirebaseFirestore firestore;
    private String currentUserId, postId;

    private CommentsAdapter adapter;
    private List<Comments> list;
    private List<Users> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        comment_et = findViewById(R.id.comments_add_comment_et);

        firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        list = new ArrayList<>();
        usersList = new ArrayList<>();
        adapter = new CommentsAdapter(CommentsActivity.this, list, usersList);

        postId = getIntent().getStringExtra("postid");

        setAdapter();

        //retrieve the comments from the cloud firestore
        firestore.collection("posts/" + postId + "/Comments").addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        Comments comments = documentChange.getDocument().toObject(Comments.class);
                        String userId = documentChange.getDocument().getString("user");

                        // .get() : so we just want this one time so that's why we are not applying snapshot listener here
                        firestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Users users = task.getResult().toObject(Users.class);
                                    usersList.add(users);
                                    list.add(comments);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(CommentsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void setAdapter() {
        RecyclerView commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        commentsRecyclerView.setHasFixedSize(true);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(adapter);
    }

    public void addcomment(View view) {
            String comment = comment_et.getText().toString();
            if (!comment.isEmpty()){
                Map<String,Object> commentsMap = new HashMap<>();
                commentsMap.put("comment",comment);
                commentsMap.put("timestamp", FieldValue.serverTimestamp()); //this will be helpful if you send the notification
                commentsMap.put("user",currentUserId);
                firestore.collection("posts/" + postId + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(CommentsActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(CommentsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else {
                Toast.makeText(CommentsActivity.this, "Please write comment !!", Toast.LENGTH_SHORT).show();
            }
        }
    }