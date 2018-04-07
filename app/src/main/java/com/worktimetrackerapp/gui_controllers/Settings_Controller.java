package com.worktimetrackerapp.gui_controllers;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryRow;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;
import com.worktimetrackerapp.util.JobArrayAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


public class Settings_Controller extends Fragment implements AdapterView.OnItemClickListener{
    View currentView;
    private ListView joblist;
    private JobArrayAdapter jaa;
    private Database mydb;
    private LiveQuery liveQuery;
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
                   JobSettingsPopUp ipp = new JobSettingsPopUp();
                   ipp.showJobInfoPopup(null, getActivity());

                }catch (Exception e){
                    System.out.println(e);
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


        if (liveQuery == null) {
            liveQuery = JobView.createQuery().toLiveQuery();
            liveQuery.setDescending(true);
            liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {
                    app.runOnUiThread(new Runnable() {
                        public void run() {
                            jaa.clear();
                            for (Iterator<QueryRow> it = event.getRows(); it.hasNext(); ) {
                                jaa.add(it.next());
                            }
                            jaa.notifyDataSetChanged();
                        }
                    });
                }
            });

            liveQuery.start();
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        QueryRow row = (QueryRow) adapterView.getItemAtPosition(position);
        Document document = row.getDocument();
        try {
            JobSettingsPopUp ipp = new JobSettingsPopUp();
            ipp.showJobInfoPopup(document, getActivity());

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

