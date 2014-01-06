package com.pwc.ittraining;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.pwc.webmaster.security.Authenticate;

public class HomeActivity extends TabActivity {
	private TabHost tabs;
	private TabHost.TabSpec spec;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		setContentView(R.layout.activity_home);

		tabs = (TabHost) findViewById(android.R.id.tabhost);
		tabs.setup(this.getLocalActivityManager());

		initializeTabs(tabs);

		// 默认选中第一个
		tabs.setCurrentTab(0);
		((ImageView) tabs.getTabWidget().getChildTabViewAt(0)
				.findViewById(R.id.img_tab))
				.setImageResource(R.drawable.icons_news);
		((LinearLayout) tabs.getTabWidget().getChildTabViewAt(0)
				.findViewById(R.id.tab_layout))
				.setBackgroundColor(getResources().getColor(R.color.darkgrey));

		tabs.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				clickTab(tabId);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		Authenticate authenticate = new Authenticate(getApplicationContext(),
				getString(R.string.sso_package_name));
		Boolean authenticationResult = authenticate.appAuthentication(this,
				getLocalClassName(), false);

		if (!authenticationResult)
			return;
	}

	public void initializeTabs(TabHost tabs) {
		spec = tabs.newTabSpec(getString(R.string.title_tab1));
		spec.setContent(new Intent(this, NewsActivity.class));
		spec.setIndicator(createTabView(getString(R.string.title_tab1),
				getResources().getDrawable(R.drawable.icons_news)));
		tabs.addTab(spec);

		spec = tabs.newTabSpec(getString(R.string.title_tab2));
		spec.setContent(new Intent(this, FavoriteActivity.class));
		spec.setIndicator(createTabView(getString(R.string.title_tab2),
				getResources().getDrawable(R.drawable.icons_learning)));
		tabs.addTab(spec);

		spec = tabs.newTabSpec(getString(R.string.title_tab3));
		spec.setContent(new Intent(this, OutageActivity.class));
		spec.setIndicator(createTabView(getString(R.string.title_tab3),
				getResources().getDrawable(R.drawable.icons_category)));
		tabs.addTab(spec);

		spec = tabs.newTabSpec(getString(R.string.title_tab4));
		spec.setContent(new Intent(this, SuggestionListActivity.class));
		spec.setIndicator(createTabView(getString(R.string.title_tab4),
				getResources().getDrawable(R.drawable.icons_suggestion)));

		tabs.addTab(spec);
	}

	private void clickTab(String tabID) {

		if (tabID == getString(R.string.title_tab1)) {
			tabs.setCurrentTab(0);
			// ((ImageView) tabs.getTabWidget().getChildTabViewAt(0)
			// .findViewById(R.id.img_tab))
			// .setImageResource(R.drawable.icons_news);
			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(0)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(
							R.color.darkgrey));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(1)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(2)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(3)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));
		} else if (tabID == getString(R.string.title_tab2)) {
			tabs.setCurrentTab(1);
			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(0)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(1)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(
							R.color.darkgrey));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(2)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(3)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));
		} else if (tabID == getString(R.string.title_tab3)) {
			tabs.setCurrentTab(2);
			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(0)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(1)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(2)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(
							R.color.darkgrey));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(3)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));
		} else if (tabID == getString(R.string.title_tab4)) {
			tabs.setCurrentTab(3);
			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(0)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(1)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(2)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(R.color.orange));

			((LinearLayout) tabs.getTabWidget().getChildTabViewAt(3)
					.findViewById(R.id.tab_layout))
					.setBackgroundColor(getResources().getColor(
							R.color.darkgrey));
		} else
			return;
	}

	private View createTabView(String text, Drawable icon) {

		Typeface ariAlTF = Typeface.createFromAsset(getAssets(),
				"fonts/arial.ttf");
		View view = LayoutInflater.from(this).inflate(R.layout.tab_indicator,
				null);
		TextView tv = (TextView) view.findViewById(R.id.tv_tab);
		ImageView img = (ImageView) view.findViewById(R.id.img_tab);

		img.setContentDescription(text);
		img.setImageDrawable(icon);
		tv.setText(text);
		tv.setTypeface(ariAlTF);

		img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageView curBtn = (ImageView) v;

				String des = curBtn.getContentDescription().toString();

				clickTab(des);
			}
		});

		return view;
	}
	
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        return false;  
    }  
}
