package  com.watnapp.etipitaka;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class BookmarkActivity extends Activity {
	
	private BookmarkDBAdapter bookmarkDBAdapter;
	private String language = "";
		
	private MatrixCursor convertCursor(Cursor cursor) {
		final String [] matrix = { "_id", "line1", "line2" };
		MatrixCursor newCursor = new MatrixCursor(matrix);		
		
		cursor.moveToFirst();
		int key = 0;
		while(!cursor.isAfterLast()) {
			int volumn = cursor.getInt(BookmarkDBAdapter.VOLUME_COL);
			int page = cursor.getInt(BookmarkDBAdapter.PAGE_COL);
			int item = cursor.getInt(BookmarkDBAdapter.ITEM_COL);
			String note = cursor.getString(BookmarkDBAdapter.NOTE_COL);
			
			String line1 = Utils.arabic2thai(""+(key+1), getResources()) + ". " + getString(R.string.th_book_label) + " " + Utils.arabic2thai(Integer.toString(volumn), getResources());
			line1 = line1 + " " + getString(R.string.th_page_label) + " " + Utils.arabic2thai(Integer.toString(page), getResources());
			line1 = line1 + " " + getString(R.string.th_items_label) + " " + Utils.arabic2thai(Integer.toString(item), getResources());
			String line2 = note;
			
			newCursor.addRow(new Object[] { key++, line1, line2});
			cursor.moveToNext();
		}
		
		return newCursor;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ListView listview = new ListView(this);
		setContentView(listview);
		bookmarkDBAdapter = new BookmarkDBAdapter(this);
		bookmarkDBAdapter.open();
		
		MatrixCursor cursor = convertCursor(bookmarkDBAdapter.getEntries(language,""));
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(BookmarkActivity.this, R.layout.bookmark_item, cursor,
    	        		new String[] {"line1", "line2"},
    	        		new int[] {R.id.bm_line1, R.id.bm_line2});
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Cursor cursor = bookmarkDBAdapter.getEntries(language,"");
				cursor.moveToPosition(arg2);

				int item = cursor.getInt(BookmarkDBAdapter.ITEM_COL);
				int volumn = cursor.getInt(BookmarkDBAdapter.VOLUME_COL);
				int page = cursor.getInt(BookmarkDBAdapter.PAGE_COL);
				
        		Intent intent = new Intent(BookmarkActivity.this, ReadBookActivity.class);
        		Bundle dataBundle = new Bundle();
        		dataBundle.putInt("VOL", volumn);
        		dataBundle.putInt("PAGE", page);
        		dataBundle.putInt("ITEM", item);
        		dataBundle.putString("LANG", language);
        		
        		intent.putExtras(dataBundle);
        		startActivity(intent);					
				
			}
			
		});
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(bookmarkDBAdapter != null) {
			bookmarkDBAdapter.close();
		}
	}
}