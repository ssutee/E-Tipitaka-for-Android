package com.watnapp.etipitaka;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {
    /** Called when the activity is first created. */
	private MainTipitakaDBAdapter mainTipitakaDBAdapter = null;
	private Handler handler = new Handler();
	//private Button btSearch;
	//private TextView output;
	//private EditText text;
	private ArrayList<String> resultList = new ArrayList<String>();
	private ProgressDialog pdialog;
	//private long start_time;
	private TextView statusText;
	private TextView searchText;
	private ListView resultView;
	private View divider1;
	private View divider2;
	private String savedQuery;
	private String selCate;
	private MatrixCursor savedCursor;
	private SpecialCursorAdapter adapter;
	private TableLayout table;
	private int pVinai = 0;
	private int pSuttan = 0;
	private int pAbhidhum = 0;
	private int suVinai = 0;
	private int suSuttan = 0;
	private int suAbhidhum = 0;
	private int firstPosVinai = Integer.MAX_VALUE;
	private int firstPosSuttan = Integer.MAX_VALUE;
	private int firstPosAbhidhum = Integer.MAX_VALUE;
	private Intent intent;
	private Dialog cateDialog;
	private float line1Size = 12f;
	private float line2Size = 12f;
	private SharedPreferences prefs;	

	private SearchHistoryDBAdapter searchHistoryDBAdapter;
	private SearchResultsDBAdapter searchResultsDBAdapter;
	private BookmarkDBAdapter bookmarkDBAdapter;
	
	private SearchResultsItem savedResultsItem;
	private long savedResultsItemPosition;
	
	public String lang = "thai";
	
	private static final int SHOW_READBOOK_ACTIVITY = 1;
	private static final int SHOW_BOOKMARK_ACTIVITY = 2;
	
	private String [] readPages = null;
	
	// for highlighting selected items
	private class SpecialCursorAdapter extends SimpleCursorAdapter {
		private ArrayList<Integer> checkClicked = new ArrayList<Integer>();
		private ArrayList<Integer> checkSecondaryClicked = new ArrayList<Integer>();
		private ArrayList<Integer> checkSaved = new ArrayList<Integer>();
		private ArrayList<Integer> checkMarked = new ArrayList<Integer>();
		private int posVinai = Integer.MAX_VALUE;
		private int posSuttan = Integer.MAX_VALUE;
		private int posAbhidhum = Integer.MAX_VALUE;
		private Context context;
		
		public SpecialCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			this.context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			//Toast.makeText(SearchPage.this, Integer.toString(position), Toast.LENGTH_SHORT).show();
			
			TextView line1 = (TextView)view.findViewById(R.id.line1);
			line1.setTextSize(prefs.getFloat("Line1Size", 12f));
			TextView line2 = (TextView)view.findViewById(R.id.line2);
			line2.setTextSize(prefs.getFloat("Line2Size", 12f));
			
			ImageView star = (ImageView)view.findViewById(R.id.star_mark);
			
			if(checkMarked.contains(position)) {
				//star.setImageDrawable(context.getResources().getDrawable(R.drawable.star_big_on));
				star.setVisibility(View.VISIBLE);
			} else {
				star.setVisibility(View.INVISIBLE);
				//star.setImageDrawable(context.getResources().getDrawable(R.drawable.star_big_off));
			}
			
			RelativeLayout topLayout = (RelativeLayout)view.findViewById(R.id.top_layout);
						
			if(checkClicked.contains(position)) {
				line1.setBackgroundColor(Color.rgb(90, 90, 90));
				line2.setBackgroundColor(Color.rgb(90, 90, 90));
				star.setBackgroundColor(Color.rgb(90, 90, 90));
				topLayout.setBackgroundColor(Color.rgb(90, 90, 90));
			} else if(checkSecondaryClicked.contains(position)) {
				line1.setBackgroundColor(Color.rgb(45, 45, 45));
				line2.setBackgroundColor(Color.rgb(45, 45, 45));
				star.setBackgroundColor(Color.rgb(45, 45, 45));
				topLayout.setBackgroundColor(Color.rgb(45, 45, 45));				
			} else {
				line1.setBackgroundColor(Color.BLACK);
				line2.setBackgroundColor(Color.BLACK);
				star.setBackgroundColor(Color.BLACK);
				topLayout.setBackgroundColor(Color.BLACK);				
			}

			if(checkSaved.contains(position)) {
				line1.setBackgroundColor(Color.rgb(25, 25, 90));
				line2.setBackgroundColor(Color.rgb(25, 25, 90));
				star.setBackgroundColor(Color.rgb(25, 25, 90));
				topLayout.setBackgroundColor(Color.rgb(25, 25, 90));
			}
			
			if(position >= posVinai && position < posSuttan && position < posAbhidhum) {				
				line2.setTextColor(Color.argb(255, 30, 144, 255));
				//Log.i("VI",Integer.toString(position));
			} else if(position >= posSuttan && position < posAbhidhum) {
				line2.setTextColor(Color.argb(255, 255, 69, 0));
				//Log.i("SU",Integer.toString(position));
			} else if(position >= posAbhidhum) {
				line2.setTextColor(Color.argb(255, 160, 32, 240));
				//Log.i("AB",Integer.toString(position));
			}

			return view;			
		}

		public void addMarkedPosition(Integer position) {
			if(!checkMarked.contains(position)) {
				checkMarked.add(position);
				this.notifyDataSetChanged();
			}
		}
		
		public void addClickedPosition(Integer position) {
			if (!checkClicked.contains(position)) {				
				checkClicked.add(position);
				this.notifyDataSetChanged();
			}	
		}
		
		public void addSecondaryClickedPosition(Integer position) {
			if (!checkSecondaryClicked.contains(position)) {				
				checkSecondaryClicked.add(position);
				this.notifyDataSetChanged();
			}				
		}
		
		public void addSavedPosition(Integer position) {
			if(!checkSaved.contains(position)) {
				checkSaved.add(position);
				this.notifyDataSetChanged();
			}
		}
		
		public ArrayList<Integer> getPrimaryClicked() {
			return checkClicked;
		}
		
		public ArrayList<Integer> getSecondaryClicked() {
			return checkSecondaryClicked;
		}
		
		public ArrayList<Integer> getSaved() {
			return checkSaved;
		}
		
		public ArrayList<Integer> getMarked() {
			return checkMarked;
		}
		
		public void setPrimaryClicked(ArrayList<Integer> al) {
			checkClicked = al;
			this.notifyDataSetChanged();
		}
		
		public void setSecondaryClicked(ArrayList<Integer> al) {
			checkSecondaryClicked = al;
			this.notifyDataSetChanged();
		}
		
		public void setSaved(ArrayList<Integer> al) {
			checkSaved = al;
			this.notifyDataSetChanged();
		}
		
		public void setMarked(ArrayList<Integer> al) {
			checkMarked = al;
			this.notifyDataSetChanged();
		}
		
		public boolean isMarked(Integer position) {
			return checkMarked.contains(position);
		}
		
		public void clearClickedPosition(Integer position) {
			checkClicked.remove(position);
			checkSecondaryClicked.remove(position);
			this.notifyDataSetChanged();
		}
		
		public void clearMarkedPosition(Integer position) {
			checkMarked.remove(position);
			this.notifyDataSetChanged();
		}
		
		public void clearClickedPosition() {
			checkClicked.clear();
			checkSecondaryClicked.clear();
			this.notifyDataSetChanged();
		}
		
		public void clearSavedPosition(Integer position) {
			checkSaved.remove(position);
			this.notifyDataSetChanged();
		}
		
		public void setVinaiPosition(int position) {
			posVinai = position;
		}
		
		public void setSuttanPosition(int position) {
			posSuttan = position;
		}		

		public void setAbhidhumPosition(int position) {
			posAbhidhum = position;
		}
		
	}

	/*
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_SEARCH) {
			return false;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}
	*/
	
	private void showResults(ArrayList<String> _resultList, boolean isSaved, String keywords, ArrayList<Integer> pList, ArrayList<Integer> sList, ArrayList<Integer> savedList, ArrayList<Integer> markedList) {
        savedCursor = convertToCursor(_resultList);
        adapter = new SpecialCursorAdapter(SearchActivity.this, R.layout.result_item, savedCursor,
        		new String[] {"line1", "line2"},
        		new int[] {R.id.line1, R.id.line2});
        if(pList != null) {
        	adapter.setPrimaryClicked(pList);
        }
        
        if(sList != null) {
        	adapter.setSecondaryClicked(sList);
        }
   
        if(savedList != null) {
        	adapter.setSaved(savedList);
        }
        
        if(markedList != null) {
        	adapter.setMarked(markedList);
        }
        
        adapter.setVinaiPosition(firstPosVinai);
        adapter.setSuttanPosition(firstPosSuttan);
        adapter.setAbhidhumPosition(firstPosAbhidhum);
        
        TextView p1 = (TextView) findViewById(R.id.npage1);
        TextView p2 = (TextView) findViewById(R.id.npage2);
        TextView p3 = (TextView) findViewById(R.id.npage3);
        
        p1.setText(Utils.arabic2thai(Integer.toString(pVinai), getResources())+" "+getString(R.string.th_page));
        p2.setText(Utils.arabic2thai(Integer.toString(pSuttan), getResources())+" "+getString(R.string.th_page));
        p3.setText(Utils.arabic2thai(Integer.toString(pAbhidhum), getResources())+" "+getString(R.string.th_page));
        
        TextView s1 = (TextView) findViewById(R.id.nsutt1);
        TextView s2 = (TextView) findViewById(R.id.nsutt2);
        TextView s3 = (TextView) findViewById(R.id.nsutt3);
        
        s1.setText(Utils.arabic2thai(Integer.toString(suVinai), getResources())+" "+getString(R.string.th_suttra));
        s2.setText(Utils.arabic2thai(Integer.toString(suSuttan), getResources())+" "+getString(R.string.th_suttra));
        s3.setText(Utils.arabic2thai(Integer.toString(suAbhidhum), getResources())+" "+getString(R.string.th_suttra));

        // save search history
        if(isSaved) {
            String tmp = Utils.arabic2thai(""+(pVinai+pSuttan+pAbhidhum), getResources()) + " " + getString(R.string.th_page);
            tmp += " " + Utils.arabic2thai(""+(suVinai+suSuttan+suAbhidhum), getResources()) + " " + getString(R.string.th_suttra);
            String line1 = String.format("(%s)", tmp);

            String line2 = "";
            if(selCate.charAt(0) == '1') {
            	line2 += String.format("%s(%s/%s)  ", 
            			getString(R.string.ss_vinai), 
            			Utils.arabic2thai(pVinai+"", getResources()), 
            			Utils.arabic2thai(suVinai+"", getResources()));
            			
            }
            if(selCate.charAt(1) == '1') {
            	line2 += String.format("%s(%s/%s)  ", 
            			getString(R.string.ss_suttan), 
            			Utils.arabic2thai(pSuttan+"", getResources()),
            			Utils.arabic2thai(suSuttan+"", getResources()));
            }
            if(selCate.charAt(2) == '1') {
            	line2 += String.format("%s(%s/%s)", 
            			getString(R.string.ss_abhidum), 
            			Utils.arabic2thai(pAbhidhum+"", getResources()), 
            			Utils.arabic2thai(suAbhidhum+"", getResources()));
            }
            line2 = line2.trim();    	        
            
            
            searchHistoryDBAdapter.open();
    	    SearchHistoryItem item1 = new SearchHistoryItem(lang, keywords, pVinai+pSuttan+pAbhidhum, suVinai+suSuttan+suAbhidhum, selCate, line1, line2);
    	    if(!searchHistoryDBAdapter.isDuplicated(item1)) {
    	    	searchHistoryDBAdapter.insertEntry(item1);
    	    }
            searchHistoryDBAdapter.close();
        }
        
        // save search results
        if(isSaved) {
	        searchResultsDBAdapter.open();
	        try {
	        	String content = Utils.toStringBase64(_resultList);
	        	String pClicked = Utils.toStringBase64(adapter.getPrimaryClicked());
	        	String sClicked = Utils.toStringBase64(adapter.getSecondaryClicked());
	        	String saved = Utils.toStringBase64(adapter.getSaved());
	        	String marked = Utils.toStringBase64(adapter.getMarked());
	        	
		        SearchResultsItem item2 = new SearchResultsItem(lang, keywords, 
		        		pVinai+":"+pSuttan+":"+pAbhidhum,
		        		suVinai+":"+suSuttan+":"+suAbhidhum,selCate, content);
		        if(!searchResultsDBAdapter.isDuplicated(item2)) {
			        item2.setPrimaryClicked(pClicked);
			        item2.setSecondaryClicked(sClicked);
			        item2.setSaved(saved);
			        item2.setMarked(marked);
		        	savedResultsItemPosition = searchResultsDBAdapter.insertEntry(item2);
		        	//Toast.makeText(this, "SAVE"+":"+savedResultsItemPosition, Toast.LENGTH_SHORT).show();
		        } else {
		        	Cursor cursor = searchResultsDBAdapter.getEntries(lang, keywords, selCate);
		        	if(cursor.getCount() > 0 && cursor.moveToFirst()) {
		        		savedResultsItemPosition = cursor.getInt(SearchResultsDBAdapter.ID_COL);
		        		pClicked = cursor.getString(SearchResultsDBAdapter.PRIMARY_CLIKCED_COL);
		        		sClicked = cursor.getString(SearchResultsDBAdapter.SECONDARY_CLIKCED_COL);
		        		saved = cursor.getString(SearchResultsDBAdapter.SAVED_COL);
		        		marked = cursor.getString(SearchResultsDBAdapter.MARKED_COL);
		        		
				        item2.setPrimaryClicked(pClicked);
				        item2.setSecondaryClicked(sClicked);
				        item2.setSaved(saved);
				        item2.setMarked(marked);
				        
		        		try {
		        			adapter.setPrimaryClicked((ArrayList<Integer>)Utils.fromStringBase64(pClicked));
		        			adapter.setSecondaryClicked((ArrayList<Integer>)Utils.fromStringBase64(sClicked));
		        			adapter.setSaved((ArrayList<Integer>)Utils.fromStringBase64(saved));
		        			adapter.setMarked((ArrayList<Integer>)Utils.fromStringBase64(marked));
		        		} catch(IOException e) {
		        			e.printStackTrace();
		        		} catch(ClassNotFoundException e) {
		        			e.printStackTrace();
		        		}
		        	}
		        	cursor.close();
		        }
		        savedResultsItem = item2;
	        } catch(IOException e) {
	        	e.printStackTrace();
	        }
	        searchResultsDBAdapter.close();    	        
        }
        
        String slang = "";
        if(lang.equals("thai"))
        	slang = getString(R.string.th_lang);
        else if(lang.equals("pali"))
        	slang = getString(R.string.pl_lang);
        
        searchText.setText("\"" + keywords + "\" (" + slang + ") ");
		
		if(_resultList.size() > 0) {
			statusText.setText(getString(R.string.th_found) + 
					" " + Utils.arabic2thai(Integer.toString(_resultList.size()), getResources()) + 
					" " + getString(R.string.th_page) + 
					" " + Utils.arabic2thai(Integer.toString(suVinai+suSuttan+suAbhidhum), getResources()) +
					" " + getString(R.string.th_suttra));
		} else {
			statusText.setText(getString(R.string.not_found));
		}
            	        
        table.setVisibility(View.VISIBLE);
        divider1.setVisibility(View.VISIBLE);
        divider2.setVisibility(View.VISIBLE);
        
        resultView.setAdapter(adapter);
		
	}
		
	private Runnable doUpdateGUI = new Runnable() {
    	public void run() {
    		pdialog.incrementProgressBy(1);  
    		pdialog.setMessage(getString(R.string.th_found)+" "+Integer.toString(resultList.size())+" "+getString(R.string.th_page));
    		if(pdialog.getProgress() == pdialog.getMax()) {
    			pdialog.dismiss();
    			showResults(resultList, true, savedQuery, null, null, null, null);
    		}
    	}
    };

	public class QueryAllThread implements Runnable {
		private String query;
		private ArrayList<String> resultList;
		private boolean vinai = true;
		private boolean suttan = true;
		private boolean abhidham = true;
		
		public QueryAllThread(String query, ArrayList<String> resultList) {
			this.query = query;
			this.resultList = resultList;
		}

		public QueryAllThread(String query, ArrayList<String> resultList, boolean vinai, boolean suttan, boolean abhidham) {
			this.query = query;
			this.resultList = resultList;
			this.vinai = vinai;
			this.suttan = suttan;
			this.abhidham = abhidham;
		}
		
		private void search(int vol) {
			mainTipitakaDBAdapter.open();
    		Cursor cursor = mainTipitakaDBAdapter.search(vol, this.query, lang);    		
    		cursor.moveToFirst();
    		while(cursor.isAfterLast() == false) {
    			this.resultList.add(cursor.getString(0)+":"+cursor.getString(1)+":"+cursor.getString(2)+":"+cursor.getString(3)+":"+cursor.getString(4));
    			cursor.moveToNext();
    		}
    		cursor.close();
    		mainTipitakaDBAdapter.close();
    		handler.post(doUpdateGUI);							
		}
		
		@Override
		public void run() {
			if(vinai) {
				for(int i=1; i<=8; i++) {
					search(i);
				} 
			}
			if(suttan) {
				for(int i=9; i<=33; i++) {
					search(i);
				}
			}
			if(abhidham) {
				for(int i=34; i<=45; i++) {
					search(i);
				}
			}
    	}
	}
	
	private MatrixCursor filterCursor(MatrixCursor cursor, boolean vFlag, boolean sFlag, boolean aFlag) {
		final String [] matrix = { "_id", "line1", "line2" };
		MatrixCursor newCursor = new MatrixCursor(matrix);

		int rowId = 0;
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			String line2 = cursor.getString(1);
			if(vFlag && line2.startsWith(getString(R.string.vinai_full))) {
				newCursor.addRow(new Object[] { rowId++, cursor.getString(0), cursor.getString(1)});
			} else if(sFlag && line2.startsWith(getString(R.string.suttan_full))) {
				newCursor.addRow(new Object[] { rowId++, cursor.getString(0), cursor.getString(1)});
			} else if(aFlag && line2.startsWith(getString(R.string.abhidum_full))) {
				newCursor.addRow(new Object[] { rowId++, cursor.getString(0), cursor.getString(1)});
			}
			cursor.moveToNext();
		}
		
		return newCursor;
	}
	
	private MatrixCursor convertToCursor(ArrayList<String> results) {
		final String [] matrix = { "_id", "line1", "line2" };
		MatrixCursor cursor = new MatrixCursor(matrix);
		Resources res = this.getResources();
		pVinai = 0;
		pSuttan = 0;
		pAbhidhum = 0;
		suVinai = 0;
		suSuttan = 0;
		suAbhidhum = 0;
		firstPosVinai = Integer.MAX_VALUE;
		firstPosSuttan = Integer.MAX_VALUE;
		firstPosAbhidhum = Integer.MAX_VALUE;
		
		ArrayList<String> al_tmp = new ArrayList<String>();
		
		String [] bnames = res.getStringArray(R.array.thaibook);
		
		int key = 0;
		
		boolean vFound = false;
		boolean sFound = false;
		boolean aFound = false;
		
		for(String item : results) {
			String [] tokens = item.split(":");
			int vol = Integer.parseInt(tokens[1]);
			
			if(vol >= 1 && vol <= 8) {
				pVinai++;
			}
			else if(vol >=9 && vol <= 33) {
				pSuttan++;
			}
			else if(vol >= 34){
				pAbhidhum++;
			}
		
			if(!vFound && vol >= 1 && vol <= 8) {
				vFound = true;
				firstPosVinai = key;
			}
			else if(!sFound && vol >=9 && vol <= 33) {
				sFound = true;
				firstPosSuttan = key;
			}
			else if(!aFound && vol >= 34) {
				aFound = true;
				firstPosAbhidhum = key;
			}
			
			String sVol = Integer.toString(vol);
			int page = Integer.parseInt(tokens[2]);
			String sPage = Integer.toString(page);
			String slang = null;
			

			for(String sut : tokens[4].split("\\s+")) {
				if(! al_tmp.contains(sVol+":"+sut)) {
					al_tmp.add(sVol+":"+sut);
					if(vol >= 1 && vol <= 8) {
						suVinai++;
					}
					else if(vol >=9 && vol <= 33) {
						suSuttan++;
					}
					else {
						suAbhidhum++;
					}
				}
				// count only one time 
				break;
			}
			
			if (lang.equals("thai"))
				slang = getString(R.string.th);
			else if (lang.equals("pali")) {
				slang = getString(R.string.pl);
			}
			String line1 =  Utils.arabic2thai(Integer.toString(key+1), getResources()) + ". " +  
					getString(R.string.th_tipitaka_label) + " " + "("+ slang + ")" + " "  +
					getString(R.string.th_book_label) + " " +
					Utils.arabic2thai(sVol, getResources()) + " " +
					getString(R.string.th_page_label) + " " +  
					Utils.arabic2thai(sPage, getResources());
			
			String [] ts = tokens[3].split("\\s+");

			String t_items;
			if(ts.length > 1) {
				t_items = Utils.arabic2thai(ts[0], getResources()) + "-" + Utils.arabic2thai(ts[ts.length-1], getResources());
			} else {
				t_items = Utils.arabic2thai(ts[0], getResources());
			}
			
			String tmp = "";
			int count = 0;
			for(String t: bnames[vol-1].trim().split("\\s+")) {
				if(count==4)
					break;
				tmp = tmp + t + " ";
				count++;
			}
			String line2 = tmp + " " + getString(R.string.th_items_label) + " " + t_items;
			cursor.addRow(new Object[] { key++, line1, line2});
		}				
		return cursor;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//if(dbhelper != null && dbhelper.isOpened()) {
		//	dbhelper.close();
		//}
		
		//if(searchHistoryDBAdapter != null) {
		//	searchHistoryDBAdapter.close();
		//}
	}
	
	private void doSearch(String _query, String _lang) {
		divider1.setVisibility(View.INVISIBLE);
		divider2.setVisibility(View.INVISIBLE);

    	//savedQuery = intent.getStringExtra(SearchManager.QUERY);
		savedQuery = _query;
		lang = _lang;
    	
        //SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, 
        //		ETPKSearchSuggestionProvider.AUTHORITY, ETPKSearchSuggestionProvider.MODE);
        //suggestions.saveRecentQuery(savedQuery, null);
    	
        //dbhelper = new DataBaseHelper(SearchActivity.this);
		mainTipitakaDBAdapter = new MainTipitakaDBAdapter(SearchActivity.this);
        //dbhelper.openDataBase();
        resultList.clear();
        //start_time = System.currentTimeMillis();

        pdialog = new ProgressDialog(SearchActivity.this);
		pdialog.setCancelable(false);
		pdialog.setMessage(getString(R.string.th_searching));
		pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pdialog.setProgress(0);
		
		boolean b1 = ((CheckBox) cateDialog.findViewById(R.id.cb_vinai)).isChecked();
		boolean b2 = ((CheckBox) cateDialog.findViewById(R.id.cb_suttan)).isChecked();
		boolean b3 = ((CheckBox) cateDialog.findViewById(R.id.cb_abhidham)).isChecked();

		// convert selected categories into string
		selCate = "";
		if(b1) {
			selCate += "1";
		} else {
			selCate += "0";
		}

		if(b2) {
			selCate += "1";
		} else {
			selCate += "0";
		}
		
		if(b3) {
			selCate += "1";
		} else {
			selCate += "0";
		}

		
		Thread searchThread = new Thread(new QueryAllThread(savedQuery, resultList,b1 ,b2, b3));
		searchThread.start();
		
		int maxSearch = 0;

		if(b1) {
			maxSearch += 8;
		}
		if(b2) {
			maxSearch += 25;
		}
		if(b3) {
			maxSearch += 12;
		}
		
		pdialog.setMax(maxSearch);
		if(maxSearch > 0) {
			pdialog.show();
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Bundle dataBundle = data.getExtras();
		if(requestCode == SHOW_READBOOK_ACTIVITY && dataBundle != null) {
			readPages = dataBundle.getStringArray("READ_PAGES");
			if(readPages != null) {
				for(String s : readPages) {
					String [] tokens = s.split(":");
					int volume = Integer.parseInt(tokens[0]);
					int page = Integer.parseInt(tokens[1]);
					int position = 0;
					for(String result : resultList) {
						String [] items = result.split(":");
						if(volume == Integer.parseInt(items[1]) && page == Integer.parseInt(items[2])) {
							adapter.addSecondaryClickedPosition(position);
							try {
								String sClicked = Utils.toStringBase64(adapter.getSecondaryClicked());
								savedResultsItem.setSecondaryClicked(sClicked);
								searchResultsDBAdapter.open();
								searchResultsDBAdapter.updateEntry(savedResultsItemPosition, savedResultsItem);
								searchResultsDBAdapter.close();
							} catch(IOException e) {
								e.printStackTrace();
							}
						}
						position++;
					}
				}
			}
		} else if(requestCode == SHOW_BOOKMARK_ACTIVITY && dataBundle != null) {
			String [] removedItems = dataBundle.getStringArray("REMOVED_ITEMS");
			ArrayList<Integer> newSaved = adapter.getSaved();
			int position = 0;
			//Toast.makeText(SearchActivity.this, removedItems.toString(), Toast.LENGTH_SHORT).show();
			for(String result: resultList) {
				String [] tokens = result.split(":");
				String key = String.format("%d:%d", Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])); // volume : page
				Log.i("KEY", key);
				for(String rItem: removedItems) {
					Log.i("ITEM", rItem);
					if(rItem.equals(key)) {
						newSaved.remove(new Integer(position));
						break;
					}
				}
				position++;
			}
			adapter.setSaved(newSaved);
			try {
				savedResultsItem.setSaved(Utils.toStringBase64(newSaved));
				searchResultsDBAdapter.open();
				searchResultsDBAdapter.updateEntry(savedResultsItemPosition, savedResultsItem);
				searchResultsDBAdapter.close();			
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        line1Size = prefs.getFloat("Line1Size", 12f);        
        line2Size = prefs.getFloat("Line2Size", 12f);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
        line1Size = prefs.getFloat("Line1Size", 12f);        
        line2Size = prefs.getFloat("Line2Size", 12f);
	}
	
	
	private void updateClickedStatusData(long position, SearchResultsItem item) {
		try {
			String pClicked = Utils.toStringBase64(adapter.getPrimaryClicked());
			String sClicked = Utils.toStringBase64(adapter.getPrimaryClicked());
			String mClicked = Utils.toStringBase64(adapter.getSaved());
			item.setPrimaryClicked(pClicked);
			item.setSecondaryClicked(sClicked);
			item.setSaved(mClicked);

			searchResultsDBAdapter.open();
			searchResultsDBAdapter.updateEntry(position, item);
			searchResultsDBAdapter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}		
	}
	

	public void toggleStar(int _position) {
		if(adapter.isMarked(_position)) {
			adapter.clearMarkedPosition(_position);
		} else {
			adapter.addMarkedPosition(_position);
		}
		
		ArrayList<Integer> newMarked = adapter.getMarked();		
		
		try {
			savedResultsItem.setMarked(Utils.toStringBase64(newMarked));
			searchResultsDBAdapter.open();
			searchResultsDBAdapter.updateEntry(savedResultsItemPosition, savedResultsItem);
			searchResultsDBAdapter.close();			
		} catch(IOException e) {
			e.printStackTrace();
		}		
	}
	
	private void deleteMemoAt(String _language, int _volume, int _page, String _keywords, int _position) {
		bookmarkDBAdapter.open();
		Cursor c = bookmarkDBAdapter.getEntries(lang, _volume, _page, _keywords);
		c.moveToFirst();
		while(!c.isAfterLast()) {
			bookmarkDBAdapter.removeEntry(c.getInt(0));
			c.moveToNext();
		}
		bookmarkDBAdapter.close();
		adapter.clearSavedPosition(_position);
		updateClickedStatusData(savedResultsItemPosition, savedResultsItem);		
	}
	
	private void memoAt(int _volume, int _item, int _page, String _language, String _keywords, int _position) {
		final Dialog memoDialog = new Dialog(SearchActivity.this);
		memoDialog.setContentView(R.layout.memo_dialog);
		
		final Button memoBtn = (Button)memoDialog.findViewById(R.id.memo_btn);
		final EditText memoText = (EditText)memoDialog.findViewById(R.id.memo_text);
		
		final int volume = _volume;
		final int item = _item;
		final int page = _page;
		final String language = _language;
		final String keywords = _keywords;
		final int position = _position;
		
		memoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bookmarkDBAdapter.open();
				BookmarkItem bookmarkItem = new BookmarkItem(language, volume, page, item, memoText.getText().toString(), keywords);
				
				if(!bookmarkDBAdapter.isDuplicated(bookmarkItem)) {
					bookmarkDBAdapter.insertEntry(bookmarkItem);
					// update list status
					adapter.addSavedPosition(position);
					try {
						String saved = Utils.toStringBase64(adapter.getSaved());
						savedResultsItem.setSaved(saved);
						searchResultsDBAdapter.open();
						searchResultsDBAdapter.updateEntry(savedResultsItemPosition, savedResultsItem);
						searchResultsDBAdapter.close();
					} catch(IOException e) {
						e.printStackTrace();
					}														
					Toast.makeText(SearchActivity.this, getString(R.string.memo), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(SearchActivity.this, getString(R.string.duplicated_item), Toast.LENGTH_SHORT).show();
				}
				bookmarkDBAdapter.close();
				memoDialog.dismiss();
			}
		});
		
		memoDialog.setCancelable(true);
		String title1 = "";
		if(language.equals("thai")) {
			title1 = getString(R.string.th_tipitaka_label) + " " + getString(R.string.th_lang);
		} else if(language.equals("pali")) {
			title1 = getString(R.string.th_tipitaka_label) + " " + getString(R.string.pl_lang);
		}
		
		TextView sub_title = (TextView)memoDialog.findViewById(R.id.memo_sub_title);
		String title2 = getString(R.string.th_book_label) + " " + Utils.arabic2thai(Integer.toString(volume), getResources());
		title2 = title2 + " " + getString(R.string.th_page_label) + " " + Utils.arabic2thai(Integer.toString(page), getResources());
		title2 = title2 + " " + getString(R.string.th_items_label) + " " + Utils.arabic2thai(Integer.toString(item), getResources());
		sub_title.setText(title2);
		memoDialog.setTitle(title1);
		memoDialog.show();		
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	searchHistoryDBAdapter = new SearchHistoryDBAdapter(this);
    	searchResultsDBAdapter = new SearchResultsDBAdapter(this);
    	bookmarkDBAdapter = new BookmarkDBAdapter(this);
    	//searchHistoryDBAdapter.open();        
        
        setContentView(R.layout.results_list);
        
        Context context = getApplicationContext();
        prefs =  PreferenceManager.getDefaultSharedPreferences(context);
        
        line1Size = prefs.getFloat("Line1Size", 12f);        
        line2Size = prefs.getFloat("Line2Size", 12f);        
        
        statusText = (TextView) findViewById(R.id.result_status);
        statusText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(SearchActivity.this, "OK", Toast.LENGTH_SHORT).show();
			}
		});
        
        TextView vinaiLabel = (TextView) findViewById(R.id.vinai_label);
        vinaiLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPosVinai != Integer.MAX_VALUE) {
					resultView.setSelected(true);
					resultView.setSelection(firstPosVinai);
				}
			}
		});
        
        TextView vinaiLabel2 = (TextView) findViewById(R.id.npage1);
        vinaiLabel2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPosVinai != Integer.MAX_VALUE) {
					resultView.setSelected(true);
					resultView.setSelection(firstPosVinai);
				}
			}
		});

        TextView vinaiLabel3 = (TextView) findViewById(R.id.nsutt1);
        vinaiLabel3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPosVinai != Integer.MAX_VALUE) {
					resultView.setSelected(true);
					resultView.setSelection(firstPosVinai);
				}
			}
		});
        
        
        TextView suttanLabel = (TextView) findViewById(R.id.suttan_label);
        suttanLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPosSuttan != Integer.MAX_VALUE) {
					resultView.setSelected(true);
					resultView.setSelection(firstPosSuttan);
				}
			}
		});
        
        TextView suttanLabel2 = (TextView) findViewById(R.id.npage2);
        suttanLabel2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPosSuttan != Integer.MAX_VALUE) {
					resultView.setSelected(true);
					resultView.setSelection(firstPosSuttan);
				}
			}
		});

        TextView suttanLabel3 = (TextView) findViewById(R.id.nsutt2);
        suttanLabel3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPosSuttan != Integer.MAX_VALUE) {
					resultView.setSelected(true);
					resultView.setSelection(firstPosSuttan);
				}
			}
		});
        
        
        TextView abhidumLabel = (TextView) findViewById(R.id.abhidum_label);
        abhidumLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPosAbhidhum != Integer.MAX_VALUE) {
					resultView.setSelected(true);
					resultView.setSelection(firstPosAbhidhum);
				}
			}
		});

        TextView abhidumLabel2 = (TextView) findViewById(R.id.npage3);
        abhidumLabel2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPosAbhidhum != Integer.MAX_VALUE) {
					resultView.setSelected(true);
					resultView.setSelection(firstPosAbhidhum);
				}
			}
		});

        TextView abhidumLabel3 = (TextView) findViewById(R.id.nsutt3);
        abhidumLabel3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPosAbhidhum != Integer.MAX_VALUE) {
					resultView.setSelected(true);
					resultView.setSelection(firstPosAbhidhum);
				}
			}
		});        
        
        searchText = (TextView) findViewById(R.id.search_word);
        resultView = (ListView) findViewById(R.id.result_list);
        table = (TableLayout) findViewById(R.id.table_layout);
        divider1 = findViewById(R.id.result_divider_1);
        divider2 = findViewById(R.id.result_divider_2);
        
        
        // long click action
        resultView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
				
				String starLabel;
				final Integer position = new Integer(arg2);
				if (adapter.isMarked(position)) {
					starLabel = getString(R.string.unmarked);
				} else {
					starLabel = getString(R.string.marked);
				}
				
				final CharSequence[] items = {
						getString(R.string.unread), 
						getString(R.string.unread_all), 
						getString(R.string.memo),
						getString(R.string.delete_memo),
						starLabel};
				
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String [] tokens = resultList.get(position).split(":");
						final int volume = Integer.parseInt(tokens[1]);
						final int page = Integer.parseInt(tokens[2]);
						final int item = Integer.parseInt(tokens[3].split("\\s+")[0]);
						AlertDialog.Builder builder;
						switch(which) {
							case 0: // unread
								adapter.clearClickedPosition(position);
								updateClickedStatusData(savedResultsItemPosition, savedResultsItem);
								break;
							case 1: // unread all
								builder = new AlertDialog.Builder(SearchActivity.this);   
								builder.setTitle(getString(R.string.unread_all_items));
								builder.setIcon(android.R.drawable.ic_dialog_alert);
								builder.setMessage(getString(R.string.confirm_unread_all_items));
								builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										adapter.clearClickedPosition();
										updateClickedStatusData(savedResultsItemPosition, savedResultsItem);
									}
								});
								builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										return;
									}
								});
								builder.show();
								break;
							case 2: // memo
								//Toast.makeText(SearchActivity.this, resultList.get(position), Toast.LENGTH_SHORT).show();
								memoAt(volume, item, page ,lang, savedQuery, position);
								break;
							case 3: // delete memo
								bookmarkDBAdapter.open();
								int memoCount = bookmarkDBAdapter.getEntries(lang, volume, page, savedQuery).getCount();
								bookmarkDBAdapter.close();
								if (memoCount > 0) {
									builder = new AlertDialog.Builder(SearchActivity.this);   
									builder.setTitle(getString(R.string.delete_memo));
									builder.setIcon(android.R.drawable.ic_dialog_alert);
									builder.setMessage(getString(R.string.confirm_delete_memo));
									builder.setPositiveButton(getString(R.string.yes), 
											new DialogInterface.OnClickListener() {									
										@Override
										public void onClick(DialogInterface dialog, int which) {
											deleteMemoAt(lang, volume, page, savedQuery, position);
											Toast.makeText(SearchActivity.this, R.string.deleted_memo, 
													Toast.LENGTH_SHORT).show();
										}
									});
									builder.setNegativeButton(getString(R.string.no), 
											new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											return;
										}
									});
									builder.show();
									
								} else {
									adapter.clearSavedPosition(position);
									updateClickedStatusData(savedResultsItemPosition, savedResultsItem);										
									Toast.makeText(SearchActivity.this, 
											R.string.memo_not_found, Toast.LENGTH_SHORT).show();
								}
								break;
							case 4:
								toggleStar(position);
								break;
						}
					}
				});	
				builder.show();
				return false;
			}
		});
        
        // click action
        resultView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String [] tokens = ((String) resultList.get(arg2)).split(":");
				int volume = Integer.parseInt(tokens[1]);
				int page = Integer.parseInt(tokens[2]);
				
				//Toast.makeText(SearchPage.this, Integer.toString(arg2) + ":" + Long.toString(arg3), Toast.LENGTH_SHORT).show();
				
				adapter.addClickedPosition(arg2);				
				try {
					String pClicked = Utils.toStringBase64(adapter.getPrimaryClicked());
					savedResultsItem.setPrimaryClicked(pClicked);
					searchResultsDBAdapter.open();
					searchResultsDBAdapter.updateEntry(savedResultsItemPosition, savedResultsItem);
					searchResultsDBAdapter.close();
				} catch(IOException e) {
					e.printStackTrace();
				}				
				
        		Intent intent = new Intent(SearchActivity.this, ReadBookActivity.class);
        		Bundle dataBundle = new Bundle();
        		dataBundle.putInt("VOL", volume);
        		dataBundle.putInt("PAGE", page);
        		dataBundle.putString("LANG", lang);
        		dataBundle.putString("QUERY", savedQuery);
        		
        		intent.putExtras(dataBundle);
        		startActivityForResult(intent,SHOW_READBOOK_ACTIVITY);								
				
			}
        	
        });
        
		intent = getIntent();
		Bundle dataBundle = intent.getExtras();
		if(dataBundle != null && dataBundle.containsKey("QUERY") && dataBundle.containsKey("LANG") && !dataBundle.containsKey("CONTENT")) {
			cateDialog = new Dialog(SearchActivity.this);						
			cateDialog.setContentView(R.layout.cate_dialog);
			cateDialog.setCancelable(true);
			cateDialog.setTitle(getString(R.string.select_cate));

			Button okBtn = (Button)cateDialog.findViewById(R.id.okcatebtn);
			final String query = dataBundle.getString("QUERY");
			final String lang = dataBundle.getString("LANG");
			okBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean b1 = ((CheckBox) cateDialog.findViewById(R.id.cb_vinai)).isChecked();
					boolean b2 = ((CheckBox) cateDialog.findViewById(R.id.cb_suttan)).isChecked();
					boolean b3 = ((CheckBox) cateDialog.findViewById(R.id.cb_abhidham)).isChecked();

					String sCate = "";
					if(b1) {
						sCate += "1";
					} else {
						sCate += "0";
					}
					if(b2) {
						sCate += "1";
					} else {
						sCate += "0";
					}
					if(b3) {
						sCate += "1";
					} else {
						sCate += "0";
					}
					
					if(b1 | b2 | b3) {
						cateDialog.dismiss();
						//TODO if the keywords was searched
						doSearch(query, lang);						
					}
				}
			});
			
			cateDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					SearchActivity.this.finish();
				}
			});
        	        	
        	table.setVisibility(View.INVISIBLE);

        	cateDialog.show();	
        	
		} else if (dataBundle != null && dataBundle.containsKey("LANG") && dataBundle.containsKey("QUERY")  && dataBundle.containsKey("CONTENT")) {
			String content = dataBundle.getString("CONTENT");
			String pClicked = dataBundle.getString("PCLICKED");
			String sClicked = dataBundle.getString("SCLICKED");
			String saved = dataBundle.getString("SAVED");
			String marked = dataBundle.getString("MARKED");
			String sCate = dataBundle.getString("SCATE");
						
			savedQuery = dataBundle.getString("QUERY");
			lang = dataBundle.getString("LANG");
			resultList.clear();
			
			// save result item and its position
			searchResultsDBAdapter.open();
			Cursor cursor = searchResultsDBAdapter.getEntries(lang, savedQuery, sCate);
			if(cursor.getCount() > 0 && cursor.moveToFirst()) {
				savedResultsItemPosition = cursor.getInt(SearchResultsDBAdapter.ID_COL);
				savedResultsItem = searchResultsDBAdapter.getEntry(savedResultsItemPosition);
			}
			cursor.close();
			searchResultsDBAdapter.close();
			
	        try {
	        	resultList = (ArrayList<String>) Utils.fromStringBase64(content);
	        	ArrayList<Integer> pClickedList = (ArrayList<Integer>)Utils.fromStringBase64(pClicked);
	        	ArrayList<Integer> sClickedList = (ArrayList<Integer>)Utils.fromStringBase64(sClicked);
	        	ArrayList<Integer> savedList = (ArrayList<Integer>)Utils.fromStringBase64(saved);
	        	ArrayList<Integer> markedList = (ArrayList<Integer>)Utils.fromStringBase64(marked);
				showResults(resultList, false, savedQuery, pClickedList, sClickedList, savedList, markedList);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        } catch (ClassNotFoundException e) {
	        	e.printStackTrace();
	        }
		}
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		super.onOptionsItemSelected(item);
		SharedPreferences.Editor editor = prefs.edit();
		
		switch (item.getItemId()) {
			case R.id.zoom_in_result:
				line1Size=prefs.getFloat("Line1Size", 12f)+1;
				line2Size=prefs.getFloat("Line2Size", 12f)+1;
				editor.putFloat("Line1Size", line1Size);
				editor.putFloat("Line2Size", line2Size);
				editor.commit();
				adapter.notifyDataSetChanged();
				return true;
			case R.id.zoom_out_result:
				line1Size=prefs.getFloat("Line1Size", 12f)-1;
				line2Size=prefs.getFloat("Line2Size", 12f)-1;
				editor.putFloat("Line1Size", line1Size);
				editor.putFloat("Line2Size", line2Size);
				editor.commit();
				adapter.notifyDataSetChanged();
				return true;
			case R.id.jump_to_result_item:
				final Dialog dialog = new Dialog(SearchActivity.this);
				dialog.setContentView(R.layout.goto_position_dialog);
				dialog.setTitle(R.string.goto_result_position);
				((Button)dialog.findViewById(R.id.goto_position_btn)).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String input = ((EditText)dialog.findViewById(R.id.goto_position_edittext)).getText().toString();
						resultView.setSelected(true);
						resultView.setSelection(Integer.parseInt(input)-1);						
						dialog.dismiss();
					}
				});
				dialog.show();
				return true;
			case R.id.results_bookmark:
	    		Intent intent;
	    		if(lang.equals("thai")) {
	    			intent = new Intent(SearchActivity.this, BookmarkThaiActivity.class);
	    		} else {
	    			intent = new Intent(SearchActivity.this, BookmarkPaliActivity.class);
	    		}
	    		Bundle dataBundle = new Bundle();
	    		dataBundle.putString("KEYWORDS", savedQuery);
	    		intent.putExtras(dataBundle);
	    		startActivityForResult(intent, SHOW_BOOKMARK_ACTIVITY);	
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.search_menu, menu);
	    
	    return true;
	}		
	    
    
    /*
    private void hideKeyboard() {
    	InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }*/
	
}