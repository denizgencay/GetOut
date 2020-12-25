package com.imc.getout.fragments.mainFragments.notificationsFragments;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.imc.getout.OthersProfileActivity;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.cards.FriendRequestsCard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.Holder> {

    private ArrayList<FriendRequestsCard> requests;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ViewGroup parent;
    private Fragment fragment;
    private FragmentTransaction fragmentTransaction;

    public FriendRequestAdapter(ArrayList<FriendRequestsCard> requests, FirebaseAuth firebaseAuth, FirebaseFirestore firebaseFirestore,Fragment fragment,FragmentTransaction fragmentTransaction) {
        this.requests = requests;
        this.firebaseAuth = firebaseAuth;
        this.firebaseFirestore = firebaseFirestore;
        this.fragment = fragment;
        this.fragmentTransaction = fragmentTransaction;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.friend_request_card,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        final String receiverUid = firebaseAuth.getCurrentUser().getUid();

        final FriendRequestsCard frc = requests.get(position);

        final String senderUid = frc.getUid();

        holder.name.setText(frc.getname());
        holder.username.setText(frc.getUsername());
        Picasso.get().load(frc.getProfilePhoto()).noFade().into(holder.profilePhoto);
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> receiverFriendRequests = frc.getReceiverFriendsRequest();
                ArrayList<String> receiverFriends = frc.getReceiverFriends();

                receiverFriendRequests.remove(senderUid);
                receiverFriends.add(senderUid);

                HashMap<String,Object> receiverUpdate = new HashMap<>();
                receiverUpdate.put("friendRequests",receiverFriendRequests);
                receiverUpdate.put("friends",receiverFriends);

                DocumentReference documentReference = firebaseFirestore.collection("Users").document(receiverUid);

                documentReference.set(receiverUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        ArrayList<String> senderFriends = frc.getSenderFriends();

                        senderFriends.add(receiverUid);

                        HashMap<String,Object> senderUpdate = new HashMap<>();
                        senderUpdate.put("friends",senderFriends);

                        DocumentReference doc = firebaseFirestore.collection("Users").document(senderUid);

                        doc.set(senderUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                fragmentTransaction.detach(fragment).attach(fragment).commit();
                            }
                        });
                    }
                });
            }
        });
        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> receiverFriendRequests = frc.getReceiverFriendsRequest();

                receiverFriendRequests.remove(senderUid);

                HashMap<String,Object> update = new HashMap<>();
                update.put("friendRequests",receiverFriendRequests);

                DocumentReference documentReference = firebaseFirestore.collection("Users").document(receiverUid);

                documentReference.set(update,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fragmentTransaction.detach(fragment).attach(fragment).commit();
                    }
                });
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parent.getContext(), OthersProfileActivity.class);
                intent.putExtra("uid",frc.getUid());
                parent.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView name,username;
        ImageView profilePhoto,accept,reject;
        RelativeLayout card;

        public Holder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.friend_request_card_name);
            username = itemView.findViewById(R.id.friend_request_card_username);
            profilePhoto = itemView.findViewById(R.id.friend_request_card_profile_image);
            accept = itemView.findViewById(R.id.friend_request_card_accept);
            reject = itemView.findViewById(R.id.friend_request_card_reject);
            card = itemView.findViewById(R.id.friend_request_card);
        }
    }
}
