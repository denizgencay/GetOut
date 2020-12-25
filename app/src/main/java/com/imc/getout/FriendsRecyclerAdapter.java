package com.imc.getout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.imc.getout.fragments.mainFragments.cards.FriendsCard;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendsRecyclerAdapter extends RecyclerView.Adapter<FriendsRecyclerAdapter.Holder> {

    private ArrayList<FriendsCard> friends;
    private FriendsActivity activity;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserUid;
    private ViewGroup parent;

    public FriendsRecyclerAdapter(ArrayList<FriendsCard> friends, FriendsActivity activity, FirebaseFirestore firebaseFirestore,String currentUserUid) {
        this.friends = friends;
        this.activity = activity;
        this.firebaseFirestore = firebaseFirestore;
        this.currentUserUid = currentUserUid;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.friends_card,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final FriendsCard card = friends.get(position);
        final ArrayList<String> myFriendUids = card.getMyFriends();
        final ArrayList<String> userFriendsUids = card.getUserFriends();

        holder.name.setText(card.getname());
        holder.username.setText(card.getUsername());
        Picasso.get().load(card.getPhotoUri()).noFade().into(holder.profilePhoto);
        holder.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(parent.getContext(), "Message", Toast.LENGTH_SHORT).show();
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = card.getname() + " adlı kişiyi arkadaşlıktan çıkarmak istiyor musunuz?";
                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                builder.setMessage(message);
                builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myFriendUids.remove(card.getUid());

                        HashMap<String,Object> myUpdate = new HashMap<>();
                        myUpdate.put("friends",myFriendUids);

                        DocumentReference documentReference = firebaseFirestore.collection("Users").document(currentUserUid);

                        documentReference.set(myUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                userFriendsUids.remove(currentUserUid);

                                HashMap<String,Object> userUpdate = new HashMap<>();
                                userUpdate.put("friends",userFriendsUids);

                                DocumentReference documentReference1 = firebaseFirestore.collection("Users").document(card.getUid());

                                documentReference1.set(userUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        activity.setResult(3);
                                        activity.finish();
                                    }
                                });
                            }
                        });
                    }
                });
                builder.show();
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parent.getContext(),OthersProfileActivity.class);
                intent.putExtra("uid",card.getUid());
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView name,username;
        ImageView profilePhoto,message,delete;
        RelativeLayout card;

        public Holder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.friends_card_name);
            username = itemView.findViewById(R.id.friends_card_username);
            profilePhoto = itemView.findViewById(R.id.friends_card_profile_image);
            message = itemView.findViewById(R.id.friends_card_message);
            delete = itemView.findViewById(R.id.friends_card_delete);
            card = itemView.findViewById(R.id.friends_card);
        }
    }

    public void filterList(ArrayList<FriendsCard> filteredList) {
        friends = filteredList;
        notifyDataSetChanged();
    }
}
