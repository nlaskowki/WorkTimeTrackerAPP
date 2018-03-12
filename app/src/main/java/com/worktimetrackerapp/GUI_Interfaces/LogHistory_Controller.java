package com.worktimetrackerapp.GUI_Interfaces;


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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LogHistory_Controller extends Fragment implements AdapterView.OnItemClickListener{
    View currentView;
    private ListView HistoryList;
    private LogHistoryArrayAdapter lhaa;

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
                        //showPopup(null);
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

        com.couchbase.lite.View viewItemsByDate =
                mydb.getView(String.format("%s/%s", designDocName, byDateViewName));
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
            }, "1.0");
        }

        initItemListAdapter();

        startLiveQuery(viewItemsByDate);
    }

    private void initItemListAdapter() {
        DB app = (DB) getActivity().getApplicationContext();
        lhaa = new LogHistoryArrayAdapter(
                app,
                R.layout.loghistory_row_layout,
                R.id.row_task_name,
                R.id.row_task_info,
                new ArrayList<QueryRow>()
        );
        HistoryList.setAdapter(lhaa);
        HistoryList.setOnItemClickListener(LogHistory_Controller.this);
    }

    private void startLiveQuery(com.couchbase.lite.View view) throws Exception {
        final DB app = (DB) getActivity().getApplication();

        if (liveQuery == null) {
            liveQuery = view.createQuery().toLiveQuery();
            liveQuery.setDescending(true);
            liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {
                    app.runOnUiThread(new Runnable() {
                        public void run() {
                            lhaa.clear();
                            for (Iterator<QueryRow> it = event.getRows(); it.hasNext();) {
                                lhaa.add(it.next());
                            }
                            lhaa.notifyDataSetChanged();
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
            ipp.showInfoPopup(document, getActivity());

        }catch (Exception e){
            System.out.println(e);
        }
    }


}
