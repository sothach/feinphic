package org.nulleins.feinphic;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URI;

public class PhotoViewFragment extends Fragment {

  private static final String TAG = "PVF";
  private ImageView photoViewer;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View result = inflater.inflate(R.layout.photo_viewer, container, false);
    photoViewer = (ImageView) result.findViewById(R.id.photo_viewer);
    return result;
  }

  @Override
  public void onActivityCreated(final Bundle bundle) {
    super.onActivityCreated(bundle);
    final BitmapWorkerTask task = new BitmapWorkerTask(photoViewer);
    final String photoUriString = getArguments().getString(MainActivity.PHOTO_URI);
    task.execute(URI.create(photoUriString));
  }

  class BitmapWorkerTask extends AsyncTask<URI, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;

    public BitmapWorkerTask(final ImageView imageView) {
      // Use a WeakReference to ensure the ImageView can be garbage collected
      imageViewReference = new WeakReference<>(imageView);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(final URI... params) {
      final File imageFile = new File(params[0]);
      Log.d(TAG, "loading bitmap in background thread: " +imageFile.getAbsolutePath());
      return BitmapFactory.decodeFile(imageFile.getPath());
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(final Bitmap bitmap) {
      if (bitmap != null) {
        final ImageView imageView = imageViewReference.get();
        if (imageView != null) {
          imageView.setImageBitmap(bitmap);
        }
      }
    }
  }
}
