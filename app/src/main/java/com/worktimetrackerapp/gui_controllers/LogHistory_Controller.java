package com.worktimetrackerapp.gui_controllers;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.worktimetrackerapp.util.LogHistoryArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LogHistory_Controller extends Fragment implements AdapterView.OnItemClickListener{
    View currentView;
    private ListView HistoryList;
    private LogHistoryArrayAdapter aaa;

    private Database mydb;
    private LiveQuery liveQuery;
    boolean ended;
    DB app;
    public static final String designDocName = "Task";
    public static final String byDateViewName = "byDate";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.log_history, container, false);
        HistoryList = (ListView) currentView.findViewById(R.id.log_history_list);
        TextView emptyText = (TextView) currentView.findViewById(R.id.empty_historylist);
        HistoryList.setEmptyView(emptyText);
        app = (DB) getActivity().getApplication();

        try {
            startShowList();
        } catch (Exception e) {
            app.showErrorMessage("Error initializing CBLite", e);
        }

        //floating button
            FloatingActionButton fab = currentView.findViewById(R.id.loghistoryfab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        PopUpWindows ipp = new PopUpWindows();
                        ipp.showInfoPopup(null, getActivity(),false, null);
                    }catch (Exception e){
                        System.out.println(e);
                    }
                }
            });

        return currentView;
    }

    protected void startShowList() throws Exception {
        DB app = (DB) getActivity().getApplication();
        mydb = app.getMydb();
        com.couchbase.lite.View viewItemsByDate = mydb.getView(String.format("%s/%s", designDocName, byDateViewName));

        if (viewItemsByDate.getMap() == null) {
            viewItemsByDate.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    Object createdAt = document.get("created_at");
                    if(document.get("type").equals("Task")) {
                                if (createdAt != null) {
                                    emitter.emit(createdAt.toString(), null);
                        }
                    }
                }
            }, "2");
        }

        initItemListAdapter();

        startLiveQuery(viewItemsByDate);
    }

    private void initItemListAdapter() {
        DB app = (DB) getActivity().getApplicationContext();
        aaa = new LogHistoryArrayAdapter(
                app,
                R.layout.loghistory_row_layout,
                R.id.loghistory_row_task_name,
                R.id.loghistory_row_task_jobtitle,
                R.id.loghistory_row_task_info,
                new ArrayList<QueryRow>()
        );
        HistoryList.setAdapter(aaa);
        HistoryList.setOnItemClickListener(LogHistory_Controller.this);
    }

    private void startLiveQuery(com.couchbase.lite.View view) throws Exception {
        final DB app = (DB) getActivity().getApplication();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
        String selectedDay = today.format(calendar.getTime());
        if (liveQuery == null) {
            liveQuery = view.createQuery().toLiveQuery();
            liveQuery.setDescending(true);
            liveQuery.setStartKey(selectedDay);
            liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {
                    app.runOnUiThread(new Runnable() {
                        public void run() {
                            aaa.clear();
                            for (Iterator<QueryRow> it = event.getRows(); it.hasNext();) {
                                aaa.add(it.next());
                            }
                            aaa.notifyDataSetChanged();
                        }
                    });
                }
            });

            liveQuery.start();
        }
    }

    //Handle click on item in list
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        QueryRow row = (QueryRow) adapterView.getItemAtPosition(position);
        Document document = row.getDocument();
        Map<String, Object> newProperties = new HashMap<String, Object>(document.getProperties());

        try {
            PopUpWindows ipp = new PopUpWindows();
            ipp.showInfoPopup(document, getActivity(),false, null);

        }catch (Exception e){
            System.out.println(e);
        }
    }


}
