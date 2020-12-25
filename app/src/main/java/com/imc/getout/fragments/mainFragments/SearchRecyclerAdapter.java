package com.imc.getout.fragments.mainFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.imc.getout.OthersProfileActivity;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.cards.SearchCard;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.Holder> {

    private ArrayList<SearchCard> users;
    private ViewGroup parent;
    private ArrayList<String> lastSearchedUids;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserUid;
    private SearchFragment fragment;
    private FragmentTransaction fragmentTransaction;
    private EditText searchBar;

    public SearchRecyclerAdapter(ArrayList<SearchCard> users,FirebaseFirestore firebaseFirestore,String currentUserUid,SearchFragment fragment,FragmentTransaction fragmentTransaction,EditText searchBar) {
        this.users = users;
        this.firebaseFirestore = firebaseFirestore;
        this.currentUserUid = currentUserUid;
        this.fragment = fragment;
        this.fragmentTransaction = fragmentTransaction;
        this.searchBar = searchBar;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        this.parent = parent;
        View view = layoutInflater.inflate(R.layout.search_card,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int position) {
        holder.name.setText(users.get(position).getname());
        holder.username.setText(users.get(position).getUsername());
        Picasso.get().load(users.get(position).getPhotoUrl()).noFade().into(holder.profilePhoto);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastSearchedUids != null) {
                    Collections.reverse(lastSearchedUids);
                }

                boolean alreadySearched = false;

                if (lastSearchedUids != null) {
                    for (String lastSearchedUid : lastSearchedUids) {
                        if (lastSearchedUid.equals(users.get(position).getUid())) {
                            alreadySearched = true;
                            break;
                        }
                    }
                }

                if (alreadySearched) {
                    lastSearchedUids.remove(users.get(position).getUid());
                    lastSearchedUids.add(users.get(position).getUid());

                    HashMap<String,Object> update = new HashMap<>();
                    update.put("lastSearched",lastSearchedUids);

                    DocumentReference documentReference = firebaseFirestore.collection("Users").document(currentUserUid);

                    documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            searchBar.setText("");
                            searchBar.clearFocus();
                            Intent intent = new Intent(parent.getContext(), OthersProfileActivity.class);
                            intent.putExtra("uid",users.get(position).getUid());
                            fragment.startActivityForResult(intent,1);
                        }
                    });
                } else {
                    if (lastSearchedUids == null) {
                        lastSearchedUids = new ArrayList<>();
                    }

                    if (lastSearchedUids.size() == 10) {
                        lastSearchedUids.remove(0);
                        lastSearchedUids.add(users.get(position).getUid());

                        HashMap<String,Object> update = new HashMap<>();
                        update.put("lastSearched",lastSearchedUids);
                        DocumentReference documentReference = firebaseFirestore.collection("Users").document(currentUserUid);

                        documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                searchBar.setText("");
                                searchBar.clearFocus();
                                Intent intent = new Intent(parent.getContext(), OthersProfileActivity.class);
                                intent.putExtra("uid",users.get(position).getUid());
                                fragment.startActivityForResult(intent,1);
                            }
                        });

                    } else {
                        lastSearchedUids.add(users.get(position).getUid());

                        HashMap<String,Object> update = new HashMap<>();
                        update.put("lastSearched",lastSearchedUids);
                        DocumentReference documentReference = firebaseFirestore.collection("Users").document(currentUserUid);

                        documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                searchBar.setText("");
                                searchBar.clearFocus();
                                Intent intent = new Intent(parent.getContext(), OthersProfileActivity.class);
                                intent.putExtra("uid",users.get(position).getUid());
                                fragment.startActivityForResult(intent,1);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView name,username;
        ImageView profilePhoto;
        RelativeLayout card;

        public Holder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.search_card_name);
            username = itemView.findViewById(R.id.search_card_username);
            profilePhoto = itemView.findViewById(R.id.search_card_profile_image);
            card = itemView.findViewById(R.id.search_card);
        }
    }

    public void filterList(ArrayList<SearchCard> filteredList) {
        users = filteredList;
        notifyDataSetChanged();
    }

    public void setType(ArrayList<SearchCard> list) {
        users = list;
        notifyDataSetChanged();
    }

    public void setLastSearchedUids(ArrayList<String> lastSearchedUids) {
        this.lastSearchedUids = lastSearchedUids;
    }

}
