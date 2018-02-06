package com.worktimetrackerapp.util;


import com.worktimetrackerapp.WTTApplication;

public class LoginUtil extends WTTApplication{


    private void setCurrentUserId(String userId) {
        this.mCurrentUserId = userId;
    }

    public String getCurrentUserId() {
        return this.mCurrentUserId;
    }


}
