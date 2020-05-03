package com.tenig.workmangerexample;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Constraints constraints;
    OneTimeWorkRequest oneTimeWorkRequest;
    Data data;
    int count = 0;
    WorkManager workManager;
    Handler handler;
    Runnable runnableCode;
    PeriodicWorkRequest periodicWorkRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        workManager = WorkManager.getInstance();
        //creating constraints
        constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        data = new Data.Builder()
                .putString(MyWorker.TASK_DESC, "The task data passed from MainActivity ")
                .build();


        oneTimeWorkRequest = new OneTimeWorkRequest
                .Builder(MyWorker.class)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

         PeriodicWorkRequest periodicWorkRequest
                = new PeriodicWorkRequest
                .Builder(MyWorker.class, 5000, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .setConstraints(constraints)
                .build();


        //A click listener for the button
        //inside the onClick method we will perform the work
        findViewById(R.id.buttonEnqueue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createOneTimeRequest();

            }
        });

        final TextView textView = findViewById(R.id.textViewStatus);
        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        //receiving back the data
                        if(workInfo != null && workInfo.getState().isFinished()) {
                            textView.append(workInfo.getOutputData().getString(MyWorker.TASK_DESC) + "\n" +"count="+count);
                            if(count < 10 )
                                createOneTimeRequest();
                            else {
                                count = 0 ;
//                                startHandler((long) (5 * 1000));
                            }
                        }

                        if(workInfo != null) {
                           Log.d("MainActivity", "state "+workInfo.getState().name()+ "\n" +"count="+count);
                            textView.append(workInfo.getState().name() + "\n");

                        }

                    }
                });


        handler = new Handler();
        runnableCode = new Runnable() {

            @Override
            public void run() {
                createOneTimeRequest();
            }
        };
    }

    private void stopHandler() {
        handler.removeCallbacks(runnableCode);
    }

    private void startHandler(Long delayTime) {
        handler.postDelayed(runnableCode, delayTime);
    }

    private void createOneTimeRequest()
    {
        Date currentTime = Calendar.getInstance().getTime();
        Log.d("MainActivity", "currentTime="+currentTime);

        count++;

        //This is the subclass of our WorkRequest
        ListenableFuture<List<WorkInfo>> statuses = workManager.getWorkInfosForUniqueWork("test");
        List<WorkInfo> workInfoList = null;
        try {
            workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                Log.d("MainActivity", "state = "+state);

                if(state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED){
                    Log.d("MainActivity", "state if = "+state);
                }
                else
                {
                    Log.d("MainActivity", "state else = "+state);
//                    createRequest();
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



//        WorkManager.getInstance().enqueue(oneTimeWorkRequest);
        workManager.enqueueUniqueWork("test",ExistingWorkPolicy.KEEP,oneTimeWorkRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createOneTimeRequest();
    }
}
