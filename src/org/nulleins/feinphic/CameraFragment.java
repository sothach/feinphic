package org.nulleins.feinphic;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import org.nulleins.feinphic.model.PhotoRecord;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class CameraFragment extends Fragment {

  private static final String TAG = "CAM";
  private static final String PREF_KEY_FACEFRAME_POS = "pref_key_faceframe_pos";
  private final IntentFilter intentFilter = new IntentFilter(MainActivity.SELFIE_TIME);
  private Camera camera;
  private AudioManager audioManager;
  private SoundPool soundPool;
  private int shutterSound;
  private float volume;
  private byte[] currentImage;
  private BroadcastReceiver selfietimeReceiver;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    selfietimeReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals(MainActivity.SELFIE_TIME)) {
          getFragmentManager()
              .beginTransaction()
              .show(CameraFragment.this)
              .commit();
        }
      }
    };
  }

  @Override
  public void onResume() {
    super.onResume();
    getActivity().registerReceiver(selfietimeReceiver, intentFilter);
  }

  @Override
  public void onPause() {
    super.onPause();
    getActivity().unregisterReceiver(selfietimeReceiver);
  }

  @Override
  public View onCreateView(
      final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.camera, container, false);
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    Log.d(TAG, "onActivityCreated (camera)");
    super.onCreate(savedInstanceState);
    createCamera();
  }

  private void createCamera() {
    final View view = getView();
    if(view == null) {
      Log.e(TAG, "Could not get view (camera)");
      return;
    }
    try {
      camera = getBestCamera();
    } catch ( final Exception e) {
      Log.e(TAG, "Could not open Camera (camera): " + e.getMessage());
      return;
    }

    final FrameLayout mainView = (FrameLayout) view.findViewById(R.id.camera_preview);
    final CameraPreview preview = new CameraPreview(getActivity(), camera);
    mainView.addView(preview);
    final ImageView viewfinder = createViewfinder();
    mainView.addView(viewfinder);

    final Button capture = (Button) view.findViewById(R.id.button_capture);
    final Button keep = (Button) view.findViewById(R.id.keep_image);
    final Button discard = (Button) view.findViewById(R.id.discard_image);
    keep.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(final View v) {
            if(currentImage != null) {
              saveCurrentImage();
            }
            getActivity().getFragmentManager().popBackStack();
          }
        });
    discard.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(final View v) {
            capture.setVisibility(View.VISIBLE);
            keep.setVisibility(View.INVISIBLE);
            discard.setVisibility(View.INVISIBLE);
            viewfinder.setVisibility(View.VISIBLE);
            camera.startPreview();
          }
        });
    capture.setVisibility(View.VISIBLE);
    keep.setVisibility(View.INVISIBLE);
    discard.setVisibility(View.INVISIBLE);
    capture.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(final View v) {
            camera.stopPreview();
            camera.takePicture(shutterCallback, null, jpegCallback);
            viewfinder.setVisibility(View.INVISIBLE);
            capture.setVisibility(View.INVISIBLE);
            keep.setVisibility(View.VISIBLE);
            discard.setVisibility(View.VISIBLE);
          }
        }
    );
    setupAudio();
    camera.startPreview();
  }

  private final ShutterCallback shutterCallback = new ShutterCallback() {
    @Override
    public void onShutter() {
      soundPool.play(shutterSound, volume, volume, 1, 0, 1f);
    }
  };

  private void saveCurrentImage() {
    try {
      final File imageFile = PhotoManager.createImageFile();
      PhotoManager.saveImage(imageFile, currentImage);
/*      final Intent updateFilmstrip = new Intent(getActivity(),FilmStripFragment.class); //new Intent(MainActivity.SELFIE_NEW);
      updateFilmstrip.putExtra(MediaStore.EXTRA_OUTPUT, imageFile.toURI());
      Log.d(TAG, "saveCurrentImage: broadcasting update:" + imageFile.toURI());
      getActivity().sendOrderedBroadcast(updateFilmstrip, null);*/
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private ImageView createViewfinder() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    final int frameImageId;
    switch(prefs.getString(PREF_KEY_FACEFRAME_POS, "Mid")) {
      case "Near": frameImageId = R.drawable.faceframe_near; break;
      case "Mid": frameImageId = R.drawable.faceframe_mid; break;
      case "Far": frameImageId = R.drawable.faceframe_far; break;
      default: frameImageId = -1;
    }

    final ImageView viewfinder = new ImageView(getActivity());
    if(frameImageId != -1) {
      final Bitmap faceframe = BitmapFactory.decodeResource(getResources(), frameImageId);
      viewfinder.setImageBitmap(faceframe);
    }
    return viewfinder;
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy()");
    super.onPause();
    if (camera != null) {
      camera.stopPreview();
      camera.release(); // release the camera for other applications
      camera = null;
    }
    if (soundPool != null) {
      soundPool.unload(shutterSound);
      soundPool.release();
      soundPool = null;
    }
    if(audioManager != null) {
      audioManager.setSpeakerphoneOn(false);
      audioManager.unloadSoundEffects();
    }
  }

  private final PictureCallback jpegCallback = new PictureCallback() {
    @Override
    public void onPictureTaken(final byte[] data, final Camera camera) {
      currentImage = data;
    }
  };

  private Camera getBestCamera() {
    int front = -1;
    int rear = -1;
    if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      Log.e(TAG, "Current device does not have Camera hardware");
      throw new RuntimeException("No camera hardware");
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      final Camera.CameraInfo info = new Camera.CameraInfo();
      for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
        Camera.getCameraInfo(i, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
          front = i;
        } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
          rear = i;
        }
      }
    }
    final int best = front > -1 ? front : rear;
    if(best >= 0) {
      if(best == rear) {
        //Toast.makeText(getActivity(), getResources().getText(R.string.using_rear_camera), Toast.LENGTH_SHORT).show();
      }
      return Camera.open(best);
    } else {
      return Camera.open();
    }
  }

  private void setupAudio() {
    final Context context = getActivity();
    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    volume = (float) audioManager
        .getStreamVolume(AudioManager.STREAM_MUSIC)
        / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    shutterSound = soundPool.load(context, R.raw.shutter, 1);

    audioManager.setSpeakerphoneOn(true);
    audioManager.loadSoundEffects();
  }

}