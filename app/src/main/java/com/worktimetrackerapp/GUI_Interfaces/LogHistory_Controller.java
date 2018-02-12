package com.worktimetrackerapp.GUI_Interfaces;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.worktimetrackerapp.R;

public class LogHistory_Controller extends Fragment{
    View currentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.log_history, container, false);
        return currentView;
    }
}
