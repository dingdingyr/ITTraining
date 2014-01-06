package com.pwc.ittraining;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.pwc.ittraining.common.CommonUtility;
import com.pwc.webmaster.security.Authenticate;

public class SuggestionActivity extends Activity {
	private Button btnSend;
	private ImageButton imgbtnReturn;
	private EditText txtTitle;
	private EditText txtSuggestion;
	private ProgressBar progress;

	static Authenticate authenticate;
	private String userToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_suggestion);
		userAuthentication();
		imgbtnReturn = (ImageButton) findViewById(R.id.btn_return);
		imgbtnReturn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btnSend = (Button) findViewById(R.id.btnpost);
		txtTitle = (EditText) findViewById(R.id.ettitle);
		txtSuggestion = (EditText) findViewById(R.id.etsuggestion);
		progress = (ProgressBar) findViewById(R.id.progress);

		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = txtTitle.getText().toString().trim();
				String s = txtSuggestion.getText().toString().trim();

				if (s == null || s.equals("") || title == null
						|| title.equals("")) {
					new AlertDialog.Builder(SuggestionActivity.this)
							.setMessage("Title or suggestion cannot be empty!")
							.setPositiveButton("OK", null).show();

					return;
				}

				String[] params = new String[2];
				params[0] = s;
				params[1] = title;

				PostCommentTask post = new PostCommentTask();
				post.execute(params);
			}
		});

		authenticate = new Authenticate(getApplicationContext(),
				getString(R.string.sso_package_name));

		try {
			userToken = authenticate.getDecryptedCredential();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.suggestion, menu);
		return false;
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
		try {
			userToken = authenticate.getDecryptedCredential();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class PostCommentTask extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {

			String comments = params[0];
			String title = params[1];

			String url = getResources().getString(R.string.gtsserviceserver)
					+ "/PostSuggestion/";

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
				parameters.add(new BasicNameValuePair("Subject", title));
				parameters.add(new BasicNameValuePair("SuggestionText",
						comments));

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
			progress.setVisibility(View.GONE);

			new AlertDialog.Builder(SuggestionActivity.this)
					.setMessage("Thanks for your suggestion!")
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).show();
		}
	}
}
