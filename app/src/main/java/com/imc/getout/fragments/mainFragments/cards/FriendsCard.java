package com.imc.getout.fragments.mainFragments.cards;

import java.util.ArrayList;

public class FriendsCard {

    private String name,username,photoUri,uid;
    private ArrayList<String> myFriends,userFriends;

    public FriendsCard(String name, String username, String photoUri, String uid,ArrayList<String> myFriends,ArrayList<String> userFriends) {
        this.name = name;
        this.username = username;
        this.photoUri = photoUri;
        this.uid = uid;
        this.myFriends = myFriends;
        this.userFriends = userFriends;
    }

    public ArrayList<String> getUserFriends() {
        return userFriends;
    }

    public ArrayList<String> getMyFriends() {
        return myFriends;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
