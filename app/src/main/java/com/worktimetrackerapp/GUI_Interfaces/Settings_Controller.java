package com.worktimetrackerapp.GUI_Interfaces;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;

public class Settings_Controller extends Fragment {

    View currentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.settings, container, false);

        Button DeleteAllJobs = currentView.findViewById(R.id.settings_deletejobs);

        DeleteAllJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB app = (DB) getActivity().getApplication();
                Object[] jobs = app.getAllJobs();
                for (int i = 0; i < 10; i++) {
                    if (jobs[i] != null) {
                        com.couchbase.lite.Document currentdoc = app.getMydb().getDocument((String) jobs[i]);
                        try {
                            currentdoc.delete();
                        }catch (Exception e){
                            System.out.println(e);
                        }
                    }
                }
                //app.logout();
            }
        });


        return currentView;
    }

}

