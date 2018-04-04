package com.worktimetrackerapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.worktimetrackerapp.GUI_Interfaces.Agenda_Controller;
import com.worktimetrackerapp.GUI_Interfaces.Finance_Controller;
import com.worktimetrackerapp.GUI_Interfaces.HomeNotTracking_Controller;
import com.worktimetrackerapp.GUI_Interfaces.HomeTracking_Controller;
import com.worktimetrackerapp.GUI_Interfaces.LogHistory_Controller;
import com.worktimetrackerapp.GUI_Interfaces.Settings_Controller;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "WorkTimeTracker";
    public static String mCurrentUserId;
    DB app;
    HashMap<Integer, String> menujobinterface = new HashMap<>();
    static Fragment HomeTrackingFragment;
    static Fragment CurrentFragment = null;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wttapplication);
        app = (DB) getApplication();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView username = headerView.findViewById(R.id.nav_header_username);
        TextView useremail = headerView.findViewById(R.id.nav_header_useremail);
        username.setText(app.getUserProfileName());
        useremail.setText(app.getUserEmail());
        //first frame
            FragmentManager fragmentManager = getFragmentManager();
            HomeTrackingFragment = new HomeTracking_Controller();
            if(app.getTracking()){
                fragmentManager.beginTransaction().replace(R.id.content_frame, HomeTrackingFragment).commit();
            }else{
                fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeNotTracking_Controller()).commit();
            }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Object[] jobs = app.getAllJobs();
        if(jobs[0] != null) {
            for (int i = 0; i < 10; i++) {
                if (jobs[i] != null) {
                    System.out.println(jobs[i]);
                    menujobinterface.put(i, jobs[i].toString());
                    //get job from db
                    com.couchbase.lite.Document currentdoc = app.getMydb().getDocument((String) jobs[i]);
                    menu.add(R.id.menu_jobgroup, i, i + 100, currentdoc.getProperty("jobtitle").toString());
                    System.out.println(i);
                }
            }
            menu.setGroupCheckable(R.id.menu_jobgroup, true, true);
            menu.getItem(0).setChecked(true);
            //set title bar
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            String doc = menujobinterface.get(0);
            String jobname = app.getMydb().getDocument(doc).getProperty("jobtitle").toString();
            toolbar.setTitle(jobname);
            app.setCurrentJob(doc);
            getMenuInflater().inflate(R.menu.wttapplication, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String doc = "";
        int id = item.getItemId();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String jobname = "";

        switch(id) {
            case 0:
                doc = menujobinterface.get(0);
                item.setChecked(true);
                app.setCurrentJob(doc);
                jobname = app.getMydb().getDocument(doc).getProperty("jobtitle").toString();
                toolbar.setTitle(jobname);
                return true;
            case 1:
                doc = menujobinterface.get(1);
                item.setChecked(true);
                app.setCurrentJob(doc);
                jobname = app.getMydb().getDocument(doc).getProperty("jobtitle").toString();
                toolbar.setTitle(jobname);
                return true;
            case 2:
                doc = menujobinterface.get(2);
                item.setChecked(true);
                app.setCurrentJob(doc);
                jobname = app.getMydb().getDocument(doc).getProperty("jobtitle").toString();
                toolbar.setTitle(jobname);
                return true;
            case 3:
                doc = menujobinterface.get(3);
                item.setChecked(true);
                app.setCurrentJob(doc);
                jobname = app.getMydb().getDocument(doc).getProperty("jobtitle").toString();
                toolbar.setTitle(jobname);
                return true;
            case 4:
                doc = menujobinterface.get(4);
                item.setChecked(true);
                app.setCurrentJob(doc);
                jobname = app.getMydb().getDocument(doc).getProperty("jobtitle").toString();
                toolbar.setTitle(jobname);
                return true;
        }

       return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        FragmentManager fragmentManager = getFragmentManager();
        DB db = (DB) getApplication();

        if (id == R.id.nav_home) {
            System.out.println(app.getTracking());

            if(app.getTracking()){
                if(CurrentFragment != null) {
                    fragmentManager.beginTransaction()
                            .show(HomeTrackingFragment)
                            .detach(CurrentFragment)
                            .commit();
                    CurrentFragment = null;
                }else{
                    fragmentManager.beginTransaction()
                            .show(HomeTrackingFragment)
                            .commit();
                }
            }else{
                fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeNotTracking_Controller()).commit();
            }
        } else if (id == R.id.nav_finances) {
            ScreenPicker(new Finance_Controller());

        } else if (id == R.id.nav_loghistory) {
            ScreenPicker(new LogHistory_Controller());

        } else if (id == R.id.nav_agenda) {
            ScreenPicker(new Agenda_Controller());

        } else if (id == R.id.nav_settings) {
            ScreenPicker(new Settings_Controller());

        } else if (id == R.id.nav_LogOut) {
            db.logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void ScreenPicker(Fragment NewFrag){
        FragmentManager fragmentManager = getFragmentManager();

            if (app.getTracking()) {
                if(CurrentFragment == null) {
                    fragmentManager.beginTransaction()
                            .add(R.id.content_frame, NewFrag)
                            .hide(HomeTrackingFragment)
                            .commit();
                    CurrentFragment = NewFrag;
                }else{
                    if(CurrentFragment != NewFrag){
                        fragmentManager.beginTransaction()
                                .detach(CurrentFragment)
                                .add(R.id.content_frame, NewFrag)
                                .hide(HomeTrackingFragment)
                                .commit();
                        CurrentFragment = NewFrag;
                    }
                }
            } else {
                    fragmentManager.beginTransaction().replace(R.id.content_frame, NewFrag).commit();
                    CurrentFragment = null;
            }
    }



    public static Fragment GetHTFragment(){
        return HomeTrackingFragment;
    }
    public static void setHTFragment(Fragment HT){
        HomeTrackingFragment = HT;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("Pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("resume");
        // resume tasks
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}
