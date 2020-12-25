package com.imc.getout.fragments.mainFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.provider.ContactsContract;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.imc.getout.AttendantsActivity;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.cards.ProfileInviteCard;
import com.imc.getout.fragments.mainFragments.profileFragments.MyAttendantInvitesFragment;
import com.imc.getout.fragments.mainFragments.profileFragments.MyInvitesFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.Holder> {

    private ArrayList<ProfileInviteCard> inviteCards;
    private Context mContext;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private FragmentTransaction fragmentTransaction;
    private ProfileFragment profileFragment;
    private MyInvitesFragment myInvitesFragment;
    private MyAttendantInvitesFragment myAttendantInvitesFragment;

    public ProfileRecyclerAdapter(ArrayList<ProfileInviteCard> inviteCards, Context mContext, FirebaseFirestore firebaseFirestore, FirebaseAuth firebaseAuth, FragmentTransaction fragmentTransaction, ProfileFragment profileFragment,MyInvitesFragment myInvitesFragment,MyAttendantInvitesFragment myAttendantInvitesFragment) {
        this.mContext = mContext;
        this.inviteCards = inviteCards;
        this.firebaseFirestore = firebaseFirestore;
        this.firebaseAuth = firebaseAuth;
        this.fragmentTransaction = fragmentTransaction;
        this.profileFragment = profileFragment;
        this.myInvitesFragment = myInvitesFragment;
        this.myAttendantInvitesFragment = myAttendantInvitesFragment;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.profile_invite_item,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int position) {

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int deleteInvitePaddingLeft = (int)(width*12.2)/100;
        int attendantsPaddingLeft = (int) (width*9)/100;
        int attendantsPaddingRight = (int) (width*4)/100;

        holder.type.setText(inviteCards.get(position).getInviteTypeText());
        holder.title.setText(inviteCards.get(position).getInviteTitleText());
        holder.time.setText(inviteCards.get(position).getTimeText());
        holder.location.setText(inviteCards.get(position).getInviteLocationText());
        holder.address.setText(inviteCards.get(position).getInviteAddressText());
        holder.explanation.setText(inviteCards.get(position).getInviteExplanationText());
        holder.deleteInvite.setVisibility(View.VISIBLE);
        holder.attendants.setPadding(attendantsPaddingLeft,0,attendantsPaddingRight,0);
        if (inviteCards.get(position).getAttendants() != null) {
            String attendantsText = String.valueOf(inviteCards.get(position).getAttendants().size());
            holder.attendants.setText(attendantsText);
        } else {
            String attendantsText = "0";
            holder.attendants.setText(attendantsText);
        }
        holder.attendants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,AttendantsActivity.class);
                intent.putStringArrayListExtra("list",inviteCards.get(position).getAttendants());
                mContext.startActivity(intent);
            }
        });
        holder.deleteInvite.setPadding(deleteInvitePaddingLeft,0,0,0);
        holder.deleteInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myInvitesFragment != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Bu daveti silmek istiyor musunuz?");
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

                            DocumentReference documentReference = firebaseFirestore.collection(collectionName).document(documentId);

                            documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                String uid = firebaseAuth.getCurrentUser().getUid();
                                @Override
                                public void onSuccess(Void aVoid) {
                                    final DocumentReference dr = firebaseFirestore.collection("Users").document(uid);

                                    dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            String arrayName = "";
                                            String booleanName = "";

                                            switch (type) {
                                                case "Anlık":
                                                    arrayName = "instantInviteUids";
                                                    booleanName = "activeInstantInvite";
                                                    break;
                                                case "Planlı":
                                                    arrayName = "plannedInviteUids";
                                                    booleanName = "activePlannedInvite";
                                                    break;
                                                case "Etkinlik":
                                                    arrayName = "eventUids";
                                                    booleanName = "activeEvent";
                                                    break;
                                            }

                                            ArrayList<String> activeInviteUids = (ArrayList<String>) documentSnapshot.get(arrayName);
                                            Boolean activeInvite = (Boolean) documentSnapshot.getBoolean(booleanName);

                                            if (activeInviteUids != null) {
                                                activeInviteUids.remove(documentId);
                                            }

                                            if (activeInviteUids.isEmpty()) {
                                                activeInvite = false;
                                            }

                                            HashMap<String,Object> update = new HashMap<>();
                                            update.put(arrayName,activeInviteUids);
                                            update.put(booleanName,activeInvite);

                                            dr.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(mContext, "Davetiniz Silinmiştir", Toast.LENGTH_LONG).show();
//                                        fragmentTransaction.detach(myInvitesFragment).attach(myInvitesFragment).commit();

                                                    myInvitesFragment.getDataFromFirestore();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Bu davetten ayrılmak istiyor musunuz?");
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

                            final DocumentReference documentReference = firebaseFirestore.collection(collectionName).document(documentId);

                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    final String uid = firebaseAuth.getCurrentUser().getUid();
                                    ArrayList<String> attendants = (ArrayList<String>) documentSnapshot.get("attendants");

                                    attendants.remove(uid);

                                    HashMap<String,Object> update = new HashMap<>();
                                    update.put("attendants",attendants);

                                    documentReference.set(update,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            final DocumentReference dr = firebaseFirestore.collection("Users").document(uid);

                                            dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    String arrayName = "";

                                                    switch (type) {
                                                        case "Anlık":
                                                            arrayName = "attendantInstantInviteUids";
                                                            break;
                                                        case "Planlı":
                                                            arrayName = "attendantPlannedInviteUids";
                                                            break;
                                                        case "Etkinlik":
                                                            arrayName = "attendantEventUids";
                                                            break;
                                                    }

                                                    ArrayList<String> attendantInviteUids = (ArrayList<String>) documentSnapshot.get(arrayName);

                                                    if (attendantInviteUids != null) {
                                                        attendantInviteUids.remove(documentId);
                                                    }

                                                    HashMap<String,Object> update = new HashMap<>();
                                                    update.put(arrayName,attendantInviteUids);

                                                    dr.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(mContext, "Davetten ayrıldınız", Toast.LENGTH_LONG).show();

                                                            myAttendantInvitesFragment.getDataFromFirestore();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    });
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
            }
        });
    }

    @Override
    public int getItemCount() {
        return inviteCards.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView type,title,location,address,explanation,time;
        Button deleteInvite,attendants;
        RelativeLayout card;
        public Holder(@NonNull View itemView) {
            super(itemView);

            type = itemView.findViewById(R.id.profile_invite_item_type);
            title = itemView.findViewById(R.id.profile_invite_item_title);
            time = itemView.findViewById(R.id.profile_invite_item_time);
            location = itemView.findViewById(R.id.profile_invite_item_location);
            address = itemView.findViewById(R.id.profile_invite_item_address);
            explanation = itemView.findViewById(R.id.profile_invite_item_explanation);
            deleteInvite = itemView.findViewById(R.id.profile_invite_item_button);
            attendants = itemView.findViewById(R.id.profile_invite_item_attendants_button);
            card = itemView.findViewById(R.id.profile_invite_item);
        }
    }
}
