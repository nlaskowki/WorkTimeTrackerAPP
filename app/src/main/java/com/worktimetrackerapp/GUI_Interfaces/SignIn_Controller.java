package com.worktimetrackerapp.GUI_Interfaces;


import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
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
    public static final String INTENT_ACTION_LOGOUT = "logout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_screen);
        final SignInButton btnLoginGoogle = findViewById(R.id.btn_loginGoogle);
        btnLoginGoogle.setSize(SignInButton.SIZE_STANDARD);

        //google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        String action = getIntent().getAction();
        if (INTENT_ACTION_LOGOUT.equals(action)) {
            logout();
        }
        //login Google button action
        btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signInGoogle();
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
            String email = account.getEmail();
            String profile = account.getDisplayName();
            if(idToken != null) {
                DB app = (DB) getApplication();
                app.setUserEmail(email);
                app.setUserProfileName(profile);
                app.loginWithGoogleSignIn(idToken);
            }

        } catch (ApiException e) {
            Log.w(MainActivity.TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    //******************************************************* log out ************************************************

    private void logout() {
        mGoogleSignInClient.signOut();
        clearWebViewCookies();

    }

    private void clearWebViewCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
            cookieManager.flush();
        } else {
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
        }
    }



}


