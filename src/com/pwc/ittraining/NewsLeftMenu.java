package com.pwc.ittraining;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import bean.NewsBean.NewsList.Category;

public class NewsLeftMenu {

	public static final int TAB_BAR_LISTVIEW = 4;

	public static final int TAB_BAR_NEWESTRELEASE = 0;
	public static final int TAB_BAR_MOSTVIEWED = 1;
	public static final int TAB_BAR_MOSTCOMMENTS = 2;
	public static final int TAB_BAR_MOSTLIKED = 3;

	public static interface OnLeftViewButtonClickListener1 {

		public void onNewestReleaseButtonClick();

		public void onMostViewedButtonClick();

		public void onMostCommentsButtonClick();

		public void onMostLikedButtonClick();

		public void onListViewItemClick();
	}

	private OnLeftViewButtonClickListener1 clickListener;

	private Drawable normalDrawable;
	private Drawable focusDrawable;
	private int focusIndex = TAB_BAR_MOSTVIEWED;

	private View view;
	private int[] idsLayout = new int[] { R.id.menu_newest_release_text,
			R.id.menu_most_viewed_text, R.id.menu_most_comments_text,
			R.id.menu_most_liked_text };

	private NewsActivity activity;
	private Context context;

	public ListView listView;
	public int selectPostion;
	public String lastModifiDateString;
	public int categoryId = 0;
//	public String textContent = "All";

	public CategoryAdapter categoryAdapter;

	public NewsLeftMenu(NewsActivity act) {
		activity = act;
		context = activity.getApplicationContext();

		normalDrawable = context.getResources().getDrawable(
				R.drawable.transparent);
		focusDrawable = context.getResources().getDrawable(R.color.tangerine);

		view = View.inflate(context, R.layout.v_menu_left, null);

		view.findViewById(R.id.menu_newest_release_text).setOnClickListener(
				new OnClickListener() {

					public void onClick(View v) {
						if (clickListener != null) {
							clickListener.onNewestReleaseButtonClick();
						}
					}
				});
		view.findViewById(R.id.menu_most_viewed_text).setOnClickListener(
				new OnClickListener() {

					public void onClick(View v) {
						if (clickListener != null) {
							clickListener.onMostViewedButtonClick();
						}
					}
				});
		view.findViewById(R.id.menu_most_comments_text).setOnClickListener(
				new OnClickListener() {

					public void onClick(View v) {
						if (clickListener != null) {
							clickListener.onMostCommentsButtonClick();
						}
					}
				});
		view.findViewById(R.id.menu_most_liked_text).setOnClickListener(
				new OnClickListener() {

					public void onClick(View v) {
						if (clickListener != null) {
							clickListener.onMostLikedButtonClick();
						}
					}
				});

		listView = (ListView) view.findViewById(R.id.menu_listview);

	}

	public void setOnLeftViewButtonClickListener(
			OnLeftViewButtonClickListener1 listener) {
		this.clickListener = listener;
	}

	public void setFocus(int index) {
		this.focusIndex = index;

		for (int i = 0; i < idsLayout.length; i++) {
			TextView textView = (TextView) view.findViewById(idsLayout[i]);
			if (focusIndex != 5) {
				if (i == focusIndex) {
					textView.setBackgroundDrawable(focusDrawable);
					// textView.setEnabled(false);
				} else {
					textView.setBackgroundDrawable(normalDrawable);
				}
			}

		}
	}

	public int getFocusIndex() {
		return focusIndex;
	}

	public View getView() {
		return view;
	}

	public void bindData(List<Category> categories) {
		categoryAdapter = new CategoryAdapter(context, categories);

		listView.setAdapter(categoryAdapter);
		categoryAdapter.setSelectedID(categoryId);
		final List<Category> cat = categories;
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				if (pos == 0) {
					categoryId = 0;
				} else {
					categoryId = cat.get(pos - 1).CategoryID;
				}
				if (clickListener != null) {
					clickListener.onListViewItemClick();
				}
				categoryAdapter.setSelectedID(categoryId);
				categoryAdapter.refresh(cat);

			}
		});
	}

}
