package com.pwc.ittraining.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import bean.NewsBean.NewsList.Category;
import bean.NewsBean.NewsList.DeleteDocID;
import bean.NewsBean.NewsList.NewDocument;
import bean.NewsBean.NewsSortList.PopularDocID;

import com.pwc.ittraining.DocumentItem;
import com.pwc.ittraining.R;

public class DocumentHelper {
	public Context context;
	public SqliteHelper dbHelper;
	public boolean flag = false;

	public DocumentHelper(Context appContext) {
		context = appContext;
		dbHelper = new SqliteHelper(appContext,
				appContext.getString(R.string.db_name), null, 1);
	}

	public void updateLastUpdateTime(String documentKey, String lastUpdateTime) {
		if (lastUpdateTime == null || lastUpdateTime.equals(""))
			return;

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("Value", lastUpdateTime);
		db.update("Config", cv, "Key = ?", new String[] { documentKey });

		// db.close();
	}

	public String getLastUpdateTime(String documentKey) {
		String lastUpdateTime = "";

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db.rawQuery("SELECT * FROM Config WHERE Key=?",
				new String[] { documentKey });
		while (c.moveToNext()) {
			lastUpdateTime = c.getString(c.getColumnIndex("Value"));
		}
		c.close();

		// db.close();

		return lastUpdateTime;
	}

	public String getLastModifiDate(String documentKey) {
		String lastUpdateTime = "";

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db.rawQuery("SELECT * FROM Config WHERE Key=?",
				new String[] { documentKey });
		while (c.moveToNext()) {
			lastUpdateTime = c.getString(c.getColumnIndex("Value"));
		}
		c.close();

		// db.close();

		return lastUpdateTime;
	}

	public DocumentItem getPromoteDocument(String documentKey) {
		DocumentItem promoteDocument = null;
		String documentID = "";

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db.rawQuery("SELECT * FROM Config WHERE [Key]=?",
				new String[] { documentKey });

		while (c.moveToNext()) {
			documentID = c.getString(c.getColumnIndex("Value"));
		}
		c.close();

		List<DocumentItem> docs = null;

		docs = getDocuments("News");
		if (docs != null && docs.size() > 0) {

			if (documentID.equals("")) {
				promoteDocument = docs.get(0);
			} else {
				for (int i = 0; i < docs.size(); i++) {
					if (docs.get(i).DocumentID != null
							&& docs.get(i).DocumentID.equals(documentID)) {
						promoteDocument = docs.get(i);
						break;
					} else {
						continue;
					}
				}
			}
		}
		return promoteDocument;
	}

	public DocumentItem getDocument(String documentID) {
		DocumentItem doc = new DocumentItem();
		doc.DocumentID = documentID;

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db.rawQuery("SELECT * FROM Document WHERE DocumentID=?",
				new String[] { documentID });
		// Cursor c = db.rawQuery("SELECT FROM Document join Tags on
		// Document.DocumentID = Tags.DocumentID where Document.DocumentID='1');
		while (c.moveToNext()) {
			doc.DocumentID = c.getString(c.getColumnIndex("DocumentID"));
			doc.Title = c.getString(c.getColumnIndex("Title"));
			doc.Description = c.getString(c.getColumnIndex("Description"));
			doc.LastMofifiedDate = c.getString(c
					.getColumnIndex("LastModifiedDate"));
			// doc.CategoryName = c.getString(c.getColumnIndex("CategoryName"));
			doc.CategoryId = c.getString(c.getColumnIndex("CategoryId"));
			// doc.Tags = c.getString(c.getColumnIndex("Tag"));
			doc.ContentType = c.getInt(c.getColumnIndex("ContentType"));
			doc.TabType = c.getString(c.getColumnIndex("TabType"));
			doc.Duration = c.getString(c.getColumnIndex("Duration"));
			doc.MediaUri = c.getString(c.getColumnIndex("MediaUri"));
		}
		c.close();

		//db.close();

		return doc;
	}

	public List<String> getTags(String searchString, int categoryID) {
		List<String> tags = new ArrayList<String>();

		List<String> whereClauseStringList = new ArrayList<String>();
		if (searchString.isEmpty() == false) {
			whereClauseStringList.add("t.Tag LIKE '%" + searchString + "%'");
		}
		if (categoryID != 0) {
			whereClauseStringList.add("d.CategoryID=" + categoryID);
		}

		StringBuilder sqlStringBuilder = new StringBuilder();
		sqlStringBuilder.append("SELECT tag FROM ");
		sqlStringBuilder
				.append("(SELECT t.Tag AS tag, Count(*) AS count FROM Tags t ");
		sqlStringBuilder
				.append("INNER JOIN Document d ON t.DocumentID = d.DocumentID ");
		if (whereClauseStringList.size() != 0) {
			sqlStringBuilder.append("WHERE ");
			for (int i = 0; i < whereClauseStringList.size(); i++) {
				sqlStringBuilder.append(whereClauseStringList.get(i));

				if (i < whereClauseStringList.size() - 1) {
					sqlStringBuilder.append(" AND ");
				}
			}

		}
		sqlStringBuilder.append(" GROUP BY t.Tag)");
		sqlStringBuilder.append(" ORDER BY count DESC LIMIT 0,20 ");

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db.rawQuery(sqlStringBuilder.toString(), null);
		while (c.moveToNext()) {
			tags.add(c.getString(0));
		}
		c.close();

		return tags;
	}

	public List<DocumentItem> getDocuments(String tabType) {
		List<DocumentItem> docs = new ArrayList<DocumentItem>();

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db
				.rawQuery(
						"SELECT DocumentID, Title, ContentType FROM Document WHERE TabType=? ORDER BY LastModifiedDate DESC",
						new String[] { tabType });

		while (c.moveToNext()) {

			DocumentItem doc = new DocumentItem();
			doc.DocumentID = c.getString(c.getColumnIndex("DocumentID"));
			doc.Title = c.getString(c.getColumnIndex("Title"));
			doc.ContentType = c.getInt(c.getColumnIndex("ContentType"));
			// doc.CtegoryID = c.getInt(c.getColumnIndex("CategoryId"));
			docs.add(doc);
		}
		c.close();

		return docs;
	}

	// Filter by category
	public List<DocumentItem> getAllDocuments(String tabType, int categoryId) {
		List<DocumentItem> docs = new ArrayList<DocumentItem>();

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery(
				"SELECT DocumentID, Title, ContentType FROM Document"
						+ " WHERE TabType=? AND CategoryId=?"
						+ " ORDER BY LastModifiedDate DESC", new String[] {
						tabType, "" + categoryId });

		while (c.moveToNext()) {

			DocumentItem doc = new DocumentItem();
			doc.DocumentID = c.getString(c.getColumnIndex("DocumentID"));
			doc.Title = c.getString(c.getColumnIndex("Title"));
			doc.ContentType = c.getInt(c.getColumnIndex("ContentType"));
			docs.add(doc);
		}
		c.close();

		return docs;
	}

	public List<DocumentItem> getSortDocuments(
			List<PopularDocID> popularDocumentID) {

		StringBuilder sqlStringBuilder = new StringBuilder();

		for (int i = 0; i < popularDocumentID.size(); i++) {
			sqlStringBuilder
					.append("SELECT DocumentID, Title, ContentType FROM Document"
							+ " WHERE DocumentID = '"
							+ popularDocumentID.get(i).ID + "'");
			if (i < popularDocumentID.size() - 1) {
				sqlStringBuilder.append(" UNION ALL ");
			}
		}

		String sqlString = sqlStringBuilder.toString();

		List<DocumentItem> docs = new ArrayList<DocumentItem>();

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery(sqlString, null);

		while (c.moveToNext()) {

			DocumentItem doc = new DocumentItem();
			doc.DocumentID = c.getString(c.getColumnIndex("DocumentID"));
			doc.Title = c.getString(c.getColumnIndex("Title"));
			doc.ContentType = c.getInt(c.getColumnIndex("ContentType"));
			docs.add(doc);
		}
		c.close();

		return docs;
	}

	public List<Category> getAllCategories() {
		List<Category> category = new ArrayList<Category>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db
				.rawQuery(
						"select c.CategoryName,c.CategoryId from category c inner join "
								+ "(select distinct categoryid from document) d on c.CategoryId=d.CategoryId"
								+ " order by c.CategoryOrder ", null);

		while (c.moveToNext()) {

			Category cate = new Category();
			cate.CategoryID = c.getInt(c.getColumnIndex("CategoryId"));
			cate.CategoryName = c.getString(c.getColumnIndex("CategoryName"));
			category.add(cate);
		}
		c.close();
		return category;
	}

	public void saveDocuments(List<NewDocument> newDocumentInfo) {

		if (newDocumentInfo == null || newDocumentInfo.size() == 0)
			return;

		System.out.println("title,,,"+newDocumentInfo.get(0).Title);
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.beginTransaction();

		String insertDocSql = "INSERT OR REPLACE INTO Document VALUES (?, ?, ?, ? , ?, ?, ?, ?, ? )";

//		String insertTagsSql = "INSERT INTO Tags VALUES (?, ?)";

		NewDocument temp;
		for (int i = 0; i < newDocumentInfo.size(); i++) {
			temp = newDocumentInfo.get(i);

			db.execSQL(insertDocSql, new Object[] { temp.DocumentID,
					temp.Title, temp.Description, temp.CategoryID,
					temp.ContentType, "News", temp.Duration, temp.MediaUri,
					temp.LastModifiedDate });

			// insert tags for each document
			saveTags(temp.Tags, temp.DocumentID);
		}

		db.setTransactionSuccessful();
		db.endTransaction();

	}
	
	// insert category
	public void saveCategory(List<Category> newCategories) {
		if (newCategories == null || newCategories.size() == 0)
			return;

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String insertCategorySql = "INSERT OR REPLACE INTO Category VALUES (?, ?, ?)";
		// String deleteCategorySql =
		// "delete from Category where CategoryID = ?";

		Category temp;
		for (int i = 0; i < newCategories.size(); i++) {
			temp = newCategories.get(i);

			// Delete if exists

			// db.execSQL(deleteCategorySql, new Object[] { temp.CategoryId });

			// Insert

			db.execSQL(insertCategorySql, new Object[] { temp.CategoryID,
					temp.CategoryName, temp.CategoryOrder });

		}

	}

	public void deleteDocuments(List<DeleteDocID> documentIDs) {
		if (documentIDs == null || documentIDs.size() == 0)
			return;

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String sql = "delete from Document where DocumentID = ?";
		String deleteImg = "delete from DocumentImg where DocumentID = ?";
		String deleteTag = "delete from Tags where DocumentID = ?";

		for (int i = 0; i < documentIDs.size(); i++) {
			db.execSQL(sql, new Object[] { documentIDs.get(i) });

			db.execSQL(deleteImg, new Object[] { documentIDs.get(i) });

			db.execSQL(deleteTag, new Object[] { documentIDs.get(i) });
		}

		// db.close();
	}

	public void saveTags(String[] tags, String docID) {
		if (tags == null || tags.length == 0)
			return;

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.beginTransaction();

		String sql = "INSERT INTO Tags VALUES (?, ? )";
		String temp;
		for (int i = 0; i < tags.length; i++) {
			temp = tags[i];

			db.execSQL(sql, new Object[] { docID, temp });
		}

		db.setTransactionSuccessful();
		db.endTransaction();

		// db.close();
	}

	public byte[] getDocumentImg(String documentID) {

		byte[] documentImg = null;

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db.rawQuery("SELECT * FROM DocumentImg WHERE DocumentID=?",
				new String[] { documentID });
		while (c.moveToNext()) {
			try {
				documentImg = c.getBlob(c.getColumnIndex("Img"));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		c.close();

		// db.close();

		return documentImg;
	}

	public List<DocumentItem> searchDocument(String searchName, String tabType,
			int categoryId) {
		List<DocumentItem> docs = new ArrayList<DocumentItem>();

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		StringBuilder sqlStringBuilder = new StringBuilder();
		sqlStringBuilder
				.append("SELECT DISTINCT d.DocumentID, d.Title, d.ContentType ");
		sqlStringBuilder.append("FROM Document AS d ");
		sqlStringBuilder.append("INNER JOIN Tags AS t ");
		sqlStringBuilder.append("ON d.DocumentID = t.DocumentID ");
		sqlStringBuilder.append("WHERE (t.Tag LIKE '" + searchName + "%' ");
		sqlStringBuilder.append("OR d.Title LIKE '%" + searchName + "%') ");

		if (categoryId != 0) {
			sqlStringBuilder.append("AND d.CategoryId=" + categoryId + " ");
		}

		sqlStringBuilder.append("ORDER BY d.LastModifiedDate DESC");

		Cursor c = db.rawQuery(sqlStringBuilder.toString(), null);
		while (c.moveToNext()) {
			DocumentItem doc = new DocumentItem();
			doc.DocumentID = c.getString(c.getColumnIndex("DocumentID"));
			doc.Title = c.getString(c.getColumnIndex("Title"));
			doc.ContentType = c.getInt(c.getColumnIndex("ContentType"));

			docs.add(doc);
		}
		c.close();

		// db.close();

		return docs;
	}

	public void saveDocumentImage(String documentID, byte[] pic) {
		if (pic == null)
			return;

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.execSQL("delete from DocumentImg where DocumentID=?",
				new Object[] { documentID });

		ContentValues cv = new ContentValues();

		cv.put("DocumentID", documentID);
		cv.put("Img", pic);

		db.insert("DocumentImg", null, cv);

//		 db.close();
	}

	public void saveFavorites(List<String> favoriteIDs) {
		if (favoriteIDs == null || favoriteIDs.size() == 0)
			return;

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String deleteSql = "delete from Favorite";
		String sql = "INSERT INTO Favorite VALUES (?)";

		db.execSQL(deleteSql);

		for (int i = 0; i < favoriteIDs.size(); i++) {
			db.execSQL(sql, new Object[] { favoriteIDs.get(i) });
		}
	}

	public void addFavorite(String documentID) {
		if (documentID == null || documentID.equals(""))
			return;

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.beginTransaction();

		String sql = "INSERT INTO Favorite VALUES (?)";

		db.execSQL("delete from Favorite where DocumentID=?",
				new Object[] { documentID });

		db.execSQL(sql, new Object[] { documentID });

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public boolean isFavorite(String documentID) {
		boolean isFavorite = false;

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db.rawQuery("SELECT * FROM Favorite WHERE DocumentID=?",
				new String[] { documentID });

		while (c.moveToNext()) {
			isFavorite = true;
		}
		c.close();

		return isFavorite;
	}

	public void deleteFavorite(String documentID) {
		if (documentID == null || documentID.equals(""))
			return;

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String sql = "delete from Favorite where DocumentID = ?";

		db.execSQL(sql, new Object[] { documentID });
	}

	public List<DocumentItem> getFavorites() {
		List<DocumentItem> docs = new ArrayList<DocumentItem>();

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db
				.rawQuery(
						"SELECT Document.DocumentID, Document.Title, Document.ContentType"
								+ " FROM Favorite INNER JOIN Document "
								+ "ON Favorite.DocumentID=Document.DocumentID "
								+ "ORDER BY Document.LastModifiedDate DESC ",
						null);
		while (c.moveToNext()) {
			DocumentItem doc = new DocumentItem();
			doc.DocumentID = c.getString(c.getColumnIndex("DocumentID"));
			doc.Title = c.getString(c.getColumnIndex("Title"));
			doc.ContentType = c.getInt(c.getColumnIndex("ContentType"));
			
			docs.add(doc);
			
		}
		c.close();

		// db.close();

		return docs;
	}

}
