package com.pwc.ittraining.widget;

import java.util.ArrayList;

import com.pwc.ittraining.DocumentItem;

public class FavorManager {
	private static ArrayList<FavorObserver> observers = new ArrayList<FavorObserver>();

	public static void cancelFavorManager(DocumentItem document) {
		for (int i = 0; i < observers.size(); i++) {
			observers.get(i).cancel(document);
		}
	}

	public static void successFavorManager(DocumentItem document) {
		for (int i = 0; i < observers.size(); i++) {
			observers.get(i).success(document);
		}
	}

	public static void addObserver(FavorObserver observer) {
		observers.add(observer);
	}

	public static void removeObserver(FavorObserver observer) {
		observers.remove(observer);
	}
}
