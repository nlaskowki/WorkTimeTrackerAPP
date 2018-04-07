package com.worktimetrackerapp.GUI_Interfaces;

        import android.annotation.SuppressLint;
        import android.app.Activity;
        import android.app.AlertDialog;
        import android.app.DatePickerDialog;
        import android.app.Dialog;
        import android.app.DialogFragment;
        import android.app.Fragment;
        import android.app.FragmentManager;
        import android.app.TimePickerDialog;
        import android.content.DialogInterface;
        import android.content.DialogInterface.OnClickListener;
        import android.os.Bundle;
        import android.os.CountDownTimer;
        import android.os.SystemClock;
        import android.support.annotation.Nullable;
        import android.text.Html;
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

        import java.math.BigDecimal;
        import java.text.DecimalFormat;
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
    private double ExtraCosts;
    private long holdLastPause;

    //Count down timer variables
    private long START_TIME_IN_MILLISECONDS = (long) (numberOfHoursWorked * 3600000);
    private TextView TxtOutputTimer;
    private CountDownTimer TimeWorkedCountDownTimer;
    private boolean TimerRunning;
    private long TimeLeftInMillisecs = START_TIME_IN_MILLISECONDS;

    //Popup box variables
    private String userWageInput = " ";
    private double overTimeWageEarned;
    private double overTimeCounter;


    DB app;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd - HH:mm", Locale.US);
    Date startDateFormat = null;
    Date EndDateFormat = null;
    private Calendar myCalendar;
    private Date updatedEndDate = null;
    private static DecimalFormat df2 = new DecimalFormat(".##");

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

        System.out.println("Hourlywage1231213 = " + hourlyWage);

        wagePerHour = hourlyWage / 3600;

        System.out.println("Wage Per hour1232124 = " + wagePerHour);


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

        TaskViewName = (TextView) currentView.findViewById(R.id.TaskNametxtVw);
        overTimeWageCount = (TextView) currentView.findViewById(R.id.overTimeWageTV);
        OverTimeEarnedTextView = (TextView) currentView.findViewById(R.id.textView6);
        ClockInOutToggleBtn = (ToggleButton) currentView.findViewById(R.id.clockIn_OutToggleBtn);
        workChronometer = (Chronometer) currentView.findViewById(R.id.workChrono);
        TakeBreakToggleBtn = (ToggleButton) currentView.findViewById((R.id.breakToggleBtn));
        WageTextView = (TextView) currentView.findViewById(R.id.wageEarnedTxtVw);
        TxtOutputTimer = (TextView) currentView.findViewById(R.id.TimeUntilTaskDoneTimertxtvw);
        submitUpdateWageButton = (Button) currentView.findViewById(R.id.UpdateWageSubmitBtn);

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


                    System.out.println("Before popup extra costs " + totalWageEarned);

                    showExtraCostsPopup();

                    System.out.println("After popup extra costs " + totalWageEarned);
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

        totalWageEarned = 0.0;

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

                                totalWageEarned += wagePerHour;

                                String.valueOf(count123);

                                WageTextView.setText(String.format("$ %.2f", count123));

                                if (overTimeWageCount.isShown()){

                                    overTimeCounter += overTimeWageEarned;

                                    overTimeWageCount.setText(String.format("$ %.2f", overTimeCounter));

                                    System.out.println("OverTime wage value " + overTimeCounter);



                                }


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

    private void updateWageValue(){

        String wageTxt = userWageInput;

        double wageD = parseDouble(wageTxt);

        BigDecimal OverTimeAmountDiff = new BigDecimal(wageD).subtract(new BigDecimal(hourlyWage));

        System.out.println("Old houldy wage " + hourlyWage);
        hourlyWage = wageD;
        System.out.println("New hourly wage "+ hourlyWage);

        overTimeWageEarned = OverTimeAmountDiff.doubleValue();

        if (overTimeWageEarned <= 0 ){

            overTimeWageCount.setVisibility(INVISIBLE);
            OverTimeEarnedTextView.setVisibility(INVISIBLE);

        }

        System.out.println("Overtime Wage earened amount ++++++++++++++++" + overTimeWageEarned);

        wagePerHour = hourlyWage / 3600;

        overTimeWageEarned = overTimeWageEarned / 3600;



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

        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        String hourlyWageString = Double.toString(hourlyWage);

        final double overtimevalue;

        View view  = getActivity().getLayoutInflater().inflate(R.layout.home_tracking_dialog, null);
        dialog.setContentView(view);

        final EditText editTextWageupdate = (EditText) view.findViewById(R.id.UpdateWageEditText);
        editTextWageupdate.setText(hourlyWageString);
        Button submitBtn = (Button) view.findViewById(R.id.UpdateWageSubmitBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userWageInput = editTextWageupdate.getText().toString();

                if (editTextWageupdate.getText().toString().isEmpty()){

                    editTextWageupdate.setError(Html.fromHtml("<font color='#ffffff'>Field cannot be empty</font>"));

                    editTextWageupdate.requestFocus();

                    return;

                }

                if (!userWageInput.matches("^[1-9]\\d*(\\.\\d+)?$")){

                    editTextWageupdate.setError(Html.fromHtml("<font color='#ffffff'>Field input is invalid</font>"));

                    editTextWageupdate.requestFocus();

                    return;

               }

                final Double doubleWageInput = parseDouble(userWageInput);


                if (doubleWageInput > 300){

                    editTextWageupdate.setError(Html.fromHtml("<font color='#ffffff'>Field cannot be greater than $300</font>"));

                    editTextWageupdate.requestFocus();

                    return;

                }

                else {

                    showContinueAddedHoursAlert();
                    updateWageValue();
                    dialog.dismiss();



                }
            }
        });

        dialog.show();



    }

    public void showContinueAddedHoursAlert(){

        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(false);

        View view  = getActivity().getLayoutInflater().inflate(R.layout.overtime_popup_dialog_home_tracking, null);
        dialog.setContentView(view);

        final EditText newEndDateValue = view.findViewById(R.id.newEndDateEditTxt);
        Button newEndDateSubmitBtn = view.findViewById(R.id.newEndDateSubmitBtn);


        newEndDateValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DateTimeSetter(newEndDateValue, (String) newEndDateValue.getText().toString());

            }
        });

        newEndDateSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (newEndDateValue.getText().toString().isEmpty()){

                    newEndDateValue.setError(Html.fromHtml("<font color='#ffffff'>Field cannot be empty</font>"));

                    newEndDateValue.requestFocus();

                    return;

                }

                String overTime = newEndDateValue.getText().toString();

                try {
                    updatedEndDate = dateFormatter.parse(overTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date_To_Decimal_Converter convert = new Date_To_Decimal_Converter();

                long newTimeInMillis = convert.getDateTime(EndDateFormat,updatedEndDate);

                System.out.println("Date in miliis is " + newTimeInMillis);

                if (newTimeInMillis <= 0){

                    newEndDateValue.setError(Html.fromHtml("<font color='#ffffff'>Time is before your current end time</font>"));

                    newEndDateValue.requestFocus();

                    return;

                } else {

                    dialog.dismiss();
                    TimeLeftInMillisecs = newTimeInMillis;
                    continueThread = true;
                    wageTrack();
                    startTimer();
                    workChronometer.setBase(workChronometer.getBase() + SystemClock.elapsedRealtime() - holdLastPause);
                    workChronometer.start();

                }

            }
        });
        dialog.show();



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

                    }
                }, myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), true).show();
            }
        },myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    public void showExtraCostsPopup(){
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(false);

        View view  = getActivity().getLayoutInflater().inflate(R.layout.extra_costs_dialog, null);
        dialog.setContentView(view);

        final TextView extraCostTitle = (TextView) view.findViewById(R.id.extraCostsTitle);
        final EditText extraCostEditText = (EditText) view.findViewById(R.id.extraCostsEditTxt);
        Button extraCostSubmitBtn = (Button) view.findViewById(R.id.extraCostsSubmitButton);

        extraCostSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String stringExtraCostInput = extraCostEditText.getText().toString();

                if (extraCostEditText.getText().toString().isEmpty()){

                    extraCostEditText.setError(Html.fromHtml("<font color='#ffffff'>Field cannot be empty</font>"));

                    extraCostEditText.requestFocus();

                    return;

                }

                if (!stringExtraCostInput.matches("^[1-9]\\d*(\\.\\d+)?$")){

                    extraCostEditText.setError(Html.fromHtml("<font color='#ffffff'>Field input is invalid</font>"));

                    extraCostEditText.requestFocus();

                    return;

                } else {

                    ExtraCosts = Double.parseDouble(extraCostEditText.getText().toString());

                    System.out.println("Extra costs here 1234123 " + ExtraCosts);

                    final Document currentdoc = app.getTaskDoc();

                    dialog.dismiss();

                    double Extracosts = ExtraCosts;
                    double TaskEarnings = totalWageEarned;

                    app.setTracking(false);

                    System.out.println("Extra costs value  = " + df2.format(Extracosts));
                    System.out.println("TaskEarnings value = " + df2.format(TaskEarnings));

                    try {
                        app.EndTask(currentdoc, Double.valueOf(df2.format(Extracosts)), Double.valueOf(df2.format(TaskEarnings)));

                        System.out.println("OverTIme sent to DB " + overTimeCounter);

                        if (overTimeWageEarned <= 0){

                            overTimeCounter = 0;

                        }

                        app.StartTaskOvertime(currentdoc,Double.valueOf(df2.format(overTimeCounter)));

                    }catch(Exception e){
                        System.out.println(e);
                    }

                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeNotTracking_Controller()).commit();




                }

            }
        });
        dialog.show();


    }

}









