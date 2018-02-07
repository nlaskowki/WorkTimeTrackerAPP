package com.worktimetrackerapp.util;

import android.content.Intent;

import com.worktimetrackerapp.WTTApplication;

public class LoginUtil extends WTTApplication{


    private void setCurrentUserId(String userId) {
        this.mCurrentUserId = userId;
    }

    public String getCurrentUserId() {
        return this.mCurrentUserId;
    }


}
