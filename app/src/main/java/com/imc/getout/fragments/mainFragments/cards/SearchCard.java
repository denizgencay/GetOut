package com.imc.getout.fragments.mainFragments.cards;

public class SearchCard {

    private String photoUrl,name,username,uid;
    private int priority;

    public SearchCard(String photoUrl, String name, String username, String uid,int priority) {
        this.photoUrl = photoUrl;
        this.name = name;
        this.username = username;
        this.uid = uid;
        this.priority = priority;
    }

    public SearchCard(String photoUrl, String name, String username, String uid) {
        this.photoUrl = photoUrl;
        this.name = name;
        this.username = username;
        this.uid = uid;
    }

    public int getPriority() {
        return priority;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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

}
