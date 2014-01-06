package com.pwc.ittraining;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pwc.ittraining.NewsActivity.RefreshTask;
import com.pwc.ittraining.PullToRefreshListView.OnRefreshListener;
import com.pwc.ittraining.common.CommonUtility;
import com.pwc.ittraining.common.DocumentHelper;
import com.pwc.webmaster.security.Authenticate;

public class FavoriteActivity extends BaseActivity {

	private PullToRefreshListView listView;
//	private ListView searchlist;
	private TrainingListAdapter tlAdapter;

	private DocumentHelper documentHelper;

	private ProgressBar status;
	private boolean flag = false;

	static Authenticate authenticate;
	private String userToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_favorite);

//		searchlist = (ListView) findViewById(R.id.listview_search);
		listView = (PullToRefreshListView) findViewById(R.id.listview_learning);
		status = (ProgressBar) findViewById(R.id.status);

//		searchlist.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View v,
//					int position, long id) {
//				TextView tview = (TextView) v
//						.findViewById(R.id.tview_trainingID);
//				Intent intent = new Intent(FavoriteActivity.this,
//						DetailActivity.class);
//				intent.putExtra("TrainingID", tview.getText());
//				startActivity(intent);
//			}
//		});
//
//		searchlist.setOnScrollListener(new OnScrollListener() {
//			@Override
//			public void onScroll(AbsListView view, int arg1, int arg2, int arg3) {
//				if (tlAdapter != null) {
//					tlAdapter.CurrentFirstPosition = arg1;
//					tlAdapter.CurrentItemCount = arg2;
//				}
//			}
//
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//			}
//
//		});
		listView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				flag = true;
				RefreshTask refresh = new RefreshTask();
				refresh.execute("");
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TextView tview = (TextView) v
						.findViewById(R.id.tview_trainingID);
				Intent intent = new Intent(FavoriteActivity.this,
						DetailActivity.class);
				intent.putExtra("TrainingID", tview.getText());
				startActivity(intent);
			}
		});

//		listView.setOnScrollListener(new OnScrollListener() {
//			@Override
//			public void onScroll(AbsListView view, int arg1, int arg2, int arg3) {
//				if (tlAdapter != null) {
//					tlAdapter.CurrentFirstPosition = arg1;
//					tlAdapter.CurrentItemCount = arg2;
//				}
//			}
//
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//			}
//
//		});

		authenticate = new Authenticate(getApplicationContext(),
				getString(R.string.sso_package_name));
		try {
			userToken = authenticate.getDecryptedCredential();
		} catch (Exception e) {
			e.printStackTrace();
		}

		documentHelper = new DocumentHelper(getApplicationContext());
		RefreshTask refresh = new RefreshTask();
		refresh.execute("");
		
		
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!super.authenticationResult)
			return;

		if (documentHelper == null) {
			documentHelper = new DocumentHelper(getApplicationContext());
		}
		flag = true;
		RefreshTask refreshTask = new RefreshTask();
		refreshTask.execute("");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
//		FavorManager.removeObserver(observer);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}

	public class RefreshTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!flag) {
				status.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				Log.d("traininglog", "load training");

				String url = getResources()
						.getString(R.string.gtsserviceserver)
						+ "/GetAllFavoriteByStaffID";

				HttpClient dhc = CommonUtility.getNewHttpClient();
				HttpGet request = new HttpGet(url);

				request.addHeader("Authorization", userToken);

				HttpResponse httpResponse = dhc.execute(request);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();

					return EntityUtils.toString(entity);

				} else {
					return "404";
				}

			} catch (Exception ex) {
				return "error";
			}
		}

		@Override
		protected void onPostExecute(String result) {

			listView.onRefreshComplete();
			listView.setRefreshable(true);
			if (result.equals("error")) {
				bindData();
				return;
			}

			if (result.equals("404")) {
				bindData();
				return;
			}
			saveData(result);
		}
	}

	private void saveData(String result) {
		if (result == null || result.equals("")) {
			bindData();
			return;
		}

		List<String> favoriteIDs = new ArrayList<String>();
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
								reader.beginObject();
								tagName = reader.nextName();

								temp = getReader(reader);
								reader.endObject();

								favoriteIDs.add(temp);
							}
							reader.endArray();
						} else
							temp = getReader(reader);
					} else {
						temp = getReader(reader);
					}
				}
			}
			reader.endObject();
		} catch (Exception e) {
			e.printStackTrace();
		}

		documentHelper.saveFavorites(favoriteIDs);
//		FavorManager.addObserver(observer);
		bindData();
		
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

	private void bindData() {
		List<DocumentItem> docs = new ArrayList<DocumentItem>();

		docs = documentHelper.getFavorites();
//		Collections.reverse(docs);
		tlAdapter = new TrainingListAdapter(FavoriteActivity.this, docs,
				userToken);
		listView.setAdapter(tlAdapter);

		status.setVisibility(View.GONE);
	}

	
	public class AddFavorite extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
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
//				documentHelper.addFavorite(ti.DocumentID);
			}
		}

	}

	public class DeleteFavorite extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
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
			}

		}

	}

//	private FavorObserver observer = new FavorObserver() {
//
//		@Override
//		public void cancel(DocumentItem document) {
//			List<DocumentItem> docs = documentHelper.getFavorites();
//			for (int i = 0; i < docs.size(); i++) {
//				if (docs.get(i).DocumentID == document.DocumentID || docs.get(i).DocumentID.equals(document.DocumentID)) {
//					documentHelper.deleteFavorite(document.DocumentID);
//					break;
//				}
//			}
//			handler.post(new Runnable() {
//
//				@Override
//				public void run() {
//					tlAdapter.notifyDataSetChanged();
//				}
//			});
//		}
//
//		@Override
//		public void success(DocumentItem document) {
//			documentHelper.addFavorite(document.DocumentID);
//			handler.post(new Runnable() {
//
//				@Override
//				public void run() {
//					tlAdapter.notifyDataSetChanged();
//				}
//			});
//		}
//	};
	
}
