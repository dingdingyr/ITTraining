package com.pwc.ittraining;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import bean.NewsBean.NewsList.Category;

public class CategoryAdapter extends BaseAdapter {

	private List<Category> data;
	private Context context;
	public String aString;
	public int selectedID = 0;

	public CategoryAdapter(Context context, List<Category> categories) {
		this.data = categories;
		this.context = context;
	}

	public void refresh(List<Category> categories) {
		this.data = categories;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return data.size() + 1;
	}

	@Override
	public Category getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = View
					.inflate(context, R.layout.v_menu_list_item, null);

			holder = new ViewHolder();

			holder.root = (LinearLayout) convertView
					.findViewById(R.id.menu_list_item_root);

			holder.listTextView = (TextView) convertView
					.findViewById(R.id.list_item_text);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (position == 0) {
			holder.listTextView.setText("All");
			if (selectedID == 0) {
				holder.root.setBackgroundResource(R.color.tangerine);
			} else {
				holder.root.setBackgroundResource(R.color.white);
			}

		} else {
			holder.listTextView.setText(data.get(position - 1).CategoryName);
			if (selectedID == data.get(position - 1).CategoryID) {
				holder.root.setBackgroundResource(R.color.tangerine);
			} else {
				holder.root.setBackgroundResource(R.color.white);
			}
		}
		return convertView;
	}

	public void setSelectedID(int position) {
		selectedID = position;
		notifyDataSetChanged();
	}

	private class ViewHolder {
		public TextView listTextView;
		public View root;
	}

}
