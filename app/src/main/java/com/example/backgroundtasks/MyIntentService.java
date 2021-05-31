package com.example.backgroundtasks;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

public class MyIntentService extends IntentService {

    private static final String ACTION_EXERCISE1 = "com.example.intent_service.action.exercise1";
    private static final String PARAMETER1 = "com.example.intent_service.extra.parameter1";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;

    public MyIntentService(String name) {
        super(name);
    }

    private static void runService(Context context, int parameter) {
        Intent intent = new Intent(context, MyIntentService.class);

        intent.setAction(ACTION_EXERCISE1);
        intent.putExtra(PARAMETER1, parameter);

        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
