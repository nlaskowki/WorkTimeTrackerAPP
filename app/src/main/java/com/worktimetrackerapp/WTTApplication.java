package com.worktimetrackerapp;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.worktimetrackerapp.R;
import com.worktimetrackerapp.GUI_Interfaces.Agenda_Controller;
import com.worktimetrackerapp.GUI_Interfaces.Finance_Controller;
import com.worktimetrackerapp.GUI_Interfaces.HomeTracking_Controller;
import com.worktimetrackerapp.GUI_Interfaces.LogHistory_Controller;
import com.worktimetrackerapp.GUI_Interfaces.Settings_Controller;
import com.worktimetrackerapp.util.*;

public class WTTApplication extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "WorkTimeTracker";
    public String mCurrentUserId;
    Manager dbManager =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wttapplication);

        //first run login/singup screens**************************

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        getMenuInflater().inflate(R.menu.wttapplication, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_User1) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        FragmentManager fragmentManager = getFragmentManager();

        if (id == R.id.nav_home) {
            //implement picking hometracking or homenottracking

            fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeTracking_Controller()).commit();
        } else if (id == R.id.nav_finances) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new Finance_Controller()).commit();
        } else if (id == R.id.nav_loghistory) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new LogHistory_Controller()).commit();
        } else if (id == R.id.nav_agenda) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new Agenda_Controller()).commit();
        } else if (id == R.id.nav_settings) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new Settings_Controller()).commit();
        } else if (id == R.id.nav_LogOut) {
            //log out
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //display error messages from db
    public void showErrorMessage(final String errorMessage, final Throwable throwable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.util.Log.e(TAG, errorMessage, throwable);
                String msg = String.format("%s: %s",
                        errorMessage, throwable != null ? throwable : "");
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
