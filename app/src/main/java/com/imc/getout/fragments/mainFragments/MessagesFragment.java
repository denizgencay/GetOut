package com.imc.getout.fragments.mainFragments;

import android.content.Context;
import android.content.Intent;
import android.media.MediaExtractor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.imc.getout.ChatActivity;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.cards.MessagesCard;
import com.imc.getout.fragments.mainFragments.cards.SearchCard;
import com.imc.getout.models.MessagesFragmentModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MessagesFragment extends Fragment {

    private Context context;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ArrayList<MessagesCard> messages;

    private RecyclerView recyclerView;
    private MessagesRecyclerAdapter adapter;
    private String myUid;

    private MessagesFragmentModel messagesFragmentModel;

    private final int CHAT_RESULT = 1;

    private boolean redirected;
    private String chatId,userId;

    public MessagesFragment(boolean redirected,String chatId,String userId) {
        this.redirected = redirected;
        this.chatId = chatId;
        this.userId = userId;
        System.out.println("çağrıldı1");
    }

    public MessagesFragment() {
        System.out.println("çağrıldı2");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_messages,container,false);

        messagesFragmentModel = new MessagesFragmentModel();
        messages = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        context = this.getContext();

        recyclerView = view.findViewById(R.id.messages_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new MessagesRecyclerAdapter(messages,context,firebaseFirestore,this,firebaseAuth);
        recyclerView.setAdapter(adapter);

        getDataFromFirestore();

        if (redirected) {
            redirected = false;
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chatId",chatId);
            intent.putExtra("userId",userId);
            startActivityForResult(intent,CHAT_RESULT);
        }

        return view;
    }

    private void getDataFromFirestore() {
        final String uid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(uid);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                messages.clear();

                ArrayList<String> chats = (ArrayList<String>) documentSnapshot.get("chats");

                if (chats != null) {

                    Collections.reverse(chats);

                    for (int i = 0 ; i < chats.size() ; i++) {
                        String id = chats.get(i);
                        final DocumentReference documentReference1 = firebaseFirestore.collection("Chats").document(id);

                        final int finalI = i;
                        documentReference1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot.exists()) {

                                    String name,photoUri,userUid;
                                    final String roomId = documentSnapshot.getId();

                                    documentReference1.collection("messages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (queryDocumentSnapshots != null) {

                                            }
                                        }
                                    });

                                    if (documentSnapshot.getString("senderUid").equals(uid)) {
                                        name = documentSnapshot.getString("receiverName");
                                        photoUri = documentSnapshot.getString("receiverPhotoUri");
                                        userUid = documentSnapshot.getString("receiverUid");

                                        MessagesCard message = new MessagesCard(name,photoUri,roomId,userUid, finalI);

                                        messages.add(message);
                                        messages.sort(new Comparator<MessagesCard>() {
                                            @Override
                                            public int compare(MessagesCard lhs, MessagesCard rhs) {
                                                return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                            }
                                        });
                                        adapter.notifyDataSetChanged();

                                    } else {
                                        name = documentSnapshot.getString("senderName");
                                        photoUri = documentSnapshot.getString("senderPhotoUri");
                                        userUid = documentSnapshot.getString("senderUid");

                                        MessagesCard message = new MessagesCard(name,photoUri,roomId,userUid,finalI);

                                        messages.add(message);
                                        messages.sort(new Comparator<MessagesCard>() {
                                            @Override
                                            public int compare(MessagesCard lhs, MessagesCard rhs) {
                                                return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                            }
                                        });
                                        adapter.notifyDataSetChanged();
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
        if (requestCode == CHAT_RESULT || resultCode == CHAT_RESULT) {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container,MessagesFragment.this).commit();
        }
    }
}
