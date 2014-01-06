package com.pwc.ittraining.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pwc.ittraining.R;


public class SqliteHelper extends SQLiteOpenHelper {
	private static Context _context;
	private static final int DBVersion = 1;
	private static String DBName;
	private static String pwd;
	
	public SqliteHelper(Context context){		
		super(context,context.getString(R.string.db_name),null,DBVersion);
		//_context = context;
		//DBName = context.getString(R.string.db_name);
		//pwd = Secure.getString(_context.getContentResolver(), Secure.ANDROID_ID);
		//pwd = _context.getString(R.string.pwd_part_1) + _context.getString(R.string.pwd_part_2) + _context.getString(R.string.pwd_part_3);
	}
	
	public SqliteHelper(Context context, String dbName, CursorFactory factory,int version){
		super(context, dbName, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("traininglog","db create");
		
		db.execSQL("CREATE TABLE Document (DocumentID VARCHAR PRIMARY KEY, Title vargraphic, Description vargraphic," +
				"CategoryId int, ContentType int, TabType varchar," +
				"Duration varchar, MediaUri vargraphic, LastModifiedDate timestamp)");
		
		
		db.execSQL("CREATE TABLE DocumentImg (DocumentID VARCHAR PRIMARY KEY," +
				"Img BLOB)");
		
		
		db.execSQL("CREATE TABLE Favorite (DocumentID VARCHAR PRIMARY KEY)");		

		db.execSQL("CREATE TABLE Tags (DocumentID VARCHAR  , Tag vargraphic)");
		

		db.execSQL("CREATE TABLE Category (CategoryId int PRIMARY KEY , CategoryName vargraphic ,CategoryOrder int)");
		
		
		db.execSQL("CREATE TABLE Config (ID integer primary key autoincrement, Key vargraphic, Value vargraphic)");
		
		ContentValues cv = new ContentValues();
		cv.put("Key", "NewsLastUpdateDate");
		cv.put("Value", "2000-01-01 12:00:00");
		db.insert("Config", null, cv);
		
		cv = new ContentValues();
		cv.put("Key", "NewsLastModifiDate");
		cv.put("Value", "2000-01-01 12:00:00");
		db.insert("Config", null, cv);
		
		cv = new ContentValues();
		cv.put("Key", "LearningLastUpdateDate");
		cv.put("Value", "2000-01-01 12:00:00");
		db.insert("Config", null, cv);
		
		cv=new ContentValues();
		cv.put("Key", "PromoteNews");
		cv.put("Value", "");
		db.insert("Config", null, cv);
		
		cv=new ContentValues();
		cv.put("Key", "PromoteLearning");
		cv.put("Value", "");
		db.insert("Config", null, cv);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	
	
}
