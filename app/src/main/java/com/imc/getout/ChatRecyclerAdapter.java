package com.imc.getout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.imc.getout.fragments.mainFragments.cards.ChatCard;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.Holder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    private ArrayList<ChatCard> chatCards;

    public ChatRecyclerAdapter(ArrayList<ChatCard> chatCards,FirebaseFirestore firebaseFirestore) {
        this.chatCards = chatCards;
        this.firebaseFirestore = firebaseFirestore;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.chat_item_right,parent,false);
            return new Holder(view);
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.chat_item_left,parent,false);
            return new Holder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChatCard card = chatCards.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseUser.getUid();
        holder.text.setText(card.gettext());
        holder.time.setText(card.getDateText());
        if (!card.isSeen() && card.getReceiverUid().equals(uid)) {
            HashMap<String,Object> update = new HashMap<>();
            update.put("isSeen",true);
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DocumentReference documentReference = firebaseFirestore.collection("Chats").document(card.getChatId()).collection("messages").document(card.getMessageId());

            documentReference.set(update, SetOptions.merge());
        }
    }

    @Override
    public int getItemCount() {
        return chatCards.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView text,time;

        public Holder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.show_message);
            time = itemView.findViewById(R.id.message_time);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatCards.get(position).getSenderUid().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }
}
