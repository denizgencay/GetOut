package com.imc.getout.fragments.mainFragments.notificationsFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.imc.getout.ChatActivity;
import com.imc.getout.OthersProfileActivity;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.cards.InviteRequestsCard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class InviteRequestsAdapter extends RecyclerView.Adapter<InviteRequestsAdapter.Holder> {

    private ArrayList<InviteRequestsCard> requests;
    private Context context;

    private FirebaseFirestore firebaseFirestore;
    private Fragment fragment;
    private FragmentTransaction fragmentTransaction;

    private String senderUid,senderName,senderPhotoUri,receiverName,receiverUid,receiverPhotoUri;

    public InviteRequestsAdapter(ArrayList<InviteRequestsCard> requests,Context context,FirebaseFirestore firebaseFirestore,Fragment fragment,FragmentTransaction fragmentTransaction) {
        this.requests = requests;
        this.context = context;
        this.firebaseFirestore = firebaseFirestore;
        this.fragment = fragment;
        this.fragmentTransaction = fragmentTransaction;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.invite_request_card,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final InviteRequestsCard item = requests.get(position);

        CollectionReference collectionReference = firebaseFirestore.collection("Chats");

        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        String senderUid = (String) snapshot.getString("senderUid");
                        String receiverUid = (String) snapshot.getString("receiverUid");

                        if ((senderUid.equals(item.getMyUid()) && receiverUid.equals(item.getUserUid())) ||
                                (senderUid.equals(item.getUserUid()) && receiverUid.equals(item.getMyUid()))) {
                            item.setChatActive(true);
                            item.setChatUid(snapshot.getId());
                            break;
                        }
                    }
                }
            }
        });

        holder.name.setText(item.getname());
        holder.username.setText(item.getUsername());
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> requestUids = item.getRequests();
                ArrayList<String> attendantUids = item.getAttendants();

                requestUids.remove(item.getUserUid());
                attendantUids.add(item.getUserUid());

                HashMap<String,Object> update = new HashMap<>();
                update.put("requests",requestUids);
                update.put("attendants",attendantUids);

                String invitationType = "";
                String arrayName = "";

                switch (item.getInvitationType()) {
                    case "Anlık":
                        invitationType = "instantInvites";
                        arrayName = "attendantInstantInviteUids";
                        break;
                    case "Planlı":
                        invitationType = "plannedInvites";
                        arrayName = "attendantPlannedInviteUids";
                        break;
                    case "Etkinlik":
                        invitationType = "events";
                        arrayName = "attendantEventUids";
                        break;

                }

                DocumentReference documentReference = firebaseFirestore.collection(invitationType).document(item.getInvitationUid());
                final String finalArrayName = arrayName;
                documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference documentReference1 = firebaseFirestore.collection("Users").document(item.getUserUid());

                        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                ArrayList<String> updateList = (ArrayList<String>) documentSnapshot.get(finalArrayName);

                                updateList.add(item.getInvitationUid());

                                HashMap<String,Object> update2 = new HashMap<>();
                                update2.put(finalArrayName,updateList);

                                documentReference1.set(update2,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        fragmentTransaction.detach(fragment).attach(fragment).commit();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> requestUids = item.getRequests();

                requestUids.remove(item.getUserUid());

                HashMap<String,Object> update = new HashMap<>();
                update.put("requests",requestUids);

                String invitationType = "";

                switch (item.getInvitationType()) {
                    case "Anlık":
                        invitationType = "instantInvites";
                        break;
                    case "Planlı":
                        invitationType = "plannedInvites";
                        break;
                    case "Etkinlik":
                        invitationType = "events";
                        break;

                }

                DocumentReference documentReference = firebaseFirestore.collection(invitationType).document(item.getInvitationUid());

                documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fragmentTransaction.detach(fragment).attach(fragment).commit();
                    }
                });
            }
        });
        holder.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DocumentReference documentReference = firebaseFirestore.collection("Users").document(item.getMyUid());

                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        senderName = (String) documentSnapshot.getString("name");
                        senderUid = (String) documentSnapshot.getId();
                        senderPhotoUri = (String) documentSnapshot.getString("photoUri");

                        DocumentReference documentReference1 = firebaseFirestore.collection("Users").document(item.getUserUid());

                        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                receiverName = (String) documentSnapshot.getString("name");
                                receiverUid = (String) documentSnapshot.getId();
                                receiverPhotoUri = (String) documentSnapshot.getString("photoUri");

                                if (item.isChatActive() && item.getChatUid() != null) {
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra("userId",item.getUserUid());
                                    intent.putExtra("chatId",item.getChatUid());
                                    Bundle bundle = new Bundle();
                                    bundle.putString("userId",item.getUserUid());
                                    bundle.putString("chatId",item.getChatUid());
                                    fragment.startActivityForResult(intent,1,bundle);
                                } else {
                                    HashMap<String,Object> data = new HashMap<>();
                                    data.put("senderName",senderName);
                                    data.put("senderUid",senderUid);
                                    data.put("senderPhotoUri",senderPhotoUri);
                                    data.put("receiverName",receiverName);
                                    data.put("receiverUid",receiverUid);
                                    data.put("receiverPhotoUri",receiverPhotoUri);

                                    CollectionReference collectionReference = firebaseFirestore.collection("Chats");

                                    collectionReference.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Intent intent = new Intent(context, ChatActivity.class);
                                            intent.putExtra("userId",item.getUserUid());
                                            intent.putExtra("chatId",documentReference.getId());
                                            fragment.startActivityForResult(intent,1);
                                        }
                                    });
                                }
                            }
                        });
                    }
                });

            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid",item.getUserUid());
                context.startActivity(intent);
            }
        });
        Picasso.get().load(item.getPhotoUri()).noFade().into(holder.profilePhoto);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView name,username;
        ImageView profilePhoto,accept,reject,message;
        RelativeLayout card;

        public Holder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.invite_request_card_name);
            username = itemView.findViewById(R.id.invite_request_card_username);
            profilePhoto = itemView.findViewById(R.id.invite_request_card_profile_image);
            accept = itemView.findViewById(R.id.invite_request_card_accept);
            reject = itemView.findViewById(R.id.invite_request_card_reject);
            message = itemView.findViewById(R.id.invite_request_card_message);
            card = itemView.findViewById(R.id.invite_request_card);
        }
    }
}
