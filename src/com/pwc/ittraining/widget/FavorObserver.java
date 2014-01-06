package com.pwc.ittraining.widget;


import com.pwc.ittraining.DocumentItem;


public interface FavorObserver {
	public void cancel(DocumentItem document);
	public void success(DocumentItem document);
}
