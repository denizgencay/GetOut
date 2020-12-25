package com.imc.getout;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.imc.getout.fragments.joinFragments.InstantJoinFragment;
import com.imc.getout.ui.main.JoinSectionsPagerAdapter;
import com.imc.getout.ui.main.JoinSectionsStatePagerAdapter;

public class JoinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        JoinSectionsPagerAdapter joinSectionsPagerAdapter = new JoinSectionsPagerAdapter(this,getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.join_view_pager);
        viewPager.setAdapter(joinSectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        TabLayout tabs = findViewById(R.id.joinTabs);
        tabs.setupWithViewPager(viewPager);
    }
}