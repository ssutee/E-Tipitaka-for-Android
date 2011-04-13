package  com.watnapp.etipitaka;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class BookmarkDBAdapter {
	private static final String DATABASE_NAME = "bookmark.db";
	private static final String DATABASE_TABLE = "bookmark";
	private static final int DATABASE_VERSION = 2;
	
	public static final String KEY_ID = "_id";
	public static final int ID_COL = 0;
	public static final String KEY_LANG = "lang";
	public static final int LANG_COL = 1;
	public static final String KEY_VOLUME = "volumn";
	public static final int VOLUME_COL = 2;
	public static final String KEY_PAGE = "page";
	public static final int PAGE_COL = 3;
	public static final String KEY_ITEM = "item";
	public static final int ITEM_COL = 4;
	public static final String KEY_NOTE = "note";
	public static final int NOTE_COL = 5;
	public static final String KEY_KEYWORDS = "keywords";
	public static final int KEYWORDS_COL = 6;
	
	
	private SQLiteDatabase db;
	private final Context context;
	private BookmarkDBHelper dbHelper;
	
	public static final String DATABASE_CREATE = "create table " + 
		DATABASE_TABLE + 
		" (" + KEY_ID + " integer primary key autoincrement, " +
		KEY_LANG + " text not null, " +
		KEY_VOLUME + " integer not null, " +
		KEY_PAGE + " integer not null, " +
		KEY_ITEM + " integer not null, " +
		KEY_NOTE + " text, " +
		KEY_KEYWORDS + " text);";
	
	public BookmarkDBAdapter(Context _context) {
		context = _context;
		dbHelper = new BookmarkDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public BookmarkDBAdapter open() throws SQLException {
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		db.close();
	}

	public BookmarkItem getEntry(long _rowIndex) throws SQLException {
		Cursor cursor = db.query(true, DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_VOLUME, KEY_PAGE, KEY_ITEM, KEY_NOTE, KEY_KEYWORDS}, 
				KEY_ID + "=" + _rowIndex, 
				null, null, null, null, null);
		
		if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No bookmark items found for row: " + _rowIndex);
		}
		
		String lang = cursor.getString(LANG_COL);
		int volumn = cursor.getInt(VOLUME_COL);
		int page = cursor.getInt(PAGE_COL);
		int item = cursor.getInt(ITEM_COL);
		String note = cursor.getString(NOTE_COL);
		String keywords = cursor.getString(KEYWORDS_COL);
		BookmarkItem result = new BookmarkItem(lang, volumn, page, item, note, keywords);
		return result;
	}
	
	public boolean isDuplicated(BookmarkItem item) {
		String lang = item.getLanguage();
		int v = item.getVolumn();
		int p = item.getPage();
		int i = item.getItem();
		String n = item.getNote();
		String k = item.getKeywords();
	
		String where = String.format("%s='%s' AND %s=%d AND %s=%d AND %s=%d AND %s='%s' AND %s='%s'",
				KEY_LANG, lang,
				KEY_VOLUME, v,
				KEY_PAGE, p,
				KEY_ITEM, i,
				KEY_NOTE, n,
				KEY_KEYWORDS, k);
		
		int count = db.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_LANG, KEY_VOLUME, KEY_PAGE, KEY_ITEM, KEY_NOTE, KEY_KEYWORDS}, 
				where, null, null, null, null).getCount();

		if(count > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public Cursor getAllEntries() {
		return db.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_LANG, KEY_VOLUME, KEY_PAGE, KEY_ITEM, KEY_NOTE, KEY_KEYWORDS}, null, null, null, null, null);
	}

	public Cursor getEntries(String _lang, String _keywords) {
		String where = KEY_LANG + "=" + "'" + _lang + "'" + " AND " + KEY_KEYWORDS + " = " + "'" + _keywords + "'";
		return db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_VOLUME, KEY_PAGE, KEY_ITEM, KEY_NOTE, KEY_KEYWORDS}, 
				where, null, null, null, null);
	}
	
	public Cursor getEntries(String _lang, String _keywords, String sortKey, boolean isDesc) {
		String where = KEY_LANG + "=" + "'" + _lang + "'" + " AND " + KEY_KEYWORDS + " = " + "'" + _keywords + "'";
		String orderby;
		if(isDesc) {
			orderby = sortKey + " DESC, " + KEY_PAGE + " DESC";
		}
		else {
			orderby = sortKey + " ASC,"  + KEY_PAGE + " ASC";
		}		
		
		return db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_VOLUME, KEY_PAGE, KEY_ITEM, KEY_NOTE, KEY_KEYWORDS}, 
				where, null, null, null, orderby);
	}
	
	
	public long insertEntry(BookmarkItem item) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_LANG, item.getLanguage());
		newValues.put(KEY_VOLUME, item.getVolumn());
		newValues.put(KEY_PAGE, item.getPage());
		newValues.put(KEY_ITEM, item.getItem());
		newValues.put(KEY_NOTE, item.getNote());
		newValues.put(KEY_KEYWORDS, item.getKeywords());
		
		return db.insert(DATABASE_TABLE, null, newValues);
	}
	
	public boolean removeEntry(long _rowIndex) {
		return db.delete(DATABASE_TABLE, KEY_ID + "=" + _rowIndex, null) > 0;
	}

	
	public boolean updateEntry(long _rowIndex, BookmarkItem item) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_LANG, item.getLanguage());
		newValues.put(KEY_VOLUME, item.getVolumn());
		newValues.put(KEY_PAGE, item.getPage());
		newValues.put(KEY_ITEM, item.getItem());
		newValues.put(KEY_NOTE, item.getNote());
		newValues.put(KEY_KEYWORDS, item.getKeywords());
		
		String where = KEY_ID + "=" + _rowIndex;
		
		return db.update(DATABASE_TABLE, newValues, where, null) > 0;
	}
	
	private static class BookmarkDBHelper extends SQLiteOpenHelper {
		
		public BookmarkDBHelper(Context context, String name, CursorFactory factory, int version) {
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
						new String[] {KEY_ID, KEY_LANG, KEY_VOLUME, KEY_PAGE, KEY_ITEM, KEY_NOTE}, 
						null, null, null, null, null);
				if(cursor.getCount() > 0 && cursor.moveToFirst()) {
					ContentValues newValues;
					while(!cursor.isAfterLast()) {
						newValues = new ContentValues();
						newValues.put(KEY_LANG, cursor.getString(LANG_COL));
						newValues.put(KEY_VOLUME, cursor.getInt(VOLUME_COL));
						newValues.put(KEY_PAGE, cursor.getInt(PAGE_COL));
						newValues.put(KEY_ITEM, cursor.getInt(ITEM_COL));
						newValues.put(KEY_NOTE, cursor.getString(NOTE_COL));
						newValues.put(KEY_KEYWORDS, "");
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