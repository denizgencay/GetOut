package com.imc.getout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.imc.getout.fragments.mainFragments.cards.ProfileInviteCard;
import com.imc.getout.models.OthersProfileModel;

import java.util.ArrayList;
import java.util.HashMap;

public class OthersProfileRecyclerAdapter extends RecyclerView.Adapter<OthersProfileRecyclerAdapter.Holder> {

    private ArrayList<ProfileInviteCard> inviteCards;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private OthersProfileModel othersProfileModel;

    private OthersProfileActivity othersProfileActivity;
    private ViewGroup parent;

    public OthersProfileRecyclerAdapter(ArrayList<ProfileInviteCard> inviteCards, FirebaseFirestore firebaseFirestore,FirebaseAuth firebaseAuth,OthersProfileActivity othersProfileActivity) {
        this.inviteCards = inviteCards;
        this.firebaseFirestore = firebaseFirestore;
        this.firebaseAuth = firebaseAuth;
        this.othersProfileActivity = othersProfileActivity;
        this.othersProfileModel = new OthersProfileModel();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        this.parent = parent;
        View view = layoutInflater.inflate(R.layout.others_profile_invite_item,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, final int position) {
        final String myUid = firebaseAuth.getCurrentUser().getUid();
        othersProfileModel.setManCtr(0);
        othersProfileModel.setWomanCtr(0);
        holder.type.setText(inviteCards.get(position).getInviteTypeText());
        holder.title.setText(inviteCards.get(position).getInviteTitleText());
        holder.time.setText(inviteCards.get(position).getTimeText());
        holder.location.setText(inviteCards.get(position).getInviteLocationText());
        holder.address.setText(inviteCards.get(position).getInviteAddressText());
        holder.explanation.setText(inviteCards.get(position).getInviteExplanationText());
        holder.man.setText(String.valueOf(othersProfileModel.getManCtr()));
        holder.woman.setText(String.valueOf(othersProfileModel.getWomanCtr()));
        final ArrayList<String> attendants = inviteCards.get(position).getAttendants();
        final ArrayList<String> requests = inviteCards.get(position).getRequests();

        boolean requestSent = false;
        boolean isAttendant = false;

        if (requests != null) {
            for (String requestUid : requests) {
                if (requestUid.equals(firebaseAuth.getCurrentUser().getUid())) {
                    requestSent = true;
                    holder.sendRequest.setText("İstek Gönderildi");
                    holder.sendRequest.setBackgroundColor(Color.parseColor("#999999"));
                    holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            System.out.println("x");
                            AlertDialog.Builder builder = new AlertDialog.Builder(othersProfileActivity);
                            builder.setMessage("Bu davet için yaptığınız katılma isteğini geri çekmek istiyor musunuz?");
                            builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final String type = inviteCards.get(position).getInviteTypeText();
                                    final String documentId = inviteCards.get(position).getDocumentId();
                                    String collectionName = "";
                                    switch (type) {
                                        case "Anlık":
                                            collectionName = "instantInvites";
                                            break;
                                        case "Planlı":
                                            collectionName = "plannedInvites";
                                            break;
                                        case "Etkinlik":
                                            collectionName = "events";
                                            break;
                                    }

                                    ArrayList<String> requestsClon = requests;
                                    requestsClon.remove(myUid);

                                    HashMap<String,Object> update = new HashMap<>();
                                    update.put("requests",requestsClon);

                                    DocumentReference documentReference = firebaseFirestore.collection(collectionName).document(documentId);

                                    documentReference.set(update,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            othersProfileActivity.getDataFromFirestore();
                                        }
                                    });
                                }
                            });
                            builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.show();
                        }
                    });
                    break;
                }
            }
        }

        if (attendants != null) {
            for (String attendantUid : attendants) {
                DocumentReference documentReference = firebaseFirestore.collection("Users").document(attendantUid);

                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String gender = (String) documentSnapshot.getString("gender");

                        if (gender.equals("Erkek")) {
                            int manCtr = othersProfileModel.getManCtr();
                            manCtr++;
                            othersProfileModel.setManCtr(manCtr);
                            holder.man.setText(String.valueOf(manCtr));
                        } else {
                            int womanCtr = othersProfileModel.getWomanCtr();
                            womanCtr++;
                            othersProfileModel.setWomanCtr(womanCtr);
                            holder.woman.setText(String.valueOf(womanCtr));
                        }
                    }
                });
                if (attendantUid.equals(firebaseAuth.getCurrentUser().getUid())) {
                    isAttendant = true;
                    holder.sendRequest.setVisibility(View.INVISIBLE);
                    holder.attendant.setVisibility(View.VISIBLE);
                    holder.attendant.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(othersProfileActivity);
                            builder.setMessage("Bu davetten ayrılmak istiyor musunuz?");
                            builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ArrayList<String> attendantsClon = attendants;
                                    attendantsClon.remove(myUid);

                                    final String type = inviteCards.get(position).getInviteTypeText();
                                    final String documentId = inviteCards.get(position).getDocumentId();
                                    String collectionName = "";
                                    String arrayName = "";
                                    switch (type) {
                                        case "Anlık":
                                            collectionName = "instantInvites";
                                            arrayName = "attendantInstantInviteUids";
                                            break;
                                        case "Planlı":
                                            collectionName = "plannedInvites";
                                            arrayName = "attendantPlannedInviteUids";
                                            break;
                                        case "Etkinlik":
                                            collectionName = "events";
                                            arrayName = "attendantEventUids";
                                            break;
                                    }

                                    HashMap<String,Object> update = new HashMap<>();
                                    update.put("attendants",attendantsClon);

                                    DocumentReference documentReference = firebaseFirestore.collection(collectionName).document(documentId);
                                    final String finalArrayName = arrayName;
                                    documentReference.set(update,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            DocumentReference documentReference1 = firebaseFirestore.collection("Users").document(myUid);

                                            documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    ArrayList<String> updateList = (ArrayList<String>) documentSnapshot.get(finalArrayName);

                                                    updateList.remove(documentId);

                                                    HashMap<String,Object> update2 = new HashMap<>();
                                                    update2.put(finalArrayName,updateList);

                                                    documentReference1.set(update2,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            othersProfileActivity.getDataFromFirestore();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                            builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.show();
                        }
                    });
                    break;
                }
            }
        }

        if (!requestSent && !isAttendant) {
            holder.attendant.setVisibility(View.INVISIBLE);
            holder.sendRequest.setVisibility(View.VISIBLE);
            holder.sendRequest.setText("Davete Katıl");
            holder.sendRequest.setBackgroundColor(Color.parseColor("#BF0505"));
            holder.sendRequest.setTextColor(Color.parseColor("#FAFAFA"));
            holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String type = inviteCards.get(position).getInviteTypeText();
                    final String documentId = inviteCards.get(position).getDocumentId();
                    String collectionName = "";
                    switch (type) {
                        case "Anlık":
                            collectionName = "instantInvites";
                            break;
                        case "Planlı":
                            collectionName = "plannedInvites";
                            break;
                        case "Etkinlik":
                            collectionName = "events";
                            break;
                    }

                    HashMap<String,Object> update = new HashMap<>();

                    if (requests == null) {
                        ArrayList<String> requestsClon = new ArrayList<>();

                        requestsClon.add(firebaseAuth.getCurrentUser().getUid());

                        update.put("requests",requestsClon);
                    } else {
                        requests.add(firebaseAuth.getCurrentUser().getUid());

                        update.put("requests",requests);
                    }

                    final DocumentReference documentReference = firebaseFirestore.collection(collectionName).document(documentId);

                    documentReference.set(update,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            String title = "Davetinize katılmak isteyen biri var.";
                            String body = " adlı kişi " + inviteCards.get(position).getInviteTitleText() + " başlıklı davetinize katılmak istiyor";
                            String key = "inviteRequest";
                            othersProfileActivity.sendNotification(title,body,key);
                            othersProfileActivity.getDataFromFirestore();
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return inviteCards.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView type,title,location,address,explanation,time,attendant,man,woman;
        Button sendRequest;

        public Holder(@NonNull View itemView) {
            super(itemView);

            type = itemView.findViewById(R.id.others_profile_invite_item_type);
            title = itemView.findViewById(R.id.others_profile_invite_item_title);
            location = itemView.findViewById(R.id.others_profile_invite_item_location);
            address = itemView.findViewById(R.id.others_profile_invite_item_address);
            explanation = itemView.findViewById(R.id.others_profile_invite_item_explanation);
            sendRequest = itemView.findViewById(R.id.others_profile_invite_item_send_request_button);
            man = itemView.findViewById(R.id.others_profile_item_man_text);
            woman = itemView.findViewById(R.id.others_profile_item_woman_text);
            time = itemView.findViewById(R.id.others_profile_invite_item_time);
            attendant = itemView.findViewById(R.id.others_profile_attendant_button);

        }
    }
}
