package  com.watnapp.etipitaka;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BookmarkThaiActivity extends Activity {
	
	private BookmarkDBAdapter bookmarkDBAdapter;
	private String language = "thai";
	private AlertDialog bmItemDialog;
	private int selectedItemPosition = -1;
	private MatrixCursor savedCursor;
	private SpecialCursorAdapter adapter;
	private ListView listview;
	private SharedPreferences prefs;
	private float bmLine1Size=16f;
	private float bmLine2Size=14f;
	private float bmLine3Size=12f;
	private String keywords = "";
	private ArrayList<String> removedItems = new ArrayList<String>(); 
	boolean isDesc;
	String sortKey;

    @Override
    public boolean onSearchRequested() {

    	
    	return true;
    }	

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			Intent result = new Intent();
			String [] tmp = new String[removedItems.size()];
			removedItems.toArray(tmp);
			result.putExtra("REMOVED_ITEMS", tmp);
			setResult(RESULT_CANCELED, result);
			this.finish();
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}    
    
	private class SpecialCursorAdapter extends SimpleCursorAdapter {

		public SpecialCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			Cursor cursor = this.getCursor();
			cursor.moveToPosition(position);
			int volume = cursor.getInt(3);
			TextView line1 = (TextView)view.findViewById(R.id.bm_line1);
			line1.setTextSize(prefs.getFloat("BmLine1Size", 16f));
			TextView line2 = (TextView)view.findViewById(R.id.bm_line2);
			line2.setTextSize(prefs.getFloat("BmLine2Size", 14f));			
			TextView line3 = (TextView)view.findViewById(R.id.bm_keywords);
			line3.setTextSize(prefs.getFloat("BmLine3Size", 12f));			

			if(volume >= 1 && volume <= 8) { // vinai
				line1.setTextColor(Color.rgb(30, 144, 255));
			} else if(volume >= 9 && volume <= 33) { // suttun
				line1.setTextColor(Color.rgb(255, 69, 0));
			} else { // abhidhum
				line1.setTextColor(Color.rgb(160, 32, 240));
			}

			
			return view;
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.bookmark_list_menu, menu);
	    
	    return true;
	}		

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		super.onOptionsItemSelected(item);
		SharedPreferences.Editor editor = prefs.edit();

		switch (item.getItemId()) {
			case R.id.zoom_in_result:
				bmLine1Size=prefs.getFloat("BmLine1Size", 16f)+1;
				bmLine2Size=prefs.getFloat("BmLine2Size", 14f)+1;
				bmLine3Size=prefs.getFloat("BmLine3Size", 12f)+1;
				editor.putFloat("BmLine1Size", bmLine1Size);
				editor.putFloat("BmLine2Size", bmLine2Size);
				editor.putFloat("BmLine3Size", bmLine3Size);
				editor.commit();
				adapter.notifyDataSetChanged();
				return true;
			case R.id.zoom_out_result:
				bmLine1Size=prefs.getFloat("BmLine1Size", 16f)-1;
				bmLine2Size=prefs.getFloat("BmLine2Size", 14f)-1;
				bmLine3Size=prefs.getFloat("BmLine3Size", 12f)-1;
				editor.putFloat("BmLine1Size", bmLine1Size);
				editor.putFloat("BmLine2Size", bmLine2Size);
				editor.putFloat("BmLine3Size", bmLine3Size);
				editor.commit();
				adapter.notifyDataSetChanged();
				return true;
			case R.id.sorting:
				showSortingDialog();
				return true;
			default:
				return false;
		}
	}	
	
	private void showSortingDialog() {
        final Dialog sortDialog = new Dialog(this);
        sortDialog.setContentView(R.layout.sort_bookmark_dialog);
        sortDialog.setTitle(R.string.sorting_bookmark_prefs);
        
        isDesc = prefs.getBoolean("BM_IS_DESC", false);
        sortKey = prefs.getString("BM_SORT_KEY", BookmarkDBAdapter.KEY_VOLUME);
        
        if(sortKey.equals(BookmarkDBAdapter.KEY_NOTE)) {
        	((RadioButton)sortDialog.findViewById(R.id.sort_by_note)).setChecked(true);
        } else if (sortKey.equals(BookmarkDBAdapter.KEY_ID)) {
        	((RadioButton)sortDialog.findViewById(R.id.sort_by_id)).setChecked(true);
        } else if (sortKey.equals(BookmarkDBAdapter.KEY_VOLUME)) {
        	((RadioButton)sortDialog.findViewById(R.id.sort_by_volume_page)).setChecked(true);
        } else if (sortKey.equals(BookmarkDBAdapter.KEY_KEYWORDS)) {
        	((RadioButton)sortDialog.findViewById(R.id.sort_by_keywords)).setChecked(true);
        }
        
        if(isDesc) {
        	((RadioButton)sortDialog.findViewById(R.id.desc_sort)).setChecked(true);
        } else {
        	((RadioButton)sortDialog.findViewById(R.id.asc_sort)).setChecked(true);
        }
        
        ((Button)sortDialog.findViewById(R.id.sort_ok)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = prefs.edit();
				if(((RadioButton)sortDialog.findViewById(R.id.desc_sort)).isChecked()) {
					isDesc = true;
					
				} else if(((RadioButton)sortDialog.findViewById(R.id.asc_sort)).isChecked()) {
					isDesc = false;
					
				}
				
				if(((RadioButton)sortDialog.findViewById(R.id.sort_by_id)).isChecked()) {
					sortKey = BookmarkDBAdapter.KEY_ID;
				} else if(((RadioButton)sortDialog.findViewById(R.id.sort_by_note)).isChecked()) {
					sortKey = BookmarkDBAdapter.KEY_NOTE;
				} else if(((RadioButton)sortDialog.findViewById(R.id.sort_by_volume_page)).isChecked()) {
					sortKey = BookmarkDBAdapter.KEY_VOLUME;
				} else if(((RadioButton)sortDialog.findViewById(R.id.sort_by_keywords)).isChecked()) {
					sortKey = BookmarkDBAdapter.KEY_KEYWORDS;
				} 
				
				editor.putBoolean("BM_IS_DESC", isDesc);
				editor.putString("BM_SORT_KEY", sortKey);
				
				editor.commit();
				sortDialog.dismiss();
				updateItemList();
			}
		});        
        
        sortDialog.show();
	}
	
	private MatrixCursor convertCursor(Cursor cursor) {
		final String [] matrix = { "_id", "line1", "line2", BookmarkDBAdapter.KEY_VOLUME, BookmarkDBAdapter.KEY_KEYWORDS};
		MatrixCursor newCursor = new MatrixCursor(matrix);		
		
		cursor.moveToFirst();
		int key = 0;
		while(!cursor.isAfterLast()) {
			int volume = cursor.getInt(BookmarkDBAdapter.VOLUME_COL);
			int page = cursor.getInt(BookmarkDBAdapter.PAGE_COL);
			int item = cursor.getInt(BookmarkDBAdapter.ITEM_COL);
			String note = cursor.getString(BookmarkDBAdapter.NOTE_COL);
			String keywords = cursor.getString(BookmarkDBAdapter.KEYWORDS_COL);
			// Utils.arabic2thai(""+(key+1), getResources()) + ". " + 
			String line1 = getString(R.string.th_book_label) + " " + Utils.arabic2thai(Integer.toString(volume), getResources());
			line1 = line1 + " " + getString(R.string.th_page_label) + " " + Utils.arabic2thai(Integer.toString(page), getResources());
			line1 = line1 + " " + getString(R.string.th_items_label) + " " + Utils.arabic2thai(Integer.toString(item), getResources());
			String line2 = note;
			
			newCursor.addRow(new Object[] { key++, line1, line2, volume, keywords});
			cursor.moveToNext();
		}
		cursor.close();
		return newCursor;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        bmLine1Size = prefs.getFloat("BmLine1Size", 16f);        
        bmLine2Size = prefs.getFloat("BmLine2Size", 14f); 
        bmLine3Size = prefs.getFloat("BmLine3Size", 12f); 
        updateItemList();
        adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
        bmLine1Size = prefs.getFloat("BmLine1Size", 16f);        
        bmLine2Size = prefs.getFloat("BmLine2Size", 14f);
        bmLine3Size = prefs.getFloat("BmLine3Size", 12f);
        updateItemList();
        adapter.notifyDataSetChanged();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		listview = new ListView(this);
		setContentView(listview);
		
        Context context = getApplicationContext();
        prefs =  PreferenceManager.getDefaultSharedPreferences(context);

        bmLine1Size = prefs.getFloat("BmLine1Size", 16f);        
        bmLine2Size = prefs.getFloat("BmLine2Size", 14f);
        bmLine3Size = prefs.getFloat("BmLine3Size", 12f);
        sortKey = prefs.getString("BM_SORT_KEY", BookmarkDBAdapter.KEY_VOLUME);
        isDesc = prefs.getBoolean("BM_IS_DESC", false);        
        
		bookmarkDBAdapter = new BookmarkDBAdapter(this);
		//bookmarkDBAdapter.open();
		
		Bundle dataBundle = getIntent().getExtras(); 
		if(dataBundle != null && dataBundle.containsKey("KEYWORDS")) {
			keywords = dataBundle.getString("KEYWORDS");
		}
		
		updateItemList();

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		        sortKey = prefs.getString("BM_SORT_KEY", BookmarkDBAdapter.KEY_VOLUME);
		        isDesc = prefs.getBoolean("BM_IS_DESC", false);
				bookmarkDBAdapter.open();
				Cursor cursor;
				if(keywords.length() == 0) {
					cursor = bookmarkDBAdapter.getEntries(language, sortKey, isDesc);
				} else {
					cursor = bookmarkDBAdapter.getEntries(language, keywords, sortKey, isDesc);
				}
				cursor.moveToPosition(arg2);

				int item = cursor.getInt(BookmarkDBAdapter.ITEM_COL);
				int volumn = cursor.getInt(BookmarkDBAdapter.VOLUME_COL);
				int page = cursor.getInt(BookmarkDBAdapter.PAGE_COL);
				String tmp_keywords = cursor.getString(BookmarkDBAdapter.KEYWORDS_COL);
				
				cursor.close();
				bookmarkDBAdapter.close();
				
        		Intent intent = new Intent(BookmarkThaiActivity.this, ReadBookActivity.class);
        		Bundle dataBundle = new Bundle();
        		dataBundle.putInt("VOL", volumn);
        		dataBundle.putInt("PAGE", page);
        		dataBundle.putString("LANG", language);
        		
        		if(tmp_keywords.length() > 0) {
        			dataBundle.putString("QUERY", tmp_keywords);
        		} else {
        			dataBundle.putInt("ITEM", item);
        		}
        		
        		intent.putExtras(dataBundle);
        		startActivity(intent);					
				
			}
			
		});
		
		listview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				selectedItemPosition = arg2;
				AlertDialog.Builder builder = new AlertDialog.Builder(BookmarkThaiActivity.this);
				
				CharSequence[] items1 = {getString(R.string.edit), getString(R.string.delete)};
				//CharSequence[] items2 = {getString(R.string.edit)};
				
				
		        sortKey = prefs.getString("BM_SORT_KEY", BookmarkDBAdapter.KEY_VOLUME);
		        isDesc = prefs.getBoolean("BM_IS_DESC", false);
				bookmarkDBAdapter.open();
				Cursor cursor;
				if(keywords.length() == 0) {
					cursor = bookmarkDBAdapter.getEntries(language, sortKey, isDesc);
				} else {
					cursor = bookmarkDBAdapter.getEntries(language, keywords, sortKey, isDesc);
				}
				cursor.moveToPosition(arg2);

				String k = cursor.getString(BookmarkDBAdapter.KEYWORDS_COL);
				
				cursor.close();
				bookmarkDBAdapter.close();				
								
				if(k.length() == 0) {
					builder.setItems(items1, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch(which) {
								case 0: // edit
									editItemAt(selectedItemPosition);
									break;
								case 1: // delete
									deleteItemAt(selectedItemPosition);
									break;
							}
							bmItemDialog.dismiss();
						}
					});
				} else {
					builder.setItems(items1, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch(which) {
								case 0: // edit
									editItemAt(selectedItemPosition);
									break;
								case 1: // delete
									deleteItemAt(selectedItemPosition);
									break;									
							}
							bmItemDialog.dismiss();
						}
					});					
				}
								
				bmItemDialog = builder.create();
				bmItemDialog.show();
				
				return true; // don't pass click event to others
			}
		});
		
	}
	
	private void updateItemList() {
		isDesc = prefs.getBoolean("BM_IS_DESC", false);
		sortKey = prefs.getString("BM_SORT_KEY", BookmarkDBAdapter.KEY_VOLUME);
		bookmarkDBAdapter.open();
		
		if(keywords.length() == 0) {
			savedCursor = convertCursor(bookmarkDBAdapter.getEntries(language, sortKey, isDesc));
		} else {
			savedCursor = convertCursor(bookmarkDBAdapter.getEntries(language, keywords, sortKey, isDesc));
		}

		adapter = new SpecialCursorAdapter(BookmarkThaiActivity.this, R.layout.bookmark_item, savedCursor,
    	        		new String[] {"line1", "line2", BookmarkDBAdapter.KEY_KEYWORDS},
    	        		new int[] {R.id.bm_line1, R.id.bm_line2, R.id.bm_keywords});
		listview.setAdapter(adapter);
		bookmarkDBAdapter.close();
	}
	
	private boolean editItemAt(int _position) {
        sortKey = prefs.getString("BM_SORT_KEY", BookmarkDBAdapter.KEY_VOLUME);
        isDesc = prefs.getBoolean("BM_IS_DESC", false);
		bookmarkDBAdapter.open();
		Cursor c;
		if(keywords.length() == 0) {
			c = bookmarkDBAdapter.getEntries(language, sortKey, isDesc);
		} else {
			c = bookmarkDBAdapter.getEntries(language, keywords, sortKey, isDesc);
		}
		final int position = _position;
		if(c.moveToPosition(position)) {
			String note = c.getString(BookmarkDBAdapter.NOTE_COL);
			AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
			alert.setTitle(getString(R.string.edit_note));
			alert.setIcon(android.R.drawable.ic_menu_edit);
			final EditText input = new EditText(this);
			input.setText(note);
			input.setTextSize(14);
			input.setMaxLines(5);
			alert.setView(input);
		    alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {  
		        public void onClick(DialogInterface dialog, int whichButton) {  
		            String value = input.getText().toString();
		            sortKey = prefs.getString("BM_SORT_KEY", BookmarkDBAdapter.KEY_VOLUME);
		            isDesc = prefs.getBoolean("BM_IS_DESC", false);
		            bookmarkDBAdapter.open();
		            Cursor c;
		            if(keywords.length() == 0) {
		            	c = bookmarkDBAdapter.getEntries(language, sortKey, isDesc);
		            } else {
		            	c = bookmarkDBAdapter.getEntries(language, keywords, sortKey, isDesc);
		            }
					if(c.moveToPosition(position)) {
						long rowIndex = c.getInt(BookmarkDBAdapter.ID_COL);
						BookmarkItem bmItem = bookmarkDBAdapter.getEntry(rowIndex);
						bmItem.setNote(value);
						bookmarkDBAdapter.updateEntry(rowIndex, bmItem);
						updateItemList();
					}
					c.close();
					bookmarkDBAdapter.close();
		        }  
		    });
		    c.close();
		    bookmarkDBAdapter.close();
			alert.show();
			return true;
		} else {
			c.close();
			bookmarkDBAdapter.close();
			return false;
		}	
	}
	
	private void deleteItemAt(int _position) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);   
		alert.setTitle(getString(R.string.delete_item));
		alert.setIcon(android.R.drawable.ic_dialog_alert);
		TextView textview = new TextView(this);
		textview.setText(getString(R.string.confirm_delete_item));
		textview.setTextSize(16);
		textview.setGravity(Gravity.CENTER_HORIZONTAL);
		alert.setView(textview);
		final int position = _position;
		alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        sortKey = prefs.getString("BM_SORT_KEY", BookmarkDBAdapter.KEY_VOLUME);
		        isDesc = prefs.getBoolean("BM_IS_DESC", false);
				bookmarkDBAdapter.open();
				Cursor c;
				if(keywords.length() == 0) {
					c = bookmarkDBAdapter.getEntries(language, sortKey, isDesc);
				} else {
					c = bookmarkDBAdapter.getEntries(language, keywords, sortKey, isDesc);
				}
				
				if(c.moveToPosition(position)) {
					int rowIndex = c.getInt(BookmarkDBAdapter.ID_COL);
					int page = c.getInt(BookmarkDBAdapter.VOLUME_COL);
					int volumn = c.getInt(BookmarkDBAdapter.PAGE_COL);
					
					// save information in order to return to SearchActivity
					removedItems.add(page+":"+volumn);
					bookmarkDBAdapter.removeEntry(rowIndex);
					updateItemList();
				}
				c.close();
				bookmarkDBAdapter.close();
				return;
				
			}
		});
		alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
				
			}
		});
		alert.show();
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//if(bookmarkDBAdapter != null) {
		//	bookmarkDBAdapter.close();
		//}
	}
	
	public void setKeywords(String _keywords) {
		keywords = _keywords;
	}
}
