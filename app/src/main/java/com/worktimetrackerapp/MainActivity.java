package com.worktimetrackerapp;

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

import com.worktimetrackerapp.GUI_Interfaces.Agenda_Controller;
import com.worktimetrackerapp.GUI_Interfaces.Finance_Controller;
import com.worktimetrackerapp.GUI_Interfaces.HomeTracking_Controller;
import com.worktimetrackerapp.GUI_Interfaces.LogHistory_Controller;
import com.worktimetrackerapp.GUI_Interfaces.Settings_Controller;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "WorkTimeTracker";
    public static String mCurrentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wttapplication);
        System.out.println("Try");
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
            fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeTracking_Controller()).commit();

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
        if (id == R.id.action_Job1) {
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
            //implement picking home_tracking or home_not_tracking

            fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeTracking_Controller()).commit();
        } else if (id == R.id.nav_finances) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new Finance_Controller()).commit();
        } else if (id == R.id.nav_loghistory) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new LogHistory_Controller()).commit();
        } else if (id == R.id.nav_agenda) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new Agenda_Controller()).commit();
        } else if (id == R.id.nav_settings) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new Settings_Controller()).commit();
            try {
                //db.AddJob("SE", "Student", "OU", 0.0, 0.0);
                //db.AddJob("SE", "Student", "OU", 0.0, 0.0);
                //db.StartTask("Test", "" ,0.0, "ou", 0.0, "");
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
