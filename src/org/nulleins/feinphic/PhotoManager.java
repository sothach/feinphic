package org.nulleins.feinphic;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class PhotoManager {

  private static final String PICTURE_DIR = "feinphic";

  static File getAlbumStorageDir() {
    // Get the directory for the user's public pictures directory
    final File file = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES), PICTURE_DIR);
    return file;
  }

  static void saveImage(final File pictureFile, final byte[] data) throws IOException {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(pictureFile);
      fos.write(data);
    } finally {
      if(fos != null) {
        fos.close();
      }
    }
  }

  static File[] sortedPhotoFiles() {
    final File[] pictureDir = PhotoManager.getAlbumStorageDir().listFiles();
    final File[] result = Arrays.copyOf(pictureDir, pictureDir.length);
    Arrays.sort(result, new Comparator<File>() {
      public int compare(final File first, final File second) {
        return Long.valueOf(first.lastModified()).compareTo(second.lastModified());
      }
    });
    return result;
  }

  static File createImageFile() throws IOException {
    final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    final String imageFileName = "FPA_" + timeStamp + ".jpg";
    final File file = new File(PhotoManager.getAlbumStorageDir(),imageFileName);
    if(!file.createNewFile()) {
      throw new IOException("failed to create image file: " + file.getAbsolutePath());
    }
    return file;
  }

  static void deletePhotos() {
    for(final File picture : PhotoManager.getAlbumStorageDir().listFiles()) {
      picture.delete();
    }
  }

}
