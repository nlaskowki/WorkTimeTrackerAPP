package com.worktimetrackerapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.worktimetrackerapp.GUI_Interfaces.SignIn_Controller;

public class EntryPoint extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent SignIn = new Intent(getApplicationContext(), SignIn_Controller.class);
        SignIn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SignIn);
    }
}
