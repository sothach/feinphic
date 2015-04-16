package org.nulleins.feinphic;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import org.nulleins.feinphic.model.PhotoRecord;

import java.io.File;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.Gravity.*;

public class FilmStripFragment extends ListFragment {

  private static final String TAG = "FSF";
  private static final int THUMBNAIL_SIZE = 120;
  private PhotoViewAdapter viewAdapter;

  public FilmStripFragment() {
    setRetainInstance(true);
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewAdapter = new PhotoViewAdapter(getActivity());
    setListAdapter(viewAdapter);
  }

  @Override
  public void onResume() {
    super.onResume();
    final int count = loadExistingPhotos();
    if(count == 0) {
      final View toastRoot = getActivity().getLayoutInflater().inflate(R.layout.tip_dialog, null);
      final Toast toast = new Toast( getActivity());
      toast.setView(toastRoot);
      toast.setGravity(CENTER_HORIZONTAL | CENTER_VERTICAL, 0, 0);
      toast.setDuration(Toast.LENGTH_LONG);
      toast.show();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
        final PhotoRecord item = (PhotoRecord) viewAdapter.getItem(position);
        final Intent viewPhoto = new Intent(getActivity(), PhotoViewFragment.class);
        final Bundle b = new Bundle();
        b.putString(MainActivity.PHOTO_URI, item.getStorage().toString());
        viewPhoto.putExtras(b);
        Log.d(TAG, "publishing view intent for : " + item.getStorage());

        final Fragment photoViewer = new PhotoViewFragment();
        photoViewer.setArguments(b);
        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, photoViewer)
            .addToBackStack(null)
            .commit();
      }
    });
    getListView().setDividerHeight(0);
    setSelection(0);
  }

  private static final Pattern FILE_PATTERN = Pattern.compile("FPA_\\d{8}_\\d{6}\\.jpg");
  private int loadExistingPhotos() {
    int result = 0;
    final Matcher matcher = FILE_PATTERN.matcher("");
    viewAdapter.removeAllViews();
    for(final File imageFile : PhotoManager.sortedPhotoFiles()) {
      matcher.reset(imageFile.getName());
      if(matcher.matches()) {
        final Bitmap photo = getPreview(imageFile.toURI());
        viewAdapter.add(new PhotoRecord(photo, imageFile.lastModified(), "", imageFile.toURI()));
        result++;
      }
    }
    return result;
  }

  private Bitmap getPreview(final URI uri) {
    final File image = new File(uri);
    final BitmapFactory.Options bounds = new BitmapFactory.Options();
    bounds.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(image.getPath(), bounds);
    if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
      return null;

    final int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
        : bounds.outWidth;

    final BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inSampleSize = originalSize / THUMBNAIL_SIZE;
    return BitmapFactory.decodeFile(image.getPath(), opts);
  }

  public void reset() {
    if(viewAdapter != null) {
      viewAdapter.removeAllViews();
    }
  }
}
