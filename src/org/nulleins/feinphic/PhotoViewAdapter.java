package org.nulleins.feinphic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.nulleins.feinphic.model.PhotoRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PhotoViewAdapter extends BaseAdapter {

	private ArrayList<PhotoRecord> list = new ArrayList<>();
	private static LayoutInflater inflater = null;
	private final Context context;

	public PhotoViewAdapter(final Context context) {
		this.context = context;
		inflater = LayoutInflater.from(this.context);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(final int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {

		View newView = convertView;
		final ViewHolder holder;
		final PhotoRecord curr = list.get(position);

		if (convertView == null) {
			holder = new ViewHolder();
			newView = inflater.inflate(R.layout.photo_history_view, parent, false);
			holder.photo = (ImageView) newView.findViewById(R.id.photo);
			holder.time = (TextView) newView.findViewById(R.id.time);
			holder.date = (TextView) newView.findViewById(R.id.date);
			newView.setTag(holder);

		} else {
			holder = (ViewHolder) newView.getTag();
		}

		holder.photo.setImageBitmap(curr.getPhoto());
		final Date taken = new Date(curr.getDatetime());
		holder.date.setText(new SimpleDateFormat("dd MMM yyyy").format(taken));
		holder.time.setText("@ "+new SimpleDateFormat("HH:mm").format(taken));

		return newView;
	}

	static class ViewHolder {
		ImageView photo;
		TextView time;
		TextView date;
	}


	public void add(final PhotoRecord listItem) {
		list.add(0,listItem);
		notifyDataSetChanged();
	}

	public ArrayList<PhotoRecord> getList() {
		return list;
	}

	public void removeAllViews() {
		list.clear();
		this.notifyDataSetChanged();
	}
}
