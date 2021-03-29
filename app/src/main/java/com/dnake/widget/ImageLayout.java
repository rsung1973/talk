package com.dnake.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

public class ImageLayout extends LinearLayout {

	public ImageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setAdapter(BaseAdapter adapter) {
		this.removeAllViews();

		for (int i = 0; i < adapter.getCount(); i++) {
			View view = adapter.getView(i, null, null);
			view.setPadding(8, 0, 8, 0);
			this.addView(view);
		}
	}
}
