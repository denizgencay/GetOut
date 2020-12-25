package com.imc.getout.fragments.joinFragments;

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
import androidx.fragment.app.Fragment;
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
import com.imc.getout.R;
import com.imc.getout.models.JoinModel;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class EventJoinFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private ArrayList<Invite> invites,allInvites;
    private RecyclerAdapter recyclerAdapter;
    private EditText searchBar;
    private JoinModel joinModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_join_fragment,container,false);

        invites = new ArrayList<>();
        searchBar = view.findViewById(R.id.eventSearch);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        joinModel = new JoinModel();
        allInvites = new ArrayList<>();

        getMyData();

        RecyclerView recyclerView = view.findViewById(R.id.event_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapter = new RecyclerAdapter(invites);
        recyclerView.setAdapter(recyclerAdapter);

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

        return view;
    }

    private void getMyData() {
        String uid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference= firebaseFirestore.collection("Users").document(uid);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String city = (String) documentSnapshot.getString("city");
                getDataFromFirestore(city);
            }
        });
    }

    private void filter(String text) {
        ArrayList<Invite> filteredList = new ArrayList<>();
        String uid = firebaseAuth.getCurrentUser().getUid();

        if (!text.isEmpty()) {
            for (Invite item : allInvites) {
                if (item.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                        item.getCity().toLowerCase(new Locale("tr","TR")).contains(text.toLowerCase(new Locale("tr","TR"))) ||
                        item.getState().toLowerCase(new Locale("tr","TR")).contains(text.toLowerCase(new Locale("tr","TR"))) ||
                        item.getLocation().toLowerCase().contains(text.toLowerCase()) ||
                        item.getname().toLowerCase().contains(text.toLowerCase())) {
                    if (!item.getUserUid().equals(uid)) {
                        filteredList.add(item);
                    }
                }
            }
            recyclerAdapter.filterList(filteredList);
        } else {
            recyclerAdapter.filterList(invites);
        }
    }

    private void getDataFromFirestore(final String myCity) {
        final String uid = firebaseAuth.getCurrentUser().getUid();

        CollectionReference collectionReference = firebaseFirestore.collection("events");

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getActivity(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }

                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {

                        Map<String,Object> data = snapshot.getData();

                        String title = (String) data.get("title");
                        String name = (String) data.get("name");
                        String location = (String) data.get("locationField");
                        String date = (String) data.get("date") + " " +(String) data.get("hour");
                        String personNumber = (String) data.get("currentNumberOfPerson").toString() + "/"
                                +(String) data.get("numberOfPerson").toString() ;
                        String address = (String) data.get("locationAddress");
                        String photo = (String) data.get("userPhoto");
                        String city = (String) data.get("locationCity");
                        String state = (String) data.get("locationState");
                        String userUid = (String) data.get("userUid");

                        Invite invite = new Invite(title,location,name,date,personNumber,address,state,city,photo,userUid);
                        if (!userUid.equals(uid)) {
                            if (city.toLowerCase(new Locale("tr","TR")).equals(myCity.toLowerCase(new Locale("tr","TR")))) {
                                allInvites.add(invite);
                                invites.add(invite);
                            } else {
                                invites.add(invite);
                            }
                        }
                        recyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
