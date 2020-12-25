package com.imc.getout.fragments.mainFragments.profileFragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.imc.getout.R;
import com.imc.getout.fragments.mainFragments.ProfileFragment;
import com.imc.getout.fragments.mainFragments.ProfileRecyclerAdapter;
import com.imc.getout.fragments.mainFragments.cards.ProfileInviteCard;

import java.util.ArrayList;
import java.util.Comparator;

public class MyInvitesFragment extends Fragment {

    private ProgressBar progressBar;
    private NestedScrollView nestedScrollView;
    private ViewPager viewPager;
    private RecyclerView recyclerView;
    private ProfileRecyclerAdapter adapter;

    private ArrayList<ProfileInviteCard> invites;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ProfileFragment profileFragment;
    private FragmentTransaction fragmentTransaction;

    private int initialViewPagerHeight,initialScrollViewHeight;
    private int width,height,cardHeight;

    private LinearLayout.LayoutParams layoutParams;
    private RelativeLayout.LayoutParams nestedScrollViewParams;

    private ListenerRegistration listenerRegistration;

    public MyInvitesFragment(ProfileFragment profileFragment, ViewPager viewPager,NestedScrollView nestedScrollView,ProgressBar progressBar) {
        this.profileFragment = profileFragment;
        this.viewPager = viewPager;
        this.nestedScrollView = nestedScrollView;
        this.progressBar = progressBar;

        WindowManager wm = (WindowManager) profileFragment.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }

    @Override
    public void onResume() {
        super.onResume();
        nestedScrollView.smoothScrollTo(0,0);
        viewPager.setVisibility(View.VISIBLE);
        getDataFromFirestore();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewPager.setVisibility(View.INVISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_invites,container,false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        invites = new ArrayList<>();

        fragmentTransaction = getFragmentManager().beginTransaction();

        recyclerView = view.findViewById(R.id.my_invites_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfileRecyclerAdapter(invites,profileFragment.getContext(),firebaseFirestore,firebaseAuth,fragmentTransaction,profileFragment,this,null);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        layoutParams = new LinearLayout.LayoutParams(viewPager.getLayoutParams());
        nestedScrollViewParams = new RelativeLayout.LayoutParams(nestedScrollView.getLayoutParams());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listenerRegistration.remove();
    }

    public void getDataFromFirestore() {
        initialViewPagerHeight = (int)(height*1.9)/10;
        initialScrollViewHeight = (int)(height*9.5)/10;
        cardHeight = (int)(height*3.81)/10;
        String uid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(uid);

        listenerRegistration = documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                invites.clear();
                final ArrayList<String> instantInviteUids = (ArrayList<String>) documentSnapshot.get("instantInviteUids");
                final ArrayList<String> plannedInviteUids = (ArrayList<String>) documentSnapshot.get("plannedInviteUids");
                final ArrayList<String> eventUids = (ArrayList<String>) documentSnapshot.get("eventUids");

                int total = instantInviteUids.size() + plannedInviteUids.size() + eventUids.size();

                if (total > 0) {
                    initialViewPagerHeight = cardHeight*total;
                    initialScrollViewHeight += initialViewPagerHeight;

                    int viewWidth = viewPager.getWidth();
                    int nestedWidth = nestedScrollView.getWidth();

                    layoutParams.height = initialViewPagerHeight;
                    layoutParams.width = viewWidth;
                    viewPager.setLayoutParams(layoutParams);

                    nestedScrollViewParams.height = initialScrollViewHeight;
                    nestedScrollViewParams.width = nestedWidth;
                    nestedScrollView.setLayoutParams(nestedScrollViewParams);

                }

                int i = 0;

                if (instantInviteUids != null) {
                    if (!instantInviteUids.isEmpty()) {
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
                        for (String uid : eventUids) {
                            final int finalI = i;
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
            }
        });
    }
}
