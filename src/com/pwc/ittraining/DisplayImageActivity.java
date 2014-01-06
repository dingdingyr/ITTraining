package com.pwc.ittraining;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.pwc.ittraining.common.PhotoView;
import com.pwc.webmaster.security.Authenticate;

public class DisplayImageActivity extends Activity {
	private String userToken;
	private String documentID;

	private ViewPager mViewPager;
	private SamplePagerAdapter adapter;

	private ArrayList<String> image = new ArrayList<String>();

	private Bitmap bitmap;
	private DisplayImageOptions options;// 　用于设置图片显示的类。
	// 图片异步加载库 universal-image-loader-1.8.4-with-sources.jar
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			adapter.notifyDataSetChanged();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_launcher);

		imageLoader.init(ImageLoaderConfiguration
				.createDefault(getApplicationContext()));
		options = new DisplayImageOptions.Builder()
				.resetViewBeforeLoading()
				.cacheOnDisc()
				// 设置下载的图片是否缓存在SD卡中
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)) // 设置成圆角图片
				.build(); // 创建配置过得DisplayImageOption对象
		// .showStubImage(R.drawable.ic_stub) // 设置图片下载期间显示的图片
		// .cacheInMemory(true) // 设置下载的图片是否缓存在内存中

		setContentView(R.layout.activity_pageview);

		Intent intent = this.getIntent();
		Bundle data = intent.getExtras();
		documentID = (String) data.get("documentid");

		userAuthentication();

		if (!isNetworkConnected(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_unavailable_network), Toast.LENGTH_SHORT).show();
			return;
		}
		
		final String url = getResources().getString(R.string.gtsserviceserver)
				+ "/GetImageListByDocumentID/?documentID=" + documentID;
		
		new Thread() {
			@Override
			public void run() {
				JSONObject jsonObject = doGet(url);
				try {
					JSONArray array = jsonObject.getJSONArray("Data");
					for (int i = 0; i < array.length(); i++) {
						JSONObject jsonItem = array.getJSONObject(i);
						String url1 = (String) jsonItem.get("ImageUri");
						image.add(url1);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				handler.sendEmptyMessage(0);
			}
		}.start();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		adapter = new SamplePagerAdapter();
		mViewPager.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	private boolean isNetworkConnected(Context context) {  
	     if (context != null) {  
	          ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
	                 .getSystemService(Context.CONNECTIVITY_SERVICE);  
	         NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
	         if (mNetworkInfo != null) {  
	             return mNetworkInfo.isAvailable();  
	          }  
	     }  
	     return false;  
	 }
	
	// 网络连接获取数据
	private JSONObject doGet(String url) {
		try {
			
			
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			request.addHeader("Authorization", userToken);
			HttpResponse response = httpClient.execute(request);

			if (response.getStatusLine().getStatusCode() == 200) {
				String result = EntityUtils.toString(response.getEntity());
				JSONObject object = new JSONObject(result);
				return object;
			} else {
				Toast.makeText(getApplicationContext(), "Failed to get data",
						Toast.LENGTH_SHORT).show();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	class SamplePagerAdapter extends PagerAdapter {

		@Override
		public View instantiateItem(ViewGroup container, int position) {

			View imageLayout = View.inflate(getApplicationContext(),
					R.layout.pro_imageshow, null);
			PhotoView photoView = (PhotoView) imageLayout
					.findViewById(R.id.image);
			TextView pagerCount = (TextView) imageLayout
					.findViewById(R.id.txtcount);
			final ProgressBar progressBar = (ProgressBar) imageLayout
					.findViewById(R.id.loading);

			pagerCount.setText((position + 1) + "/" + image.size());

			String imgUrl = getResources().getString(R.string.gtsserviceserver)
					+ "/" + image.get(position);

			imageLoader.displayImage(imgUrl, photoView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							progressBar.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) {
							case IO_ERROR:
								message = "Input/Output error";
								break;
							case DECODING_ERROR:
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED:
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY:
								message = "Out Of Memory error";
								break;
							case UNKNOWN:
								message = "Unknown error";
								break;
							}
							Toast.makeText(getApplicationContext(), message,
									Toast.LENGTH_SHORT).show();

							progressBar.setVisibility(View.GONE);

						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							progressBar.setVisibility(View.GONE);
						}
					});
			((ViewPager) container).addView(imageLayout, 0);

			return imageLayout;
		}

		@Override
		public int getCount() {
			return image.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// container.removeView((View) object);//删除页卡
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

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
	}

	@Override
	protected void onStop() {
		super.onStop();
		image.clear();
		// 释放图片
		if (!bitmap.isRecycled()) {
			bitmap.recycle();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		image.clear();
		// 释放图片
		if (!bitmap.isRecycled()) {
			bitmap.recycle();
		}
	}
}
