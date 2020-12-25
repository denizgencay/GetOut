package com.imc.getout.models;

import java.util.ArrayList;
import java.util.HashMap;

public class SignUpModel {

    private ArrayList<String> usernames;

    public SignUpModel() {
        this.usernames = new ArrayList<>();
    }

    public ArrayList<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(ArrayList<String> usernames) {
        this.usernames = usernames;
    }
}
