package com.worktimetrackerapp.GUI_Interfaces;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.worktimetrackerapp.R;

/**
 * Created by Maximus on 3/13/2018.
 */

public class Task_Setup_Controller extends Fragment {

    View currentView;

    private Button nextButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.activity_user_information, container, false);

        nextButton = (Button) currentView.findViewById(R.id.btn_next);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fragmentManager = getFragmentManager();

                fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeTracking_Controller()).commit();

            }
        });



        return currentView;
    }


}

