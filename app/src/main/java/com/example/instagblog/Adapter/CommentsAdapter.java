package com.example.instagblog.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagblog.Model.Comments;
import com.example.instagblog.Model.Users;
import com.example.instagblog.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Activity context;
    private List<Comments> commentsList;
    private List<Users> usersList;
    public CommentsAdapter(Activity context, List<Comments> commentsList,List<Users> usersList) {
        this.context = context;
        this.commentsList = commentsList;
        this.usersList =usersList;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_comment,parent,false);

        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        Comments comments = commentsList.get(position);
        holder.setComment(comments.getComment());

        Users users =usersList.get(position);
        holder.setUsername(users.getName());
        holder.setCircleImageView(users.getImage());

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder{
        TextView  mComment , mUsername;
        View v;
        CircleImageView circleImageView;


        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
        }

        public  void setComment(String comment){
            mComment = v.findViewById(R.id.each_comment_tv);
            mComment.setText(comment);
        }

        public void  setUsername (String username){
            mUsername = v.findViewById(R.id.each_comment_username_tv);
            mUsername.setText(username);
        }

        public void setCircleImageView(String profilePic){
            circleImageView = v.findViewById(R.id.each_comment_profile_pic_iv);
            //use glide library to set the image
            Glide.with(context).load(profilePic).into(circleImageView);
        }

    }
}
