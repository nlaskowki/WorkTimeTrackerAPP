package com.worktimetrackerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


//this is our main class

public class WTTApplication  extends AppCompatActivity {

    protected String TokenID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sidebar_menu);

    }
}