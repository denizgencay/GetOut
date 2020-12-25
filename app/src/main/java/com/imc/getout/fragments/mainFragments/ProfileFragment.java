package com.imc.getout.fragments.mainFragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.imc.getout.FriendsActivity;
import com.imc.getout.ProfileEditActivity;
import com.imc.getout.R;
import com.imc.getout.SettingsActivity;
import com.imc.getout.SignInActivity;
import com.imc.getout.fragments.mainFragments.cards.ProfileInviteCard;
import com.imc.getout.fragments.mainFragments.profileFragments.MyAttendantInvitesFragment;
import com.imc.getout.fragments.mainFragments.profileFragments.MyInvitesFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView name,age,city,aboutMe,username,activeInvites;
    private ImageView photo,profileEdit,settings,friends;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ProfileRecyclerAdapter adapter;
    private NestedScrollView nestedScrollView;
    private LinearLayout linearLayout;
    private ProgressBar progressBar;

    private ArrayList<ProfileInviteCard> invites;
    private FragmentTransaction fragmentTransaction;

    private int PROFILE_EDIT_REQUEST_CODE = 1;
    private int SETTINGS_REQUEST_CODE = 2;
    private int FRIENDS_REQUEST_CODE = 3;

    AppBarLayout appBarLayout;
    private ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        name = view.findViewById(R.id.profile_name);
        age = view.findViewById(R.id.profile_age);
        city = view.findViewById(R.id.profile_city);
        photo = view.findViewById(R.id.profile_page_pp);
        aboutMe = view.findViewById(R.id.profile_aboutMe);
        username = view.findViewById(R.id.profile_username);
        profileEdit = view.findViewById(R.id.profile_to_edit);
        settings = view.findViewById(R.id.profile_to_settings);
        nestedScrollView = view.findViewById(R.id.profileScrollView);
        linearLayout = view.findViewById(R.id.profileLinearLayout);
        progressBar = view.findViewById(R.id.profile_progress_bar);

        //activeInvites = view.findViewById(R.id.profile_active_invites_text);
        friends = view.findViewById(R.id.profile_to_friends);
        invites = new ArrayList<>();

        viewPager = (ViewPager) view.findViewById(R.id.profile_viewpager);
        setupViewPager(viewPager);

        TabLayout tabs = (TabLayout) view.findViewById(R.id.profile_tabs);
        tabs.setupWithViewPager(viewPager);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        fragmentTransaction = getFragmentManager().beginTransaction();
        ProfileFragment profileFragment = this;


        profileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                startActivityForResult(intent,PROFILE_EDIT_REQUEST_CODE);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),SettingsActivity.class);
                startActivityForResult(intent,SETTINGS_REQUEST_CODE);
            }
        });

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FriendsActivity.class);
                startActivityForResult(intent,FRIENDS_REQUEST_CODE);
            }
        });

        getDataFromFirestore();

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new MyInvitesFragment(this,viewPager,nestedScrollView,progressBar),"Açık Davetlerim");
        adapter.addFragment(new MyAttendantInvitesFragment(this,viewPager,nestedScrollView,progressBar),"Katıldığım Davetler");
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager childFragmentManager,int behavior) {
            super(childFragmentManager,behavior);
        }

        public void addFragment(Fragment fragment,String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void getDataFromFirestore() {

        if (firebaseAuth.getCurrentUser() != null) {
            final DocumentReference documentReference = firebaseFirestore.collection("Users")
                    .document(firebaseAuth.getCurrentUser().getUid());

            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    }
                        String nameText = (String) documentSnapshot.getString("name");
                        Timestamp birthday = (Timestamp) documentSnapshot.getTimestamp("birthday");
                        String cityText = (String) documentSnapshot.getString("city");
                        String photoUrl = (String) documentSnapshot.getString("photoUri");
                        String aboutMeText = (String) documentSnapshot.getString("aboutMe");
                        String usernameText = (String) documentSnapshot.getString("username");

                        if (birthday != null) {
                            long ageInMillies = birthday.toDate().getTime();
                            Date now = new Date();
                            long nowInMillies = now.getTime();
                            long aYearInMillies = (long)(1000*60*60*24*365.25);
                            int userAge = (int)((nowInMillies-ageInMillies)/aYearInMillies);

                            age.setText(String.valueOf(userAge));
                        }

                        name.setText(nameText);

                        city.setText(cityText);
                        aboutMe.setText(aboutMeText);
                        username.setText(usernameText);
                        Picasso.get().load(photoUrl).noFade().into(photo);
                    }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(requestCode + " " +resultCode);

        if (requestCode == PROFILE_EDIT_REQUEST_CODE) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        } else if (resultCode == SETTINGS_REQUEST_CODE) {
            Intent intent = new Intent(getActivity(),SignInActivity.class);
            startActivity(intent);
            getActivity().finish();
        } else if (resultCode == FRIENDS_REQUEST_CODE) {
            Intent intent = new Intent(getActivity(),FriendsActivity.class);
            startActivityForResult(intent,FRIENDS_REQUEST_CODE);
        }
    }

}
