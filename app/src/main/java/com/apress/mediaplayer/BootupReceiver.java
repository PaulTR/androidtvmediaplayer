package com.apress.mediaplayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Paul on 10/20/15.
 */
public class BootupReceiver extends BroadcastReceiver {
    private static final String TAG = "BootupActivity";

    private static final long INITIAL_DELAY = 5000;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootupActivity initiated");
        if (intent.getAction().endsWith(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService( new Intent( context, RecommendationsService.class ) );
            scheduleRecommendationUpdate(context);
        }
    }

    private void scheduleRecommendationUpdate(Context context) {
        Log.d(TAG, "Scheduling recommendations update");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent recommendationIntent = new Intent(context, RecommendationsService.class);
        PendingIntent alarmIntent = PendingIntent.getService(context, 0, recommendationIntent, 0);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                INITIAL_DELAY,
                AlarmManager.INTERVAL_HALF_HOUR,
                alarmIntent);
    }

}
