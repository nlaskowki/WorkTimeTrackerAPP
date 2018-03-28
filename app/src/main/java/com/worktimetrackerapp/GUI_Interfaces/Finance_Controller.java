package com.worktimetrackerapp.GUI_Interfaces;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;
import com.worktimetrackerapp.util.Finances;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.worktimetrackerapp.R.layout.finance_list;

public class Finance_Controller extends Fragment {

    View currentView;
    List<Finances> finances = new ArrayList<Finances>();
    Database mydb;
    DB app;
    com.couchbase.lite.View financeView;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.finance, container, false);

        ListView lstFinances = currentView.findViewById(R.id.lstFinances);

        FinanceListAdapter adapter = new FinanceListAdapter();
        lstFinances.setAdapter(adapter);

        addFinances();

        return currentView;
    }

    private class FinanceListAdapter extends ArrayAdapter<Finances> {

        public FinanceListAdapter() {
            super(getActivity(), finance_list, finances);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(finance_list, parent, false);
            }

            Finances currentFinance = finances.get(position);

            TextView title = view.findViewById(R.id.titleOfJobs);
            title.setText(currentFinance.getTitle());
            TextView today = view.findViewById(R.id.amtToday);
            today.setText(currentFinance.getAmtToday());
            TextView thisWeek = view.findViewById(R.id.amtThisWeek);
            thisWeek.setText(currentFinance.getAmtThisWeek());
            TextView thisMonth = view.findViewById(R.id.amtThisMonth);
            thisMonth.setText(currentFinance.getAmtThisMonth());
            TextView thisQuarter = view.findViewById(R.id.amtThisQuarter);
            thisQuarter.setText(currentFinance.getAmtThisQuarter());



            return view;
        }
    }

    //Determine the finance object details and add them to the list adapter
    private void addFinances() {

        try { // Initialize the database variables
            GetFinances();
        }catch (Exception e){
            System.out.println(e);
        }

        //Initialize storage arrays for the values of the finance objects
        Object[] jobs = app.getAllJobs();
        String[] jobTitles = new String[10];

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        //get current month first and last day
        Calendar cal = Calendar.getInstance();
        String currentDay = formatter.format(cal.getTime());
        cal.set(Calendar.DAY_OF_WEEK, 1);
        String firstDayW = formatter.format(cal.getTime());
        cal.set(Calendar.DATE, 1);
        String firstDayM = formatter.format(cal.getTime());

        String firstDayQ = formatter.format(cal.getTime());

        finances.add(new Finances("Total", totalAllJobs(currentDay, currentDay) + "", totalAllJobs(firstDayW, currentDay) + "",
                totalAllJobs(firstDayM, currentDay) + "", totalAllJobs(firstDayQ, currentDay) + ""));

        int i = 0;
        while (jobs[i] != null) {
            jobTitles[i] = jobs[i].toString();
            i++;
        }

        int j = 0;
        while (jobTitles[j] != null) {
            finances.add(new Finances(jobTitles[j], totalThisJob(currentDay, currentDay, jobTitles[j]) + "",
                    totalThisJob(firstDayW, currentDay, jobTitles[j]) + "", totalThisJob(firstDayM, currentDay, jobTitles[j]) + "",
                    totalThisJob(firstDayQ, currentDay, jobTitles[j]) + ""));
            j++;
        }
    }

    protected void GetFinances() throws Exception {
        app = (DB) getActivity().getApplication();
        mydb = app.getMydb();

        financeView = mydb.getView("FinanceView");
        financeView.setMap(new Mapper(){
            @Override
            public void map(Map<String, Object> document, Emitter emitter){
                if(document.get("type").equals("Task")) {
                    if(document.get("TaskScheduledStartDate") != null) {
                        String date = (String) document.get("TaskScheduledStartDate");
                        emitter.emit(date.toString(), null);
                    }
                }//end if
            }
        },"1");

    }

    private Double totalAllJobs(String firstDay, String LastDay){
        Double total = 0.0;
        QueryEnumerator result = null;
        Query query = financeView.createQuery();
        query.setDescending(true);
        query.setStartKey(LastDay);//last day you want
        query.setEndKey(firstDay); //first day you want
        try {
            result = query.run();
        }catch (Exception e){
            System.out.println(e);
        }
        int i = 0;
        for(Iterator<QueryRow> it = result; it.hasNext();) {
            QueryRow itnow = it.next();
            com.couchbase.lite.Document currentdoc = mydb.getDocument(itnow.getDocumentId());
            if(currentdoc.getProperty("TaskEarnings") != null) {
                total = total + Double.parseDouble(currentdoc.getProperty("TaskEarnings").toString());
            }
        }
        return total;
    }

    private Double totalThisJob(String firstDay, String LastDay, String jobTitle){
        Double total = 0.0;
        QueryEnumerator result = null;
        Query query = financeView.createQuery();
        query.setDescending(true);
        query.setStartKey(LastDay);//last day you want
        query.setEndKey(firstDay); //first day you want
        try {
            result = query.run();
        }catch (Exception e){
            System.out.println(e);
        }
        int i = 0;
        for(Iterator<QueryRow> it = result; it.hasNext();) {
            QueryRow itnow = it.next();
            com.couchbase.lite.Document currentdoc = mydb.getDocument(itnow.getDocumentId());
            if(currentdoc.getProperty("TaskEarnings") != null) {
                if(currentdoc.getProperty("JobTitle") == jobTitle) {
                    total = total + Double.parseDouble(currentdoc.getProperty("TaskEarnings").toString());
                }
            }
        }
        return total;
    }


}
