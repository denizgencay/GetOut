package com.imc.getout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.imc.getout.Notifications.MyFirebaseMessaging;
import com.imc.getout.fragments.mainFragments.HomeFragment;
import com.imc.getout.fragments.mainFragments.MessagesFragment;
import com.imc.getout.fragments.mainFragments.NotificationsFragment;
import com.imc.getout.fragments.mainFragments.ProfileFragment;
import com.imc.getout.fragments.mainFragments.SearchFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView.getSelectedItemId() == R.id.nav_home) {
            super.onBackPressed();
            finish();
        } else {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

        if (bundle != null) {
            redirect(bundle,bottomNav);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle bundle = intent.getExtras();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

        if (bundle != null) {
            redirect(bundle,bottomNav);
        }
    }

    private void redirect(Bundle bundle,BottomNavigationView bottomNav) {
        if (bundle != null) {
            System.out.println(bundle.getString("key"));
            switch (bundle.getString("key")) {
                case "Message":
                    bottomNav.setSelectedItemId(R.id.nav_messages);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MessagesFragment(true,bundle.getString("chatId"),bundle.getString("userId"))).commit();
                    break;
                case "friendRequest":
                    bottomNav.setSelectedItemId(R.id.nav_notifications);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new NotificationsFragment()).commit();
                    break;
                case "inviteRequest":
                    bottomNav.setSelectedItemId(R.id.nav_notifications);
                    String type = "inviteRequest";
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new NotificationsFragment(type)).commit();
                    break;
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_notifications:
                            selectedFragment = new NotificationsFragment();
                            break;
                        case R.id.nav_search:
                            selectedFragment = new SearchFragment();
                            break;
                        case R.id.nav_messages:
                            selectedFragment = new MessagesFragment();
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).addToBackStack(null).commit();
                    return true;
                }
            };
}
