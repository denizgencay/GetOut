package com.imc.getout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.JsonObject;
import com.imc.getout.Notifications.ApiClient;
import com.imc.getout.fragments.mainFragments.MessagesFragment;
import com.imc.getout.fragments.mainFragments.ProfileRecyclerAdapter;
import com.imc.getout.fragments.mainFragments.cards.ProfileInviteCard;
import com.imc.getout.models.OthersProfileModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OthersProfileActivity extends AppCompatActivity {

    private OthersProfileRecyclerAdapter adapter;

    private ArrayList<ProfileInviteCard> invites,instantInvites,plannedInvites,events;

    private TextView name,age,city,aboutMe,username,activeInvites,aboutMeTextHolder;
    private ImageView profilePhoto,sendMessage,sendFriendRequest;
    private String uid,myUid;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private final int CHAT_RESULT = 1;

    private OthersProfileModel othersProfileModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);

        othersProfileModel = new OthersProfileModel();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        myUid = firebaseAuth.getCurrentUser().getUid();

        name = findViewById(R.id.others_profile_name);
        username = findViewById(R.id.others_profile_username);
        age = findViewById(R.id.others_profile_age);
        city = findViewById(R.id.others_profile_city);
        aboutMe = findViewById(R.id.others_profile_aboutMe);
        activeInvites = findViewById(R.id.others_profile_active_invites_text);
        profilePhoto = findViewById(R.id.others_profile_page_pp);
        aboutMeTextHolder = findViewById(R.id.others_profile_aboutMe_text);
        invites = new ArrayList<>();
        instantInvites = new ArrayList<>();
        plannedInvites = new ArrayList<>();
        events = new ArrayList<>();
        sendMessage = findViewById(R.id.others_profile_send_message);
        sendFriendRequest = findViewById(R.id.others_profile_send_friend_request);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!othersProfileModel.isAlreadyFriends()) {
                    if (othersProfileModel.isPremium()) {
                        startChatIntent();
                    } else {
                        if (othersProfileModel.isInviteRequested() || othersProfileModel.isInviteAttendant()) {
                            startChatIntent();
                        } else {
                            Toast.makeText(OthersProfileActivity.this, "Sadece premium kullanıcılar arkadaş olmadıkları kişilere mesaj gönderebilir"
                                    , Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    startChatIntent();
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.others_profile_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OthersProfileRecyclerAdapter(invites,firebaseFirestore,firebaseAuth,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        Intent intent = getIntent();

        uid = intent.getStringExtra("uid");

        getMyInfo();
        getIfChatActive();
        getDataFromFirestore();

    }

    private void getIfChatActive() {
        CollectionReference collectionReference = firebaseFirestore.collection("Chats");

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        String senderUid = (String) snapshot.getString("senderUid");
                        String receiverUid = (String) snapshot.getString("receiverUid");

                        if ((senderUid.equals(myUid) && receiverUid.equals(uid)) ||
                                (senderUid.equals(uid) && receiverUid.equals(myUid))) {

                            othersProfileModel.setChatActive(true);
                            othersProfileModel.setChatId(snapshot.getId());
                            break;
                        }
                    }
                }
            }
        });
    }

    private void getMyInfo() {
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(myUid);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                boolean isPremium = (boolean) documentSnapshot.getBoolean("isPremium");
                String senderName = (String) documentSnapshot.getString("name");
                String senderPhotoUri = (String) documentSnapshot.getString("photoUri");
                ArrayList<String> chats = (ArrayList<String>) documentSnapshot.get("chats");
                ArrayList<String> instantInviteUids = (ArrayList<String>) documentSnapshot.get("instantInviteUids");
                ArrayList<String> plannedInviteUids = (ArrayList<String>) documentSnapshot.get("plannedInviteUids");
                ArrayList<String> eventUids = (ArrayList<String>) documentSnapshot.get("eventUids");

                othersProfileModel.setPremium(isPremium);
                othersProfileModel.setSenderName(senderName);
                othersProfileModel.setSenderPhotoUri(senderPhotoUri);
                othersProfileModel.setChats(chats);
                othersProfileModel.setName(senderName);

                if (instantInviteUids != null) {
                    for (String instantInviteUid : instantInviteUids) {
                        DocumentReference documentReference1 = firebaseFirestore.collection("instantInvites").document(instantInviteUid);

                        documentReference1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                ArrayList<String> requests = (ArrayList<String>) documentSnapshot.get("requests");
                                ArrayList<String> attendants = (ArrayList<String>) documentSnapshot.get("attendants");

                                if (requests != null) {
                                    for (String requestUid : requests) {
                                        if (requestUid.equals(uid)) {
                                            othersProfileModel.setInviteRequested(true);
                                            break;
                                        }
                                    }
                                }

                                if (attendants != null && !othersProfileModel.isInviteRequested()) {
                                    for (String attendantUid : attendants) {
                                        if (attendantUid.equals(uid)) {
                                            othersProfileModel.setInviteAttendant(true);
                                            break;
                                        }
                                    }
                                }
                            }
                        });
                    }
                }

                if (plannedInviteUids != null) {
                    for (String plannedInviteUid : plannedInviteUids) {
                        DocumentReference documentReference1 = firebaseFirestore.collection("plannedInvites").document(plannedInviteUid);

                        documentReference1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                ArrayList<String> requests = (ArrayList<String>) documentSnapshot.get("requests");
                                ArrayList<String> attendants = (ArrayList<String>) documentSnapshot.get("attendants");

                                if (requests != null) {
                                    for (String requestUid : requests) {
                                        if (requestUid.equals(uid)) {
                                            othersProfileModel.setInviteRequested(true);
                                            break;
                                        }
                                    }
                                }

                                if (attendants != null && !othersProfileModel.isInviteRequested()) {
                                    for (String attendantUid : attendants) {
                                        if (attendantUid.equals(uid)) {
                                            othersProfileModel.setInviteAttendant(true);
                                            break;
                                        }
                                    }
                                }
                            }
                        });
                    }
                }

                if (eventUids != null) {
                    for (String eventUid : eventUids) {
                        DocumentReference documentReference1 = firebaseFirestore.collection("events").document(eventUid);

                        documentReference1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                ArrayList<String> requests = (ArrayList<String>) documentSnapshot.get("requests");
                                ArrayList<String> attendants = (ArrayList<String>) documentSnapshot.get("attendants");

                                if (requests != null) {
                                    for (String requestUid : requests) {
                                        if (requestUid.equals(uid)) {
                                            othersProfileModel.setInviteRequested(true);
                                            break;
                                        }
                                    }
                                }

                                if (attendants != null && !othersProfileModel.isInviteRequested()) {
                                    for (String attendantUid : attendants) {
                                        if (attendantUid.equals(uid)) {
                                            othersProfileModel.setInviteAttendant(true);
                                            break;
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public void getDataFromFirestore() {
        invites.clear();
        final String guestUid = firebaseAuth.getCurrentUser().getUid();

        final DocumentReference documentReference = firebaseFirestore.collection("Users").document(uid);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(OthersProfileActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }

                if (documentSnapshot != null) {
                    final String nameText = (String) documentSnapshot.getString("name");
                    String usernameText = (String) documentSnapshot.getString("username");
                    String cityText = (String) documentSnapshot.getString("city");
                    final String photoUrl = (String) documentSnapshot.getString("photoUri");
                    Timestamp birthday = (Timestamp) documentSnapshot.getTimestamp("birthday");
                    String aboutMeText = (String) documentSnapshot.getString("aboutMe");
                    String pushToken = (String) documentSnapshot.getString("pushToken");
                    final ArrayList<String> friends = (ArrayList<String>) documentSnapshot.get("friends");
                    final ArrayList<String> friendRequests = (ArrayList<String>) documentSnapshot.get("friendRequests");
                    final ArrayList<String> instantInviteUids = (ArrayList<String>) documentSnapshot.get("instantInviteUids");
                    final ArrayList<String> plannedInviteUids = (ArrayList<String>) documentSnapshot.get("plannedInviteUids");
                    final ArrayList<String> eventUids = (ArrayList<String>) documentSnapshot.get("eventUids");

                    othersProfileModel.setReceiverName(nameText);
                    othersProfileModel.setReceiverPhotoUri(photoUrl);
                    othersProfileModel.setPushToken(pushToken);

                    boolean alreadyFriends = false;
                    boolean requestSent = false;

                    if (friends != null) {
                        for (String userUid : friends) {
                            if (userUid.equals(guestUid)) {
                                sendFriendRequest.setImageResource(R.drawable.ic_unfriend);
                                sendFriendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String message = nameText + " adlı kişiyi arkadaşlıktan çıkarmak istiyor musunuz?";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(OthersProfileActivity.this);
                                        builder.setMessage(message);
                                        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                final DocumentReference documentReference1 = firebaseFirestore.collection("Users").document(myUid);

                                                documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        ArrayList<String> myFriends = (ArrayList<String>) documentSnapshot.get("friends");

                                                        if (myFriends != null) {
                                                            myFriends.remove(uid);
                                                            HashMap<String,Object> update = new HashMap<>();
                                                            update.put("friends",myFriends);

                                                            documentReference1.set(update,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    ArrayList<String> friendsClone = friends;
                                                                    friendsClone.remove(myUid);
                                                                    HashMap<String,Object> updateUser = new HashMap<>();
                                                                    updateUser.put("friends",friendsClone);

                                                                    documentReference.set(updateUser,SetOptions.merge());
                                                                }
                                                            });
                                                        }
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
                                });
                                othersProfileModel.setAlreadyFriends(true);
                                alreadyFriends = true;
                                break;
                            }
                        }
                    }

                    if (friendRequests != null){
                        for (String userUid : friendRequests) {
                            if (userUid.equals(guestUid)) {
                                sendFriendRequest.setImageResource(R.drawable.ic_person_add_gray_24dp);
                                sendFriendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(OthersProfileActivity.this);
                                        builder.setMessage("Takip isteğinizi geri çekmek istiyor musunuz?");
                                        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ArrayList<String> friendRequestsClon = friendRequests;
                                                friendRequestsClon.remove(myUid);
                                                HashMap<String,Object> update = new HashMap<>();
                                                update.put("friendRequests",friendRequestsClon);
                                                documentReference.set(update,SetOptions.merge());
                                            }
                                        });
                                        builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                        builder.show();
                                    }
                                });
                                requestSent = true;
                                break;
                            }
                        }
                    }

                    if (!alreadyFriends && !requestSent) {
                        sendFriendRequest.setImageResource(R.drawable.ic_person_add_black_24dp);
                        sendFriendRequest.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                HashMap<String,Object> update = new HashMap<>();

                                if (friendRequests == null) {
                                    ArrayList<String> friendRequestsClon = new ArrayList<>();
                                    update.put("friendRequests",friendRequestsClon);
                                } else {
                                    ArrayList<String> friendRequestsClon = friendRequests;
                                    friendRequests.add(guestUid);
                                    update.put("friendRequests",friendRequestsClon);
                                }

                                documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendFriendRequest.setImageResource(R.drawable.ic_person_add_gray_24dp);
                                        String title = "Yeni bir arkadaşlık isteğiniz var.";
                                        String body = " adlı kişi sizi arkadaş eklemek istiyor.";
                                        String key = "friendRequest";
                                        sendNotification(title,body,key);
                                    }
                                });
                            }
                        });

                    }

                    int i = 0;

                    if (instantInviteUids != null) {
                        if (!instantInviteUids.isEmpty()) {
                            activeInvites.setVisibility(View.VISIBLE);
                            for (String uid : instantInviteUids) {
                                final int finalI = i;
                                DocumentReference dr = firebaseFirestore.collection("instantInvites").document(uid);

                                dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot != null) {
                                            String inviteTitleText = (String) documentSnapshot.getString("title");
                                            String inviteTypeText = "Anlık";
                                            String inviteLocationText = (String) documentSnapshot.getString("locationField");
                                            String timeText = (String) documentSnapshot.getString("hour");
                                            String inviteAddressText = (String) documentSnapshot.getString("locationAddress");
                                            String inviteExplanationText = (String) documentSnapshot.getString("explanation");
                                            String documentId = documentSnapshot.getId();
                                            long maxPerson = (long) documentSnapshot.get("numberOfPerson");
                                            long currentPerson = (long) documentSnapshot.get("currentNumberOfPerson");
                                            final ArrayList<String> instantAttendantUids = (ArrayList<String>) documentSnapshot.get("attendants");
                                            final ArrayList<String> instantRequestUids = (ArrayList<String>) documentSnapshot.get("requests");

                                            ProfileInviteCard pic = new ProfileInviteCard(inviteTypeText,inviteTitleText,inviteLocationText,inviteAddressText,inviteExplanationText,instantAttendantUids,instantRequestUids,documentId,finalI,maxPerson,currentPerson,timeText);
                                            invites.add(pic);

                                            invites.sort(new Comparator<ProfileInviteCard>() {
                                                @Override
                                                public int compare(ProfileInviteCard lhs, ProfileInviteCard rhs) {
                                                    return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                                }
                                            });
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                                i++;
                            }
                        }
                    }

                    if (plannedInviteUids != null) {
                        if (!plannedInviteUids.isEmpty()) {
                            activeInvites.setVisibility(View.VISIBLE);
                            for (String uid : plannedInviteUids) {
                                final int finalI = i;
                                DocumentReference dr2 = firebaseFirestore.collection("plannedInvites").document(uid);

                                dr2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot != null) {
                                            String inviteTitleText = (String) documentSnapshot.getString("title");
                                            String inviteTypeText = "Planlı";
                                            String inviteLocationText = (String) documentSnapshot.getString("locationField");
                                            String timeText = (String) documentSnapshot.getString("date") + " " +
                                                    (String) documentSnapshot.getString("hour");
                                            String inviteAddressText = (String) documentSnapshot.getString("locationAddress");
                                            String inviteExplanationText = (String) documentSnapshot.getString("explanation");
                                            String documentId = documentSnapshot.getId();
                                            long maxPerson = (long) documentSnapshot.get("numberOfPerson");
                                            long currentPerson = (long) documentSnapshot.get("currentNumberOfPerson");
                                            final ArrayList<String> plannedAttendantUids = (ArrayList<String>) documentSnapshot.get("attendants");
                                            final ArrayList<String> plannedRequestUids = (ArrayList<String>) documentSnapshot.get("requests");

                                            ProfileInviteCard pic = new ProfileInviteCard(inviteTypeText,inviteTitleText,inviteLocationText,inviteAddressText,inviteExplanationText,plannedAttendantUids,plannedRequestUids,documentId,finalI,maxPerson,currentPerson,timeText);
                                            invites.add(pic);

                                            System.out.println(invites.size());
                                            invites.sort(new Comparator<ProfileInviteCard>() {
                                                @Override
                                                public int compare(ProfileInviteCard lhs, ProfileInviteCard rhs) {
                                                    return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                                }
                                            });
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                                i++;
                            }
                        }
                    }

                    if (eventUids != null) {
                        if (!eventUids.isEmpty()) {
                            activeInvites.setVisibility(View.VISIBLE);
                            for (String uid : eventUids) {
                                final int finalI = 0;
                                DocumentReference dr3 = firebaseFirestore.collection("events").document(uid);

                                dr3.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot != null) {
                                            String inviteTitleText = (String) documentSnapshot.getString("title");
                                            String inviteTypeText = "Etkinlik";
                                            String inviteLocationText = (String) documentSnapshot.getString("locationField");
                                            String timeText = (String) documentSnapshot.getString("date") + " " +
                                                    (String) documentSnapshot.getString("hour");
                                            String inviteAddressText = (String) documentSnapshot.getString("locationAddress");
                                            String inviteExplanationText = (String) documentSnapshot.getString("explanation");
                                            String documentId = documentSnapshot.getId();
                                            long maxPerson = (long) documentSnapshot.get("numberOfPerson");
                                            long currentPerson = (long) documentSnapshot.get("currentNumberOfPerson");
                                            final ArrayList<String> eventAttendantUids = (ArrayList<String>) documentSnapshot.get("attendants");
                                            final ArrayList<String> eventRequestUids = (ArrayList<String>) documentSnapshot.get("requests");

                                            ProfileInviteCard pic = new ProfileInviteCard(inviteTypeText,inviteTitleText,inviteLocationText,inviteAddressText,inviteExplanationText,eventAttendantUids,eventRequestUids,documentId,finalI,maxPerson,currentPerson,timeText);
                                            invites.add(pic);

                                            System.out.println(invites.size());
                                            invites.sort(new Comparator<ProfileInviteCard>() {
                                                @Override
                                                public int compare(ProfileInviteCard lhs, ProfileInviteCard rhs) {
                                                    return lhs.getPriority() < rhs.getPriority() ? -1 : (lhs.getPriority() > rhs.getPriority()) ? 1 : 0;
                                                }
                                            });
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                                i++;
                            }
                        }
                    }

                    long ageInMillies = birthday.toDate().getTime();
                    Date now = new Date();
                    long nowInMillies = now.getTime();
                    long aYearInMillies = (long)(1000*60*60*24*365.25);
                    int userAge = (int)((nowInMillies-ageInMillies)/aYearInMillies);

                    name.setText(nameText);
                    username.setText(usernameText);
                    city.setText(cityText);
                    age.setText(String.valueOf(userAge));
                    aboutMe.setText(aboutMeText);
                    if (aboutMe.getText().toString().isEmpty()) {
                        aboutMeTextHolder.setVisibility(View.INVISIBLE);
                    }
                    Picasso.get().load(photoUrl).noFade().into(profilePhoto);
                }
            }
        });

    }

    public void sendNotification(String title, String body, String key) {
        JsonObject payload = buildNotificationPayload(title,body,key);

        ApiClient.getApiService().sendNotification(payload).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {

                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(OthersProfileActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private JsonObject buildNotificationPayload(String title, String body, String key){
        JsonObject payload = new JsonObject();
        payload.addProperty("to",othersProfileModel.getPushToken());

        String bodyText = othersProfileModel.getName() + body;

        JsonObject notification = new JsonObject();
        notification.addProperty("title",title);
        notification.addProperty("body",bodyText);
        notification.addProperty("sound","default");

        JsonObject data = new JsonObject();
        data.addProperty("key",key);

        payload.add("notification",notification);
        payload.add("data",data);

        return payload;
    }

    private void startChatIntent() {
        if (othersProfileModel.isChatActive() && othersProfileModel.getChatId() != null) {
            Intent intent = new Intent(OthersProfileActivity.this, ChatActivity.class);
            intent.putExtra("userId", uid);
            intent.putExtra("chatId", othersProfileModel.getChatId());
            startActivityForResult(intent, 1);
        } else {
            HashMap<String, Object> data = new HashMap<>();
            data.put("senderName", othersProfileModel.getSenderName());
            data.put("senderUid", myUid);
            data.put("senderPhotoUri", othersProfileModel.getSenderPhotoUri());
            data.put("receiverName", othersProfileModel.getReceiverName());
            data.put("receiverUid", uid);
            data.put("receiverPhotoUri", othersProfileModel.getReceiverPhotoUri());

            CollectionReference collectionReference = firebaseFirestore.collection("Chats");

            collectionReference.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(final DocumentReference documentReference) {
                    Intent intent = new Intent(OthersProfileActivity.this, ChatActivity.class);
                    intent.putExtra("userId", uid);
                    intent.putExtra("chatId", documentReference.getId());
                    startActivityForResult(intent, 1);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHAT_RESULT && resultCode == CHAT_RESULT) {
            final String chatId = data.getStringExtra("chatId");

            final DocumentReference documentReference = firebaseFirestore.collection("Chats").document(chatId);

            documentReference.collection("messages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.size() == 0) {
                        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                othersProfileModel.setChatActive(false);
                                othersProfileModel.setChatId(null);
                            }
                        });
                    }
                }
            });
        }
    }
}
