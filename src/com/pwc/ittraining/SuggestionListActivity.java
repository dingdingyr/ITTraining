package com.pwc.ittraining;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import com.pwc.webmaster.security.Authenticate;

public class SuggestionListActivity extends BaseActivity {
	private ImageButton postSuggestion;
	private WebView suggestionview;
	private String userToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_suggestion_list);

		postSuggestion = (ImageButton) findViewById(R.id.leavesuggestion);
		postSuggestion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SuggestionListActivity.this,
						SuggestionActivity.class);
				startActivity(intent);
			}
		});

		suggestionview = (WebView) findViewById(R.id.suggestionview);

		final Context myApp = this;
		final class MyWebChromeClient extends WebChromeClient {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				new AlertDialog.Builder(SuggestionListActivity.this)
						.setTitle("Warning").setMessage(message)
						.setPositiveButton("OK", null).show();
				result.cancel();
				return true;
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
			}

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, final JsResult result) {
				new AlertDialog.Builder(myApp)
						.setMessage(message)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.confirm();
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.cancel();
									}
								}).create().show();

				return true;
			}
		}

		suggestionview.setWebChromeClient(new MyWebChromeClient());

		suggestionview.setWebViewClient(new WebViewClient() {

			public boolean shouldOverrideUrlLoading(WebView view,
					String urlConection) {
				Map<String, String> header = new HashMap<String, String>();
				header.put("AUTHORIZATION", userToken);
				view.loadUrl(urlConection, header);
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				view.setVisibility(View.GONE);
				if (!SuggestionListActivity.this.isFinishing()) {
					new AlertDialog.Builder(SuggestionListActivity.this)
							.setCancelable(false).setTitle("Warning")
							.setMessage(R.string.error_unavailable_network)
							.setPositiveButton("OK", new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).show();
				}
			}

			@Override
			public void onReceivedSslError(WebView view,
					android.webkit.SslErrorHandler handler,
					android.net.http.SslError error) {
				handler.proceed();
			};
		});
		setWebView();
	}

	private void setWebView(){
		suggestionview.getSettings().setJavaScriptEnabled(true);
		suggestionview.getSettings().setLoadWithOverviewMode(true);
		Map<String, String> header = new HashMap<String, String>();
		header.put("AUTHORIZATION", userToken);
		suggestionview.loadUrl(
				getResources().getString(R.string.suggestionurl), header);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.suggestion, menu);
		return false;
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
		setWebView();
		
	}

}
