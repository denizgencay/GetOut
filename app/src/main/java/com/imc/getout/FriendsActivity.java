package com.imc.getout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.imc.getout.fragments.mainFragments.cards.FriendsCard;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    private ArrayList<FriendsCard> friends;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FriendsRecyclerAdapter adapter;
    private EditText search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friends = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        search = findViewById(R.id.friends_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });

        String uid = firebaseAuth.getCurrentUser().getUid();

        final FriendsActivity activity = this;

        recyclerView = findViewById(R.id.friends_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendsRecyclerAdapter(friends,activity,firebaseFirestore,uid);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = findViewById(R.id.friends_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                activity.setResult(3);
                finish();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                if (recyclerView.getScrollY() == 0) return false;
                else return true;
            }
        });

        getDataFromFirestore();
    }

    private void filter(String text){
        ArrayList<FriendsCard> filteredList = new ArrayList<>();

        if (!text.isEmpty()) {
            for (FriendsCard card : friends) {
                if (card.getname().toLowerCase().contains(text.toLowerCase()) ||
                    card.getUsername().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(card);
                }
            }
            adapter.filterList(filteredList);
        }
    }

    private void getDataFromFirestore() {
        String uid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(uid);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    final ArrayList<String> friendUids = (ArrayList<String>) documentSnapshot.get("friends");

                    if (friendUids != null) {
                        for (String friendUid : friendUids) {
                            DocumentReference doc = firebaseFirestore.collection("Users").document(friendUid);

                            doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String name = (String) documentSnapshot.getString("name");
                                    String username = (String) documentSnapshot.getString("username");
                                    String photoUri = (String) documentSnapshot.getString("photoUri");
                                    ArrayList<String> friendsFriendList = (ArrayList<String>) documentSnapshot.get("friends");
                                    String cardUid = documentSnapshot.getId();

                                    FriendsCard card = new FriendsCard(name,username,photoUri,cardUid,friendUids,friendsFriendList);

                                    friends.add(card);

                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }
        });
    }
}

