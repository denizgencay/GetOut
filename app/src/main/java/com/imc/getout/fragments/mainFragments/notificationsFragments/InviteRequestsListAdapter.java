package com.imc.getout.fragments.mainFragments.notificationsFragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.cards.InviteRequestsListItem;

import java.util.ArrayList;

public class InviteRequestsListAdapter extends RecyclerView.Adapter<InviteRequestsListAdapter.Holder> {

    private ArrayList<InviteRequestsListItem> inviteRequestsListItems;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private Fragment fragment;
    private FragmentTransaction fragmentTransaction;

    public InviteRequestsListAdapter(ArrayList<InviteRequestsListItem> inviteRequestsListItems,Context context,FirebaseFirestore firebaseFirestore,FragmentTransaction fragmentTransaction,Fragment fragment) {
        this.inviteRequestsListItems = inviteRequestsListItems;
        this.context = context;
        this.firebaseFirestore = firebaseFirestore;
        this.fragment = fragment;
        this.fragmentTransaction = fragmentTransaction;
    }

    @NonNull
    @Override
    public InviteRequestsListAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.invite_requests_list,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteRequestsListAdapter.Holder holder, int position) {
        InviteRequestsListItem item = inviteRequestsListItems.get(position);

        if (item.getRequests().isEmpty()) {
            holder.title.setVisibility(View.GONE);
            holder.requests.setVisibility(View.GONE);
        } else {
            holder.title.setText(item.gettitle());
        }
        holder.requests.setLayoutManager(new LinearLayoutManager(context));
        InviteRequestsAdapter adapter = new InviteRequestsAdapter(item.getRequests(),context,firebaseFirestore,fragment,fragmentTransaction);
        holder.requests.setAdapter(adapter);
        holder.requests.setNestedScrollingEnabled(false);

    }

    @Override
    public int getItemCount() {
        return inviteRequestsListItems.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView title;
        RecyclerView requests;


        public Holder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.invite_requests_list_invite_type);
            requests = itemView.findViewById(R.id.invite_requests_list_recycler_view);
        }
    }
}
