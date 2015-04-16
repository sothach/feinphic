package org.nulleins.feinphic;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import org.nulleins.feinphic.model.PhotoRecord;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FeinPhicActivity extends ListActivity {

  private static final String TAG = "FPA";
  private static final int THUMBNAIL_SIZE = 120;
  private PhotoViewAdapter viewAdapter;
  private String currentImagePath;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final ListView photoListView = getListView();
    viewAdapter = new PhotoViewAdapter(getApplicationContext());
    setListAdapter(viewAdapter);
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
        dispatchTakePictureIntent();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      final URI imageUri = URI.create("file:" + currentImagePath);
      final Bitmap photo = getPreview(imageUri);
      final File imageFile = new File(currentImagePath);
      viewAdapter.add(new PhotoRecord(photo, imageFile.lastModified(), "", imageUri));
      galleryAddPic(Uri.parse(imageUri.toString()));
    }
  }

  static final int REQUEST_IMAGE_CAPTURE = 1;

  private void dispatchTakePictureIntent() {
    final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      try {
        final String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
          final File imageFile = PhotoManager.createImageFile();
          currentImagePath = imageFile.getPath();
          takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        }
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
      } catch (final IOException ex) {
        Log.w(TAG, "Unable to store photo: " + ex.getMessage());
      }
    }
  }

  private void galleryAddPic(final Uri contentUri) {
    final Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    mediaScanIntent.setData(contentUri);
    this.sendBroadcast(mediaScanIntent);
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

}
