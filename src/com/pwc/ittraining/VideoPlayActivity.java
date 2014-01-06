package com.pwc.ittraining;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.pwc.webmaster.security.Authenticate;

public class VideoPlayActivity extends Activity {

	public final static String EXTRA_MESSAGE = "com.example.videoviewtest.MESSAGE";
	public CustomVideoView videoView;
	private String videoUri;

	private ProgressBar mProgressBar;

	private int initialWidth = 0;
	private int initialHeight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

		setContentView(R.layout.activity_video_play);

		mProgressBar = (ProgressBar) findViewById(R.id.status);
		mProgressBar.setVisibility(View.VISIBLE);

		Intent intent = this.getIntent();
		Bundle data = intent.getExtras();
		videoUri = (String) data.get("videouri");

		Display display = getWindowManager().getDefaultDisplay();
		Point p = new Point();
		display.getSize(p);
		initialWidth = p.x;
		initialHeight = p.y;

		videoView = (CustomVideoView) findViewById(R.id.videoView);
		Uri video = Uri.parse(videoUri);
		videoView.setMediaController(new MediaController(this));
		videoView.setVideoURI(video);
		videoView.setDimensions(initialWidth, initialHeight);
		videoView.requestFocus();

		videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mProgressBar.setVisibility(View.GONE);
				videoView.start();
			}
		});
		videoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				showAlertMsg(VideoPlayActivity.this, "Can't play this video.");
				return true;
			}
		});
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
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		System.out.println("Configuration Changed");
		Configuration c = getResources().getConfiguration();

		if (c.orientation == Configuration.ORIENTATION_PORTRAIT) {
			videoView.setDimensions(16, 9);
			videoView.getHolder().setFixedSize(16, 9);

		} else if (c.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			videoView.setDimensions(9, 16);
			videoView.getHolder().setFixedSize(9, 16);
		}
	}

	private void showAlertMsg(Activity activity, String msg) {
		if (!activity.isFinishing()) {
			new AlertDialog.Builder(activity)
					// .setTitle("Warning")
					.setMessage(msg)
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).show();
		}
	}
}
