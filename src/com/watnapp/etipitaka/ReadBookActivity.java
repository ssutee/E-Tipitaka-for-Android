package com.watnapp.etipitaka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ReadBookActivity extends Activity { //implements OnGesturePerformedListener {
	private EditText textContent;
	private TextView pageLabel;
	private TextView itemsLabel;
	private TextView headerLabel;
	private Gallery gPage;
	
	private MainTipitakaDBAdapter mainTipitakaDBAdapter;
	
	//private DataBaseHelper dbhelper = null;
	private int selected_volume;
	private int selected_page;
	
	private int jumpItem;
	private boolean isJump = false;
	private int toPosition = -1;
	//private int jumpLine = 0;
	
	private ImageButton nextBtn;
	private ImageButton backBtn;
	private ImageButton zoomInBtn;
	private ImageButton zoomOutBtn;
	//private Button gotoBtn;
	private Handler mHandler = new Handler();
	private View main;
	private String keywords = "";
	private Dialog dialog;
	private Dialog selectDialog;
	private Dialog itemsDialog;
	private Dialog memoDialog;
	private EditText edittext;
	private String savedItems;
	private int [] npage_thai;
	private int [] npage_pali;
	private int [] nitem;
	private final int autoHideTime = 2000;
	private String [] found_pages;
	private String lang = "thai";
	private float textSize = 0f;
	private ScrollView scrollview;
	//private boolean isZoom = false;
	private boolean searchCall = false;
	private String bmLang;
	private int bmVolume;
	private int bmPage;
	private int bmItem;
	private EditText memoText;
	private BookmarkDBAdapter bookmarkDBAdapter = null;
	private SearchDialog searchDialog = null;
	
	//GestureLibrary mLibrary;
	
	SharedPreferences prefs;
	
	// save read pages for highlighting in the results list
	private ArrayList<String> savedReadPages = null;

    @Override
    public boolean onSearchRequested() {
    	searchDialog = new SearchDialog(ReadBookActivity.this, lang);
    	searchDialog.show();
		
    	return true;
    }
    
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			Intent result = new Intent();
			//Toast.makeText(this, savedReadPages.toString(), Toast.LENGTH_SHORT).show();
			
			String [] tmp = new String[savedReadPages.size()];
			savedReadPages.toArray(tmp);

			result.putExtra("READ_PAGES", tmp);
			setResult(RESULT_CANCELED, result);
				
			this.finish();
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.goto_menu, menu);
	    return true;
	}
	
	private int getSubVolume(int volume, int item, int page, String language) {
		mainTipitakaDBAdapter.open();
		Cursor p_cursor = mainTipitakaDBAdapter.getPageByItem(volume, item, language, false);
		
		//Log.i("INFO", volume+":"+item+":"+language);
		
		if(p_cursor.getCount() == 1) {
			p_cursor.moveToFirst();
			//Log.i("PAGE", p_cursor.getString(0));
			p_cursor.close();
			mainTipitakaDBAdapter.close();
			return 0;
		}
		
		//Log.i("FOUND", p_cursor.getCount()+"");
		p_cursor.moveToFirst();
		int i = 0;
		int prev = -1;
		int now = 0;
		ArrayList<String> tmp1 = new ArrayList<String>();
		ArrayList<ArrayList<String>> tmp2 = new ArrayList<ArrayList<String>>();
		while(!p_cursor.isAfterLast()) {
			now = Integer.parseInt(p_cursor.getString(0));
			//Log.i("PAGE", p_cursor.getString(0));
			if(prev == -1) {
				tmp1.add(p_cursor.getString(0));
			} else {
				if(now == prev+1) {
					tmp1.add(p_cursor.getString(0));
				} else {
					tmp2.add(tmp1);
					tmp1 = new ArrayList<String>();
					tmp1.add(p_cursor.getString(0));
				}
			}
			p_cursor.moveToNext();
			i++;
			prev = now;

		}
		tmp2.add(tmp1);
		
		i = 0;
		boolean isFound = false;
		for(ArrayList<String> al : tmp2) {
			if(al.contains(Integer.toString(page))) {
				isFound = true;
				break;
			}
			i++;
		}
		p_cursor.close();
		mainTipitakaDBAdapter.close();
		
		if(!isFound)
			return 0;
				
		return i;
	}
	
	private void jumpTo(int volume, int item, int page, int sub, String language) {
		jumpItem = item;
		isJump = true;
		
		mainTipitakaDBAdapter.open();
		Cursor n_cursor = mainTipitakaDBAdapter.getPageByItem(volume, item, language, true);
		n_cursor.moveToPosition(sub);
		int new_page = 1;
		
		if(n_cursor.getCount() > 0) {
			new_page = Integer.parseInt(n_cursor.getString(0));
		}
		
		n_cursor.close();
		mainTipitakaDBAdapter.close();
		
		setGalleryPages(new_page);
		
	}
	
	private void memoAt(int volume, int item, int page, String language) {
		memoDialog = new Dialog(ReadBookActivity.this);
		memoDialog.setContentView(R.layout.memo_dialog);
		
		bmLang = language;
		bmVolume = volume;
		bmPage = page;
		bmItem = item;
		
		
		Button memoBtn = (Button)memoDialog.findViewById(R.id.memo_btn);
		memoText = (EditText)memoDialog.findViewById(R.id.memo_text);
		
		memoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bookmarkDBAdapter.open();
				BookmarkItem bookmarkItem = new BookmarkItem(bmLang, bmVolume, bmPage, bmItem, memoText.getText().toString(),"");
				long row = bookmarkDBAdapter.insertEntry(bookmarkItem);
				bookmarkDBAdapter.close();
				Toast.makeText(ReadBookActivity.this, getString(R.string.memo), Toast.LENGTH_SHORT).show();
				memoDialog.dismiss();
			}
		});
		
		memoDialog.setCancelable(true);
		String title1 = "";
		if(lang.equals("thai")) {
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
	
	private void memoItem() {
		String [] items = savedItems.split("\\s+");
		selected_page = gPage.getSelectedItemPosition() + 1;

		if(items.length > 1) {
			itemsDialog = new Dialog(ReadBookActivity.this);						
			itemsDialog.setContentView(R.layout.select_dialog);
			itemsDialog.setCancelable(true);
			
			itemsDialog.setTitle(getString(R.string.select_item_memo));
			
			ListView pageView = (ListView) itemsDialog.findViewById(R.id.list_pages);						
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ReadBookActivity.this, R.layout.page_item, R.id.show_page);			

			for(String item : items) {
				dataAdapter.add(getString(R.string.th_items_label) + " " + Utils.arabic2thai(item, getResources()));
			}
			
			pageView.setAdapter(dataAdapter);			
			pageView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					String [] items = savedItems.split("\\s+");
					memoAt(selected_volume, Integer.parseInt(items[arg2]), selected_page, lang);
					itemsDialog.dismiss();
				}
				
			});			
			itemsDialog.show();			
		} else {
			memoAt(selected_volume, Integer.parseInt(items[0]), selected_page, lang);
		}		
	}
	
	private void swap() {
		int scrollPosition = scrollview.getScrollY();		
		selected_page = gPage.getSelectedItemPosition() + 1;
		saveReadingState(lang, selected_page, scrollPosition);
		
		if(lang.equals("thai")) {
			lang = "pali";
		} else if(lang.equals("pali")) {
			lang = "thai";
		}
		
		selected_page = prefs.getInt(lang+":PAGE", 1);
		toPosition = prefs.getInt(lang+":POSITION", 0);
		isJump = true;
		
		setGalleryPages(selected_page);
		
	}
	
	private void compare() {
		Log.i("ITEM",savedItems);
		String [] items = savedItems.split("\\s+");
		selected_page = gPage.getSelectedItemPosition() + 1;

		int scrollPosition = scrollview.getScrollY();
		saveReadingState(lang, selected_page, scrollPosition);
		
		if(items.length > 1) {
			itemsDialog = new Dialog(ReadBookActivity.this);						
			itemsDialog.setContentView(R.layout.select_dialog);
			itemsDialog.setCancelable(true);
			
			itemsDialog.setTitle(getString(R.string.select_item_compare));
			
			ListView pageView = (ListView) itemsDialog.findViewById(R.id.list_pages);						
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ReadBookActivity.this, R.layout.page_item, R.id.show_page);			

			for(String item : items) {
				dataAdapter.add(getString(R.string.th_items_label) + " " + Utils.arabic2thai(item, getResources()));
			}
			
			pageView.setAdapter(dataAdapter);			
			pageView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					String [] items = savedItems.split("\\s+");
					int sub = getSubVolume(selected_volume, Integer.parseInt(items[arg2]), selected_page, lang);
					if(lang.equals("thai")) {
						lang = "pali";
					} else if(lang.equals("pali")) {
						lang = "thai";
					}
					jumpTo(selected_volume, Integer.parseInt(items[arg2]), selected_page, sub, lang);
					itemsDialog.dismiss();
				}
			});			
			itemsDialog.show();			
		} else {
			int sub = getSubVolume(selected_volume, Integer.parseInt(items[0]), selected_page, lang);
			if(lang.equals("thai")) {
				lang = "pali";
			} else if(lang.equals("pali")) {
				lang = "thai";
			}
			jumpTo(selected_volume, Integer.parseInt(items[0]), selected_page, sub, lang);
		}
				
	}
	
	private void gotoPage() {
    	dialog = new Dialog(ReadBookActivity.this);
    	dialog.setContentView(R.layout.goto_dialog);
    	dialog.setCancelable(true);
    	Button gotoBtn = (Button)dialog.findViewById(R.id.gotobtn);
    	edittext = (EditText) dialog.findViewById(R.id.edittext);
    	edittext.setHint(R.string.enter_page);
    
    	int p = 0;
    	if(lang.equals("thai"))
    		p = npage_thai[selected_volume-1];
    	else if(lang.equals("pali"))
    		p = npage_pali[selected_volume-1];
    	    	
    	dialog.setTitle(getResources().getString(R.string.between_page) + " " + Utils.arabic2thai("1", getResources()) + " - " + Utils.arabic2thai(Integer.toString(p), getResources()));
    	
    	gotoBtn.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				try {
					int page = Integer.parseInt(edittext.getText().toString());
					
					if(page <= gPage.getCount()) {
						gPage.setSelection(page-1);
						dialog.dismiss();
					}
				} catch (java.lang.NumberFormatException e) { }				
			}
				
		});
    	
    	dialog.show();		
	}
	
	private void gotoItem() {
    	dialog = new Dialog(ReadBookActivity.this);
    	dialog.setContentView(R.layout.goto_dialog);
    	dialog.setCancelable(true);
    	Button gotoBtn = (Button)dialog.findViewById(R.id.gotobtn);
    	edittext = (EditText) dialog.findViewById(R.id.edittext);
    	edittext.setHint(R.string.enter_item);
    	
    	dialog.setTitle(getResources().getString(R.string.between_item) + " " + Utils.arabic2thai("1", getResources()) + " - " + Utils.arabic2thai(Integer.toString(nitem[selected_volume-1]), getResources()));
    	
    	gotoBtn.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				try {
					int item = Integer.parseInt(edittext.getText().toString());
					jumpItem = item;
					isJump = true;					
					//dbhelper.openDataBase();
					mainTipitakaDBAdapter.open();
					Cursor cursor = mainTipitakaDBAdapter.getPageByItem(selected_volume, item, lang, true);
					//Toast.makeText(ReadBookActivity.this, Integer.toString(cursor.getCount()), Toast.LENGTH_SHORT).show();
					int n = cursor.getCount();
					if (n == 1) {
						cursor.moveToFirst();
						String sPage = cursor.getString(0);
						//Toast.makeText(ReadBookActivity.this, sPage, Toast.LENGTH_SHORT).show();
						gPage.setSelection(Integer.parseInt(sPage)-1);
						dialog.dismiss();
					} else if (n > 1) {
						dialog.dismiss();
						
						selectDialog = new Dialog(ReadBookActivity.this);						
						selectDialog.setContentView(R.layout.select_dialog);
						selectDialog.setCancelable(true);
						
						selectDialog.setTitle(getResources().getString(R.string.th_items_label) + " " + Utils.arabic2thai(Integer.toString(item), getResources()) + " " + getResources().getString(R.string.more_found));
						
						ListView pageView = (ListView) selectDialog.findViewById(R.id.list_pages);
												
						ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ReadBookActivity.this, R.layout.page_item, R.id.show_page);

						found_pages = new String[cursor.getCount()];
						cursor.moveToFirst();
						int i = 0;
						while(!cursor.isAfterLast()) {
							found_pages[i] = cursor.getString(0);
							dataAdapter.add(getResources().getString(R.string.th_page_label) + " " + Utils.arabic2thai(cursor.getString(0), getResources()));
							cursor.moveToNext();
							i++;
						}
						
						pageView.setAdapter(dataAdapter);
						
						pageView.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
								gPage.setSelection(Integer.parseInt(found_pages[arg2])-1);
								selectDialog.dismiss();
							}
							
						});
						
						
						
						selectDialog.show();
					}
					cursor.close();
					mainTipitakaDBAdapter.close();

				} catch (java.lang.NumberFormatException e) { }
				
			}
				
		});
    	
    	dialog.show();		
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		super.onOptionsItemSelected(item);
		
		//SharedPreferences.Editor editor = prefs.edit();
		
		switch (item.getItemId()) {
	    case R.id.goto_item:
	    	gotoItem();
	        return true;
	    case R.id.goto_page:
	    	gotoPage();
	        return true;
	    case R.id.compare:
	    	compare();
	    	return true;
	    case R.id.swap:
	    	swap();
	    	return true;
	    case R.id.memo:
	    	memoItem();
	    	return true;
	    default:
	        return false;
	    }
	}	
		
	private void setGalleryPages(int currentPage) {		
		String [] t_pages = null;
		int n = 0;
				
		if(lang.equals("thai")) {
			t_pages = new String[npage_thai[selected_volume-1]];
			n = npage_thai[selected_volume-1];
		} else if(lang.equals("pali")) {
			t_pages = new String[npage_pali[selected_volume-1]];
			n = npage_pali[selected_volume-1];
		}
		
        for(int i=1; i<=n; i++) {
        	t_pages[i-1] = Utils.arabic2thai(Integer.toString(i), getResources());
        }        
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.my_gallery_item_2, t_pages);        
        gPage.setAdapter(adapter);
        gPage.setSelection(currentPage-1);
	}
	
	private Runnable mHideButtons = new Runnable() {
		public void run() {
			//Toast.makeText(ReadPage.this, "Hide them", Toast.LENGTH_SHORT).show();
			nextBtn.setVisibility(View.INVISIBLE);
			backBtn.setVisibility(View.INVISIBLE);
			zoomInBtn.setVisibility(View.INVISIBLE);
			zoomOutBtn.setVisibility(View.INVISIBLE);
			//gotoBtn.setVisibility(View.INVISIBLE);
			
			main.requestLayout();
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		saveReadingState("thai", 1, 0);
		saveReadingState("pali", 1, 0);
		
		//if (dbhelper != null && dbhelper.isOpened())
		//	dbhelper.close();
		//if(bookmarkDBAdapter != null)
		//	bookmarkDBAdapter.close();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		textContent.setTextSize(textSize);
		if(searchDialog != null) {
			searchDialog.updateHistoryList();
		}
        /*if(lang == "thai") {
        	npage = getResources().getIntArray(R.array.npage_thai);
        }
        else if(lang == "pali") {
        	npage = getResources().getIntArray(R.array.npage_pali);
        	
        }*/
        //Toast.makeText(this, "Restart", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		textContent.setTextSize(textSize);
		/*
		int p = 0;
		if(lang == "thai") {
        	p = npage_thai.length;
        }
        else if(lang == "pali") {
        	p = npage_pali.length;
        }
        
        Toast.makeText(this, Integer.toString(p), Toast.LENGTH_SHORT).show();*/
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        //if (!mLibrary.load()) {
        //    finish();
        //}        
        
        Context context = getApplicationContext();
        prefs =  PreferenceManager.getDefaultSharedPreferences(context);
        textSize = prefs.getFloat("TextSize", 16f);
        
        //Toast.makeText(this, "Create", Toast.LENGTH_SHORT).show();
        
        main =  View.inflate(this, R.layout.read, null);
        setContentView(main);
        
        //GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
        //gestures.addOnGesturePerformedListener(this);
        
        savedReadPages = new ArrayList<String>();
        
        //dbhelper = new DataBaseHelper(this);
        //dbhelper.openDataBase();

        mainTipitakaDBAdapter = new MainTipitakaDBAdapter(this);
        bookmarkDBAdapter = new BookmarkDBAdapter(this);
        //bookmarkDBAdapter.open();
        
        final Resources res = getResources();
        
    	npage_thai = res.getIntArray(R.array.npage_thai);   
    	npage_pali = res.getIntArray(R.array.npage_pali);
        	
        nitem = res.getIntArray(R.array.nitem);
        
        textContent = (EditText)findViewById(R.id.main_text);
        
        pageLabel = (TextView) findViewById(R.id.page_label);
        itemsLabel = (TextView) findViewById(R.id.items_label);
        headerLabel = (TextView) findViewById(R.id.header);
        nextBtn = (ImageButton) findViewById(R.id.nextbtn);
        backBtn = (ImageButton) findViewById(R.id.backbtn);
        zoomInBtn = (ImageButton) findViewById(R.id.zoominbtn);
        zoomOutBtn = (ImageButton) findViewById(R.id.zoomoutbtn);
       // gotoBtn = (Button) findViewById(R.id.gotobtn);
        
		scrollview = (ScrollView)findViewById(R.id.scrollview);
		scrollview.setSmoothScrollingEnabled(false);
		        
		nextBtn.setVisibility(View.INVISIBLE);
		backBtn.setVisibility(View.INVISIBLE);
		zoomInBtn.setVisibility(View.INVISIBLE);
		zoomOutBtn.setVisibility(View.INVISIBLE);
		//gotoBtn.setVisibility(View.INVISIBLE);
		
		main.requestLayout();
        
        gPage = (Gallery) findViewById(R.id.gallery_page);
        
        //final int [] npage = res.getIntArray(R.array.npage);
                
		if(ReadBookActivity.this.getIntent().getExtras() != null) {
			Bundle dataBundle = ReadBookActivity.this.getIntent().getExtras();
			int vol = dataBundle.getInt("VOL");
			int page = dataBundle.getInt("PAGE");
			lang = dataBundle.getString("LANG");
			savedReadPages.clear();
			
			if (dataBundle.containsKey("QUERY")) {
				keywords = dataBundle.getString("QUERY");
				searchCall = true;
				isJump = true;
			} else if(dataBundle.containsKey("ITEM")) {
				isJump = true;
				jumpItem = dataBundle.getInt("ITEM");
			}

			selected_volume = vol;
			setGalleryPages(page);
		}
		
		gPage.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				savedReadPages.add(selected_volume+":"+(arg2+1));
				mainTipitakaDBAdapter.open();
				Cursor cursor = mainTipitakaDBAdapter.getContent(selected_volume, arg2+1, lang);
				cursor.moveToFirst();
				String content = cursor.getString(1);
				
				// highlight keywords (yellow)
				if(keywords.trim().length() > 0) {
					keywords = keywords.replace('+', ' ');
					String [] tokens = keywords.split("\\s+");
					Arrays.sort(tokens, new StringLengthComparator());
					Collections.reverse(Arrays.asList(tokens));
					int count = 0;
					for(String token: tokens) {
						content = content.replace(token, String.format("<font color='#f9f109'><b>-:*%d*:-</b></font>", count));
						count++;
					}
					
					count = 0;
					for(String token: tokens) {
						content = content.replace(String.format("-:*%d*:-", count), token);						
						count++;
					}
				}				
				
				// highlight items numbers (orange)
				content = content.replaceAll(getString(R.string.regex_item), "<font color='#EE9A00'><b>$0</b></font>");
				
				textContent.setText(Html.fromHtml(content.replace("\n", "<br/>")));
				
				pageLabel.setText(res.getString(R.string.th_page_label) + "  " + 
						Utils.arabic2thai(Integer.toString(arg2+1), getResources()));
								
				savedItems = cursor.getString(0);	
				cursor.close();
				mainTipitakaDBAdapter.close();
				String [] tokens = savedItems.split("\\s+");
				String t_items = "";
				if(tokens.length > 1) {
					t_items = String.format("%s-%s", 
							Utils.arabic2thai(tokens[0], getResources()), 
							Utils.arabic2thai(tokens[tokens.length-1], getResources()));
				} else {
					t_items = Utils.arabic2thai(tokens[0], getResources());
				}
				
				String tmp = res.getString(R.string.th_items_label).trim() + " " + t_items;
				itemsLabel.setText(Html.fromHtml("<pre>"+tmp+"</pre>"));
				String header = getString(R.string.th_tipitaka_book).trim() + 
					" " + Utils.arabic2thai(Integer.toString(selected_volume), getResources());
				headerLabel.setText(header);
				
				String i_tmp = "";
				if(searchCall) {
					searchCall = false;
					i_tmp = keywords.split("\\s+")[0].replace('+', ' ');
				} else {
					i_tmp = "[" + Utils.arabic2thai(Integer.toString(jumpItem), getResources()) + "]";
				}
								
				if(isJump && toPosition == -1) {
					isJump = false;
					int offset =  textContent.getText().toString().indexOf(i_tmp);
					final int jumpLine = textContent.getLayout().getLineForOffset(offset);
					
					scrollview.postDelayed(new Runnable() {
						@Override
						public void run() {
							int y=0;
							if(jumpLine > 2)
								y = textContent.getLayout().getLineTop(jumpLine-2);
							else
								y = textContent.getLayout().getLineTop(0);
							scrollview.scrollTo(0, y);
						}
					},300);
				} else if(isJump && toPosition > -1) {
					isJump = false;
					scrollview.postDelayed(new Runnable() {
						@Override
						public void run() {
							scrollview.scrollTo(0, toPosition);
							toPosition = -1;
						}
					},300);
				} else {
					scrollview.fullScroll(View.FOCUS_UP);
				}
				gPage.requestFocus();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
		
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				readNext();
				mHandler.removeCallbacks(mHideButtons);
				mHandler.postDelayed(mHideButtons, autoHideTime);				
			}
			
		});

		backBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				readBack();
				mHandler.removeCallbacks(mHideButtons);
				mHandler.postDelayed(mHideButtons, autoHideTime);
				
			}
			
		});
		
		
		zoomInBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = prefs.edit();
		    	textSize = prefs.getFloat("TextSize", 16f);
		    	textContent.setTextSize(textSize+1);
		    	editor.putFloat("TextSize", textSize+1);
		    	editor.commit();
				
			}
		});
		
		zoomOutBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = prefs.edit();
		    	textSize = prefs.getFloat("TextSize", 16f);
		    	textContent.setTextSize(textSize-1);
		    	editor.putFloat("TextSize", textSize-1);
		    	editor.commit();		
				
			}
		});
		
		textContent.setOnTouchListener(new OnTouchListener () {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				nextBtn.bringToFront();
				backBtn.bringToFront();
				zoomInBtn.bringToFront();
				zoomOutBtn.bringToFront();
				
				nextBtn.setVisibility(View.VISIBLE);
				backBtn.setVisibility(View.VISIBLE);
				zoomInBtn.setVisibility(View.VISIBLE);
				zoomOutBtn.setVisibility(View.VISIBLE);
				//gotoBtn.setVisibility(View.VISIBLE);
				main.requestLayout();
				
				mHandler.removeCallbacks(mHideButtons);
				mHandler.postDelayed(mHideButtons, autoHideTime);
				
				/*
				// multi-touch zoom in and zoom out
				if(event.getPointerCount() > 1) {
					float dist = spacing(event);
					//Log.i("SPACE", Float.toString(dist));
					return true;
				}*/
				
				return false;
			}
			
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
	        //gPage.setVisibility(View.GONE);
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	        //gPage.setVisibility(View.VISIBLE);
	    }
	}	
	
	/*
	private float spacing(MotionEvent event) {
		   float x = event.getX(0) - event.getX(1);
		   float y = event.getY(0) - event.getY(1);
		   return FloatMath.sqrt(x * x + y * y);
	}
	*/

	private void saveReadingState(String _lang, int page, int scrollPosition) {
		SharedPreferences.Editor editor = prefs.edit();
    	editor.putInt(_lang+":PAGE", page);
    	editor.putInt(_lang+":POSITION", scrollPosition);
    	editor.commit();		
	}
	
	private void readNext() {
		int pos = gPage.getSelectedItemPosition();
		if(pos+1 < gPage.getCount()) {
			gPage.setSelection(pos+1);
		}		
	}
	
	private void readBack() {
		int pos = gPage.getSelectedItemPosition();
		if(pos-1 >= 0) {
			gPage.setSelection(pos-1);
		}		
	}
	
	/*
	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
	    ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
	    if (predictions.size() > 0 && predictions.get(0).score > 1.0) {
	        String action = predictions.get(0).name;
	        if ("left".equals(action)) {
	        	readNext();
	        } else if ("right".equals(action)) {
	        	readBack();
	        } 
	    }
	}*/	
	
}