package com.imc.getout.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.imc.getout.fragments.joinFragments.EventJoinFragment;
import com.imc.getout.fragments.joinFragments.InstantJoinFragment;
import com.imc.getout.fragments.joinFragments.PlannedJoinFragment;

public class JoinSectionsStatePagerAdapter extends FragmentStatePagerAdapter {


    public JoinSectionsStatePagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public JoinSectionsStatePagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new InstantJoinFragment();
                break;
            case 1:
                fragment = new PlannedJoinFragment();
                break;
            case 2:
                fragment = new EventJoinFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        CharSequence title = "";
        switch (position) {
            case 0:
                title = "Anlık";
                break;
            case 1:
                title = "Planlı";
                break;
            case 2:
                title = "Etkinlik";
                break;
        }
        return title;
    }
}
