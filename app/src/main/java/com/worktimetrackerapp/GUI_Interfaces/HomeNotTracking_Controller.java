package com.worktimetrackerapp.GUI_Interfaces;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.worktimetrackerapp.R;

public class HomeNotTracking_Controller extends Fragment {

    private Button StartTaskButton;

    View currentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.home_not_tracking, container, false);

        StartTaskButton = (Button) currentView.findViewById(R.id.StartTaskButton);

        StartTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



            }
        });























































        return currentView;
    }

}
