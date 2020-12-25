package com.imc.getout;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.imc.getout.fragments.mainFragments.cards.ProfileAttendantCard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AttendantsRecyclerAdapter extends RecyclerView.Adapter<AttendantsRecyclerAdapter.Holder> {

    private ArrayList<ProfileAttendantCard> cards;
    private ViewGroup parent;

    public AttendantsRecyclerAdapter(ArrayList<ProfileAttendantCard> cards) {
        this.cards = cards;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.attendant_card,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.name.setText(cards.get(position).getname());
        holder.age.setText(cards.get(position).getAge());
        holder.username.setText(cards.get(position).getUsername());
        if (cards.get(position).getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.more.setVisibility(View.INVISIBLE);
        }
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parent.getContext(),OthersProfileActivity.class);
                intent.putExtra("uid", cards.get(position).getUid());
                parent.getContext().startActivity(intent);
            }
        });
        Picasso.get().load(cards.get(position).getProfilePhoto()).noFade().into(holder.profilePhoto);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView name,age,username;
        ImageView profilePhoto,more;

        public Holder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.attendant_card_name);
            age = itemView.findViewById(R.id.attendant_card_age);
            username = itemView.findViewById(R.id.attendant_card_username);
            more = itemView.findViewById(R.id.attendant_card_more);
            profilePhoto = itemView.findViewById(R.id.attendant_card_profile_image);
        }
    }

}
