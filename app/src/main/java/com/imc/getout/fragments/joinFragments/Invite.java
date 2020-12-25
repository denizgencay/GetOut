package com.imc.getout.fragments.joinFragments;

public class Invite {

    private String title,location,name,date,personNumber,address,state,city,photoUrl,userUid;

    public Invite(String title, String location, String name, String date, String personNumber, String address, String state, String city, String photoUrl,String userUid) {
        this.title = title;
        this.location = location;
        this.name = name;
        this.date = date;
        this.personNumber = personNumber;
        this.address = address;
        this.state = state;
        this.city = city;
        this.photoUrl = photoUrl;
        this.userUid = userUid;
    }

    public String getUserUid() {
        return userUid;
    }

    public String getTitle() {
        return this.title;
    }

    public String getLocation() {
        return this.location;
    }

    public String getname() {
        return this.name;
    }

    public String getDate() {
        return this.date;
    }

    public String getPersonNumber() {
        return this.personNumber;
    }

    public String getAddress() {
        return this.address;
    }

    public String getState() {
        if (state != null) {
            return this.state;
        } else {
            return "";
        }
    }

    public String getCity() {
        return this.city;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }
}
