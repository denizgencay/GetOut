package com.imc.getout.fragments.mainFragments.cards;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class InviteRequestsListItem {

    private String title;
    private ArrayList<String> requestUids;
    private ArrayList<InviteRequestsCard> requests;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView.Adapter adapter;

    public InviteRequestsListItem(String title, ArrayList<String> requestUids,FirebaseFirestore firebaseFirestore) {
        this.title = title;
        this.requestUids = requestUids;
        this.firebaseFirestore = firebaseFirestore;
        this.requests = new ArrayList<>();
    }

    public void setAdapter(RecyclerView.Adapter adapter){
        this.adapter = adapter;
    }

    public String gettitle() {
        return title;
    }

    public void settitle(String title) {
        this.title = title;
    }

    public ArrayList<InviteRequestsCard> getRequests() {
        return requests;
    }

    public void addRequests(InviteRequestsCard card) {
        this.requests.add(card);
    }
}
