package com.imc.getout.fragments.mainFragments;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.imc.getout.ChatActivity;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.cards.ChatCard;
import com.imc.getout.fragments.mainFragments.cards.MessagesCard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesRecyclerAdapter extends RecyclerView.Adapter<MessagesRecyclerAdapter.Holder> {

    private ArrayList<MessagesCard> messages;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private Fragment fragment;
    private FirebaseAuth firebaseAuth;

    public MessagesRecyclerAdapter(ArrayList<MessagesCard> messages, Context context,FirebaseFirestore firebaseFirestore,Fragment fragment,FirebaseAuth firebaseAuth) {
        this.messages = messages;
        this.context = context;
        this.firebaseFirestore = firebaseFirestore;
        this.fragment = fragment;
        this.firebaseAuth = firebaseAuth;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.message_card,null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        final MessagesCard card = messages.get(position);
        final String myUid = firebaseAuth.getCurrentUser().getUid();

        CollectionReference collectionReference = firebaseFirestore.collection("Chats").document(card.getChatUid()).collection("messages");

        collectionReference.orderBy("sendTime", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        holder.lastMessage.setText(snapshot.getString("text"));
                        break;
                    }
                    int ctr = 0;
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        if (snapshot.getString("receiverUid").equals(myUid) && !snapshot.getBoolean("isSeen") ) {
                            ctr++;
                        }
                    }
                    if (ctr > 0) {
                        holder.unSeenMessageCtr.setVisibility(View.VISIBLE);
                        holder.unSeenMessageCtr.setText(String.valueOf(ctr));
                    }
                }
            }
        });

        holder.name.setText(card.getname());
        Picasso.get().load(card.getProfilePhoto()).noFade().into(holder.profilePhoto);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chatId",card.getChatUid());
                intent.putExtra("userId",card.getUserUid());
                fragment.startActivityForResult(intent,1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView name,lastMessage,unSeenMessageCtr;
        CircleImageView profilePhoto;
        RelativeLayout card;

        public Holder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.message_card_name);
            lastMessage = itemView.findViewById(R.id.message_card_last_message);
            profilePhoto = itemView.findViewById(R.id.message_card_profile_image);
            unSeenMessageCtr = itemView.findViewById(R.id.message_card_unread_messages);
            card = itemView.findViewById(R.id.message_card);
        }
    }
}
