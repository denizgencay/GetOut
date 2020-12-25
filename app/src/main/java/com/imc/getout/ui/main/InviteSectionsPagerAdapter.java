package com.imc.getout.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.imc.getout.fragments.inviteFragments.EventInviteFragment;
import com.imc.getout.fragments.inviteFragments.InstantInviteFragment;
import com.imc.getout.fragments.inviteFragments.PlannedInviteFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class InviteSectionsPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;

    public InviteSectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
       Fragment fragment = null;
       switch (position){
           case 0:
               fragment = new InstantInviteFragment();
               break;
           case 1:
               fragment = new PlannedInviteFragment();
               break;
           case 2:
               fragment = new EventInviteFragment();
               break;
       }
       return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0 :
                return "Anlık";
            case 1 :
                return "Planlı";
            case 2 :
                return "Etkinlik";
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}