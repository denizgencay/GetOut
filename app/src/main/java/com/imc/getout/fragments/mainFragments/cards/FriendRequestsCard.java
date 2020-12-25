package com.imc.getout.fragments.mainFragments.cards;

import java.util.ArrayList;

public class FriendRequestsCard {

    private String name,username,profilePhoto,uid;
    private ArrayList<String> receiverFriendsRequest,receiverFriends,senderFriends;

    public FriendRequestsCard(String name, String username, String profilePhoto, String uid,ArrayList<String> receiverFriendsRequest,ArrayList<String> receiverFriends,ArrayList<String> senderFriends) {
        this.name = name;
        this.username = username;
        this.profilePhoto = profilePhoto;
        this.uid = uid;
        this.receiverFriendsRequest = receiverFriendsRequest;
        this.receiverFriends = receiverFriends;
        this.senderFriends = senderFriends;
    }

    public ArrayList<String> getReceiverFriendsRequest() {
        return receiverFriendsRequest;
    }

    public ArrayList<String> getReceiverFriends() {
        return receiverFriends;
    }

    public ArrayList<String> getSenderFriends() {
        return senderFriends;
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

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
