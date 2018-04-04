package com.worktimetrackerapp.GUI_Interfaces;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.RelativeDateTimeFormatter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;
import com.worktimetrackerapp.util.JobArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class Settings_Controller extends Fragment implements AdapterView.OnItemClickListener{
    View currentView;
    private ListView joblist;
    private JobArrayAdapter jaa;
    private Database mydb;
    DB app;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.settings, container, false);
        joblist = currentView.findViewById(R.id.settings_job_list);
        TextView emptyText = currentView.findViewById(R.id.no_jobs_available);
        joblist.setEmptyView(emptyText);
        app = (DB) getActivity().getApplication();

        try {
            ShowJobList();
        } catch (Exception e) {
            app.showErrorMessage("Error initializing CBLite (for Settings Job List)", e);
        }

        FloatingActionButton fab = currentView.findViewById(R.id.settings_add_jobs);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                   // JobSettingsPopUp ipp = new JobSettingsPopUp();
                   // ipp.showInfoPopup(null, getActivity(),false, null);

                }catch (Exception e){
                    System.out.println(e);
                }
            }
        });

        final CheckBox darktheme = (CheckBox) currentView.findViewById( R.id.darkthemeChk );
        darktheme.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View currentView)
            {
                if ( darktheme.isChecked() )
                {
                    app.setTheme(R.style.Theme_Dark);
                }
                else{
                    app.setTheme(R.style.Theme_Light);
                }
            }
        });


        return currentView;
    }

    protected void ShowJobList() throws Exception {
        //View JobList;
         app = (DB) getActivity().getApplication();
         mydb = app.getMydb();
        com.couchbase.lite.View JobView;

        initItemListAdapter();
        //JobList = mydb.getView("JobView");
        JobView = mydb.getView("settingjobs");
        JobView.setMap(new Mapper(){
            @Override
            public void map(Map<String, Object> document, Emitter emitter){
                if(document.get("type").equals("UserInfo")) {
                    if(document.get("jobtitle") != null) {
                        String date = (String) document.get("jobtitle");
                        emitter.emit(date.toString(), null);
                    }
                }//end if
            }
        },"1");

        Query MyQuery = JobView.createQuery();
        MyQuery.setDescending(true);
        QueryEnumerator result = MyQuery.run();
        int i = 0;
        for(Iterator<QueryRow> it = result; it.hasNext();) {
            jaa.add(it.next());
            i++;
        }

    }

    private void initItemListAdapter() {
        DB app = (DB) getActivity().getApplicationContext();
        jaa = new JobArrayAdapter(
                app,
                R.layout.agenda_row_layout,
                R.id.agenda_row_task_name,
                R.id.agenda_row_task_info,
                new ArrayList<QueryRow>()
        );
        joblist.setAdapter(jaa);
        joblist.setOnItemClickListener(Settings_Controller.this);

    }


       /* Button DeleteAllJobs = currentView.findViewById(R.id.settings_deletejobs);

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
        });*/




    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        QueryRow row = (QueryRow) adapterView.getItemAtPosition(position);
        Document document = row.getDocument();
        Map<String, Object> newProperties = new HashMap<String, Object>(document.getProperties());

        try {
            JobSettingsPopUp ipp = new JobSettingsPopUp();
           // ipp.showInfoPopup(document, getActivity(),false, null);

        }catch (Exception e){
            System.out.println(e);
        }
    }
}

