package com.imc.getout.fragments.mainFragments.cards;

import java.util.ArrayList;

public class MessagesCard {

    private String name,lastMessage,profilePhoto,chatUid,userUid;
    private ArrayList<String> messages;
    private int priority;

    public MessagesCard(String name, String profilePhoto,String chatUid,String userUid,int priority) {
        this.name = name;
        this.profilePhoto = profilePhoto;
        this.chatUid = chatUid;
        this.userUid = userUid;
        this.priority = priority;
        messages = new ArrayList<>();
    }

    public void addMessages(String text) {
        messages.add(text);
        System.out.println(text);
    }

    public String getMessage(int index) {
        return messages.get(index);
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getChatUid() {
        return chatUid;
    }

    public void setChatUid(String chatUid) {
        this.chatUid = chatUid;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
