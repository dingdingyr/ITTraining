package com.pwc.ittraining;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pwc.ittraining.common.CommonUtility;
import com.pwc.ittraining.common.DocumentHelper;

public class TrainingListAdapter extends BaseAdapter {

	private Context context;
	private List<DocumentItem> data;
	public HashMap<String, Bitmap> photo_map = new HashMap<String, Bitmap>();
	private ViewHolder holder;
	public int CurrentFirstPosition;
	public int CurrentItemCount;
	private int maxPosition;
	private String userToken;

	private DocumentHelper documentHelper;
	private DocumentItem document;

	public TrainingListAdapter(Context _context, List<DocumentItem> tiList,
			String _userToken) {
		context = _context;
		data = tiList;
		userToken = _userToken;

		notifyDataSetChanged();
		documentHelper = new DocumentHelper(context);
	}

	public void refresh(Activity activity) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.training_list, null);
			holder = new ViewHolder();
			holder.photo = (ImageView) convertView.findViewById(R.id.image_pic);
			holder.photoMark = (ImageView) convertView
					.findViewById(R.id.image_pic_mark);
			holder.title = (TextView) convertView
					.findViewById(R.id.tview_title);
			holder.documentID = (TextView) convertView
					.findViewById(R.id.tview_trainingID);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String documentID = data.get(position).DocumentID;
		document = documentHelper.getDocument(documentID);

		holder.title.setText(data.get(position).Title);
		holder.documentID.setText(documentID);

		if (maxPosition < position)
			maxPosition = position;
		try {
			byte[] img = documentHelper.getDocumentImg(documentID);
			if (img != null) {
				holder.photo.setImageBitmap(CommonUtility.getImage(img));
			} else {
				holder.photo.setImageDrawable(context.getResources()
						.getDrawable(R.drawable.default_pic));
				if (maxPosition <= position) {

					new DisplayImageTask()
							.execute(documentID, holder, position);
				}
			}

			if ((document.ContentType + "").equals(context.getResources()
					.getString(R.string.imagecontenttypecode))) {
				holder.photoMark.setImageResource(R.drawable.type_image);
			} else if ((document.ContentType + "").equals(context
					.getResources().getString(R.string.htmlcontenttypecode))) {
				holder.photoMark.setImageResource(R.drawable.type_html);
			} else {
				holder.photoMark.setImageResource(R.drawable.type_video);
			}

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return convertView;
	}

	static class ViewHolder {
		public ImageView photo;
		public ImageView photoMark;
		public TextView title;
		public TextView documentID;
	}

	private class DisplayImageTask extends AsyncTask<Object, Integer, Boolean> {
		byte[] pic;
		Bitmap bitmap;
		ViewHolder h;

		@Override
		protected Boolean doInBackground(Object... params) {
			String documentID = params[0].toString();

			h = (ViewHolder) params[1];
			int position = Integer.parseInt(params[2].toString());
			pic = getImage(documentID);
			documentHelper.saveDocumentImage(documentID, pic);
			bitmap = CommonUtility.getImage(pic);

			if (position >= CurrentFirstPosition
					&& position <= CurrentFirstPosition + CurrentItemCount
					&& bitmap != null
					&& h.documentID.getText().toString().equals(documentID)) {
				return true;
			}
			return false;
		}

		private byte[] getImage(String documentID) {
			try {
				String url = context.getString(R.string.gtsserviceserver)
						+ "/GetThumbnailByDocumentID/?documentID=[documentID]";

				String SERVER_URL = url.replace("[documentID]", documentID);

				HttpClient dhc = CommonUtility.getNewHttpClient();
				HttpGet request = new HttpGet(SERVER_URL);
				request.setHeader("AUTHORIZATION", userToken);

				HttpResponse httpResponse = dhc.execute(request);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					return EntityUtils.toByteArray((HttpEntity) httpResponse
							.getEntity());
				} else {
					// return null;
				}

				return null;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				return null;
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (result) {
				System.out.println(h.documentID.getText());
				h.photo.setImageBitmap(bitmap);
				notifyDataSetChanged();
			}
		}
	}

}
