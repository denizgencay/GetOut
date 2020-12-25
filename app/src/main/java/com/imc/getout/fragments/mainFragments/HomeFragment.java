package com.imc.getout.fragments.mainFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.imc.getout.InviteActivity;
import com.imc.getout.JoinActivity;
import com.imc.getout.R;

public class HomeFragment extends Fragment {

    Button join;
    Button invite;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home,container,false);
        join = view.findViewById(R.id.join);
        invite = view.findViewById(R.id.invite);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent intent = new Intent(getContext(), JoinActivity.class);
              startActivity(intent);
            }
        });

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), InviteActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }


}
