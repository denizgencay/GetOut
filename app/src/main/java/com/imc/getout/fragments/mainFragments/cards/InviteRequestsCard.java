package com.imc.getout.fragments.mainFragments.cards;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class InviteRequestsCard {

    private String name,username,photoUri,userUid,invitationUid,invitationType,myUid,chatUid;
    private ArrayList<String> requests,attendants;
    private boolean chatActive;

    public InviteRequestsCard(String name, String username, String photoUri, String userUid, String invitationUid, String invitationType,ArrayList<String> requests,ArrayList<String> attendants,String myUid) {
        this.name = name;
        this.username = username;
        this.photoUri = photoUri;
        this.userUid = userUid;
        this.invitationUid = invitationUid;
        this.invitationType = invitationType;
        this.requests = requests;
        this.attendants = attendants;
        this.myUid = myUid;
    }

    public void setChatUid(String chatUid) {
        this.chatUid = chatUid;
    }

    public void setChatActive(boolean chatActive) {
        this.chatActive = chatActive;
    }

    public String getChatUid() {
        return chatUid;
    }

    public boolean isChatActive() {
        return chatActive;
    }

    public String getMyUid() {
        return myUid;
    }

    public ArrayList<String> getRequests() {
        return requests;
    }

    public ArrayList<String> getAttendants() {
        return attendants;
    }

    public String getInvitationUid() {
        return invitationUid;
    }

    public String getInvitationType() {
        return invitationType;
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

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String uid) {
        this.userUid = uid;
    }
}
