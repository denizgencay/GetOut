package com.imc.getout.fragments.mainFragments.notificationsFragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.cards.InviteRequestsCard;
import com.imc.getout.fragments.mainFragments.cards.InviteRequestsListItem;

import java.util.ArrayList;
import java.util.Collections;

public class InviteRequestsFragment extends Fragment {

    private ArrayList<InviteRequestsListItem> inviteRequestListItems;

    private InviteRequestsListAdapter adapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private SwipeRefreshLayout swipeRefreshLayout;
    private Fragment fragment = this;

    private final int CHAT_RESULT = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_requests,container,false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        inviteRequestListItems = new ArrayList<>();

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        final RecyclerView recyclerView = view.findViewById(R.id.invite_requests_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InviteRequestsListAdapter(inviteRequestListItems,this.getContext(),firebaseFirestore,fragmentTransaction,this);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = view.findViewById(R.id.invite_requests_swipe_refresh_layout);
        swipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                if (recyclerView.getScrollY() == 0) return false;
                else return true;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipeRefreshLayout.setRefreshing(true);
                fragmentTransaction.detach(fragment).attach(fragment).commit();
                swipeRefreshLayout.setRefreshing(false);

            }
        });

        getDataFromFirestore();

        return view;
    }

    private void getDataFromFirestore() {
        final String myUid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(myUid);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                ArrayList<String> instantInviteUids = (ArrayList<String>) documentSnapshot.get("instantInviteUids");
                ArrayList<String> plannedInviteUids = (ArrayList<String>) documentSnapshot.get("plannedInviteUids");
                ArrayList<String> eventUids = (ArrayList<String>) documentSnapshot.get("eventUids");

                final int total = instantInviteUids.size()+plannedInviteUids.size()+eventUids.size();

                if (instantInviteUids != null) {
                   for (String instantInviteUid : instantInviteUids) {
                       DocumentReference dr = firebaseFirestore.collection("instantInvites").document(instantInviteUid);

                       dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                           @Override
                           public void onSuccess(DocumentSnapshot documentSnapshot) {
                               String type = (String) documentSnapshot.getString("title");
                               final ArrayList<String> requests = (ArrayList<String>) documentSnapshot.get("requests");
                               final ArrayList<String> attendants = (ArrayList<String>) documentSnapshot.get("attendants");
                               final String inviteUid = documentSnapshot.getId();

                               final InviteRequestsListItem item = new InviteRequestsListItem(type,requests,firebaseFirestore);

                               inviteRequestListItems.add(item);

                               if (requests != null) {
                                   Collections.reverse(requests);
                                   for (String requestUid : requests) {
                                       DocumentReference doc = firebaseFirestore.collection("Users").document(requestUid);

                                       doc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                           @Override
                                           public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                               String name = (String) documentSnapshot.getString("name");
                                               String username = (String) documentSnapshot.getString("username");
                                               String photoUri = (String) documentSnapshot.getString("photoUri");
                                               String userUid = (String) documentSnapshot.getId();

                                               InviteRequestsCard card = new InviteRequestsCard(name,username,photoUri,userUid,inviteUid,"Anlık",requests,attendants,myUid);

                                               item.addRequests(card);

                                               if (inviteRequestListItems.size() <= total) {
                                                   adapter.notifyDataSetChanged();
                                               }
                                           }
                                       });
                                   }
                               }
                           }
                       });
                   }
                }

                if (plannedInviteUids != null) {
                    for (String plannedInviteUid : plannedInviteUids) {
                        DocumentReference dr2 = firebaseFirestore.collection("plannedInvites").document(plannedInviteUid);

                        dr2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String type = (String) documentSnapshot.getString("title");
                                final ArrayList<String> requests = (ArrayList<String>) documentSnapshot.get("requests");
                                final ArrayList<String> attendants = (ArrayList<String>) documentSnapshot.get("attendants");
                                final String inviteUid = documentSnapshot.getId();

                                final InviteRequestsListItem item = new InviteRequestsListItem(type,requests,firebaseFirestore);

                                inviteRequestListItems.add(item);

                                if (requests != null) {
                                    Collections.reverse(requests);
                                    for (String requestUid : requests) {
                                        DocumentReference doc2 = firebaseFirestore.collection("Users").document(requestUid);

                                        doc2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                String name = (String) documentSnapshot.getString("name");
                                                String username = (String) documentSnapshot.getString("username");
                                                String photoUri = (String) documentSnapshot.getString("photoUri");
                                                String uid = (String) documentSnapshot.getId();

                                                InviteRequestsCard card = new InviteRequestsCard(name,username,photoUri,uid,inviteUid,"Planlı",requests,attendants,myUid);

                                                item.addRequests(card);

                                                if (inviteRequestListItems.size() <= total) {
                                                    adapter.notifyDataSetChanged();
                                                }

                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }

                if (eventUids != null) {
                    for (String eventUid : eventUids) {
                        DocumentReference dr3 = firebaseFirestore.collection("events").document(eventUid);

                        dr3.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String type = (String) documentSnapshot.getString("title");
                                final ArrayList<String> requests = (ArrayList<String>) documentSnapshot.get("requests");
                                final ArrayList<String> attendants = (ArrayList<String>) documentSnapshot.get("attendants");
                                final String inviteUid = documentSnapshot.getId();

                                final InviteRequestsListItem item = new InviteRequestsListItem(type,requests,firebaseFirestore);

                                inviteRequestListItems.add(item);

                                if (requests != null) {
                                    Collections.reverse(requests);
                                    for (String requestUid : requests) {
                                        DocumentReference doc2 = firebaseFirestore.collection("Users").document(requestUid);

                                        doc2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                String name = (String) documentSnapshot.getString("name");
                                                String username = (String) documentSnapshot.getString("username");
                                                String photoUri = (String) documentSnapshot.getString("photoUri");
                                                String uid = (String) documentSnapshot.getId();

                                                InviteRequestsCard card = new InviteRequestsCard(name,username,photoUri,uid,inviteUid,"Etkinlik",requests,attendants,myUid);

                                                item.addRequests(card);

                                                if (inviteRequestListItems.size() <= total) {
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                }
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
        if (requestCode == CHAT_RESULT && resultCode == CHAT_RESULT) {
            String chatId = data.getStringExtra("chatId");

            final DocumentReference documentReference = firebaseFirestore.collection("Chats").document(chatId);

            documentReference.collection("messages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.size() == 0) {
                        documentReference.delete();
                        getFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
                    } else {

                    }
                }
            });
        }
    }
}
