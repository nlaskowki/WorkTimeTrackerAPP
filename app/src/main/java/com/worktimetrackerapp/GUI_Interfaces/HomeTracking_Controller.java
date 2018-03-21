package com.worktimetrackerapp.GUI_Interfaces;


        import android.app.Activity;
        import android.app.AlertDialog;
        import android.app.Fragment;
        import android.content.DialogInterface;
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
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.ToggleButton;

        import com.couchbase.lite.Document;
        import com.worktimetrackerapp.DB;
        import com.worktimetrackerapp.R;

        import java.text.SimpleDateFormat;
        import java.time.Period;
        import java.util.Date;
        import java.util.Locale;

public class HomeTracking_Controller extends Fragment {


    //Variables from task settings
    public double hourlyWage = 19.50;
    public double numberOfHoursWorked = .002;
    public String taskName = "Test Job";

    //Chronometer and toggle buttons variables
    private Chronometer workChronometer;
    private ToggleButton ClockInOutToggleBtn;
    private ToggleButton TakeBreakToggleBtn;
    private TextView TaskViewName;

    //Wage calculations variables
    private TextView WageTextView;
    private double count123;
    private double wagePerHour = hourlyWage / 3600;
    Thread t;
    boolean continueThread;
    private double totalWageEarned;
    private long holdLastPause;

    //Count down timer variables
    private long START_TIME_IN_MILLISECONDS = (long) (numberOfHoursWorked * 3600000);
    private TextView TxtOutputTimer;
    private CountDownTimer TimeWorkedCountDownTimer;
    private boolean TimerRunning;
    private long TimeLeftInMillisecs = START_TIME_IN_MILLISECONDS;

    //Popup box variables
    private EditText userWageInput;
    private EditText userHoursInput;
    DB app;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd - HH:mm", Locale.US);

    View currentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.home_tracking, container, false);
        app = (DB) getActivity().getApplication();

        final Document currentdoc = app.getTaskDoc();
        Double wage = (Double) currentdoc.getProperty("taskwage");
        String Taskname = (String) currentdoc.getProperty("taskname");
        String startTask = currentdoc.getProperty("TaskScheduledStartDate").toString() + " - " + currentdoc.getProperty("TaskScheduledStartTime");
        String endTask = currentdoc.getProperty("TaskScheduledEndDate").toString() + " - " + currentdoc.getProperty("TaskScheduledEndTime");
        Date startDateFormat = null;
        Date EndDateFormat = null;
        try {
            startDateFormat = dateFormatter.parse(startTask);
            EndDateFormat = dateFormatter.parse(endTask);

        }catch(Exception e){
            System.out.println(e);
        }
        //milliseconds
        long diff = EndDateFormat.getTime() - startDateFormat.getTime();

        TaskViewName = (TextView) currentView.findViewById(R.id.TaskNametxtVw);
        ClockInOutToggleBtn = (ToggleButton) currentView.findViewById(R.id.clockIn_OutToggleBtn);
        workChronometer = (Chronometer) currentView.findViewById(R.id.workChrono);
        TakeBreakToggleBtn = (ToggleButton) currentView.findViewById((R.id.breakToggleBtn));
        WageTextView = (TextView) currentView.findViewById(R.id.wageEarnedTxtVw);

        TxtOutputTimer = (TextView) currentView.findViewById(R.id.TimeUntilTaskDoneTimertxtvw);


        TaskViewName.setText(taskName);
        TakeBreakToggleBtn.setVisibility(View.INVISIBLE);


        ClockInOutToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){
                    app.setTracking(true);
                    try {
                        app.StartTask(currentdoc);
                    }catch(Exception e){
                        System.out.println(e);
                    }
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
                    app.setTracking(false);
                    Double Extracosts = 0.0;
                    Double TaskEarnings = 0.0;
                    try {
                        app.EndTask(currentdoc, Extracosts, TaskEarnings);
                    }catch(Exception e){
                        System.out.println(e);
                    }

                }

            }
        });
        ClockInOutToggleBtn.setChecked(true);
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
                holdLastPause = SystemClock.elapsedRealtime();
                workChronometer.stop();
                continueThread = false;
                showAlert();

            }
        }.start();
        TimerRunning = true;


    }

    private void pauseTimer(){
        TimeWorkedCountDownTimer.cancel();
        TimerRunning = false;

    }

    private void resetTimer(){
        TimeLeftInMillisecs = START_TIME_IN_MILLISECONDS;
        updateCountDownText();

    }

    private void updateTimerValue(){

        String timeTxt = userHoursInput.getText().toString();

        double timeD = Double.parseDouble(timeTxt);

        System.out.println(timeD);

        numberOfHoursWorked = timeD;

        START_TIME_IN_MILLISECONDS = (long) (numberOfHoursWorked * 3600000);

        TimeLeftInMillisecs = START_TIME_IN_MILLISECONDS;
        updateCountDownText();


    }

    private void updateWageValue(){

        String wageTxt = userWageInput.getText().toString();

        double wageD = Double.parseDouble(wageTxt);

        System.out.println(hourlyWage);
        hourlyWage = wageD;
        System.out.println(hourlyWage);

        wagePerHour = hourlyWage / 3600;




    }

    private void updateCountDownText() {
        int hours = (int) (TimeLeftInMillisecs / 1000) / 3600;

        int minutes = (int) (TimeLeftInMillisecs / (1000 * 60)) % 60;

        int seconds = (int) (TimeLeftInMillisecs / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

        TxtOutputTimer.setText(timeLeftFormatted);
    }



    public void showAlert(){

        AlertDialog.Builder myAlert = new AlertDialog.Builder(this.getContext());
        myAlert.setMessage("Your time has run out. Would you like to continue working or clock out?")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showContinueAddWageAlert();
                    }
                })
                .setNegativeButton("Clock out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ClockInOutToggleBtn.setChecked(false);

                    }
                })
                .create();
        myAlert.show();

    }

    public void showContinueAddWageAlert(){

        AlertDialog.Builder myAlert = new AlertDialog.Builder(this.getContext());
        myAlert.setMessage("Update your current hourly wage $" + hourlyWage+ " or type the same wage to keep it the same.");
        userWageInput = new EditText(this.getContext());
        myAlert.setView(userWageInput);

        myAlert.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        updateWageValue();

                        dialog.dismiss();

                        showContinueAddedHoursAlert();



                    }
                })
                .create();
        myAlert.show();

    }

    public void showContinueAddedHoursAlert(){

        AlertDialog.Builder myAlert = new AlertDialog.Builder(this.getContext());
        myAlert.setMessage("Update Hours Added to Task:");

        userHoursInput = new EditText(this.getContext());
        myAlert.setView(userHoursInput);

        myAlert.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {



                dialog.dismiss();

                resetTimer();
                updateTimerValue();

                continueThread = true;
                wageTrack();

                startTimer();
                workChronometer.setBase(workChronometer.getBase() + SystemClock.elapsedRealtime() - holdLastPause);
                workChronometer.start();

            }
        }).create();
        myAlert.show();






    }






}
