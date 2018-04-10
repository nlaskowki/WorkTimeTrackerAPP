package com.worktimetrackerapp.gui_controllers;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;
import com.worktimetrackerapp.util.AgendaArrayAdapter;
import com.worktimetrackerapp.util.OnObjectChangeListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

public class HomeNotTracking_Controller extends Fragment {

    private ListView HNTList;
    private AgendaArrayAdapter aaa;

    private Database mydb;
    DB app;
    TextView agendaheader;
    String selectedDay;

    com.couchbase.lite.View viewItemsByDate;
    View currentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.home_not_tracking, container, false);

        Button startTaskButton = currentView.findViewById(R.id.StartTaskHomeNotButton);

        startTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    PopUpWindows ipp = new PopUpWindows();
                    ipp.showInfoPopup(null, getActivity(),true, getFragmentManager());
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        //List
            HNTList = currentView.findViewById(R.id.homenottracking_list_view);
            TextView emptyText = currentView.findViewById(R.id.empty_homenottrackinglist);
            HNTList.setEmptyView(emptyText);
            app = (DB) getActivity().getApplication();
            agendaheader = new TextView(getContext());
            agendaheader.setTextSize(getResources().getDimension(R.dimen.listview_header));

            try {
                Calendar calendar = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
                selectedDay = today.format(calendar.getTime());
                agendaheader.setText(selectedDay);
                HNTList.addHeaderView(agendaheader);
                agendaheader.setFocusable(false);
                startShowList();
                startLiveQuery(selectedDay);
            } catch (Exception e) {
                e.printStackTrace();
            }


        app.getcurrentJob().setOnObjectChangeListener(new OnObjectChangeListener() {
            @Override
            public void OnObjectChanged(Object newobj) {
                if(currentView != null) {
                    try {
                        startLiveQuery(selectedDay);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return currentView;
    }

    protected void startShowList() throws Exception {
        DB app = (DB) getActivity().getApplication();
        mydb = app.getMydb();
        viewItemsByDate = mydb.getView("TaskScheduledStartDate");
        viewItemsByDate.setMap(new Mapper(){
            @Override
            public void map(Map<String, Object> document, Emitter emitter){
                if(document.get("type").equals("Task")) {
                    if(document.get("TaskScheduledStartDate") != null) {
                            if(document.get("TaskEndDateTime") == null) {
                                String date = (String) document.get("TaskScheduledStartDate");
                                emitter.emit(date, null);
                            }
                    }
                }//end if
            }
        },"1");

        initItemListAdapter();
    }

    private void initItemListAdapter() {
        DB app = (DB) getActivity().getApplicationContext();
        aaa = new AgendaArrayAdapter(
                app,
                R.layout.agenda_row_layout,
                R.id.agenda_row_task_name,
                R.id.agenda_row_task_info,
                new ArrayList<QueryRow>()
        );
        HNTList.setAdapter(aaa);

        HNTList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position != 0) {
                    QueryRow row = (QueryRow) adapterView.getItemAtPosition(position);
                    final Document document = row.getDocument();

                    AlertDialog.Builder btnStart = new AlertDialog.Builder(getActivity());

                    btnStart.setMessage("Do you want to start this task?").setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DB app = (DB) getActivity().getApplication();
                                    app.setTaskDoc(document);
                                    FragmentManager FM = getFragmentManager();
                                    FM.beginTransaction().replace(R.id.content_frame, app.getHTFragment()).commit();
                                }
                            })

                            .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = btnStart.create();
                    alert.setTitle("WTT");
                    alert.show();
                }
            }
        }) ;
    }

    private void startLiveQuery(String SelectedDay) {
        final DB app = (DB) getActivity().getApplication();
        final String jobname = app.getcurrentJob().getcurrentJob().toString();
        Query MyQuery = viewItemsByDate.createQuery();
        MyQuery.setDescending(true);
        MyQuery.setStartKey(SelectedDay);
        MyQuery.setEndKey(SelectedDay);
        LiveQuery liveQuery = MyQuery.toLiveQuery();
        liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
            public void changed(final LiveQuery.ChangeEvent event) {
                app.runOnUiThread(new Runnable() {
                    public void run() {
                        aaa.clear();
                        for (Iterator<QueryRow> it = event.getRows(); it.hasNext();) {
                            QueryRow item = it.next();
                            if(app.getMydb().getDocument(item.getDocumentId()).getProperty("jobtitle").equals(jobname)) {
                                aaa.add(item);
                            }
                        }
                        aaa.notifyDataSetChanged();
                    }
                });
            }
        });
        liveQuery.start();
    }

}
