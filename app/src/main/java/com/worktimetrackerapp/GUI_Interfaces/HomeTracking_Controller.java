package com.worktimetrackerapp.GUI_Interfaces;


        import android.app.Activity;
        import android.app.Fragment;
        import android.os.Bundle;
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


        import com.worktimetrackerapp.R;

public class HomeTracking_Controller extends Fragment {

    private Button TestButton;
    private Chronometer workChronometer;
    private ToggleButton ClockInOutToggleBtn;
    private ToggleButton TakeBreakToggleBtn;
    private TextView WageTextView;
    private double count123;
    private double wagePerHour = .50;
    Thread t;
    boolean continueThread = true;
    private double totalWageEarned;

    private long holdLastPause;


    View currentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.home_tracking, container, false);

        //TestButton = (Button) currentView.findViewById(R.id.jobDoneBtn);
        ClockInOutToggleBtn = (ToggleButton) currentView.findViewById(R.id.clockIn_OutToggleBtn);
        workChronometer = (Chronometer) currentView.findViewById(R.id.workChrono);
        TakeBreakToggleBtn = (ToggleButton) currentView.findViewById((R.id.breakToggleBtn));
        WageTextView = (TextView) currentView.findViewById(R.id.wageEarnedTxtVw);


        ClockInOutToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){

                    continueThread = true;
                    count123 = 0;
                    workChronometer.setBase(SystemClock.elapsedRealtime());
                    workChronometer.start();
                    wageTrack();



                }

                else {

                    workChronometer.stop();
                    continueThread = false;

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


                }

                else {

                    workChronometer.setBase(workChronometer.getBase() + SystemClock.elapsedRealtime() - holdLastPause);
                    workChronometer.start();
                    continueThread = true;
                    wageTrack();



                }
            }
        });

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

                                WageTextView.setText("$ " + String.valueOf(count123));

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



}
