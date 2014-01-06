package com.pwc.ittraining;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import bean.NewsBean;
import bean.NewsBean.NewsList.Category;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.pwc.ittraining.NewsLeftMenu.OnLeftViewButtonClickListener1;
import com.pwc.ittraining.PullToRefreshListView.OnRefreshListener;
import com.pwc.ittraining.common.CommonUtility;
import com.pwc.ittraining.common.DocumentHelper;
import com.pwc.ittraining.common.SlidingActivityBase;
import com.pwc.ittraining.common.SlidingActivityHelper;
import com.pwc.ittraining.common.SlidingMenu;
import com.pwc.ittraining.common.SlidingMenu.OnClosedListener;
import com.pwc.ittraining.common.SlidingMenu.OnOpenedListener;
import com.pwc.webmaster.security.Authenticate;

public class NewsActivity extends BaseActivity implements SlidingActivityBase {
	private SlidingMenu sm;
	private SlidingActivityHelper mHelper;

	private PullToRefreshListView listView;
	private PullToRefreshListView searchlist;
	private TrainingListAdapter tlAdapter;

	private DocumentHelper documentHelper;
	private List<DocumentItem> pouplarDocID;
	private int categoryId = 0;
	private int currIndex = 0;
	private NewsLeftMenu leftMenu;

	private ProgressBar status;
	private AutoCompleteTextView autoCompleteTextView;
	private ArrayAdapter<String> adapter;
	private ImageView delButton;
	private ImageView searchButton;

	static Authenticate authenticate;
	private String userToken;

	private boolean flag = false;
	private boolean flagImg = false;
	private boolean pullFlag = false;
	private String urlString;
	private NewsBean.NewsList updatedDocument;
	private NewsBean.NewsSortList sortDocument;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				bindDateNoHeader(pouplarDocID, true);
			} else if (msg.what == 0) {
				leftMenu.setFocus(0);
				leftMenu.categoryId = 0;
				leftMenu.categoryAdapter.setSelectedID(leftMenu.categoryId);
				categoryId = leftMenu.categoryId;
				RefreshTask refreshTask = new RefreshTask();
				refreshTask.execute("");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new SlidingActivityHelper(this);
		mHelper.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_news);

		userAuthentication();

		findViewById(R.id.news_title_open).setOnClickListener(
				new OnClickListener() {

					public void onClick(View arg0) {
						hideSoftInput();
						showMenu();
					}
				});

		delButton = (ImageView) findViewById(R.id.delete_btn);
		searchButton = (ImageView) findViewById(R.id.search_btn);

		searchlist = (PullToRefreshListView) findViewById(R.id.listview_search);
		listView = (PullToRefreshListView) findViewById(R.id.listview_news);
		status = (ProgressBar) findViewById(R.id.status);

		autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.txtsearch);

		documentHelper = new DocumentHelper(getApplicationContext());

		RefreshTask refresh = new RefreshTask();
		refresh.execute("");

		leftMenu = new NewsLeftMenu(NewsActivity.this);
		setBehindContentView(getLeftView());

		sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setMode(SlidingMenu.LEFT);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		sm.setOnOpenedListener(new OnOpenedListener() {

			@Override
			public void onOpened() {
			}
		});

		sm.setOnClosedListener(new OnClosedListener() {

			@Override
			public void onClosed() {
			}
		});

		leftMenu.setOnLeftViewButtonClickListener(new OnLeftViewButtonClickListener1() {
			@Override
			public void onNewestReleaseButtonClick() {
				onClickLeftMenu(NewsLeftMenu.TAB_BAR_NEWESTRELEASE);
			}

			@Override
			public void onMostViewedButtonClick() {
				if (!isNetworkConnected(getApplicationContext())) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.error_unavailable_network),
							Toast.LENGTH_SHORT).show();
					return;
				}
				onClickLeftMenu(NewsLeftMenu.TAB_BAR_MOSTVIEWED);
			}

			@Override
			public void onMostCommentsButtonClick() {
				if (!isNetworkConnected(getApplicationContext())) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.error_unavailable_network),
							Toast.LENGTH_SHORT).show();
					return;
				}
				onClickLeftMenu(NewsLeftMenu.TAB_BAR_MOSTCOMMENTS);
			}

			@Override
			public void onMostLikedButtonClick() {
				if (!isNetworkConnected(getApplicationContext())) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.error_unavailable_network),
							Toast.LENGTH_SHORT).show();
					return;
				}
				onClickLeftMenu(NewsLeftMenu.TAB_BAR_MOSTLIKED);
			}

			@Override
			public void onListViewItemClick() {
				if (!isNetworkConnected(getApplicationContext())) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							currIndex = 0;
							leftMenu.setFocus(0);
							status.setVisibility(View.GONE);
						}
					});
				}
				onClickLeftMenu(NewsLeftMenu.TAB_BAR_LISTVIEW);
			}

		});

		leftMenu.setFocus(currIndex);

		autoCompleteTextView
				.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						AutoCompleteTextView view = (AutoCompleteTextView) v;
						if (hasFocus) {
							List<String> string = documentHelper.getTags("",
									categoryId);
							adapter = new ArrayAdapter<String>(
									getApplicationContext(),
									R.layout.v_search_item, string);
							view.setAdapter(adapter);
							view.showDropDown();
						}
					}
				});

		autoCompleteTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (s.length() > 0) {

					delButton.setVisibility(View.VISIBLE);
					delButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							hideSoftInput();
							autoCompleteTextView.setText("");
							currIndex = 0;
							leftMenu.setFocus(currIndex);

							if (categoryId == 0) {
								flagImg = true;
								bindData();
							} else {
								List<DocumentItem> tiList = documentHelper
										.getAllDocuments("News", categoryId);
								bindDateNoHeader(tiList, false);
							}

						}
					});
				} else {
					delButton.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String query = s.toString();
				List<String> string = documentHelper.getTags(query,
						leftMenu.categoryId);
				adapter = new ArrayAdapter<String>(getApplicationContext(),
						R.layout.v_search_item, string);
				autoCompleteTextView.setAdapter(adapter);
			}
		});

		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideSoftInput();
				currIndex = 0;
				leftMenu.setFocus(currIndex);
				String string = autoCompleteTextView.getText().toString()
						.trim();
				if (string == null || string.equals("")) {
					Toast.makeText(getApplicationContext(), "Please input",
							Toast.LENGTH_SHORT).show();
					return;
				} else {
					List<DocumentItem> docs = documentHelper.searchDocument(
							string, "News", leftMenu.categoryId);

					bindDateNoHeader(docs, false);
				}

			}
		});

		searchlist.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				refresh();
			}
		});
		searchlist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TextView tview = (TextView) v
						.findViewById(R.id.tview_trainingID);

				Intent intent = new Intent(NewsActivity.this,
						DetailActivity.class);
				intent.putExtra("TrainingID", tview.getText());
				startActivity(intent);
			}
		});

		listView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				refresh();
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TextView tview = (TextView) v
						.findViewById(R.id.tview_trainingID);
				Intent intent = new Intent(NewsActivity.this,
						DetailActivity.class);
				intent.putExtra("TrainingID", tview.getText());
				startActivity(intent);
			}
		});
	}

	private void refresh() {
		flag = true;
		pullFlag = true;
		leftMenu.setFocus(0);
		currIndex = 0;
		leftMenu.categoryId = 0;
		categoryId = leftMenu.categoryId;
		leftMenu.categoryAdapter.setSelectedID(leftMenu.categoryId);

		RefreshTask refresh = new RefreshTask();
		refresh.execute("");
		autoCompleteTextView.setText("");
	}

	@Override
	protected void onResume() {
		super.onResume();

		userAuthentication();

	}

	public class RefreshTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (flag == false) {
				status.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				String url = getResources()
						.getString(R.string.gtsserviceserver)
						+ "/GetUpdatedNewsDocumentInfoByDateTime/"
						+ "?lastModifiedDateTimeString=[lastupdatetime]";

				String lastUpdateTime = URLEncoder.encode(
						documentHelper.getLastUpdateTime("NewsLastUpdateDate"),
						HTTP.UTF_8);

				String SERVER_URL = url.replace("[lastupdatetime]",
						lastUpdateTime);

				HttpClient dhc = CommonUtility.getNewHttpClient();
				HttpGet request = new HttpGet(SERVER_URL);
				request.setHeader("AUTHORIZATION", userToken);
				HttpResponse httpResponse = dhc.execute(request);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					String result = EntityUtils.toString(httpResponse
							.getEntity());
					return result;
				} else {
					failedToData();
					return "404";
				}

			} catch (final Exception ex) {
				failedToData();
				listRefreshComplete();
				ex.printStackTrace();
				return "error";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			listRefreshComplete();
			if (result.equals("error")) {
				failedToData();
				listRefreshComplete();
				bindData();
				return;
			}

			if (result.equals("404")) {
				failedToData();
				listRefreshComplete();
				bindData();
				return;
			}

			saveData(result);
		}
	}

	private void failedToData() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "Failed to get data",
						Toast.LENGTH_SHORT).show();
				status.setVisibility(View.GONE);
			}
		});
	}

	private void listRefreshComplete() {
		searchlist.onRefreshComplete();
		searchlist.setRefreshable(true);
		listView.onRefreshComplete();
		listView.setRefreshable(true);
	}

	private void saveData(String result) {
		if (result == null || result.equals("")) {
			bindData();
			return;
		}

		try {
			Gson gson = new Gson();

			JsonParser parser = new JsonParser();

			JsonObject jsonObject = parser.parse(result).getAsJsonObject();

			JsonObject jsonArray = jsonObject.getAsJsonObject("Data");

			Type type = new TypeToken<NewsBean.NewsList>() {
			}.getType();

			updatedDocument = gson.fromJson(jsonArray, type);

			// Delete document
			documentHelper.deleteDocuments(updatedDocument.DeleteDocumentID);

			// Update updatetime
			documentHelper.updateLastUpdateTime("NewsLastUpdateDate",
					updatedDocument.UpdateDateTime);

			// Save new documents
			documentHelper.saveDocuments(updatedDocument.NewDocumentInfo);

			// Save category
			documentHelper.saveCategory(updatedDocument.UpdateCategoryList);

		} catch (Exception e) {
			e.printStackTrace();
		}

		bindData();
	}

	private void displayList(boolean showHeader) {

		if (showHeader) {
			searchlist.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		} else {
			searchlist.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}
	}

	private void bindData() {

		List<Category> cts = documentHelper.getAllCategories();

		leftMenu.bindData(cts);

		DocumentItem promote = documentHelper.getPromoteDocument("PromoteNews");
		View mTop = View.inflate(getApplicationContext(),
				R.layout.documents_list, null);

		TextView titleview = (TextView) mTop.findViewById(R.id.title);
		titleview.setBackgroundColor(getResources().getColor(R.color.black));
		titleview.getBackground().setAlpha(150);

		TextView docidview = (TextView) mTop.findViewById(R.id.txtPromoteID);

		if (promote != null) {
			titleview.setText(promote.Title);
			docidview.setText(promote.DocumentID);
			GetImageTask getPromoteDoc = new GetImageTask();
			getPromoteDoc.execute(mTop);

		}

	}

	private void bindDateNoHeader(List<DocumentItem> tiList,
			boolean refreshCategory) {

		if (refreshCategory) {

			List<Category> cts = documentHelper.getAllCategories();

			leftMenu.bindData(cts);
		}

		tlAdapter = new TrainingListAdapter(NewsActivity.this, tiList,
				userToken);
		searchlist.setAdapter(tlAdapter);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				tlAdapter.notifyDataSetChanged();
			}
		});
		tlAdapter.refresh(activityContext);
		displayList(false);
	}

	private class GetImageTask extends AsyncTask<View, Integer, View> {
		private View item;
		private Bitmap img;
		private String documentID;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (flag == false) {
			status.setVisibility(View.VISIBLE);
		}
		}
		@Override
		protected View doInBackground(View... params) {
			byte[] pic;

			item = (View) params[0];

			documentID = ((TextView) item.findViewById(R.id.txtPromoteID))
					.getText().toString();

			pic = documentHelper.getDocumentImg(documentID);

			if (pic == null) {
				pic = getImage(documentID);

			}
			if (pic != null) {
				documentHelper.saveDocumentImage(documentID, pic);
				img = CommonUtility.getLargeImage(pic);

				return params[0];

			} else
				return null;
		}

		private byte[] getImage(String documentID) {
			try {
				String url = getString(R.string.gtsserviceserver)
						+ "/GetThumbnailByDocumentID/?documentID=[documentID]";

				String SERVER_URL = url.replace("[documentID]", documentID);

				HttpClient dhc = CommonUtility.getNewHttpClient();
				HttpGet request = new HttpGet(SERVER_URL);

				request.setHeader("AUTHORIZATION", userToken);

				HttpResponse httpResponse = dhc.execute(request);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();

					return EntityUtils.toByteArray(entity);
				} else {
					return null;
				}
			} catch (Exception ex) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(View result) {

			if (result != null) {

				ImageView v = (ImageView) (result.findViewById(R.id.imgPreview));

				v.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(NewsActivity.this,
								DetailActivity.class);
						intent.putExtra("TrainingID", documentID);
						startActivity(intent);
					}
				});

				v.setImageBitmap(img);
			}

			if (listView.getHeaderViewsCount() == 1) {
				listView.addHeaderView(item);
			}

				DisplayImageTask displayImageTask = new DisplayImageTask();
				displayImageTask.execute("");

		}
	}

	private boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	private void onClickLeftMenu(int index) {
		String lastModifiDate;
		try {
			lastModifiDate = URLEncoder.encode(
					documentHelper.getLastModifiDate("NewsLastUpdateDate"),
					HTTP.UTF_8);

			if (index == 4) {
				index = currIndex;
				leftMenu.categoryAdapter.notifyDataSetChanged();
			}

			if (index == currIndex && categoryId == leftMenu.categoryId) {
				// do nothing
				showContent();
				return;
			}

			categoryId = leftMenu.categoryId;
			currIndex = index;
			leftMenu.setFocus(currIndex);

			if (currIndex == 0) {
				if (categoryId == 0) {
					bindData();
				} else {
					List<DocumentItem> tiList = documentHelper.getAllDocuments(
							"News", categoryId);
					bindDateNoHeader(tiList, false);
				}
				autoCompleteTextView.setText("");
			} else {
				switch (currIndex) {
				case 1:
					urlString = getResources().getString(R.string.most_view)
							+ "/GetMostViewedDocuments/?lastModifiedDateTimeString="
							+ lastModifiDate + "&categoryID=" + categoryId;
					break;
				case 2:
					urlString = getResources().getString(
							R.string.most_commented)
							+ "/GetMostCommentsDocuments/?lastModifiedDateTimeString="
							+ lastModifiDate + "&categoryID=" + categoryId;
					break;
				case 3:
					urlString = getResources().getString(R.string.most_liked)
							+ "/GetMostLikedDocuments/?lastModifiedDateTimeString="
							+ lastModifiDate + "&categoryID=" + categoryId;
					break;
				default:
					break;
				} 
				json();
				autoCompleteTextView.setText("");
			}
			showContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void json() {

		new Thread() {
			@Override
			public void run() {

				try {
					String result = doGet(urlString);

					Gson gson = new Gson();

					JsonParser parser = new JsonParser();

					JsonObject jsonObject = parser.parse(result)
							.getAsJsonObject();

					JsonObject jsonArray = jsonObject.getAsJsonObject("Data");

					Type type = new TypeToken<NewsBean.NewsSortList>() {
					}.getType();

					sortDocument = gson.fromJson(jsonArray, type);

					// Delete document
					documentHelper
							.deleteDocuments(sortDocument.UpdatedDocumentInfo.DeleteDocumentID);

					// Update updatetime
					documentHelper.updateLastUpdateTime("NewsLastUpdateDate",
							sortDocument.UpdatedDocumentInfo.UpdateDateTime);

					// Save new documents
					documentHelper
							.saveDocuments(sortDocument.UpdatedDocumentInfo.NewDocumentInfo);

					// Save category
					documentHelper
							.saveCategory(sortDocument.UpdatedDocumentInfo.UpdateCategoryList);

					// PopuplarDocumentID
					pouplarDocID = documentHelper
							.getSortDocuments(sortDocument.PopularDocumentID);

				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (pouplarDocID.size() == 0) {
					handler.sendMessage(Message.obtain(handler, 0));

				} else {
					handler.sendMessage(Message.obtain(handler, 1));
				}
				
			}
		}.start();

	}

	// 网络连接获取数据
	private String doGet(String url) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				status.setVisibility(View.VISIBLE);
			}
		});

		try {

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			request.addHeader("Authorization", userToken);
			HttpResponse response = httpClient.execute(request);

			if (response.getStatusLine().getStatusCode() == 200) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						status.setVisibility(View.GONE);
					}
				});

				String result = EntityUtils.toString(response.getEntity());

				return result;
			} else {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.error_unavailable_network),
						Toast.LENGTH_SHORT).show();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private class DisplayImageTask extends AsyncTask<Object, Integer, Boolean> {
		byte[] pic;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (flag == false) {
				status.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			if (!pullFlag) {
				List<DocumentItem> tiList = documentHelper.getDocuments("News");
				
				String documentID = "";
				if (tiList.size() > 2) {
					for (int i = 0; i < 2; i++) {
						documentID = tiList.get(i).DocumentID;
						pic = documentHelper.getDocumentImg(documentID);
						if (pic == null) {
							pic = getImage(documentID);
							documentHelper.saveDocumentImage(documentID, pic);
						}
					}
				} else {
					for (int i = 0; i < tiList.size(); i++) {
						documentID = tiList.get(i).DocumentID;
						pic = documentHelper.getDocumentImg(documentID);
						if (pic == null) {
							pic = getImage(documentID);
							documentHelper.saveDocumentImage(documentID, pic);
						}
					}
				}
			}

			return true;
		}

		private byte[] getImage(String documentID) {
			try {

				String url = getResources()
						.getString(R.string.gtsserviceserver)
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
					failedToData();
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
			status.setVisibility(View.GONE);
			List<DocumentItem> tiList = documentHelper.getDocuments("News");
			tlAdapter = new TrainingListAdapter(NewsActivity.this, tiList,
					userToken);
			listView.setAdapter(tlAdapter);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					tlAdapter.notifyDataSetChanged();
				}
			});

			tlAdapter.refresh(activityContext);

			displayList(true);
		}
	}

	private View getLeftView() {
		return leftMenu.getView();
	}

	public void showConent() {
		if (sm != null) {
			sm.showContent();
		}
	}

	public boolean isBlockFlingFinish() {
		return true;
	}

	@Override
	protected boolean onFlingLeftToRight() {
		if (sm.isMenuShowing()) {
			if (sm.isSecondaryMenuShowing()) {
				sm.showContent();
				return true;
			}
		} else {
			showMenu();
			return true;
		}
		return false;
	}

	private void hideSoftInput() { // 隐藏软键盘
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPostCreate(android.os.Bundle)
	 */
	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mHelper.onPostCreate(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#findViewById(int)
	 */
	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v != null)
			return v;
		return mHelper.findViewById(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os
	 * .Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mHelper.onSaveInstanceState(outState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#setContentView(int)
	 */
	@Override
	public void setContentView(int id) {
		setContentView(getLayoutInflater().inflate(id, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#setContentView(android.view.View)
	 */
	@Override
	public void setContentView(View v) {
		setContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#setContentView(android.view.View,
	 * android.view.ViewGroup.LayoutParams)
	 */
	@Override
	public void setContentView(View v, LayoutParams params) {
		super.setContentView(v, params);
		mHelper.registerAboveContentView(v, params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase#
	 * setBehindContentView(int)
	 */
	public void setBehindContentView(int id) {
		setBehindContentView(getLayoutInflater().inflate(id, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase#
	 * setBehindContentView(android.view.View)
	 */
	public void setBehindContentView(View v) {
		setBehindContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase#
	 * setBehindContentView(android.view.View,
	 * android.view.ViewGroup.LayoutParams)
	 */
	public void setBehindContentView(View v, LayoutParams params) {
		mHelper.setBehindContentView(v, params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase#getSlidingMenu
	 * ()
	 */
	public SlidingMenu getSlidingMenu() {
		return mHelper.getSlidingMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase#toggle()
	 */
	public void toggle() {
		mHelper.toggle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase#showAbove()
	 */
	public void showContent() {
		mHelper.showContent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase#showBehind()
	 */
	public void showMenu() {
		mHelper.showMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase#showSecondaryMenu
	 * ()
	 */
	public void showSecondaryMenu() {
		mHelper.showSecondaryMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase#
	 * setSlidingActionBarEnabled(boolean)
	 */
	public void setSlidingActionBarEnabled(boolean b) {
		mHelper.setSlidingActionBarEnabled(b);
	}

}
