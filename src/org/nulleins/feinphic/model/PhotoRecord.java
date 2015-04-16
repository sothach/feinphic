package org.nulleins.feinphic.model;

import android.graphics.Bitmap;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoRecord {
  private final Bitmap photo;
  private final long datetime;
  private final String place;
  private final URI storage;

  public PhotoRecord(final Bitmap photo, final long datetime, final String place, final URI storage) {
    this.photo = photo;
    this.datetime = datetime;
    this.place = place;
    this.storage = storage;
  }

  public Bitmap getPhoto() {
    return photo;
  }

  public long getDatetime() {
    return datetime;
  }

  public String formatDatetime() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(datetime));
  }

  public String getPlace() {
    return place;
  }
  public URI getStorage() {
    return storage;
  }

}
