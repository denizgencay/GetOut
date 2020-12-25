package com.imc.getout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
//import com.google.firestore.v1.Document;
//import com.imc.getout.Notifications.ApiClient;
//import com.imc.getout.Notifications.Data;
//import com.imc.getout.Notifications.MyResponse;
//import com.imc.getout.Notifications.Sender;
//import com.imc.getout.Notifications.Token;
import com.google.gson.JsonObject;
import com.imc.getout.Notifications.ApiClient;
import com.imc.getout.Notifications.MyFirebaseMessaging;
import com.imc.getout.fragments.mainFragments.cards.ChatCard;
import com.imc.getout.models.ChatModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private ArrayList<ChatCard> chatCards;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private CircleImageView profilePhoto;
    private TextView userName;
    private EditText messageText;
    private ImageButton sendButton;
    private ImageView backButton;

    private RecyclerView recyclerView;
    private ChatRecyclerAdapter adapter;

    private String chatId,userId;
    private ChatModel chatModel;
    private NotificationApiService notificationApiService;
    private boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        chatId = intent.getStringExtra("chatId");
        userId = intent.getStringExtra("userId");
        MyFirebaseMessaging.USER_ID = userId;
        System.out.println(userId);
        setResult(1,intent);

        chatCards = new ArrayList<>();
        chatModel = new ChatModel();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

//        notificationApiService = ApiClient.getClient("https://fcm.googleapis.com/").create(NotificationApiService.class);

        profilePhoto = findViewById(R.id.chat_profile_image);
        userName = findViewById(R.id.chat_name);
        messageText = findViewById(R.id.chat_message_text);
        sendButton = findViewById(R.id.chat_send_message);
        backButton = findViewById(R.id.chat_back_button);

        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this,OthersProfileActivity.class);
                intent.putExtra("uid",userId);
                startActivity(intent);
            }
        });

        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this,OthersProfileActivity.class);
                intent.putExtra("uid",userId);
                startActivity(intent);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String message;
                Timestamp sendTimeDate;
                String uid = firebaseAuth.getCurrentUser().getUid();

                message = messageText.getText().toString().trim();
                Date date = new Date();
                sendTimeDate = new Timestamp(date);

                if (!message.isEmpty()) {
                    CollectionReference collectionReference = firebaseFirestore.collection("Chats").document(chatId).collection("messages");

                    HashMap<String,Object> data = new HashMap<>();
                    data.put("senderUid",uid);
                    data.put("receiverUid",userId);
                    data.put("text",message);
                    data.put("sendTime",sendTimeDate);
                    data.put("isSeen",false);

                    collectionReference.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            sendNotification(message);
                        }
                    });

                    messageText.setText("");
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ChatRecyclerAdapter(chatCards,firebaseFirestore);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        getMyInfo();
        getUserInfo();
        getDataFromFirestore();

    }

    private void sendNotification(String message) {
        JsonObject payload = buildNotificationPayload(message);

        ApiClient.getApiService().sendNotification(payload).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChatActivity.this, "Notification send successful", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ChatActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private JsonObject buildNotificationPayload(String message) {
        String myUid = firebaseAuth.getCurrentUser().getUid();
        JsonObject payload = new JsonObject();
        payload.addProperty("to",chatModel.getPushToken());

        String messageText = chatModel.getName() + ": " + message;

        JsonObject notification = new JsonObject();
        JsonObject data = new JsonObject();
        notification.addProperty("title","Yeni mesaj");
        notification.addProperty("body",messageText);
        notification.addProperty("sound","default");

        data.addProperty("key","Message");
        data.addProperty("chatId",chatId);
        data.addProperty("userId",myUid);

        payload.add("notification",notification);
        payload.add("data",data);

        return payload;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyFirebaseMessaging.USER_ID = null;
    }



    private void getMyInfo() {
        String myUid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(myUid);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name = (String) documentSnapshot.getString("name");

                chatModel.setName(name);
            }
        });
    }

    private void getUserInfo() {
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(userId);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String photoUri = (String) documentSnapshot.getString("photoUri");
                String name = (String) documentSnapshot.getString("name");
                String pushToken = (String) documentSnapshot.getString("pushToken");

                Picasso.get().load(photoUri).noFade().into(profilePhoto);
                userName.setText(name);
                chatModel.setPushToken(pushToken);
            }
        });

    }

    private void getDataFromFirestore() {
        CollectionReference collectionReference = firebaseFirestore.collection("Chats").document(chatId).collection("messages");

        collectionReference.orderBy("sendTime", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                chatCards.clear();

                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        String senderUid = (String) snapshot.getString("senderUid");
                        String text = (String) snapshot.get("text");
                        String receiverUid = (String) snapshot.get("receiverUid");
                        boolean isSeen = (boolean) snapshot.getBoolean("isSeen");
                        String messageId = (String) snapshot.getId();
                        Timestamp time = (Timestamp) snapshot.getTimestamp("sendTime");

                        Date date = time.toDate();
                        String dateText;

                        if (date.getHours() < 10) {
                            if (date.getMinutes() < 10) {
                                dateText = "0" + date.getHours() + ":" + "0" + date.getMinutes();
                            } else {
                                dateText = "0" + date.getHours() + ":" + date.getMinutes();
                            }
                        } else {
                            if (date.getMinutes() < 10) {
                                dateText = date.getHours() + ":" + "0" + date.getMinutes();
                            } else {
                                dateText = date.getHours() + ":" + date.getMinutes();
                            }
                        }


                        ChatCard card = new ChatCard(senderUid,receiverUid,text,dateText,isSeen,messageId,chatId);

                        chatCards.add(card);

                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(adapter.getItemCount()-1);
                    }
                }
            }
        });
    }
}
