package com.imc.getout.fragments.mainFragments.cards;

public class ProfileAttendantCard {

    private String name,age,username,profilePhoto,uid;

    public ProfileAttendantCard(String name, String age, String username, String profilePhoto,String uid) {
        this.name = name;
        this.age = age;
        this.username = username;
        this.profilePhoto = profilePhoto;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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
}
