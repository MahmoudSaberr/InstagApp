package com.example.instagblog.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagblog.Activites.CommentsActivity;
import com.example.instagblog.Model.Post;
import com.example.instagblog.Model.Users;
import com.example.instagblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> mList;
    private List<Users> usersList;
    private Activity context;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public PostAdapter(Activity context ,List<Post> mList,List<Users> usersList) {
        this.mList = mList;
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.each_post,parent,false);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = mList.get(position);
        holder.setPostImage(post.getImage());
        holder.setPostCaption(post.getCaption());

        long milliseconds =post.getTime().getTime();
        /*
        -first getTime() is coming from our model class that we have stored in file store
        -Second getTime() will get the exact time from first getTime() value
         */
        String date = DateFormat.format("MM/dd/yyyy" , new Date(milliseconds)).toString();
        holder.setPostDate(date);

        /*
        //comment this to fix our performance

        String userId = post.getUser();
            firestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
        */

        //retrieve username & image
        String username = usersList.get(position).getName();
        String image = usersList.get(position).getImage();

        //set this value to our holder
        holder.setPostUsername(username);
        holder.setProfilePic(image);

        /*
                   //comment this to fix our performance
           }
                else
                {
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        //Like_btn (iv)
        String postId = post.PostId;
        String currentUserId = auth.getCurrentUser().getUid();
        holder.postLike_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // here ew need to create sub collection for likes
                firestore.collection("posts/" + postId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                // Note: we are just passing this current user id for just verifying if there is data or not
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){ // if there is no data we just set it
                            Map<String ,Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp()); //this will be helpful if you send the notification
                            firestore.collection("posts/" + postId + "/Likes").document(currentUserId).set(likesMap);
                        }
                        else //there is already existing likes of the same user
                        {
                            //we will just remove that
                            firestore.collection("posts/" + postId + "/Likes").document(currentUserId).delete();
                        }
                    }
                });
            }
        });

        //like color change
        firestore.collection("posts/" + postId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                /*
                we need to check if this error is equal to null because this is @Nullable error so
                we need to check it first, if it doesn't handle this what will happen >> if the user
                clicks on the logout so the app will be crashed because this query will be still
                running and it will pass the null pointer exception , so we need to check that out
                 */
                if (error == null){
                    //then only run this queries
                    if(value.exists()) //means there is data
                    {
                        holder.postLike_iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_after_liked));
                    }
                    else
                    {
                        holder.postLike_iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_before_liked));
                    }
                }
            }
        });

        //likes count
        firestore.collection("posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (!value.isEmpty()){
                        int  count = value.size(); // it will give us the number od documents in sub collection likes
                        holder.setPostLikes(count);
                    }
                    else {
                        holder.setPostLikes(0);
                    }
                }
            }
        });

        //comment implementation
        holder.postComment_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(context , CommentsActivity.class);
                commentIntent.putExtra("postid",postId);
                context.startActivity(commentIntent);

            }
        });

        //delete post
        if (currentUserId.equals(post.getUser())){ // check the login user is equals th user of the post
            holder.postDelete_btn.setVisibility(View.VISIBLE);
            holder.postDelete_btn.setClickable(true);
            holder.postDelete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // we will first display the alert dialog to user
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Delete")
                            .setMessage("Are You Sure ?")
                            .setNegativeButton("No",null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    //before delete the post , we will delete the sub collections( Likes & Comments)
                                    firestore.collection("posts/" + postId + "/Comments").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for (QueryDocumentSnapshot snapshot : task.getResult())
                                                    {
                                                        // .document () : to delete each document
                                                        firestore.collection("posts/"+ postId +"/Comments").document(snapshot.getId()).delete();
                                                    }
                                                }
                                            });

                                    firestore.collection("posts/" + postId + "/Likes").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for (QueryDocumentSnapshot snapshot : task.getResult())
                                                    {
                                                        // .document () : to delete each document
                                                        firestore.collection("posts/"+ postId +"/Likes").document(snapshot.getId()).delete();
                                                    }
                                                }
                                            });

                                    //it only delete fields , but not delete sub collection
                                    firestore.collection("posts").document(postId).delete();
                                    mList.remove(position);
                                    notifyDataSetChanged();
                                }
                            });
                    alert.show();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{
        ImageView postImage_iv,postLike_iv,postComment_iv;
        CircleImageView profilePic_iv;
        TextView postUsername_tv ,posDate_tv,postCaption_tv,postLikes_tv;
        ImageButton postDelete_btn;
        View view;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            postLike_iv = view.findViewById(R.id.each_post_like_iv);
            postComment_iv = view.findViewById(R.id.each_post_comment_iv);
            postDelete_btn = view.findViewById(R.id.each_post_delete_ib);
        }

        public void setPostLikes(int count)
        {
            postLikes_tv = view.findViewById(R.id.each_post_like_count_tv);
            postLikes_tv.setText(count + " Likes");
        }

        public void setPostImage(String urlPost)
        {
            postImage_iv = view.findViewById(R.id.each_post_image_iv);
            //use glide library to set the image
            Glide.with(context).load(urlPost).into(postImage_iv);
        }

        public void setProfilePic(String urlProfile)
        {
            profilePic_iv = view.findViewById(R.id.each_post_profile_pic_iv);
            //use glide library to set the image
            Glide.with(context).load(urlProfile).into(profilePic_iv);
        }

        public void setPostUsername(String username){
            postUsername_tv = view.findViewById(R.id.each_post_username_tv);
            postUsername_tv.setText(username);
        }

        public void setPostDate(String date){
            posDate_tv = view.findViewById(R.id.each_post_date_tv);
            posDate_tv.setText(date);
        }

        public void setPostCaption(String caption){
            postCaption_tv = view.findViewById(R.id.each_post_caption_tv);
            postCaption_tv.setText(caption);
        }

    }
}
