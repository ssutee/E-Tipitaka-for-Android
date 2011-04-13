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

public class SearchHistoryDBAdapter {
	private static final String DATABASE_NAME = "history.db";
	private static final String DATABASE_TABLE = "history";
	private static final int DATABASE_VERSION = 6;
	
	public static final String KEY_ID = "_id";
	public static final int ID_COL = 0;

	public static final String KEY_LANG = "lang";
	public static final int LANG_COL = 1;
	
	public static final String KEY_KEYWORDS = "keywords";
	public static final int KEYWORDS_COL = 2;
	
	public static final String KEY_N_PAGE = "npage";
	public static final int N_PAGE_COL = 3;
	
	public static final String KEY_N_SUT = "nsut";
	public static final int N_SUT_COL = 4;
	
	public static final String KEY_SEL_CATE = "scate";
	public static final int SEL_CATE_COL = 5;
	
	public static final String KEY_LINE1 = "line1";
	public static final int LINE1_COL = 6;

	public static final String KEY_LINE2 = "line2";
	public static final int LINE2_COL = 7;
	
	public static final String KEY_FREQ = "freq";
	public static final int FREQ_COL = 8;

	public static final String KEY_PRIORITY = "priority";
	public static final int PRIORITY_COL = 9;
	
	public static final String KEY_CODE = "code";
	public static final int CODE_COL = 10;
	
	private SQLiteDatabase db;
	private final Context context;
	private SearchHistoryDBHelper dbHelper;
	
	
	public static final String DATABASE_CREATE = "create table " + 
		DATABASE_TABLE + 
		" (" + KEY_ID + " integer primary key autoincrement, " +
		KEY_LANG + " text not null, " +
		KEY_KEYWORDS + " text not null, " +
		KEY_N_PAGE + " integer not null, " +
		KEY_N_SUT + " integer not null, " +
		KEY_SEL_CATE + " text not null, " +
		KEY_LINE1 + " text not null, " +
		KEY_LINE2 + " text not null, " +
		KEY_FREQ + " integer not null, " +
		KEY_PRIORITY + " text not null, " +
		KEY_CODE + " text not null);";	
	
	public SearchHistoryDBAdapter(Context _context) {
		context = _context;
		dbHelper = new SearchHistoryDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}	
	
	public SearchHistoryDBAdapter open() throws SQLException {
		db = dbHelper.getWritableDatabase();
		return this;
	}	
	
	public void close() {
		db.close();
	}	

	public boolean isDuplicated(SearchHistoryItem item) {
		String lang = item.getLanguage();
		String keywords = item.getKeywords();
		int nPage = item.getNPage();
		int nSut = item.getNSut();
		String sCate = item.getSelectedCategories();
		String line1 = item.getLine1();
		String line2 = item.getLine2();
		String where = String.format("%s='%s' AND %s='%s' AND %s=%d AND %s=%d AND %s='%s' AND %s='%s' AND %s='%s'", 
				KEY_LANG,lang,
				KEY_KEYWORDS, keywords,
				KEY_N_PAGE, nPage,
				KEY_N_SUT, nSut,
				KEY_SEL_CATE, sCate,
				KEY_LINE1, line1,
				KEY_LINE2, line2);
		
		int count = db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2}, 
				where, null, null, null, null).getCount();
		
		if(count > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public SearchHistoryItem getEntry(long _rowIndex) throws SQLException {
		Cursor cursor = db.query(true, DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2, KEY_FREQ, KEY_PRIORITY, KEY_CODE}, 
				KEY_ID + "=" + _rowIndex, 
				null, null, null, null, null);
		
		if((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No bookmark items found for row: " + _rowIndex);
		}
		
		String lang = cursor.getString(LANG_COL);
		String keywords = cursor.getString(KEYWORDS_COL);
		int nPage = cursor.getInt(N_PAGE_COL);
		int nSut = cursor.getInt(N_SUT_COL);
		String sCate = cursor.getString(SEL_CATE_COL);
		String line1 = cursor.getString(LINE1_COL);
		String line2 = cursor.getString(LINE2_COL);
		int freq = cursor.getInt(FREQ_COL);
		String priority = cursor.getString(PRIORITY_COL);
		String code = cursor.getString(CODE_COL);
		
		
		SearchHistoryItem result = new SearchHistoryItem(lang, keywords, nPage, nSut, sCate, line1, line2, freq, priority, code);
		return result;
	}
	
	
	public Cursor getAllEntries() {
		return db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2, KEY_FREQ, KEY_PRIORITY, KEY_CODE}, 
				null, null, null, null, null);
	}	
	
	public Cursor getEntries(String _lang, String key, boolean isDesc) {
		String where = KEY_LANG + "=" + "'" + _lang + "'";
		String orderby;
		if(isDesc) {
			orderby = key + " DESC" + ", " + KEY_PRIORITY + " DESC";
		}
		else {
			orderby = key + " ASC" + ", " + KEY_PRIORITY + " ASC";
		}
		return db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2, KEY_FREQ, KEY_PRIORITY, KEY_CODE}, 
				where, null, null, null, orderby);
	}		
	
	public Cursor getEntries(String _lang, String subText, String key, boolean isDesc) {
		String where = KEY_LANG + " = " + "'" + _lang + "'" + " AND " + KEY_KEYWORDS + " LIKE " +  "'%" + subText + "%'";
		String orderby;
		if(isDesc) {
			orderby = key + " DESC" + ", " + KEY_PRIORITY + " DESC";
		}
		else {
			orderby = key + " ASC" + ", " + KEY_PRIORITY + " ASC";
		}		
		return db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2, KEY_FREQ, KEY_PRIORITY, KEY_CODE}, 
				where, null, null, null, orderby);
	}

	public Cursor getEntries(String _lang, String subText, String key, boolean isDesc, String code, String number) {
		String where = KEY_LANG + " = " + "'" + _lang + "'" + 
						" AND " + KEY_KEYWORDS + " LIKE " +  "'%" + subText + "%'" +
						" AND " + KEY_CODE + " LIKE " + "'" + code + "%'" +
						" AND " + KEY_PRIORITY + " LIKE" + "'" + number + "%'";
		String orderby;
		if(isDesc) {
			orderby = key + " DESC" + ", " + KEY_PRIORITY + " DESC";
		}
		else {
			orderby = key + " ASC" + ", " + KEY_PRIORITY + " ASC";
		}		
		return db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2, KEY_FREQ, KEY_PRIORITY, KEY_CODE}, 
				where, null, null, null, orderby);
	}
	
	
	public Cursor getEntries(String _lang, String keywords, String sCate, String key, boolean isDesc) {
		String where = KEY_LANG + "=" + "'" + _lang + "'" + 
			" AND " + KEY_KEYWORDS + "=" + "'" + keywords + "'" +
			" AND " + KEY_SEL_CATE + "=" + "'" + sCate + "'";
		String orderby;
		if(isDesc) {
			orderby = key + " DESC" + ", " + KEY_PRIORITY + " DESC";
		}
		else {
			orderby = key + " ASC" + ", " + KEY_PRIORITY + " ASC";
		}		
		return db.query(DATABASE_TABLE, 
				new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2, KEY_FREQ, KEY_PRIORITY, KEY_CODE}, 
				where, null, null, null, orderby);		
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
	
	public long insertEntry(SearchHistoryItem item) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_LANG, item.getLanguage());
		newValues.put(KEY_KEYWORDS, item.getKeywords());
		newValues.put(KEY_N_PAGE, item.getNPage());
		newValues.put(KEY_N_SUT, item.getNSut());
		newValues.put(KEY_SEL_CATE, item.getSelectedCategories());
		newValues.put(KEY_LINE1, item.getLine1());
		newValues.put(KEY_LINE2, item.getLine2());
		newValues.put(KEY_FREQ, item.getFrequency());
		newValues.put(KEY_PRIORITY, item.getPriority());
		newValues.put(KEY_CODE, item.getCode());
		
		return db.insert(DATABASE_TABLE, null, newValues);
	}	
	
	public int updateEntry(long _rowIndex, SearchHistoryItem item) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_LANG, item.getLanguage());
		newValues.put(KEY_KEYWORDS, item.getKeywords());
		newValues.put(KEY_N_PAGE, item.getNPage());
		newValues.put(KEY_N_SUT, item.getNSut());
		newValues.put(KEY_SEL_CATE, item.getSelectedCategories());
		newValues.put(KEY_LINE1, item.getLine1());
		newValues.put(KEY_LINE2, item.getLine2());
		newValues.put(KEY_FREQ, item.getFrequency());
		newValues.put(KEY_PRIORITY, item.getPriority());
		newValues.put(KEY_CODE, item.getCode());
		
		String where = KEY_ID + " = " + _rowIndex;
		
		return db.update(DATABASE_TABLE, newValues, where, null);
	}
	
	private static class SearchHistoryDBHelper extends SQLiteOpenHelper {
		
		public SearchHistoryDBHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("UPGRADE", oldVersion+" --> "+newVersion);
			if(oldVersion == 1) { // add freq column
				ArrayList<ContentValues> tmp = new ArrayList<ContentValues>();
				Cursor cursor = db.query(DATABASE_TABLE, 
						new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2}, 
						null, null, null, null, null);
				if(cursor.getCount() > 0 && cursor.moveToFirst()) {
					ContentValues newValues;
					String line1;
					String [] tokens;
					while(!cursor.isAfterLast()) {
						newValues = new ContentValues();
						newValues.put(KEY_LANG, cursor.getString(LANG_COL));
						newValues.put(KEY_KEYWORDS, cursor.getString(KEYWORDS_COL));
						newValues.put(KEY_N_PAGE, cursor.getInt(N_PAGE_COL));
						newValues.put(KEY_N_SUT, cursor.getInt(N_SUT_COL));
						newValues.put(KEY_SEL_CATE, cursor.getString(SEL_CATE_COL));

						line1 = cursor.getString(LINE1_COL);
						tokens = line1.split("\\(");
						if(tokens.length == 2) {
							newValues.put(KEY_LINE1, "("+tokens[1]);
						} else {
							newValues.put(KEY_LINE1, line1);
						}
							
						newValues.put(KEY_LINE2,cursor.getString(LINE2_COL));
						newValues.put(KEY_FREQ, 0);
						newValues.put(KEY_PRIORITY, "0");
						newValues.put(KEY_CODE, "A");
						tmp.add(newValues);
						cursor.moveToNext();
					}	
				}
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
				onCreate(db);
				for(ContentValues values : tmp) {
					db.insert(DATABASE_TABLE, null, values);
				}
			} else if(oldVersion == 2) { // change freq column from string to integer
				ArrayList<ContentValues> tmp = new ArrayList<ContentValues>();
				Cursor cursor = db.query(DATABASE_TABLE, 
						new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2, KEY_FREQ}, 
						null, null, null, null, null);	
				if(cursor.getCount() > 0 && cursor.moveToFirst()) {
					ContentValues newValues;
					String line1;
					String [] tokens;
					while(!cursor.isAfterLast()) {
						newValues = new ContentValues();
						newValues.put(KEY_LANG, cursor.getString(LANG_COL));
						newValues.put(KEY_KEYWORDS, cursor.getString(KEYWORDS_COL));
						newValues.put(KEY_N_PAGE, cursor.getInt(N_PAGE_COL));
						newValues.put(KEY_N_SUT, cursor.getInt(N_SUT_COL));
						newValues.put(KEY_SEL_CATE, cursor.getString(SEL_CATE_COL));

						line1 = cursor.getString(LINE1_COL);
						tokens = line1.split("\\(");
						if(tokens.length == 2) {
							newValues.put(KEY_LINE1, "("+tokens[1]);
						} else {
							newValues.put(KEY_LINE1, line1);
						}
							
						newValues.put(KEY_LINE2,cursor.getString(LINE2_COL));
						newValues.put(KEY_FREQ, Integer.parseInt(cursor.getString(FREQ_COL)));
						newValues.put(KEY_PRIORITY, "0");
						newValues.put(KEY_CODE, "A");
						tmp.add(newValues);
						cursor.moveToNext();
					}	
				}
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
				onCreate(db);
				for(ContentValues values : tmp) {
					db.insert(DATABASE_TABLE, null, values);
				}
			} else if(oldVersion == 3) {
				ArrayList<ContentValues> tmp = new ArrayList<ContentValues>();
				Cursor cursor = db.query(DATABASE_TABLE, 
						new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2, KEY_FREQ}, 
						null, null, null, null, null);	
				if(cursor.getCount() > 0 && cursor.moveToFirst()) {
					ContentValues newValues;	
					String line1;
					String [] tokens;
					while(!cursor.isAfterLast()) {
						newValues = new ContentValues();
						newValues.put(KEY_LANG, cursor.getString(LANG_COL));
						newValues.put(KEY_KEYWORDS, cursor.getString(KEYWORDS_COL));
						newValues.put(KEY_N_PAGE, cursor.getInt(N_PAGE_COL));
						newValues.put(KEY_N_SUT, cursor.getInt(N_SUT_COL));
						newValues.put(KEY_SEL_CATE, cursor.getString(SEL_CATE_COL));

						line1 = cursor.getString(LINE1_COL);
						tokens = line1.split("\\(");
						if(tokens.length == 2) {
							newValues.put(KEY_LINE1, "("+tokens[1]);
						} else {
							newValues.put(KEY_LINE1, line1);
						}
							
						newValues.put(KEY_LINE2,cursor.getString(LINE2_COL));
						newValues.put(KEY_FREQ, cursor.getInt(FREQ_COL));
						newValues.put(KEY_PRIORITY, "0");
						newValues.put(KEY_CODE, "A");

						tmp.add(newValues);
						cursor.moveToNext();
					}	
				}
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
				onCreate(db);
				for(ContentValues values : tmp) {
					db.insert(DATABASE_TABLE, null, values);
				}				
			} else if(oldVersion == 4) {
				ArrayList<ContentValues> tmp = new ArrayList<ContentValues>();
				Cursor cursor = db.query(DATABASE_TABLE, 
						new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2, KEY_FREQ, KEY_PRIORITY}, 
						null, null, null, null, null);	
				if(cursor.getCount() > 0 && cursor.moveToFirst()) {
					ContentValues newValues;	
					String line1;
					String [] tokens;
					while(!cursor.isAfterLast()) {
						newValues = new ContentValues();
						newValues.put(KEY_LANG, cursor.getString(LANG_COL));
						newValues.put(KEY_KEYWORDS, cursor.getString(KEYWORDS_COL));
						newValues.put(KEY_N_PAGE, cursor.getInt(N_PAGE_COL));
						newValues.put(KEY_N_SUT, cursor.getInt(N_SUT_COL));
						newValues.put(KEY_SEL_CATE, cursor.getString(SEL_CATE_COL));
						
						line1 = cursor.getString(LINE1_COL);
						tokens = line1.split("\\(");
						if(tokens.length == 2) {
							newValues.put(KEY_LINE1, "("+tokens[1]);
						} else {
							newValues.put(KEY_LINE1, line1);
						}
						
						newValues.put(KEY_LINE2,cursor.getString(LINE2_COL));
						newValues.put(KEY_FREQ, cursor.getInt(FREQ_COL));
						newValues.put(KEY_PRIORITY, cursor.getString(PRIORITY_COL));
						newValues.put(KEY_CODE, "A");

						tmp.add(newValues);
						cursor.moveToNext();
					}	
				}
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
				onCreate(db);
				for(ContentValues values : tmp) {
					db.insert(DATABASE_TABLE, null, values);
				}				
			} else if(oldVersion == 5) {
				ArrayList<ContentValues> tmp = new ArrayList<ContentValues>();
				Cursor cursor = db.query(DATABASE_TABLE, 
						new String[] {KEY_ID, KEY_LANG, KEY_KEYWORDS, KEY_N_PAGE, KEY_N_SUT, KEY_SEL_CATE, KEY_LINE1, KEY_LINE2, KEY_FREQ, KEY_PRIORITY}, 
						null, null, null, null, null);	
				if(cursor.getCount() > 0 && cursor.moveToFirst()) {
					ContentValues newValues;	
					while(!cursor.isAfterLast()) {
						newValues = new ContentValues();
						newValues.put(KEY_LANG, cursor.getString(LANG_COL));
						newValues.put(KEY_KEYWORDS, cursor.getString(KEYWORDS_COL));
						newValues.put(KEY_N_PAGE, cursor.getInt(N_PAGE_COL));
						newValues.put(KEY_N_SUT, cursor.getInt(N_SUT_COL));
						newValues.put(KEY_SEL_CATE, cursor.getString(SEL_CATE_COL));
						newValues.put(KEY_LINE1, cursor.getString(LINE1_COL));
						newValues.put(KEY_LINE2, cursor.getString(LINE2_COL));
						newValues.put(KEY_FREQ, cursor.getInt(FREQ_COL));
						newValues.put(KEY_PRIORITY, cursor.getString(PRIORITY_COL));
						newValues.put(KEY_CODE, "A");
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