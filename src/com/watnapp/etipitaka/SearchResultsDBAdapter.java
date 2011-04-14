package com.watnapp.etipitaka;

import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class SearchResultsDBAdapter {
	private static final String DATABASE_NAME = "results.db";
	private static final String DATABASE_TABLE = "results";
	private static final int DATABASE_VERSION = 4;
	
	public static final String KEY_ID = "_id";
	public static final int ID_COL = 0;

	public static final String KEY_LANG = "lang";
	public static final int LANG_COL = 1;
	
	public static final String KEY_KEYWORDS = "keywords";
	public static final int KEYWORDS_COL = 2;
			
	public static final String KEY_PAGES = "pages";
	public static final int PAGES_COL = 3;
	
	public static final String KEY_SUTS = "suts";
	public static final int SUTS_COL = 4;	
	
	public static final String KEY_SEL_CATE = "scate";
	public static final int SEL_CATE_COL = 5;	
	
	public static final String KEY_CONTENT = "content";
	public static final int CONTENT_COL = 6;
	
	public static final String KEY_PRIMARY_CLICKED = "pclicked";
	public static final int PRIMARY_CLIKCED_COL = 7;
	
	public static final String KEY_SECONDARY_CLICKED = "sclicked";
	public static final int SECONDARY_CLIKCED_COL = 8;
	
	public static final String KEY_SAVED = "saved";
	public static final int SAVED_COL = 9;
	
	public static final String KEY_MARKED = "marked";
	public static final int MARKED_COL = 10;

	
	private SQLiteDatabase db;
	private final Context context;
	private SearchResultsDBHelper dbHelper;	

	public static final String DATABASE_CREATE = "create table " + 
		DATABASE_TABLE + 
		" (" + KEY_ID + " integer primary key autoincrement, " +
		KEY_LANG + " text not null, " +
		KEY_KEYWORDS + " text not null, " +
		KEY_PAGES + " text not null, " +
		KEY_SUTS + " text not null, " +
		KEY_SEL_CATE + " text not null, " +
		KEY_CONTENT + " text not null, " +
		KEY_PRIMARY_CLICKED + " text not null, " +		
		KEY_SECONDARY_CLICKED + " text not null, " + 
		KEY_SAVED + " text not null, " +
		KEY_MARKED + " text not null);";		
	
	public SearchResultsDBAdapter(Context _context) {
		context = _context;
		dbHelper = new SearchResultsDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public SearchResultsDBAdapter open() throws SQLException {
		db = dbHelper.getWritableDatabase();
		return this;
	}	
	
	public void close() {
		db.close();
	}
	
	public boolean isDuplicated(SearchResultsItem item) {
		String where = String.format("%s='%s' AND %s='%s' AND %s='%s' AND %s='%s'", 
				KEY_LANG, item.getLanguage(),
				KEY_KEYWORDS, item.getKeywords(),
				KEY_SEL_CATE, item.getSelectedCategories(),
				KEY_SUTS, item.getSuts());
		
		int count = db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_PAGES, KEY_SUTS, KEY_SEL_CATE, KEY_CONTENT, 
					KEY_PRIMARY_CLICKED, KEY_SECONDARY_CLICKED, KEY_SAVED, KEY_MARKED}, 
				where, null, null, null, null).getCount();
		
		if(count > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public SearchResultsItem getEntry(long _rowIndex) {
		Cursor cursor = db.query(true, DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_PAGES, KEY_SUTS, KEY_SEL_CATE, KEY_CONTENT, 
					KEY_PRIMARY_CLICKED, KEY_SECONDARY_CLICKED, KEY_SAVED, KEY_MARKED}, 
				KEY_ID + "=" + _rowIndex, 
				null, null, null, null, null);		
		
		if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No bookmark items found for row: " + _rowIndex);
		}
		
		String lang = cursor.getString(LANG_COL);
		String keywords = cursor.getString(KEYWORDS_COL);
		String sCate = cursor.getString(SEL_CATE_COL);
		String pages = cursor.getString(PAGES_COL);
		String suts = cursor.getString(SUTS_COL);
		String content = cursor.getString(CONTENT_COL);
		String pClicked = cursor.getString(PRIMARY_CLIKCED_COL);
		String sClicked = cursor.getString(SECONDARY_CLIKCED_COL);
		String saved = cursor.getString(SAVED_COL);
		String marked = cursor.getString(MARKED_COL);
		
		SearchResultsItem result = new SearchResultsItem(lang, keywords, pages, suts, sCate, content);
		result.setPrimaryClicked(pClicked);
		result.setSecondaryClicked(sClicked);
		result.setSaved(saved);
		result.setMarked(marked);
		
		return result;
	}
	
	public Cursor getAllEntries() {
		return db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_PAGES, KEY_SUTS, KEY_SEL_CATE, KEY_CONTENT, 
					KEY_PRIMARY_CLICKED, KEY_SECONDARY_CLICKED, KEY_SAVED, KEY_MARKED}, 
				null, null, null, null, null);		
	}
	
	public Cursor getEntries(String _lang, String keywords, String sCate) {
		String where = KEY_LANG + "=" + "'" + _lang + "'" + 
			" AND " + KEY_KEYWORDS + "=" + "'" + keywords + "'" +
			" AND " + KEY_SEL_CATE + "=" + "'" + sCate + "'";
		return db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_PAGES, KEY_SUTS, KEY_SEL_CATE, KEY_CONTENT, 
					KEY_PRIMARY_CLICKED, KEY_SECONDARY_CLICKED, KEY_SAVED, KEY_MARKED}, 
				where, null, null, null, null);		
	}
	
	public boolean removeEntry(long _rowIndex) {
		return db.delete(DATABASE_TABLE, KEY_ID + "=" + _rowIndex, null) > 0;
	}
	
	public void removeAllEntries() {
		Cursor cursor = getAllEntries();
		cursor.moveToFirst();
		int rowId;
		while(!cursor.isAfterLast()) {
			rowId = cursor.getInt(ID_COL);
			removeEntry(rowId);
			cursor.moveToNext();
		}
		cursor.close();
	}
	
	
	public int updateEntry(long _rowIndex, SearchResultsItem item) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_LANG, item.getLanguage());
		newValues.put(KEY_KEYWORDS, item.getKeywords());
		newValues.put(KEY_PAGES, item.getPages());
		newValues.put(KEY_SUTS, item.getSuts());
		newValues.put(KEY_SEL_CATE, item.getSelectedCategories());
		newValues.put(KEY_CONTENT, item.getContent());
		newValues.put(KEY_PRIMARY_CLICKED, item.getPrimaryClicked());
		newValues.put(KEY_SECONDARY_CLICKED, item.getSecondaryClicked());
		newValues.put(KEY_SAVED, item.getSaved());
		newValues.put(KEY_MARKED, item.getMarked());
		
		String where = KEY_ID + " = " + _rowIndex;
		
		return db.update(DATABASE_TABLE, newValues, where, null);
	}
	
	public long insertEntry(SearchResultsItem item) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_LANG, item.getLanguage());
		newValues.put(KEY_KEYWORDS, item.getKeywords());
		newValues.put(KEY_PAGES, item.getPages());
		newValues.put(KEY_SUTS, item.getSuts());
		newValues.put(KEY_SEL_CATE, item.getSelectedCategories());
		newValues.put(KEY_CONTENT, item.getContent());
		newValues.put(KEY_PRIMARY_CLICKED, item.getPrimaryClicked());
		newValues.put(KEY_SECONDARY_CLICKED, item.getSecondaryClicked());
		newValues.put(KEY_SAVED, item.getSaved());
		newValues.put(KEY_MARKED, item.getMarked());
		
		return db.insert(DATABASE_TABLE, null, newValues);		
	}
	
	private static class SearchResultsDBHelper extends SQLiteOpenHelper {
		
		public SearchResultsDBHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("UPGRADE", oldVersion+" --> "+newVersion);
			if(oldVersion == 1) {
				ArrayList<ContentValues> tmp = new ArrayList<ContentValues>();
				Cursor cursor = db.query(DATABASE_TABLE, 
						new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_PAGES, KEY_SUTS, KEY_SEL_CATE, KEY_CONTENT, 
							KEY_PRIMARY_CLICKED, KEY_SECONDARY_CLICKED}, 
						null, null, null, null, null);		
				if(cursor.getCount() > 0 && cursor.moveToFirst()) {
					ContentValues newValues;
					String emtryListString = "";
					try {
						emtryListString = Utils.toStringBase64(new ArrayList<String>());
					} catch(IOException e) {
						e.printStackTrace();
					}
					while(!cursor.isAfterLast()) {
						newValues = new ContentValues();
						newValues.put(KEY_LANG, cursor.getString(LANG_COL));
						newValues.put(KEY_KEYWORDS, cursor.getString(KEYWORDS_COL));
						newValues.put(KEY_PAGES, cursor.getString(PAGES_COL));
						newValues.put(KEY_SUTS, cursor.getString(SUTS_COL));
						newValues.put(KEY_SEL_CATE, cursor.getString(SEL_CATE_COL));
						newValues.put(KEY_CONTENT, cursor.getString(CONTENT_COL));
						newValues.put(KEY_PRIMARY_CLICKED, cursor.getString(PRIMARY_CLIKCED_COL));
						newValues.put(KEY_SECONDARY_CLICKED, cursor.getString(SECONDARY_CLIKCED_COL));
						newValues.put(KEY_SAVED, emtryListString);
						newValues.put(KEY_MARKED, emtryListString);
						
						tmp.add(newValues);
						cursor.moveToNext();
					}
				}
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
				onCreate(db);	
				for(ContentValues values : tmp) {
					db.insert(DATABASE_TABLE, null, values);
				}
			}
			else if(oldVersion == 2 || oldVersion == 3) {
				ArrayList<ContentValues> tmp = new ArrayList<ContentValues>();
				Cursor cursor = db.query(DATABASE_TABLE, 
						new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_PAGES, KEY_SUTS, KEY_SEL_CATE, KEY_CONTENT, 
							KEY_PRIMARY_CLICKED, KEY_SECONDARY_CLICKED, KEY_SAVED}, 
						null, null, null, null, null);		
				if(cursor.getCount() > 0 && cursor.moveToFirst()) {
					ContentValues newValues;
					String emtryListString = "";
					try {
						emtryListString = Utils.toStringBase64(new ArrayList<String>());
					} catch(IOException e) {
						e.printStackTrace();
					}					
					while(!cursor.isAfterLast()) {
						newValues = new ContentValues();
						newValues.put(KEY_LANG, cursor.getString(LANG_COL));
						newValues.put(KEY_KEYWORDS, cursor.getString(KEYWORDS_COL));
						newValues.put(KEY_PAGES, cursor.getString(PAGES_COL));
						newValues.put(KEY_SUTS, cursor.getString(SUTS_COL));
						newValues.put(KEY_SEL_CATE, cursor.getString(SEL_CATE_COL));
						newValues.put(KEY_CONTENT, cursor.getString(CONTENT_COL));
						newValues.put(KEY_PRIMARY_CLICKED, cursor.getString(PRIMARY_CLIKCED_COL));
						newValues.put(KEY_SECONDARY_CLICKED, cursor.getString(SECONDARY_CLIKCED_COL));
						newValues.put(KEY_SAVED, cursor.getString(SAVED_COL));
						newValues.put(KEY_MARKED, emtryListString);
						
						tmp.add(newValues);
						cursor.moveToNext();
					}
				}
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
				onCreate(db);	
				for(ContentValues values : tmp) {
					db.insert(DATABASE_TABLE, null, values);
				}
			}
		}
		
	}		
	
}