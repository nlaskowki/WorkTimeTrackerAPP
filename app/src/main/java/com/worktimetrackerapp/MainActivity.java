package com.worktimetrackerapp;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.worktimetrackerapp.GUI_Interfaces.Agenda_Controller;
import com.worktimetrackerapp.GUI_Interfaces.Finance_Controller;
import com.worktimetrackerapp.GUI_Interfaces.HomeNotTracking_Controller;
import com.worktimetrackerapp.GUI_Interfaces.HomeTracking_Controller;
import com.worktimetrackerapp.GUI_Interfaces.LogHistory_Controller;
import com.worktimetrackerapp.GUI_Interfaces.Settings_Controller;

import org.w3c.dom.Document;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "WorkTimeTracker";
    public static String mCurrentUserId;
    DB app;
    HashMap<Integer, String> menujobinterface = new HashMap<>();

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //first frame
            FragmentManager fragmentManager = getFragmentManager();
            if(app.getTracking()){
                fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeTracking_Controller()).commit();
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
        Bundle bundle = new Bundle();


        if (id == R.id.nav_home) {
            if(app.getTracking()){
                fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeTracking_Controller()).commit();
            }else{
                fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeNotTracking_Controller()).commit();
            }
        } else if (id == R.id.nav_finances) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new Finance_Controller()).commit();
        } else if (id == R.id.nav_loghistory) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new LogHistory_Controller()).commit();
        } else if (id == R.id.nav_agenda) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new Agenda_Controller()).commit();
        } else if (id == R.id.nav_settings) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new Settings_Controller()).commit();
            try {

            }catch (Exception e){}
        } else if (id == R.id.nav_LogOut) {
            //log out
            db.logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //get classes



}
