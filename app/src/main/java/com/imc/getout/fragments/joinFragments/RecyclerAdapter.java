package com.imc.getout.fragments.joinFragments;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imc.getout.OthersProfileActivity;
import com.imc.getout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

    private ArrayList<Invite> invites;
    private ViewGroup parent;

    public RecyclerAdapter(ArrayList<Invite> invites) {
       this.invites = invites;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_view_item,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int position) {
        holder.title.setText(invites.get(position).getTitle());
        holder.name.setText(invites.get(position).getname());
        holder.location.setText(invites.get(position).getLocation());
        holder.date.setText(invites.get(position).getDate());
        holder.personNumber.setText(invites.get(position).getPersonNumber());
        holder.address.setText(invites.get(position).getAddress());
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parent.getContext(), OthersProfileActivity.class);
                intent.putExtra("uid",invites.get(position).getUserUid());
                parent.getContext().startActivity(intent);
            }
        });
        Picasso.get().load(invites.get(position).getPhotoUrl()).noFade().into(holder.image);
    }

    @Override
    public int getItemCount() {
        return invites.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        TextView title,name,location,date,personNumber,address;
        ImageView image,more;

        public Holder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.recycler_item_invite_topic);
            name = itemView.findViewById(R.id.recycler_item_name);
            location = itemView.findViewById(R.id.recycler_item_location);
            date = itemView.findViewById(R.id.recycler_item_date);
            personNumber = itemView.findViewById(R.id.recycler_item_person_number);
            address = itemView.findViewById(R.id.recycler_item_address);
            image = itemView.findViewById(R.id.profile_image);
            more = itemView.findViewById(R.id.recycler_item_more);
        }
    }

    public void filterList(ArrayList<Invite> filteredList) {
        invites = filteredList;
        notifyDataSetChanged();
    }

}
