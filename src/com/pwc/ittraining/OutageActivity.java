package com.pwc.ittraining;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.pwc.webmaster.security.Authenticate;

public class OutageActivity extends BaseActivity {
	private WebView outageview;
	private ProgressBar progressbar;
	private String userToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_outage);

		outageview = (WebView) findViewById(R.id.outageview);
		progressbar = (ProgressBar) findViewById(R.id.progressbar);
		userAuthentication();
		final Context myApp = this;
		final class MyWebChromeClient extends WebChromeClient {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				new AlertDialog.Builder(OutageActivity.this)
						.setTitle("Warning").setMessage(message)
						.setPositiveButton("OK", null).show();
				result.cancel();
				return true;
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress == 100)
					progressbar.setVisibility(View.GONE);
				else
					progressbar.setVisibility(View.VISIBLE);
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

		outageview.setWebChromeClient(new MyWebChromeClient());

		outageview.setWebViewClient(new WebViewClient() {

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
				if (!OutageActivity.this.isFinishing()) {
					new AlertDialog.Builder(OutageActivity.this)
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

		outageview.getSettings().setJavaScriptEnabled(true);
		outageview.getSettings().setLoadWithOverviewMode(true);
		Map<String, String> header = new HashMap<String, String>();
		header.put("AUTHORIZATION", userToken);
		outageview
				.loadUrl(getResources().getString(R.string.outageurl), header);

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

		if (!super.authenticationResult)
			return;
		userAuthentication();

	}

}
