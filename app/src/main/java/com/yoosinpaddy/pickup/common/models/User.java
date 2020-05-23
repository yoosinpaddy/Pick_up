package com.yoosinpaddy.pickup.common.models;

public class User {
    String Displayname;


    String Email;
    long createdAt;
    String Uid;

    public User (){};
    public User(String displayname,String email,long createdAt, String Uid){
        this.Displayname=displayname;
        this.Email=email;
        this.createdAt=createdAt;
    }


    public String getDisplayname() {
        return Displayname;
    }

    public String getEmail() {
        return Email;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
