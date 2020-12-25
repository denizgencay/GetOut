package com.imc.getout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.imc.getout.fragments.mainFragments.cards.ProfileAttendantCard;

import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Date;

public class AttendantsActivity extends AppCompatActivity {

    private ArrayList<String> uids;
    private ArrayList<ProfileAttendantCard> cards;
    private AttendantsRecyclerAdapter adapter;

    private FirebaseFirestore firebaseFirestore;

    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendants);

        cards = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.attendants_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendantsRecyclerAdapter(cards);
        recyclerView.setAdapter(adapter);
        back = findViewById(R.id.attendants_back_button);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        uids = intent.getStringArrayListExtra("list");

        firebaseFirestore = FirebaseFirestore.getInstance();

        if (uids != null) {
            getDataFromFirestore();
        }

    }

    private void getDataFromFirestore() {
        for (String uid : uids) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(uid);

            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        String name = (String) documentSnapshot.getString("name");
                        Timestamp ageTimestamp = (Timestamp) documentSnapshot.getTimestamp("birthday");
                        String username = (String) documentSnapshot.getString("username");
                        String photoUri = (String) documentSnapshot.getString("photoUri");
                        String userId = documentSnapshot.getId();

                        long ageInMillies = ageTimestamp.toDate().getTime();
                        Date now = new Date();
                        long nowInMillies = now.getTime();
                        long aYearInMillies = (long)(1000*60*60*24*365.25);
                        int userAge = (int)((nowInMillies-ageInMillies)/aYearInMillies);

                        ProfileAttendantCard pac = new ProfileAttendantCard(name,String.valueOf(userAge),username,photoUri,userId);
                        cards.add(pac);

                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}
