package com.worktimetrackerapp.GUI_Interfaces;


        import android.app.Activity;
        import android.app.Fragment;
        import android.os.Bundle;
        import android.os.CountDownTimer;
        import android.os.SystemClock;
        import android.support.annotation.Nullable;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.Chronometer;
        import android.widget.CompoundButton;
        import android.widget.TextView;
        import android.widget.ToggleButton;
        import com.worktimetrackerapp.DB;
        import com.worktimetrackerapp.R;
        import java.util.Locale;

public class HomeTracking_Controller extends Fragment {


    //Variables from task settings
    public double hourlyWage = 19.50;
    public double numberOfHoursWorked = 5;

    //Chronometer and toggle buttons variables
    private Chronometer workChronometer;
    private ToggleButton ClockInOutToggleBtn;
    private ToggleButton TakeBreakToggleBtn;

    //Wage calculations variables
    private TextView WageTextView;
    private double count123;
    private double wagePerHour = hourlyWage / 3600;
    Thread t;
    boolean continueThread = true;
    private double totalWageEarned;
    private long holdLastPause;

    //Count down timer variables
    private static long START_TIME_IN_MILLISECONDS = 600000;
    private TextView TxtOutputTimer;
    private CountDownTimer TimeWorkedCountDownTimer;
    private boolean TimerRunning;
    private long TimeLeftInMillisecs = START_TIME_IN_MILLISECONDS;


    View currentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.home_tracking, container, false);

        ClockInOutToggleBtn = (ToggleButton) currentView.findViewById(R.id.clockIn_OutToggleBtn);
        workChronometer = (Chronometer) currentView.findViewById(R.id.workChrono);
        TakeBreakToggleBtn = (ToggleButton) currentView.findViewById((R.id.breakToggleBtn));
        WageTextView = (TextView) currentView.findViewById(R.id.wageEarnedTxtVw);

        TxtOutputTimer = (TextView) currentView.findViewById(R.id.TimeUntilTaskDoneTimertxtvw);


        TakeBreakToggleBtn.setVisibility(View.INVISIBLE);

        ClockInOutToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {



                if (b){

                    resetTimer();
                    continueThread = true;
                    count123 = 0;
                    workChronometer.setBase(SystemClock.elapsedRealtime());
                    workChronometer.start();
                    startTimer();
                    wageTrack();
                    TakeBreakToggleBtn.setVisibility(View.VISIBLE);

                }

                else {

                    workChronometer.stop();
                    continueThread = false;
                    pauseTimer();
                    TakeBreakToggleBtn.setVisibility(View.INVISIBLE);


                    if (TakeBreakToggleBtn.isChecked()){

                        TakeBreakToggleBtn.setChecked(false);

                        workChronometer.stop();
                        continueThread = false;
                        pauseTimer();

                    }

                    //This is were wage info gets sent to DB
                    totalWageEarned = 0;

                }

            }
        });

        TakeBreakToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){

                    holdLastPause = SystemClock.elapsedRealtime();
                    workChronometer.stop();
                    continueThread = false;
                    pauseTimer();


                }

                else {

                    workChronometer.setBase(workChronometer.getBase() + SystemClock.elapsedRealtime() - holdLastPause);
                    workChronometer.start();
                    continueThread = true;
                    wageTrack();
                    startTimer();



                }
            }
        });

        updateCountDownText();

        return currentView;

    }

    public void wageTrack(){



        t = new Thread(){
            @Override
            public void run(){
                while (continueThread){


                    try {
                        Thread.sleep(1000);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (totalWageEarned != 0){

                                    double count123 = totalWageEarned;



                                }

                                count123 += wagePerHour;

                                totalWageEarned += count123;

                                String.valueOf(count123);

                                WageTextView.setText(String.format("$ %.2f", count123));

                            }
                        });

                    }

                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

        };

        t.start();

    }

    private void startTimer(){
        TimeWorkedCountDownTimer = new CountDownTimer(TimeLeftInMillisecs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeLeftInMillisecs = millisUntilFinished;
                updateCountDownText();

            }



            @Override
            public void onFinish() {
                TimerRunning = false;



            }
        }.start();
        TimerRunning = true;


    }

    private void pauseTimer(){

        TimeWorkedCountDownTimer.cancel();
        TimerRunning = false;




    }

    private void  resetTimer(){
        TimeLeftInMillisecs = START_TIME_IN_MILLISECONDS;
        updateCountDownText();



    }

    private void updateCountDownText() {
        int minutes = (int) (TimeLeftInMillisecs / 1000) / 60;
        int seconds = (int) (TimeLeftInMillisecs / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        TxtOutputTimer.setText(timeLeftFormatted);
    }



}
