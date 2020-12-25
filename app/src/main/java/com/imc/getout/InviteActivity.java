package com.imc.getout;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.imc.getout.ui.main.InviteSectionsPagerAdapter;

public class InviteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        InviteSectionsPagerAdapter inviteSectionsPagerAdapter = new InviteSectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.invite_view_pager);
        viewPager.setAdapter(inviteSectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.inviteTabs);
        tabs.setupWithViewPager(viewPager);

    }
}