package com.imc.getout.models;

import java.util.ArrayList;

public class OthersProfileModel {

    private boolean chatActive,isPremium,alreadyFriends,requestSent,inviteRequested,inviteAttendant;
    private String chatId,senderName,senderPhotoUri,receiverName,receiverPhotoUri,name,pushToken;
    private ArrayList<String> chats;
    private int manCtr,womanCtr;

    public ArrayList<String> getChats() {
        return chats;
    }

    public void setChats(ArrayList<String> chats) {
        if (chats != null) {
            this.chats = chats;
        } else {
            this.chats = new ArrayList<>();
        }
    }

    public int getManCtr() {
        return manCtr;
    }

    public void setManCtr(int manCtr) {
        this.manCtr = manCtr;
    }

    public int getWomanCtr() {
        return womanCtr;
    }

    public void setWomanCtr(int womanCtr) {
        this.womanCtr = womanCtr;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInviteRequested() {
        return inviteRequested;
    }

    public void setInviteRequested(boolean inviteRequested) {
        this.inviteRequested = inviteRequested;
    }

    public boolean isInviteAttendant() {
        return inviteAttendant;
    }

    public void setInviteAttendant(boolean inviteAttendant) {
        this.inviteAttendant = inviteAttendant;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhotoUri() {
        return receiverPhotoUri;
    }

    public void setReceiverPhotoUri(String receiverPhotoUri) {
        this.receiverPhotoUri = receiverPhotoUri;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhotoUri() {
        return senderPhotoUri;
    }

    public void setSenderPhotoUri(String senderPhotoUri) {
        this.senderPhotoUri = senderPhotoUri;
    }

    public boolean isChatActive() {
        return chatActive;
    }

    public void setChatActive(boolean chatActive) {
        this.chatActive = chatActive;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public boolean isAlreadyFriends() {
        return alreadyFriends;
    }

    public void setAlreadyFriends(boolean alreadyFriends) {
        this.alreadyFriends = alreadyFriends;
    }

    public boolean isRequestSent() {
        return requestSent;
    }

    public void setRequestSent(boolean requestSent) {
        this.requestSent = requestSent;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
