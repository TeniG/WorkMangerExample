package com.tenig.workmangerexample;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.Date;

public class MyWorker extends Worker {

    //a public static string that will be used as the key
    //for sending and receiving data
    public static final String TASK_DESC = "task_desc";

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        //getting the input data
        String taskDesc = getInputData().getString(TASK_DESC);
        Date currentTime = Calendar.getInstance().getTime();



        displayNotification("My Worker", taskDesc + " "+currentTime);
        String outputDataStr;
        if(taskDesc == "The task data passed from MainActivity ")
        {
            //setting output data
            outputDataStr = "The conclusion of the task from first request";
        }
        else {
            outputDataStr = "The conclusion of the task from second request";
        }

        //setting output data
        Data outputData = new Data.Builder()
                .putString(TASK_DESC, outputDataStr)
                .build();



        return  Result.success(outputData);
    }

    private void displayNotification(String title, String task) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("simplifiedcoding", "simplifiedcoding", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "simplifiedcoding")
                .setContentTitle(title)
                .setContentText(task)
                .setSmallIcon(R.mipmap.ic_launcher);

        notificationManager.notify(1, notification.build());
    }

}
