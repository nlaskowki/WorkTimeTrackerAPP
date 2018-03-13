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
import com.worktimetrackerapp.R;
import com.worktimetrackerapp.util.Finances;
import java.util.ArrayList;
import java.util.List;

import static com.worktimetrackerapp.R.layout.finance_list;

public class Finance_Controller extends Fragment {

    View currentView;
    List<Finances> finances = new ArrayList<Finances>();

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.finance, container, false);

        ListView lstFinances = (ListView) currentView.findViewById(R.id.lstFinances);

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

    private void addFinances() {

        String[] jobTitles = new String[10];
        String[] today = new String[10];
        String[] thisWeek = new String[10];
        String[] thisMonth = new String[10];
        String[] thisQuarter = new String[10];

        //Grab data from DB, currently just using hard coded data
        jobTitles[0] = "Total";
        jobTitles[1] = "Job1";
        jobTitles[2] = "Job2";
        //jobTitles[3] = "Job3";
        today[0] = "$ 234.56";
        today[1] = "$ 234.56";
        today[2] = "$ 0";
        //today[3] = "$ 0";
        thisWeek[0] = "$ 1527.23";
        thisWeek[1] = "$ 1000.23";
        thisWeek[2] = "$ 527.00";
        //thisWeek[0] = "$ 1727.23";
        //thisWeek[3] = "$ 200.00";
        thisMonth[0] = "$ 6500.00";
        thisMonth[1] = "$ 3500.00";
        thisMonth[2] = "$ 3000.00";
        //thisMonth[0] = "$ 7500.00";
        //thisMonth[3] = "$ 1000.00";
        thisQuarter[0] = "$ 18000.00";
        thisQuarter[1] = "$ 11000.00";
        thisQuarter[2] = "$ 7000.00";
        //thisQuarter[0] = "$ 21000.00";
        //thisQuarter[3] = "$ 3000.00";



        int i = 0;
        while (jobTitles[i] != null) {
            finances.add(new Finances(jobTitles[i], today[i], thisWeek[i], thisMonth[i], thisQuarter[i]));
            i++;
        }
    }
}

