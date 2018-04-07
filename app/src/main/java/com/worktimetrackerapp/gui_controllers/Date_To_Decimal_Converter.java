package com.worktimetrackerapp.gui_controllers;

import java.util.Date;

/**
 * Created by Maximus on 3/25/2018.
 */

public class Date_To_Decimal_Converter {


    public long getDateTime(Date start, Date end){

        System.out.println("Start date: " + start);
        System.out.println("Start date: " + end);

        long diffInMillies = end.getTime() - start.getTime();



        return diffInMillies;


    }


}
