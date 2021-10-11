package com.example.instagblog.Model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

//this class will be helpful to hold the id
public class PostId {

    //simply exclude the field to not store in the cloud file store because it is already there
    @Exclude
    public String PostId;

    // Generic Constructor ( it must have at least one a generic type parameter
    public <T extends PostId> T withId (@NonNull final String id){
     this.PostId = id;
     return (T) this;
    }
}
