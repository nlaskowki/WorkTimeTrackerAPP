package com.worktimetrackerapp.util;

/**
 * Created by scoob on 3/12/2018.
 */

public class Finances {

    private String title;
    private String amtToday;
    private String amtThisWeek;
    private String amtThisMonth;
    private String amtThisQuarter;
    private String otToday;
    private String otThisWeek;
    private String otThisMonth;
    private String otThisQuarter;
    private String totalToday;
    private String totalThisWeek;
    private String totalThisMonth;
    private String totalThisQuarter;

    public Finances(String title, String amtToday, String amtThisWeek, String amtThisMonth, String amtThisQuarter,
                    String otToday, String otThisWeek, String otThisMonth, String otThisQuarter, String totalToday,
                    String totalThisWeek, String totalThisMonth, String totalThisQuarter) {
        this.title = title;
        this.amtToday = amtToday;
        this.amtThisWeek = amtThisWeek;
        this.amtThisMonth = amtThisMonth;
        this.amtThisQuarter = amtThisQuarter;
        this.otToday = otToday;
        this.otThisWeek = otThisWeek;
        this.otThisMonth = otThisMonth;
        this.otThisQuarter = otThisQuarter;
        this.totalToday = totalToday;
        this.totalThisWeek = totalThisWeek;
        this.totalThisMonth = totalThisMonth;
        this.totalThisQuarter = totalThisQuarter;
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

    public String getOtToday() {
        return otToday;
    }

    public String getOtThisWeek() {
        return otThisWeek;
    }

    public String getOtThisMonth() {
        return otThisMonth;
    }

    public String getOtThisQuarter() {
        return otThisQuarter;
    }

    public String getTotalToday() {
        return totalToday;
    }

    public String getTotalThisWeek() { return totalThisWeek; }

    public String getTotalThisMonth() {
        return totalThisMonth;
    }

    public String getTotalThisQuarter() {
        return totalThisQuarter;
    }
}
