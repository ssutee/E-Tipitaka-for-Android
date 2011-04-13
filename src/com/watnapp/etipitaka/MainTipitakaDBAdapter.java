package com.watnapp.etipitaka;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class MainTipitakaDBAdapter {
	private static final String DATABASE_NAME = "etipitaka.db";
	//private static final int DATABASE_VERSION = 1;	
	private static String DATABASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ETPK"; 
	private SQLiteDatabase db = null;
	private final Context context;
	//private MainTipitakaDBHelper dbHelper;
	
	public MainTipitakaDBAdapter(Context _context) {
		//dbHelper = new MainTipitakaDBHelper(DATABASE_PATH + File.separator + DATABASE_NAME);
		context = _context;
	}
	
	public MainTipitakaDBAdapter open() throws SQLException {
        File f = new File(DATABASE_PATH + File.separator + DATABASE_NAME);
        if(f.exists()) {
        	db = SQLiteDatabase.openDatabase(DATABASE_PATH + File.separator + DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
        } else {
        	db = null;
        }
		return this;
	}

	public void close() {
		if(db != null) {
			db.close();
		}
	}	
	
    public boolean isOpened() {
    	return db == null ? false : true;
    }	
	
    public Cursor getContent(int volumn, int page, String lang) {
    	String sPage = String.format("%04d",page);
    	String sVol = String.format("%02d", volumn);
    	String selection = String.format("volumn = '%s' AND page = '%s'", sVol, sPage);
    	final Cursor cursor = this.db.query(
    			lang, 
    			new String[] {"items","content"}, 
    			selection,
    			null, 
    			null, 
    			null, 
    			null);
    	return cursor;    	
    }    
    
    public Cursor getPageByItem(int volumn, int item, String lang, boolean single) {
    	String sItem = Integer.toString(item);
    	String selection = "";
    	if(single) {
    		selection = "item = " + sItem + " AND volumn = " + String.format("%02d", volumn) + " AND marked = 1";
    	} else {
    		selection = "item = " + sItem + " AND volumn = " + String.format("%02d", volumn);
    	}
    	final Cursor cursor = this.db.query(
    			lang+"_items", 
    			new String[] {"page"}, 
    			selection, 
    			null, 
    			null, 
    			null, 
    			null);
    	
    	return cursor;
    }    
    
    public Cursor getSutByPage(int volumn, int page, String lang) {
    	String sPage = String.format("%04d", page);
    	String selection = "";

    	selection = "page = " + sPage + " AND volumn = " + String.format("%02d", volumn);

    	Cursor cursor = this.db.query(
    			lang+"_items", 
    			new String[] {"sutra"}, 
    			selection, 
    			null, 
    			null, 
    			null, 
    			null);
    	
    	return cursor;
    }      
    
    public Cursor search(int volumn, String query, String lang) {
    	String strVol = Integer.toString(volumn);
    	String selection = "";
    	
    	if(volumn < 10) {
    		strVol = "0" + Integer.toString(volumn);
    	}
    	
    	String[] tokens = query.split("\\s+");
    	
    	selection = selection + "volumn = '" + strVol + "'";
    	for(int i=0; i<tokens.length; i++) {
    		//Log.i("Tokens", tokens[i].replace('+', ' '));
    		selection = selection + " AND content LIKE " + "'%" + tokens[i].replace('+', ' ') + "%'";
    	}
    	
    	final Cursor cursor = this.db.query(
    			lang, 
    			new String[] {"_id","volumn", "page", "items", "suts"}, 
    			selection,
    			null, 
    			null, 
    			null, 
    			null);
    	return cursor;
    }

    public Cursor searchAll(String query, String lang) {
    	final Cursor cursor = this.db.query(
    			lang, 
    			new String[] {"_id","volumn", "page", "items"}, 
    			"content LIKE " + "'%" + query + "%'", 
    			null, 
    			null, 
    			null, 
    			null);
    	return cursor;
    }    
    
    /*
	private class MainTipitakaDBHelper {
		private String dbPath;
		
		public MainTipitakaDBHelper(String dbPath) {
			this.dbPath = dbPath;
		}
		
		public SQLiteDatabase getDatabase() {
	        File f = new File(dbPath);
	        if(f.exists()) {
	        	db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
	        } else {
	        	db = null;
	        }
	        return db;
		}		
	}*/
}

