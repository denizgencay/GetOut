package com.imc.getout.fragments.mainFragments.notificationsFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.cards.FriendRequestsCard;

import java.util.ArrayList;
import java.util.Collections;

public class FriendRequestsFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ArrayList<FriendRequestsCard> requests;

    private FriendRequestAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests,container,false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        requests = new ArrayList<>();

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        final Fragment fragment = this;

        final RecyclerView recyclerView = view.findViewById(R.id.friend_requests_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FriendRequestAdapter(requests,firebaseAuth,firebaseFirestore,this,fragmentTransaction);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = view.findViewById(R.id.friend_requests_swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                fragmentTransaction.detach(fragment).attach(fragment).commit();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                if (recyclerView.getScrollY() == 0) return false;
                return true;
            }
        });

        getDataFromFirestore();

        return view;

    }

    private void getDataFromFirestore() {
        String uid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(uid);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final ArrayList<String> receiverFriendRequests = (ArrayList<String>) documentSnapshot.get("friendRequests");
                final ArrayList<String> receiverFriends = (ArrayList<String>) documentSnapshot.get("friends");

                if (receiverFriendRequests != null) {
                    Collections.reverse(receiverFriendRequests);
                    if (!receiverFriendRequests.isEmpty()) {
                        for (final String request : receiverFriendRequests) {
                            DocumentReference doc = firebaseFirestore.collection("Users").document(request);

                            doc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    String name = (String) documentSnapshot.getString("name");
                                    String username = (String) documentSnapshot.getString("username");
                                    String photoUri = (String) documentSnapshot.getString("photoUri");
                                    String requestUid = (String) documentSnapshot.getId();
                                    ArrayList<String> senderFriends = (ArrayList<String>) documentSnapshot.get("friends");

                                    FriendRequestsCard frc = new FriendRequestsCard(name,username,photoUri,requestUid,receiverFriendRequests,receiverFriends,senderFriends);
                                    requests.add(frc);

                                    if (requests.size() <= receiverFriendRequests.size()) {
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }
}
