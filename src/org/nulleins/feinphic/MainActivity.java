package org.nulleins.feinphic;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

  static final String TAG_CAMERA_FRAGMENT = "camera_fragment";
  static final String TAG_SETTINGS_FRAGMENT = "settings_fragment";
  static final String TAG_FILMSTRIP_FRAGMENT = "filmstrip_fragment";
  static final String TAG_PHOTOVIEW_FRAGMENT = "photoview_fragment";
  static final String SELFIE_NEW = "org.nulleins.feinphic.SELFIE_NEW";
  static final String SELFIE_VIEW = "org.nulleins.feinphic.SELFIE_VIEW";
  static final String SELFIE_TIME = "org.nulleins.feinphic.SELFIE_TIME";
  static final String TAKE_SELFIE_ACTION = "org.nulleins.feinphic.TAKE_SELFIE";
  static final String PHOTO_URI = "photo-uri";
  private static final String TAG = "MA";
  private CameraFragment cameraFragment;
  private SettingsFragment settingsFragment;
  private FilmStripFragment filmstripFragment;
  private PhotoViewFragment photoviewFragment;

  @Override
  public void onCreate (final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupFragments(savedInstanceState);
    if(savedInstanceState == null) {
      startService(new Intent(this, ReminderService.class));
    }
    createReminderAlarm();
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_shoot:
        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, new CameraFragment())
            .addToBackStack(null)
            .commit();
        return true;
      case R.id.action_settings:
        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, new SettingsFragment())
            .addToBackStack(null)
            .commit();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onSaveInstanceState(final Bundle savedInstanceState) {
    if (cameraFragment != null) {
      savedInstanceState.putString(TAG_CAMERA_FRAGMENT, cameraFragment.getTag());
    }
    if (settingsFragment != null) {
      savedInstanceState.putString(TAG_SETTINGS_FRAGMENT, settingsFragment.getTag());
    }
    if (filmstripFragment != null) {
      savedInstanceState.putString(TAG_FILMSTRIP_FRAGMENT, filmstripFragment.getTag());
    }
    if (photoviewFragment != null) {
      savedInstanceState.putString(TAG_PHOTOVIEW_FRAGMENT, photoviewFragment.getTag());
    }
  }

  public void clearPhotos(final View prefView) {
    final Dialog dialog = new Dialog(this);

    dialog.setContentView(R.layout.delete_dialog);
    dialog.setTitle(getResources().getText(R.string.delete));

    dialog.findViewById(R.id.action_delete).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        final Button button = (Button) prefView;
        button.setVisibility(View.INVISIBLE);
        Toast.makeText(MainActivity.this, "Deleting all photos ", Toast.LENGTH_SHORT).show();
        PhotoManager.deletePhotos();
        if (filmstripFragment != null) {
          filmstripFragment.reset();
        }
        dialog.dismiss();
      }
    });
    dialog.findViewById(R.id.cancel_delete).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        dialog.cancel();
      }
    });
    dialog.show();
  }

  private void setupFragments(final Bundle savedInstanceState) {
    final FragmentManager fragmentManager = getFragmentManager();
    if(savedInstanceState != null) {
      cameraFragment = (CameraFragment)fragmentManager
          .findFragmentByTag(savedInstanceState.getString(TAG_CAMERA_FRAGMENT));
      settingsFragment = (SettingsFragment)fragmentManager
          .findFragmentByTag(savedInstanceState.getString(TAG_SETTINGS_FRAGMENT));
      filmstripFragment = (FilmStripFragment)fragmentManager
          .findFragmentByTag(savedInstanceState.getString(TAG_FILMSTRIP_FRAGMENT));
      photoviewFragment = (PhotoViewFragment)fragmentManager
          .findFragmentByTag(savedInstanceState.getString(TAG_PHOTOVIEW_FRAGMENT));
    } else {
      cameraFragment = new CameraFragment();
      settingsFragment = new SettingsFragment();
      filmstripFragment = new FilmStripFragment();
      photoviewFragment = new PhotoViewFragment();
    }
    fragmentManager.beginTransaction()
        .replace(android.R.id.content, filmstripFragment)
        .commit();

    if(getIntent().getAction().equals(TAKE_SELFIE_ACTION)) {
      fragmentManager.beginTransaction()
          .replace(android.R.id.content, cameraFragment)
          .addToBackStack("camera")
          .commit();
    }
  }

  private void createReminderAlarm() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    final PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, ReminderService.class), 0);
    final long remIntervalSecs = ReminderService.getReminderInterval(prefs);
    if(remIntervalSecs == 0) {
      alarmManager.cancel(alarmIntent);
    } else {
      final long updateFreq = remIntervalSecs * 1000;
      final long startTime = SystemClock.elapsedRealtime() + updateFreq;
      alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime, updateFreq, alarmIntent);
      Log.d(TAG, "Poll interval=" + remIntervalSecs + "; Next reminder in " + (updateFreq / 1000) + "s");
    }
  }
}
