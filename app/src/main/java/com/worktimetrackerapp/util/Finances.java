package com.worktimetrackerapp.util;

/**
 * Created by scoob on 3/12/2018.
 */

public class Finances {

    String title = "0";
    String amtToday = "0";
    String amtThisWeek = "0";
    String amtThisMonth = "0";
    String amtThisQuarter = "0";

    public Finances(String title, String amtToday, String amtThisWeek, String amtThisMonth, String amtThisQuarter) {
        this.title = title;
        this.amtToday = amtToday;
        this.amtThisWeek = amtThisWeek;
        this.amtThisMonth = amtThisMonth;
        this.amtThisQuarter = amtThisQuarter;
    }

    public String getTitle() {
        return title;
    }

    public String getAmtToday() {
        return amtToday;
    }

    public String getAmtThisWeek() {
        return amtThisWeek;
    }

    public String getAmtThisMonth() {
        return amtThisMonth;
    }

    public String getAmtThisQuarter() {
        return amtThisQuarter;
    }
}
