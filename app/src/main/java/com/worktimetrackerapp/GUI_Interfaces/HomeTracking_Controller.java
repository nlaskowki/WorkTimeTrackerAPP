package com.worktimetrackerapp.GUI_Interfaces;

        import android.annotation.SuppressLint;
        import android.app.Activity;
        import android.app.AlertDialog;
        import android.app.DatePickerDialog;
        import android.app.Dialog;
        import android.app.DialogFragment;
        import android.app.Fragment;
        import android.app.TimePickerDialog;
        import android.content.DialogInterface;
        import android.content.DialogInterface.OnClickListener;
        import android.os.Bundle;
        import android.os.CountDownTimer;
        import android.os.SystemClock;
        import android.support.annotation.Nullable;
        import android.text.Layout;
        import android.view.ContextThemeWrapper;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.inputmethod.EditorInfo;
        import android.widget.Button;
        import android.widget.Chronometer;
        import android.widget.CompoundButton;
        import android.widget.DatePicker;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.TimePicker;
        import android.widget.Toast;
        import android.widget.ToggleButton;
        import com.couchbase.lite.Document;
        import com.worktimetrackerapp.DB;
        import com.worktimetrackerapp.R;
        import java.text.ParseException;
        import java.text.SimpleDateFormat;
        import java.time.Period;
        import java.util.Calendar;
        import java.util.Date;
        import java.util.Locale;

        import static android.R.style.Theme_DeviceDefault_Light_Dialog;
        import static android.view.View.INVISIBLE;
        import static java.lang.Double.*;

public class HomeTracking_Controller extends Fragment {

    public Button submitUpdateWageButton;
    //Variables from task settings
    public double hourlyWage;
    public double numberOfHoursWorked;
    public String taskName;

    //Chronometer and toggle buttons variables
    private Chronometer workChronometer;
    private ToggleButton ClockInOutToggleBtn;
    private ToggleButton TakeBreakToggleBtn;
    private TextView TaskViewName;
    private TextView OverTimeEarnedTextView;

    //Wage calculations variables
    private TextView WageTextView;
    private TextView overTimeWageCount;
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
    Date startDateFormat = null;
    Date EndDateFormat = null;
    private Calendar myCalendar;
    private Date updatedEndDate = null;

    View currentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.home_tracking, container, false);

        Date_To_Decimal_Converter convertTime = new Date_To_Decimal_Converter();

        long timeInMillis;

        app = (DB) getActivity().getApplication();
        final Document currentdoc = app.getTaskDoc();

        hourlyWage = parseDouble(currentdoc.getProperty("taskwage").toString());

        System.out.println("Hourlywage = " + hourlyWage);

        wagePerHour = hourlyWage / 3600;

        System.out.println("Wage Per hour = " + wagePerHour);


        taskName = (String) currentdoc.getProperty("taskname");
        String startTask = currentdoc.getProperty("TaskScheduledStartDate").toString() + " - " + currentdoc.getProperty("TaskScheduledStartTime");
        String endTask = currentdoc.getProperty("TaskScheduledEndDate").toString() + " - " + currentdoc.getProperty("TaskScheduledEndTime");

        try {
            startDateFormat = dateFormatter.parse(startTask);
            EndDateFormat = dateFormatter.parse(endTask);

        }catch(Exception e){
            System.out.println(e);
        }

        timeInMillis = convertTime.getDateTime(startDateFormat,EndDateFormat);

        TimeLeftInMillisecs = timeInMillis;

        System.out.println("difference in millis " + TimeLeftInMillisecs);

        TaskViewName = (TextView) currentView.findViewById(R.id.TaskNametxtVw);
        overTimeWageCount = (TextView) currentView.findViewById(R.id.overTimeWageTV);
        OverTimeEarnedTextView = (TextView) currentView.findViewById(R.id.textView6);
        ClockInOutToggleBtn = (ToggleButton) currentView.findViewById(R.id.clockIn_OutToggleBtn);
        workChronometer = (Chronometer) currentView.findViewById(R.id.workChrono);
        TakeBreakToggleBtn = (ToggleButton) currentView.findViewById((R.id.breakToggleBtn));
        WageTextView = (TextView) currentView.findViewById(R.id.wageEarnedTxtVw);
        TxtOutputTimer = (TextView) currentView.findViewById(R.id.TimeUntilTaskDoneTimertxtvw);
        submitUpdateWageButton= (Button) currentView.findViewById(R.id.UpdateWageSubmitBtn);
        TaskViewName.setText(taskName);
        TakeBreakToggleBtn.setVisibility(INVISIBLE);

        overTimeWageCount.setVisibility(INVISIBLE);
        OverTimeEarnedTextView.setVisibility(INVISIBLE);


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
                    TakeBreakToggleBtn.setVisibility(INVISIBLE);

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

                                count123 += wagePerHour;

                                System.out.println("Count123  = " + count123);

                                System.out.println("wagePerHour = " + wagePerHour);

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

        double timeD = parseDouble(timeTxt);

        System.out.println(timeD);

        numberOfHoursWorked = timeD;

        START_TIME_IN_MILLISECONDS = (long) (numberOfHoursWorked * 3600000);

        TimeLeftInMillisecs = START_TIME_IN_MILLISECONDS;
        updateCountDownText();


    }

    private void updateWageValue(){

        String wageTxt = userWageInput.getText().toString();

        double wageD = parseDouble(wageTxt);

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
        myAlert.setMessage("Your time has run out. Would you like to continue working or clock out?");
        myAlert.setPositiveButton("Continue", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                overTimeWageCount.setVisibility(View.VISIBLE);
                OverTimeEarnedTextView.setVisibility(View.VISIBLE);
                showContinueAddWageAlert();
            }
        });
        myAlert.setNegativeButton("Clock out", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ClockInOutToggleBtn.setChecked(false);

            }
        });
        myAlert.create();
        myAlert.show();

    }


    public void showContinueAddWageAlert(){



        boolean flag = false;
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        String hourlyWageString = Double.toString(hourlyWage);

        View view  = getActivity().getLayoutInflater().inflate(R.layout.home_tracking_dialog, null);
        dialog.setContentView(view);

        final TextView textViewUpdateWageTitle = (TextView) view.findViewById(R.id.UpdateWageDialogTitle);
        final EditText editTextWageupdate = (EditText) view.findViewById(R.id.UpdateWageEditText);
        editTextWageupdate.setText(hourlyWageString);
        Button submitBtn = (Button) view.findViewById(R.id.UpdateWageSubmitBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String stringWageInput = editTextWageupdate.getText().toString();

                if (editTextWageupdate.getText().toString().isEmpty()){

                    editTextWageupdate.setError("Field cannot be empty");

                    editTextWageupdate.requestFocus();

                    return;

                }

//                if (!stringWageInput.matches("^\\d{0,9}\\.\\d{1,4}$")){
//
//                    editTextWageupdate.setError("Field input is invalid");
//
//                    editTextWageupdate.requestFocus();
//
//                    return;
//
//                }

                final Double doubleWageInput = parseDouble(stringWageInput);

                System.out.println(doubleWageInput);

                if (doubleWageInput < 0){

                   editTextWageupdate.setError("Field cannot be less than 0");

                    editTextWageupdate.requestFocus();

                    return;

                }

                if (doubleWageInput > 300){

                    editTextWageupdate.setError("Field cannot be greater than $300");

                    editTextWageupdate.requestFocus();

                    return;

                }

                else {

                    showContinueAddedHoursAlert();
                    dialog.dismiss();



                }
            }
        });

        dialog.show();

        //updateWageValue();

    }

    public void showContinueAddedHoursAlert(){

        AlertDialog.Builder myAlert = new AlertDialog.Builder(this.getContext());
        myAlert.setMessage("Update Time Added to Task:");

        System.out.println("This is working");

        userHoursInput = new EditText(this.getContext());
        myAlert.setView(userHoursInput);

        userHoursInput.onEditorAction(EditorInfo.IME_ACTION_DONE);

        userHoursInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DateTimeSetter(userHoursInput, (String) userHoursInput.getText().toString());




            }
        });

        myAlert.setPositiveButton("Finish", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                String overTime = userHoursInput.getText().toString();

                try {
                    updatedEndDate = dateFormatter.parse(overTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date_To_Decimal_Converter convert = new Date_To_Decimal_Converter();

                long newTimeInMillis = convert.getDateTime(EndDateFormat,updatedEndDate);

                TimeLeftInMillisecs = newTimeInMillis;

                continueThread = true;
                wageTrack();

                startTimer();
                workChronometer.setBase(workChronometer.getBase() + SystemClock.elapsedRealtime() - holdLastPause);
                workChronometer.start();

            }
     }).create();
        myAlert.show();

    }

    public void DateTimeSetter(final TextView v, String dateTime){
        myCalendar = Calendar.getInstance();
        try {
            if(!dateTime.isEmpty()) {
                Date date = dateFormatter.parse(dateTime);
                myCalendar.setTime(date);
            }else{
                myCalendar = Calendar.getInstance();
            }
        }catch(Exception e){

        }
        new DatePickerDialog(getLayoutInflater().getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                new TimePickerDialog(getLayoutInflater().getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        myCalendar.set(Calendar.MINUTE, selectedMinute);
                        v.setText(dateFormatter.format(myCalendar.getTime()));

                        //double result = CalculateTaskEarnings(otherInfoStartedTask.getText().toString(), otherInfoOvertimeStartedTask.getText().toString(), otherInfoEndedTask.getText().toString(), wage.getText().toString(), WageExtraTime.getText().toString(), TaskExtraCost.getText().toString());
                        //TaskEarnings.setText(String.format("%.2f", result));

                    }
                }, myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), true).show();
            }
        },myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }



    }









