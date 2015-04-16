package org.nulleins.feinphic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

public class ReminderService extends BroadcastReceiver {

  private static final String TAG = "RSV";
  private static final String PREF_KEY_REMINDER_FREQL = "pref_key_reminder_frequency";
  private static final int SELFIE_NOTIFICATION_ID = 11151999;

  @Override
  public void onReceive(final Context context, final Intent intent) {
    Log.d(TAG, "onReceive, intent: " + intent);
    notify(context, context.getResources().getString(R.string.selfie_time_reminder));
  }

  private void notify(final Context context, final String message) {
    final Intent launchIntent = context.getPackageManager()
        .getLaunchIntentForPackage(context.getPackageName());
    launchIntent.setAction(MainActivity.TAKE_SELFIE_ACTION);

    context.sendOrderedBroadcast(new Intent(MainActivity.SELFIE_TIME), null,
        new BroadcastReceiver() {
          @Override
          public void onReceive(final Context context, final Intent intent) {
            Log.d(TAG, "Sending Reminder: " + message);
            sendNotification(context, message);
          }

          private void sendNotification(final Context context, final String message) {
            final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            final RemoteViews contentView = new RemoteViews(
                context.getPackageName(), R.layout.selfietime_notification);
            contentView.setTextViewText(R.id.text, message);

            final Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setTicker(message)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.camera_button)
                .setAutoCancel(true)
                .setContent(contentView);

            final NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(SELFIE_NOTIFICATION_ID, notificationBuilder.build());
          }
        }, null, 0, null, null);
  }

  static long getReminderInterval(final SharedPreferences prefs) {
    final String interval = prefs.getString(PREF_KEY_REMINDER_FREQL, "Test");
    if(interval.startsWith("Test")) {
      return SECS_PER_MIN;
    } else {
      return intervals.get(interval);
    }
  }

  private final static long SECS_PER_MIN = 60;
  private final static long SECS_PER_HOUR = SECS_PER_MIN * 60;
  private final static long SECS_PER_DAY = SECS_PER_HOUR * 24;
  private final static long SECS_PER_WEEK = SECS_PER_DAY * 7;
  private final static long SECS_PER_MONTH = SECS_PER_WEEK * 30;
  private final static long SECS_PER_YEAR = SECS_PER_MONTH * 12;
  private static final Map<String,Long> intervals = new HashMap<String,Long>() {{
    put("Never", 0L);
    put("Hourly", SECS_PER_HOUR);
    put("Daily", SECS_PER_DAY);
    put("Weekly", SECS_PER_WEEK);
    put("Monthly", SECS_PER_MONTH);
    put("Yearly", SECS_PER_YEAR);
  }};


}
