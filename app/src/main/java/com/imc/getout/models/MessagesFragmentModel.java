package com.imc.getout.models;

import java.util.ArrayList;
import java.util.HashMap;

public class MessagesFragmentModel {

    private HashMap<String,Object> data ;

    public MessagesFragmentModel() {
        this.data = new HashMap<>();

    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void putData(String key,Object data) {
        this.data.put(key,data);
    }

    public Object getCell(String key) {
        return data.get(key);
    }
}
