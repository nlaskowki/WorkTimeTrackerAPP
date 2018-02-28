package com.worktimetrackerapp.GUI_Interfaces;


        import android.app.Fragment;
        import android.os.Bundle;
        import android.os.SystemClock;
        import android.support.annotation.Nullable;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.Chronometer;

        import com.worktimetrackerapp.R;

public class HomeTracking_Controller extends Fragment {

    private Button TestButton;
    private Chronometer TestChronometer;


    View currentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.home_tracking, container, false);

       TestButton = (Button) currentView.findViewById(R.id.jobDoneBtn);
       TestChronometer = (Chronometer) currentView.findViewById(R.id.TestChrono);

        TestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TestChronometer.setBase(SystemClock.elapsedRealtime());
                TestChronometer.start();
            }
        });












        return currentView;







    }



}
