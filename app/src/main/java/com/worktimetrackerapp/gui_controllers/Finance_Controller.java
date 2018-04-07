package com.worktimetrackerapp.gui_controllers;


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

    private String amtToday = "0";
    private String amtThisWeek = "0";
    private String amtThisMonth = "0";
    private String amtThisQuarter = "0";
    private String otToday = "0";
    private String otThisWeek = "0";
    private String otThisMonth = "0";
    private String otThisQuarter = "0";
    private String totalToday = "0";
    private String totalThisWeek = "0";
    private String totalThisMonth = "0";
    private String totalThisQuarter = "0";

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
            TextView otToday = view.findViewById(R.id.overTimeToday);
            otToday.setText(currentFinance.getOtToday());
            TextView otThisWeek = view.findViewById(R.id.overTimeThisWeek);
            otThisWeek.setText(currentFinance.getOtThisWeek());
            TextView otThisMonth = view.findViewById(R.id.overTimeThisMonth);
            otThisMonth.setText(currentFinance.getOtThisMonth());
            TextView otThisQuarter = view.findViewById(R.id.overTimeThisQuarter);
            otThisQuarter.setText(currentFinance.getOtThisQuarter());
            TextView totalToday = view.findViewById(R.id.totalToday);
            totalToday.setText(currentFinance.getTotalToday());
            TextView totalThisWeek = view.findViewById(R.id.totalThisWeek);
            totalThisWeek.setText(currentFinance.getTotalThisWeek());
            TextView totalThisMonth = view.findViewById(R.id.totalThisMonth);
            totalThisMonth.setText(currentFinance.getTotalThisMonth());
            TextView totalThisQuarter = view.findViewById(R.id.totalThisQuarter);
            totalThisQuarter.setText(currentFinance.getTotalThisQuarter());



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
        cal.set(Calendar.MONTH, getQuarter(cal));
        String firstDayQ = formatter.format(cal.getTime());

        setJobDataAll(currentDay, firstDayW, firstDayM, firstDayQ);
        finances.add(new Finances("Total", amtToday, amtThisWeek, amtThisMonth, amtThisQuarter, otToday,
                otThisWeek, otThisMonth, otThisQuarter, totalToday, totalThisWeek, totalThisMonth, totalThisQuarter));

        for (int i = 0; i < 10; i++) {
            if (jobs[i] != null) {
                com.couchbase.lite.Document currentdoc = app.getMydb().getDocument((String) jobs[i]);
                jobTitles[i] = currentdoc.getProperty("jobtitle").toString();
                System.out.println(i);
            }
        }

        int j = 0;
        while (jobTitles[j] != null) {
            setJobData(currentDay, firstDayW, firstDayM, firstDayQ, jobTitles[j]);
            finances.add(new Finances(jobTitles[j], amtToday, amtThisWeek, amtThisMonth, amtThisQuarter, otToday,
                    otThisWeek, otThisMonth, otThisQuarter, totalToday, totalThisWeek, totalThisMonth, totalThisQuarter));
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

    public int getQuarter(Calendar cal) {
        Calendar Q1 = Calendar.getInstance();
        Q1.set(Calendar.MONTH, 3);
        Calendar Q2 = Calendar.getInstance();
        Q1.set(Calendar.MONTH, 6);
        Calendar Q3 = Calendar.getInstance();
        Q1.set(Calendar.MONTH, 9);

        if(cal.before(Q1)) {
            return 0;
        }
        else if (cal.before(Q2)) {
            return 3;
        }
        else if (cal.before(Q3)) {
            return 6;
        }
        else {
            return 9;
        }
    }

    private void setJobDataAll(String today, String firstDayW, String firstDayM, String firstDayQ) {
        totalAllJobs(today, today, 0);
        totalAllJobs(firstDayW, today, 1);
        totalAllJobs(firstDayM, today, 2);
        totalAllJobs(firstDayQ, today, 3);
    }

    private void totalAllJobs(String firstDay, String LastDay, int type){
        Double total = 0.0;
        Double overTime = 0.0;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Long date1 = (long) 0;
        Long date2 = (long) 0;
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
        for(Iterator<QueryRow> it = result; it.hasNext();) {
            QueryRow itnow = it.next();
            com.couchbase.lite.Document currentdoc = mydb.getDocument(itnow.getDocumentId());
            if(currentdoc.getProperty("TaskEarnings") != null) {
                total = total + Double.parseDouble(currentdoc.getProperty("TaskEarnings").toString());
                if(currentdoc.getProperty("TaskStartOvertimeDateTime") != null) {
                    try {
                        date1 = formatter.parse(currentdoc.getProperty("TaskStartOvertimeDateTime").toString()).getTime();
                        date2 = formatter.parse(currentdoc.getProperty("TaskEndDateTime").toString()).getTime();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    overTime = (date2 - date1) * Double.parseDouble(currentdoc.getProperty("taskwageovertime").toString());
                }
            }
        }
        if (type == 0) {
            totalToday = total + "";
            otToday = overTime + "";
            amtToday = (total - overTime) + "";
        }
        else if (type == 1) {
            totalThisWeek = total + "";
            otThisWeek = overTime + "";
            amtThisWeek = (total - overTime) + "";
        }
        else if (type == 2) {
            totalThisMonth = total + "";
            otThisMonth = overTime + "";
            amtThisMonth = (total - overTime) + "";
        }
        else {
            totalThisQuarter = total + "";
            otThisQuarter = overTime + "";
            amtThisQuarter = (total - overTime) + "";
        }
    }

    private void setJobData(String today, String firstDayW, String firstDayM, String firstDayQ, String jobTitle) {
        totalThisJob(today, today, jobTitle, 0);
        totalThisJob(firstDayW, today, jobTitle, 1);
        totalThisJob(firstDayM, today, jobTitle, 2);
        totalThisJob(firstDayQ, today, jobTitle, 3);
    }
    private void totalThisJob(String firstDay, String LastDay, String jobTitle, int type){
        Double total = 0.0;
        Double overTime = 0.0;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Long date1 = (long) 0;
        Long date2 = (long) 0;
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
        for(Iterator<QueryRow> it = result; it.hasNext();) {
            QueryRow itnow = it.next();
            com.couchbase.lite.Document currentdoc = mydb.getDocument(itnow.getDocumentId());
            if(currentdoc.getProperty("TaskEarnings") != null) {
                if(currentdoc.getProperty("jobtitle").equals(jobTitle)) {
                    total = total + Double.parseDouble(currentdoc.getProperty("TaskEarnings").toString());
                    if(currentdoc.getProperty("TaskStartOvertimeDateTime") != null) {
                        try {
                            date1 = formatter.parse(currentdoc.getProperty("TaskStartOvertimeDateTime").toString()).getTime();
                            date2 = formatter.parse(currentdoc.getProperty("TaskEndDateTime").toString()).getTime();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        overTime = (date2 - date1) * Double.parseDouble(currentdoc.getProperty("taskwageovertime").toString());
                    }
                }
            }
        }
        if (type == 0) {
            totalToday = total + "";
            otToday = overTime + "";
            amtToday = (total - overTime) + "";
        }
        else if (type == 1) {
            totalThisWeek = total + "";
            otThisWeek = overTime + "";
            amtThisWeek = (total - overTime) + "";
        }
        else if (type == 2) {
            totalThisMonth = total + "";
            otThisMonth = overTime + "";
            amtThisMonth = (total - overTime) + "";
        }
        else {
            totalThisQuarter = total + "";
            otThisQuarter = overTime + "";
            amtThisQuarter = (total - overTime) + "";
        }
    }


}
