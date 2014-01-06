package com.pwc.ittraining;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.pwc.ittraining.common.CommonUtility;
import com.pwc.ittraining.common.JsonUtility;
import com.pwc.webmaster.security.Authenticate;

public class CommentsActivity extends Activity {
	private ListView lvComments;
	private ProgressBar progress;
	private TextView tvComments;
	private EditText etComments;
	private Button btnPost;

	static Authenticate authenticate;
	private String userToken;

	private String trainingID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window win = getWindow();
		win.requestFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_comments);

		win.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.commentstitle);

		authenticate = new Authenticate(getApplicationContext(),
				getString(R.string.sso_package_name));

		ImageButton back = (ImageButton) findViewById(R.id.btn_return);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}

		});

		tvComments = (TextView) findViewById(R.id.tvComments);
		lvComments = (ListView) findViewById(R.id.listview_comments);
		progress = (ProgressBar) findViewById(R.id.progress);

		etComments = (EditText) findViewById(R.id.etleavecomment);
		btnPost = (Button) findViewById(R.id.btnpost);

		progress.setVisibility(View.VISIBLE);

		Intent intent = this.getIntent();
		Bundle data = intent.getExtras();
		trainingID = (String) data.get("TrainingID");

		GetCommentsTask task = new GetCommentsTask();
		task.execute(trainingID);

		btnPost.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String comment = etComments.getText().toString().trim();
				if (comment.equals(""))
					return;

				String[] params = new String[] { trainingID, comment };
				PostCommentTask postTask = new PostCommentTask();
				postTask.execute(params);
			}
		});

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
	}

	private void userAuthentication() {
		Authenticate authenticate = new Authenticate(getApplicationContext(),
				getString(R.string.sso_package_name));
		authenticate.appAuthentication(this, getLocalClassName(), true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.comments, menu);
		return false;
	}

	public class PostCommentTask extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {

			String trainingID = params[0];
			String comments = params[1];

			String url = getResources().getString(R.string.gtsserviceserver)
					+ "/PostComments/";

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
				parameters
						.add(new BasicNameValuePair("DocumentID", trainingID));
				parameters.add(new BasicNameValuePair("Comments", comments));

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
			GetCommentsTask task = new GetCommentsTask();
			task.execute(trainingID);
		}
	}

	public class GetCommentsTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				String documentID = params[0];

				String url = getResources()
						.getString(R.string.gtsserviceserver)
						+ "/GetCommentsByDocumentID/?documentID=[documentID]";

				String SERVER_URL = url.replace("[documentID]", documentID);

				HttpClient dhc = CommonUtility.getNewHttpClient();
				HttpGet request = new HttpGet(SERVER_URL);
				request.setHeader("AUTHORIZATION", userToken);
				dhc.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
				dhc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
						60000);

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
			progress.setVisibility(View.GONE);
			etComments.setText("");
			etComments.clearFocus();
			tvComments.requestFocus();

			if (result == null || result.equals("")) {
				return;
			}

			JsonUtility ju = new JsonUtility();
			Map<String, String> m = ju.parseSingleObject(result);

			String returnCode = m.get("ReturnCode");
			if (returnCode == null || returnCode.equals("")
					|| returnCode.equals("-1")) {
				return;
			}

			List<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
			try {
				String temp = "";
				String tagName = "";
				JsonReader reader = new JsonReader(new StringReader(result));
				reader.beginObject();
				while (reader.hasNext()) {
					while (reader.hasNext()) {
						tagName = reader.nextName();
						if (tagName.toLowerCase().equals("data")) {
							if (reader.peek() == JsonToken.BEGIN_ARRAY) {
								reader.beginArray();
								while (reader.hasNext()) {
									if (reader.peek() == JsonToken.BEGIN_OBJECT) {
										reader.beginObject();
										Map<String, Object> comment = new HashMap<String, Object>();
										while (reader.hasNext()) {
											tagName = reader.nextName();
											comment.put(tagName,
													getReader(reader));
										}
										reader.endObject();
										comments.add(comment);
									}
								}
								reader.endArray();
							}
						} else
							temp = getReader(reader);
					}
				}
				reader.endObject();

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (comments.size() > 0) {
				SimpleAdapter adapter = new SimpleAdapter(
						CommentsActivity.this, comments,
						R.layout.comments_list, new String[] { "StaffName",
								"CreatedDate", "CommentsText" },
						new int[] { R.id.staffname, R.id.createddate,
								R.id.comments });

				lvComments.setAdapter(adapter);
				CommonUtility.setListViewHeightBasedOnChildren(lvComments);

				tvComments.setText(comments.size() + " comments");
			} else
				tvComments.setText("0 comments");
		}

		private String getReader(JsonReader reader) {
			try {
				if (reader.peek() == JsonToken.NULL) {
					reader.nextNull();
					return "Null";
				} else
					return reader.nextString();
			} catch (Exception ex) {
				ex.printStackTrace();
				return "Null";
			}

		}
	}

}
