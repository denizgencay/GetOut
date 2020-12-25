package com.imc.getout.fragments.mainFragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.cards.ProfileInviteCard;
import com.imc.getout.fragments.mainFragments.cards.SearchCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

public class SearchFragment extends Fragment {

    private SearchRecyclerAdapter adapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ArrayList<SearchCard> users;
    private ArrayList<SearchCard> lastSearched;

    private EditText searchBar;
    private SearchFragment fragment;

    private final int OTHERS_PROFILE_RESULT = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);

        users = new ArrayList<>();
        lastSearched = new ArrayList<>();
        searchBar = view.findViewById(R.id.search_search);
        searchBar.addTextChangedListener(new TextWatcher() {
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

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        String uid = firebaseAuth.getCurrentUser().getUid();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        fragment = this;

        RecyclerView recyclerView = view.findViewById(R.id.search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchRecyclerAdapter(users,firebaseFirestore,uid,fragment,fragmentTransaction,searchBar);
        recyclerView.setAdapter(adapter);

        getDataFromFirestore();

        return view;
    }

    private void filter(String text) {
        ArrayList<SearchCard> filteredList = new ArrayList<>();
        String uid = firebaseAuth.getCurrentUser().getUid();

        if (!text.isEmpty() || !text.equals("")) {
            for (SearchCard user : users) {
                if (user.getname().toLowerCase(new Locale("tr","TR")).contains(text.toLowerCase(new Locale("tr","TR"))) ||
                        user.getUsername().toLowerCase(new Locale("tr","TR")).contains(text.toLowerCase(new Locale("tr","TR")))) {
                    if (!user.getUid().equals(uid)) {
                        filteredList.add(user);
                    }
                }
            }
            adapter.filterList(filteredList);
        } else {
            adapter.setType(lastSearched);
        }
    }

    private void getDataFromFirestore() {
        CollectionReference collectionReference = firebaseFirestore.collection("Users");

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }

                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        Map<String,Object> data = snapshot.getData();

                        String name = (String) data.get("name");
                        String username = (String) data.get("username");
                        String photoUrl = (String) data.get("photoUri");
                        String uid = (String) snapshot.getId();

                        SearchCard user = new SearchCard(photoUrl,name,username,uid);
                        users.add(user);

                    }
                }
            }
        });
        String currentUserUid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(currentUserUid);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                lastSearched.clear();
                final ArrayList<String> lastSearchedUids = (ArrayList<String>) documentSnapshot.get("lastSearched");

                if (lastSearchedUids != null) {
                    Collections.reverse(lastSearchedUids);
                    adapter.setLastSearchedUids(lastSearchedUids);

                    for (int i = 0 ; i < lastSearchedUids.size(); i++) {
                        DocumentReference documentReference1 = firebaseFirestore.collection("Users").document(lastSearchedUids.get(i));

                        final int finalI = i;
                        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                String name = (String) documentSnapshot.getString("name");
                                String username = (String) documentSnapshot.get("username");
                                String photoUrl = (String) documentSnapshot.get("photoUri");
                                String uid = (String) documentSnapshot.getId();

                                SearchCard user = new SearchCard(photoUrl,name,username,uid, finalI);
                                lastSearched.add(user);

                                lastSearched.sort(new Comparator<SearchCard>() {
                                    @Override
                                    public int compare(SearchCard lhs, SearchCard rhs) {
                                        return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                    }
                                });

                                adapter.setType(lastSearched);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OTHERS_PROFILE_RESULT) {
            getFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
            searchBar.setText("");
        }
    }
}
