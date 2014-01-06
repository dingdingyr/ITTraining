package com.pwc.ittraining;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pwc.ittraining.common.CommonUtility;
import com.pwc.ittraining.common.DocumentHelper;
import com.pwc.ittraining.common.FileUtility;
import com.pwc.ittraining.common.JsonUtility;
import com.pwc.ittraining.widget.FavorManager;
import com.pwc.webmaster.security.Authenticate;

public class DetailActivity extends Activity {
	private WebView detail;
	private DocumentItem ti;
	private ImageView imgPreview;
	private ImageButton btnPlay;
	private TextView title;
	private TextView about;
	private ProgressBar progress;
	private ImageButton btnDownload;
	private ImageButton btnDelete;

	private ProgressDialog pb;
	private DownloadVideo dd;
	private FileUtility fileUtility;

	private String documentID;
	private String userToken;

	private ImageView favorite;
	private ImageView unfavorite;
	private ImageView like;
	private ImageView dislike;

	private DocumentHelper documentHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window win = getWindow();
		win.requestFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_detail);

		win.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.learningdetailtitle);

		favorite = (ImageView) findViewById(R.id.favorite);
		unfavorite = (ImageView) findViewById(R.id.unfavorite);
		like = (ImageView) findViewById(R.id.like);
		dislike = (ImageView) findViewById(R.id.dislike);

		favorite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DeleteFavorite delete = new DeleteFavorite();
				delete.execute(ti.DocumentID);
			}
		});
		unfavorite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AddFavorite add = new AddFavorite();
				add.execute(ti.DocumentID);
			}
		});

		like.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PostDislike dislike = new PostDislike();
				dislike.execute(ti.DocumentID);
			}
		});

		dislike.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PostLike like = new PostLike();
				like.execute(ti.DocumentID);
			}
		});

		progress = (ProgressBar) findViewById(R.id.progress);

		Intent intent = this.getIntent();
		Bundle data = intent.getExtras();
		documentID = data.getString("TrainingID");

		ImageButton back = (ImageButton) findViewById(R.id.btn_return);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}

		});

		ImageButton comments = (ImageButton) findViewById(R.id.comments);
		comments.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(DetailActivity.this,
						CommentsActivity.class);
				intent.putExtra("TrainingID", documentID);
				startActivity(intent);
			}
		});

		TextView gtstv = (TextView) findViewById(R.id.text);
		gtstv.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}

		});

		detail = (WebView) findViewById(R.id.detailView);

		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		btnPlay = (ImageButton) findViewById(R.id.play);

		btnPlay.getBackground().setAlpha(150);

		documentHelper = new DocumentHelper(getApplicationContext());

		ti = documentHelper.getDocument(documentID);

		byte[] pic = documentHelper.getDocumentImg(documentID);

		if (pic == null) {
			GetImage getimage = new GetImage();
			getimage.execute(documentID);
		} else {
			Bitmap thumbnail = CommonUtility.getLargeImage(pic);
			imgPreview.setImageBitmap(thumbnail);
		}

		detail.getSettings().setJavaScriptEnabled(true);

		detail.loadDataWithBaseURL(null, ti.Description, "text/html", "utf-8",
				null);

		btnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if ((ti.ContentType + "")
						.equals(getString(R.string.videocontenttypecode))) {
					Intent intent = new Intent(DetailActivity.this,
							VideoPlayActivity.class);
					if (fileUtility.isFileExist("Download", ti.DocumentID
							+ ".mp4"))
						intent.putExtra("videouri", fileUtility.getFilePath(
								"Download", ti.DocumentID + ".mp4"));
					else
						intent.putExtra("videouri",
								getString(R.string.videoserver) + "/"
										+ ti.MediaUri);

					startActivity(intent);
				} else if ((ti.ContentType + "")
						.equals(getString(R.string.imagecontenttypecode))) {
					Intent intent = new Intent(DetailActivity.this,
							DisplayImageActivity.class);
					intent.putExtra("documentid", ti.DocumentID);
					startActivity(intent);
				} else {
					return;
				}
			}
		});

		title = (TextView) findViewById(R.id.detailtitle);
		title.setText(ti.Title);

		title.setBackgroundColor(getResources().getColor(R.color.black));
		title.getBackground().setAlpha(150);

		about = (TextView) findViewById(R.id.about);

		// edited by Fox to display user friendly date time format
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MMM/yyyy");
		try {
			Date postdate = dateFormat.parse(ti.LastMofifiedDate);
			about.setText("Posted: " + displayFormat.format(postdate));
		} catch (ParseException e) {
			about.setText("Posted: " + ti.LastMofifiedDate);
			e.printStackTrace();
		}

		btnDownload = (ImageButton) findViewById(R.id.download);
		btnDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pb.show();
				dd = new DownloadVideo();

				String[] params = new String[] { ti.DocumentID, ti.MediaUri };
				dd.execute(params);
			}
		});

		btnDelete = (ImageButton) findViewById(R.id.delete);
		btnDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(DetailActivity.this)
						.setTitle("Are you sure to delete this video?")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										if (fileUtility.isFileExist("Download",
												ti.DocumentID + ".mp4")) {
											fileUtility.deleteFileInSDCard(
													"Download", ti.DocumentID
															+ ".mp4");

											btnDelete.setVisibility(View.GONE);
											btnDownload
													.setVisibility(View.VISIBLE);
										}
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).show();
			}
		});
	}

	private void userAuthentication() {
		Authenticate authenticate = new Authenticate(getApplicationContext(),
				getString(R.string.sso_package_name));
		authenticate.appAuthentication(this, getLocalClassName(), true);
		try {
			userToken = authenticate.getDecryptedCredential();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		userAuthentication();
		GetLikeStatus likes = new GetLikeStatus();
		likes.execute(ti.DocumentID);
		if (documentHelper == null) {
			documentHelper = new DocumentHelper(getApplicationContext());
		}

		pb = new ProgressDialog(this);
		pb.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pb.setMessage("Downloading...");
		pb.setCancelable(false);
		pb.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dd.cancel(true);
						dialog.dismiss();
					}
				});

		fileUtility = new FileUtility();

		String fileName = ti.DocumentID + ".mp4";

		if (fileUtility.isFileExist("Download", fileName)) {
			btnDelete.setVisibility(View.VISIBLE);
			btnDownload.setVisibility(View.GONE);
		} else {
			btnDelete.setVisibility(View.GONE);
			btnDownload.setVisibility(View.VISIBLE);
		}

		if ((ti.ContentType + "")
				.equals(getString(R.string.imagecontenttypecode))) {
			btnDelete.setVisibility(View.GONE);
			btnDownload.setVisibility(View.GONE);
		}

		if ((ti.ContentType + "")
				.equals(getString(R.string.htmlcontenttypecode))) {
			btnDelete.setVisibility(View.GONE);
			btnDownload.setVisibility(View.GONE);
			btnPlay.setVisibility(View.GONE);
		}

		if (documentHelper.isFavorite(ti.DocumentID)) {
			favorite.setVisibility(View.VISIBLE);
			unfavorite.setVisibility(View.INVISIBLE);
		} else {
			favorite.setVisibility(View.INVISIBLE);
			unfavorite.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.detail, menu);
		return false;
	}

	private File fileExists(String fileName, File file) {
		File exists = null;
		for (int i = 0; i < file.listFiles().length; i++) {
			File temp = file.listFiles()[i];
			if (temp.getName().contains(fileName)) {
				exists = temp;
				break;
			}

		}
		return exists;
	}

	public class PostLike extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String documentID = params[0];

			String url = getResources().getString(R.string.gtsserviceserver)
					+ "/PostLike/";

			try {
				HttpClient dhc = CommonUtility.getNewHttpClient();
				HttpPost request = new HttpPost(url);

				request.setHeader("AUTHORIZATION", userToken);
				request.setHeader("Content-type",
						"application/x-www-form-urlencoded");

				dhc.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
				dhc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
						60000);

				List parameters = new ArrayList();
				parameters.add(new BasicNameValuePair("", documentID));

				request.setEntity(new UrlEncodedFormEntity(parameters,
						HTTP.UTF_8));

				HttpResponse httpResponse = dhc.execute(request);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();
					return EntityUtils.toString(entity);
				}
				return null;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null && !result.equals("")) {
				like.setVisibility(View.VISIBLE);
				dislike.setVisibility(View.INVISIBLE);
			}
			progress.setVisibility(View.GONE);
		}

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}

	}

	public class PostDislike extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String documentID = params[0];

			String url = getResources().getString(R.string.gtsserviceserver)
					+ "/PostDislike/";

			try {
				HttpClient dhc = CommonUtility.getNewHttpClient();
				HttpPost request = new HttpPost(url);

				request.setHeader("AUTHORIZATION", userToken);
				request.setHeader("Content-type",
						"application/x-www-form-urlencoded");

				dhc.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
				dhc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
						60000);

				List parameters = new ArrayList();
				parameters.add(new BasicNameValuePair("", documentID));

				request.setEntity(new UrlEncodedFormEntity(parameters,
						HTTP.UTF_8));

				HttpResponse httpResponse = dhc.execute(request);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();
					return EntityUtils.toString(entity);
				}
				return null;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null && !result.equals("")) {
				like.setVisibility(View.INVISIBLE);
				dislike.setVisibility(View.VISIBLE);
			}
			progress.setVisibility(View.GONE);
		}

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}

	}

	public class AddFavorite extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
			String documentID = params[0];

			String url = getResources().getString(R.string.gtsserviceserver)
					+ "/PostFavorite/";

			try {
				HttpClient dhc = CommonUtility.getNewHttpClient();
				HttpPost request = new HttpPost(url);

				request.setHeader("AUTHORIZATION", userToken);
				request.setHeader("Content-type",
						"application/x-www-form-urlencoded");

				dhc.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
				dhc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
						60000);

				List parameters = new ArrayList();
				parameters.add(new BasicNameValuePair("", documentID));

				request.setEntity(new UrlEncodedFormEntity(parameters,
						HTTP.UTF_8));

				HttpResponse httpResponse = dhc.execute(request);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();
					return EntityUtils.toString(entity);
				}
				return null;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null && !result.equals("")) {
				documentHelper.addFavorite(ti.DocumentID);				
				favorite.setVisibility(View.VISIBLE);
				unfavorite.setVisibility(View.INVISIBLE);
			}
			FavorManager.successFavorManager(ti);
			progress.setVisibility(View.GONE);
		}

	}

	public class DeleteFavorite extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
			String documentID = params[0];

			String url = getResources().getString(R.string.gtsserviceserver)
					+ "/PostUnFavorite/";

			try {
				HttpClient dhc = CommonUtility.getNewHttpClient();
				HttpPost request = new HttpPost(url);

				request.setHeader("AUTHORIZATION", userToken);
				request.setHeader("Content-type",
						"application/x-www-form-urlencoded");

				dhc.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
				dhc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
						60000);

				List parameters = new ArrayList();
				parameters.add(new BasicNameValuePair("", documentID));

				request.setEntity(new UrlEncodedFormEntity(parameters,
						HTTP.UTF_8));

				HttpResponse httpResponse = dhc.execute(request);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();
					return EntityUtils.toString(entity);
				}
				return null;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null && !result.equals("")) {
				documentHelper.deleteFavorite(ti.DocumentID);
				favorite.setVisibility(View.INVISIBLE);
				unfavorite.setVisibility(View.VISIBLE);
			}

			FavorManager.cancelFavorManager(ti);
			progress.setVisibility(View.GONE);
		}

	}

	public class GetLikeStatus extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				System.out.println("get like status");

				String url = getResources()
						.getString(R.string.gtsserviceserver)
						+ "/GetLikeStatus/" + "?documentID=[DocumentID]";

				String documentID = params[0];

				String SERVER_URL = url.replace("[DocumentID]", documentID);

				HttpClient dhc = CommonUtility.getNewHttpClient();
				HttpGet request = new HttpGet(SERVER_URL);

				request.addHeader("Authorization", userToken);

				HttpResponse httpResponse = dhc.execute(request);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();
					String aaa = EntityUtils.toString(entity);
					
					return aaa;
				} else {
					return "404";
				}
			} catch (Exception ex) {
				return "error";
			}

		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null || result.equals("") || result.equals("error")
					|| result.equals("404")) {
				like.setVisibility(View.INVISIBLE);
				dislike.setVisibility(View.VISIBLE);
				return;
			}

			JsonUtility ju = new JsonUtility();
			Map<String, String> m = ju.parseSingleObject(result);

			String returnCode = m.get("ReturnCode");
			if (returnCode == null || returnCode.equals("")
					|| returnCode.equals("-1")) {
				like.setVisibility(View.INVISIBLE);
				dislike.setVisibility(View.INVISIBLE);
				return;
			}

			String data = m.get("Data").toString();

			if (data.toLowerCase().equals("true")) {
				like.setVisibility(View.VISIBLE);
				dislike.setVisibility(View.INVISIBLE);
			} else {
				like.setVisibility(View.INVISIBLE);
				dislike.setVisibility(View.VISIBLE);
			}

		}

		@Override
		protected void onPreExecute() {
		}
	}

	public class GetImage extends AsyncTask<String, Integer, byte[]> {

		@Override
		protected byte[] doInBackground(String... params) {
			String documentID = params[0];

			String url = getString(R.string.gtsserviceserver)
					+ "/GetThumbnailByDocumentID/?documentID=" + documentID;

			byte[] result = null;
			URL myFileUrl = null;
			try {
				myFileUrl = new URL(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) myFileUrl.openConnection();
				conn.setRequestProperty("Authorization", userToken);
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();

				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				byte[] data = new byte[1024];
				int count;
				while ((count = is.read(data)) >= 0) {
					outStream.write(data, 0, count);
				}

				data = null;
				result = outStream.toByteArray();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (conn != null)
					conn.disconnect();
			}
			return result;
		}

		@Override
		protected void onPostExecute(byte[] result) {
			if (result == null)
				return;

			DocumentHelper documentHelper = new DocumentHelper(
					getApplicationContext());
			documentHelper.saveDocumentImage(documentID, result);

			Bitmap thumbnail = CommonUtility.getImage(result);
			imgPreview.setImageBitmap(thumbnail);

		}
	}

	class DownloadVideo extends AsyncTask<String, Integer, String> {
		@Override
		protected void onProgressUpdate(Integer... values) {
			pb.setProgress(values[0]);
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				String path = Environment.getExternalStorageDirectory()
						+ "/Download";
				File folder = new File(path);
				File existFile = fileExists(params[0] + ".mp4", folder);

				if (existFile != null) {
					return params[0] + ".mp4";
				} else {
					URL myurl = new URL(getString(R.string.videoserver) + "/"
							+ params[1]);
					URLConnection conn = myurl.openConnection();
					conn.connect();

					InputStream is = conn.getInputStream();

					File file = fileUtility.createFileInSDCard("Download",
							params[0] + ".mp4");
					FileOutputStream fos = new FileOutputStream(file);

					Integer length = conn.getContentLength();

					byte buff[] = new byte[1024 * 4];
					int sum = 0;
					while (true) {
						int numread = is.read(buff);
						if (numread <= 0)
							break;
						sum += numread;

						publishProgress((int) ((sum * 100) / length));

						fos.write(buff, 0, numread);
					}
					is.close();
					return params[0] + ".mp4";
				}

			} catch (Exception ex) {
				Log.getStackTraceString(ex);
				return "0";
			}

		}

		@Override
		protected void onPostExecute(String result) {
			try {
				pb.dismiss();

				if (result == null || result.equals("0")) {
					new AlertDialog.Builder(DetailActivity.this)
							.setTitle("Warning")
							.setMessage(
									getString(R.string.error_unavailable_network))
							.setPositiveButton("OK", null).show();
					return;
				}
				if (fileUtility.isFileExist("Download", result)) {
					btnDelete.setVisibility(View.VISIBLE);
					btnDownload.setVisibility(View.GONE);
				} else {
					btnDelete.setVisibility(View.GONE);
					btnDownload.setVisibility(View.VISIBLE);
				}
			} catch (Exception ex) {

			}
		}
	}

}
