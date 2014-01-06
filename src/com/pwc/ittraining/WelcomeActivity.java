package com.pwc.ittraining;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class WelcomeActivity extends BaseActivity {

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				Intent intent = new Intent();
				intent.setClass(WelcomeActivity.this, HomeActivity.class);
				startActivity(intent);
//				 overridePendingTransition(R.anim.zoom_enter,
//				 R.anim.zoom_exit);
				overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
				finish();
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_welcome);
		handler.sendEmptyMessageDelayed(0, 2000);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!super.authenticationResult)
			return;
	}
}
