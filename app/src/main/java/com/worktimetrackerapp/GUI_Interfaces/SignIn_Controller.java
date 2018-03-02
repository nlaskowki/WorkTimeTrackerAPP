package com.worktimetrackerapp.GUI_Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.couchbase.lite.android.AndroidContext;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.MainActivity;
import com.worktimetrackerapp.R;

public class SignIn_Controller extends AppCompatActivity  {

    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private AndroidContext context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_screen);
        final Button btnLogin = findViewById(R.id.btn_login);
        final Button btnLoginGoogle = findViewById(R.id.btn_loginGoogle);
        final Button btnSignUp = findViewById(R.id.btn_Login_signUp);


        //google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //login Google button action
        btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signInGoogle();
            }
        });

        //login button action
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent LaunchHome = new Intent(getApplicationContext(), MainActivity.class);
                LaunchHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(LaunchHome);
            }
        });

        //SignUp Button Action
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent GoToSignUp = new Intent(getApplicationContext(), SignUp_Controller.class);
                GoToSignUp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(GoToSignUp);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //*************************************************** Google Sign in *******************************
    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            if(idToken != null) {
                DB app = (DB) getApplication();
                app.loginWithGoogleSignIn(idToken);
            }

        } catch (ApiException e) {
            Log.w(MainActivity.TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }
}


