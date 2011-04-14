package com.watnapp.etipitaka;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchDialog extends Dialog {

	private EditText searchText;
	private EditText codeText;
	private EditText numberText;
	private InputMethodManager imm;
	private Spinner langSpinner;
	private ListView historyList;
	private Context context;
	private String lang = "thai";
	private Button queryBtn;
	private SearchHistoryDBAdapter searchHistoryDBAdapter;
	private SearchResultsDBAdapter searchResultsDBAdapter;
	private ResultsCursorAdapter historyAdapter;
	private Dialog historyItemDialog;
	private Cursor savedCursor;
	private String sortKey;
	private boolean isDesc = false;
	private SharedPreferences prefs;	
	private TextView codeLabel;
	private TextView numberLabel;
		   	
	/*
	public SearchDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	protected SearchDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}
	*/
	
	private class ResultsCursorAdapter extends SimpleCursorAdapter {

		private int markedPosition = -1;
		
		public ResultsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
		}
		
		public void setMarkedPosition(int position) {
			markedPosition = position;
			this.notifyDataSetChanged();
		}
		
		public int getMarkedPosition() {
			return markedPosition;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			
			View splitter = view.findViewById(R.id.item_splitter);
			splitter.setBackgroundColor(Color.BLACK);
			
			String skey = prefs.getString("SORT_KEY", SearchHistoryDBAdapter.KEY_KEYWORDS);
			
			TextView codeLabel = (TextView)view.findViewById(R.id.priority_code);
			TextView numberLabel = (TextView)view.findViewById(R.id.priority_number);
			
			if(position > 0 && skey.equals(SearchHistoryDBAdapter.KEY_CODE)) {
				savedCursor.moveToPosition(position-1);
				String topPriority = savedCursor.getString(SearchHistoryDBAdapter.PRIORITY_COL);
				savedCursor.moveToPosition(position);
				String currentPriority = savedCursor.getString(SearchHistoryDBAdapter.PRIORITY_COL);
				
				if(topPriority.length() != currentPriority.length()) {
					splitter.setBackgroundColor(Color.LTGRAY);
				} else if (currentPriority.length() == 4) {
					if(!currentPriority.substring(0, 2).equals(topPriority.substring(0, 2))) {
						splitter.setBackgroundColor(Color.LTGRAY);
					}
				} 

				savedCursor.moveToPosition(position-1);
				String topCode = savedCursor.getString(SearchHistoryDBAdapter.CODE_COL);
				savedCursor.moveToPosition(position);
				String currentCode = savedCursor.getString(SearchHistoryDBAdapter.CODE_COL);
				
				if(!topCode.equals(currentCode)) {
					splitter.setBackgroundColor(Color.rgb(255, 215, 0));
				}
				codeLabel.setVisibility(View.VISIBLE);
				numberLabel.setVisibility(View.VISIBLE);
				
			} else if(!skey.equals(SearchHistoryDBAdapter.KEY_CODE)) {
				codeLabel.setVisibility(View.GONE);
				numberLabel.setVisibility(View.GONE);
			}
			
			TextView line3 = (TextView)view.findViewById(R.id.hline3);
			String text = line3.getText().toString();
			String [] tokens = text.split("\\s+");
			String output = "";
			for(String s: tokens) {
				if(s.startsWith(context.getString(R.string.ss_vinai))) {
					if(s.endsWith(context.getString(R.string.zero_zero))) {
						output += s + "  ";
					} else {
						output += String.format("<font color='#1E90FF'>%s</font>  ", s);
					}
				} else if(s.startsWith(context.getString(R.string.ss_suttan))) {
					if(s.endsWith(context.getString(R.string.zero_zero))) {
						output += s + "  ";
					} else {
						output += String.format("<font color='#FF4500'>%s</font>  ", s);
					}
				} else if(s.startsWith(context.getString(R.string.ss_abhidum))) {
					if(s.endsWith(context.getString(R.string.zero_zero))) {
						output += s + "  ";
					} else {
						output += String.format("<font color='#A020F0'>%s</font>  ", s);
					}
				}
			}
			line3.setText(Html.fromHtml(output.trim()));
			
			view.setBackgroundColor(Color.BLACK);
			if(markedPosition == position) {
				view.setBackgroundColor(Color.DKGRAY);
			}
			
			return view;
		}
	}
	
	public SearchDialog(Context _context, String _lang) {
		super(_context, R.style.searchDialogStyle);
		context = _context;
		lang = _lang;
		this.setContentView(R.layout.search_dialog);
		// TODO Auto-generated constructor stub
	}
		
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if(searchText.getText().toString().trim().length() > 0 || 
	    		codeText.getText().toString().trim().length() > 0 ||
	    		numberText.getText().toString().trim().length() > 0) {
	    		searchText.setText("");
	    		codeText.setText("");
	    		numberText.setText("");
	    		return true;
	    	}  else {
	    		this.dismiss();
	    		return true;
	    	}
	    } else if(keyCode == KeyEvent.KEYCODE_MENU) {
	    	// hide keyboard
            imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchText.getApplicationWindowToken(), 0);
            final Dialog sortDialog = new Dialog(context);
            sortDialog.setContentView(R.layout.sort_dialog);
            sortDialog.setTitle(R.string.sorting_prefs);
            
            sortKey = prefs.getString("SORT_KEY", SearchHistoryDBAdapter.KEY_KEYWORDS);
            isDesc = prefs.getBoolean("IS_DESC", false);
            if(sortKey.equals(SearchHistoryDBAdapter.KEY_KEYWORDS)) {
            	((RadioButton)sortDialog.findViewById(R.id.sort_by_keywords)).setChecked(true);
            } else if (sortKey.equals(SearchHistoryDBAdapter.KEY_ID)) {
            	((RadioButton)sortDialog.findViewById(R.id.sort_by_id)).setChecked(true);
            } else if (sortKey.equals(SearchHistoryDBAdapter.KEY_N_SUT)) {
            	((RadioButton)sortDialog.findViewById(R.id.sort_by_suts)).setChecked(true);
            } else if (sortKey.equals(SearchHistoryDBAdapter.KEY_FREQ)) {
            	((RadioButton)sortDialog.findViewById(R.id.sort_by_freq)).setChecked(true);
            } else if (sortKey.equals(SearchHistoryDBAdapter.KEY_CODE)) {
            	((RadioButton)sortDialog.findViewById(R.id.sort_by_priority)).setChecked(true);
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
						sortKey = SearchHistoryDBAdapter.KEY_ID;
						hideCodePanel();
						setupNormalPopupMenu();
					} else if(((RadioButton)sortDialog.findViewById(R.id.sort_by_keywords)).isChecked()) {
						sortKey = SearchHistoryDBAdapter.KEY_KEYWORDS;
						hideCodePanel();
						setupNormalPopupMenu();
					} else if(((RadioButton)sortDialog.findViewById(R.id.sort_by_suts)).isChecked()) {
						sortKey = SearchHistoryDBAdapter.KEY_N_SUT;
						hideCodePanel();
						setupNormalPopupMenu();
					} else if(((RadioButton)sortDialog.findViewById(R.id.sort_by_freq)).isChecked()) {
						sortKey = SearchHistoryDBAdapter.KEY_FREQ;
						hideCodePanel();
						setupNormalPopupMenu();
					} else if(((RadioButton)sortDialog.findViewById(R.id.sort_by_priority)).isChecked()) {
						sortKey = SearchHistoryDBAdapter.KEY_CODE;
						showCodePanel();
						setupFullPopupMenu();
					}
					
					editor.putBoolean("IS_DESC", isDesc);
					editor.putString("SORT_KEY", sortKey);
					
					editor.commit();
					sortDialog.dismiss();
					updateHistoryList(lang, searchText.getText().toString().trim());
				}
			});
            sortDialog.show();
	    }
	    return super.onKeyDown(keyCode, event);
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	//Toast.makeText(context, "Create", Toast.LENGTH_SHORT).show();
		
		prefs =  PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		sortKey = prefs.getString("SORT_KEY", SearchHistoryDBAdapter.KEY_KEYWORDS);
		isDesc = prefs.getBoolean("IS_DESC", false);
    	searchHistoryDBAdapter = new SearchHistoryDBAdapter(context);
    	searchResultsDBAdapter = new SearchResultsDBAdapter(context);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(true);
		
		queryBtn = (Button) findViewById(R.id.query_btn);
		queryBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String query = searchText.getText().toString();
				if(query.trim().length() > 0) {
	        		Intent intent = new Intent(context, SearchActivity.class);
	        		Bundle dataBundle = new Bundle();
	        		dataBundle.putString("LANG", lang);
	        		dataBundle.putString("QUERY", query);
	        		intent.putExtras(dataBundle);
	        		context.startActivity(intent);
	        		//SearchDialog.this.dismiss();
	        		//updateHistoryList(lang, searchText.getText().toString().trim());
				}
			}
		});
		
		codeLabel = (TextView)findViewById(R.id.code_label);
		codeText = (EditText)findViewById(R.id.code_text);
		codeText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String text = searchText.getText().toString();
				updateHistoryList(lang, text);
			}
		});
		
		numberLabel = (TextView)findViewById(R.id.number_label);
		numberText = (EditText)findViewById(R.id.number_text);
		numberText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String text = searchText.getText().toString();
				updateHistoryList(lang, text);
			}
		});
		
		searchText = (EditText) findViewById(R.id.search_text);
		searchText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String text = searchText.getText().toString();
				updateHistoryList(lang, text);
			}
		});
		
		//show keyboard
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.toggleSoftInput(0, 0);		
		
		langSpinner = (Spinner) this.findViewById(R.id.lang_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        		context, R.array.langs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langSpinner.setAdapter(adapter);
        if(lang.equals("thai")) {
        	langSpinner.setSelection(0);
        } else if(lang.equals("pali")) {
        	langSpinner.setSelection(1);
        }
        langSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				switch(arg2) {
					case 0:
						lang = "thai";
						searchText.setHint(R.string.enter_thai);
						break;
					case 1:
						lang = "pali";
						searchText.setHint(R.string.enter_pali);
						break;
				}
				updateHistoryList(lang, searchText.getText().toString().trim());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        
        historyList = (ListView) this.findViewById(R.id.search_history_listview);
        
        updateHistoryList(lang);
        

        historyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//searchHistoryDBAdapter.open();
				//Cursor cursor = searchHistoryDBAdapter.getEntries(lang);
				//cursor.moveToPosition(arg2);
				//String keywords = cursor.getString(SearchHistoryDBAdapter.KEYWORDS_COL);
				
				String keywords = ((TextView)arg1.findViewById(R.id.hline1)).getText().toString();
				//String keywords = line.substring(0, line.lastIndexOf('(')).trim();
				searchText.setText("");
				searchText.append(keywords);
				
				//cursor.close();
				//searchHistoryDBAdapter.close();
		        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			    imm.toggleSoftInput(0, 0);				
			}
		});
        
        historyList.setOnTouchListener(new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
	            imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(searchText.getApplicationWindowToken(), 0);
	            //if(historyAdapter.getMarkedPosition() != -1) {
	            //	historyAdapter.setMarkedPosition(-1);
	            //}
				return false;
			}
		});                

		if(prefs.getString("SORT_KEY", SearchHistoryDBAdapter.KEY_KEYWORDS).equals(SearchHistoryDBAdapter.KEY_CODE)) {
			showCodePanel();
			setupFullPopupMenu();
		} else {
			hideCodePanel();
			setupNormalPopupMenu();
		}
        
		super.onCreate(savedInstanceState);
		
		
	}
	
	private void setupFullPopupMenu() {
        historyList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {				
				final int position = arg2;
				savedCursor.moveToPosition(position);
				int rowId = savedCursor.getInt(SearchHistoryDBAdapter.ID_COL);
				searchHistoryDBAdapter.open();
				SearchHistoryItem item = searchHistoryDBAdapter.getEntry(rowId);
				searchHistoryDBAdapter.close();
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				
				final CharSequence[] items = {
								String.format(context.getString(R.string.recall_data) + " " + context.getString(R.string.freq_format), 
										Utils.arabic2thai(item.getFrequency()+"", context.getResources())),
								context.getString(R.string.delete),
								context.getString(R.string.assign_priority),
								context.getString(R.string.move_up),
								context.getString(R.string.move_down),
								context.getString(R.string.recompute_priority)};

				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which) {
							case 0: // recall data
								recallItemAt(position);
								break;
							case 1: // delete
								deleteItemAt(position);
								break;
							case 2: // assign
								assignPriorityAt(position);
								break;
							case 3: // move up
								moveItemUp(position);
								break;
							case 4: // move down
								moveItemDown(position);
								break;
							case 5: // recompute
								recomputePriority();
								break;
						}
						
						historyItemDialog.dismiss();
					}
				});				
				historyItemDialog = builder.create();
				historyItemDialog.show();
				return false;
			}
		}); 		
	}
	
	private void setupNormalPopupMenu() {
        historyList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {				
				final int position = arg2;
				savedCursor.moveToPosition(position);
				int rowId = savedCursor.getInt(SearchHistoryDBAdapter.ID_COL);
				searchHistoryDBAdapter.open();
				SearchHistoryItem item = searchHistoryDBAdapter.getEntry(rowId);
				searchHistoryDBAdapter.close();
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				
				final CharSequence[] items = {
								String.format(context.getString(R.string.recall_data) + " " + context.getString(R.string.freq_format), 
										Utils.arabic2thai(item.getFrequency()+"", context.getResources())),
								context.getString(R.string.delete)};
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which) {
							case 0: // recall data
								recallItemAt(position);
								break;
							case 1: // delete
								deleteItemAt(position);
								break;
						}
						
						historyItemDialog.dismiss();
					}
				});				
				historyItemDialog = builder.create();
				historyItemDialog.show();
				return false;
			}
		}); 		
	}
	
	private void hideCodePanel() {
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.filter_layout);
		layout.setVisibility(View.GONE);
	}
	
	private void showCodePanel() {
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.filter_layout);
		layout.setVisibility(View.VISIBLE);
	}
	
	private void recomputePriority() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);   
		builder.setTitle(context.getString(R.string.recompute_priority));
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage(context.getString(R.string.confirm_command));
		
		builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				searchHistoryDBAdapter.open();
				Cursor cursor = searchHistoryDBAdapter.getEntries(lang, SearchHistoryDBAdapter.KEY_PRIORITY, false);
				if(cursor.moveToFirst()) {
					int count = 0;
					while(!cursor.isAfterLast()) {
						count++;
						int rowId = cursor.getInt(SearchHistoryDBAdapter.ID_COL);
						SearchHistoryItem item = searchHistoryDBAdapter.getEntry(rowId);
						item.setPriority(String.format("%04d",count));
						searchHistoryDBAdapter.updateEntry(rowId, item);
						cursor.moveToNext();
					}
				}
				cursor.close();
				searchHistoryDBAdapter.close();
				updateHistoryList();
				return;
			}
		});
		
		builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		
		
		builder.show();
		
	}
	
	private void moveItemUp(int position) {
		if(savedCursor.moveToPosition(position)) {
			int selectedRowId = savedCursor.getInt(SearchHistoryDBAdapter.ID_COL);
			
			if(savedCursor.moveToPosition(position-1)) {
				int prevRowId = savedCursor.getInt(SearchHistoryDBAdapter.ID_COL);
				searchHistoryDBAdapter.open();		
				SearchHistoryItem selectedItem = searchHistoryDBAdapter.getEntry(selectedRowId);
				SearchHistoryItem prevItem = searchHistoryDBAdapter.getEntry(prevRowId);
				String oldPriority = selectedItem.getPriority(); 
				String newPriority = prevItem.getPriority();
				String oldCode = selectedItem.getCode();
				String newCode = prevItem.getCode();
				
				selectedItem.setPriority(newPriority);
				selectedItem.setCode(newCode);
				prevItem.setPriority(oldPriority);
				prevItem.setCode(oldCode);
				
				searchHistoryDBAdapter.updateEntry(selectedRowId, selectedItem);
				searchHistoryDBAdapter.updateEntry(prevRowId, prevItem);
				searchHistoryDBAdapter.close();
				//updateHistoryList(lang, searchText.getText().toString().trim(), position-1);
				historyAdapter.setMarkedPosition(position-1);
			}
		}		
	}
	
	private void moveItemDown(int position) {
		if(savedCursor.moveToPosition(position)) {
			int selectedRowId = savedCursor.getInt(SearchHistoryDBAdapter.ID_COL);
			
			if(savedCursor.moveToPosition(position+1)) {
				int nextRowId = savedCursor.getInt(SearchHistoryDBAdapter.ID_COL);
				searchHistoryDBAdapter.open();		
				SearchHistoryItem selectedItem = searchHistoryDBAdapter.getEntry(selectedRowId);
				SearchHistoryItem nextItem = searchHistoryDBAdapter.getEntry(nextRowId);
				String oldPriority = selectedItem.getPriority(); 
				String newPriority = nextItem.getPriority();
				String oldCode = selectedItem.getCode();
				String newCode = nextItem.getCode();
				
				selectedItem.setPriority(newPriority);
				selectedItem.setCode(newCode);
				nextItem.setPriority(oldPriority);
				nextItem.setCode(oldCode);
				
				searchHistoryDBAdapter.updateEntry(selectedRowId, selectedItem);
				searchHistoryDBAdapter.updateEntry(nextRowId, nextItem);
				searchHistoryDBAdapter.close();
				//updateHistoryList(lang, searchText.getText().toString().trim(), position+1);
				historyAdapter.setMarkedPosition(position+1);
			}
		}		
	}
	
	private void assignPriorityAt(int _position) {
		final int position = _position;
		//move cursor to selected item
		savedCursor.moveToPosition(position);
		final int selectedRowId = savedCursor.getInt(SearchHistoryDBAdapter.ID_COL);
						
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.assign_dialog);
		
		searchHistoryDBAdapter.open();		
		SearchHistoryItem item = searchHistoryDBAdapter.getEntry(selectedRowId);
		TextView infoText = (TextView)dialog.findViewById(R.id.assign_info);
		infoText.setText(item.getKeywords() + " (" + item.getCode() + ":" + item.getPriority() + ")");
		searchHistoryDBAdapter.close();				
			
		dialog.setTitle(R.string.assign_priority);
		final EditText priorityEditText = (EditText)dialog.findViewById(R.id.assign_text);
		final EditText codeEditText = (EditText)dialog.findViewById(R.id.code_text);
		
		((Button)dialog.findViewById(R.id.assign_btn)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String priorityText = priorityEditText.getText().toString();
				String codeText = codeEditText.getText().toString();
				if(priorityText.length() > 0 || codeText.length() > 0) {
					searchHistoryDBAdapter.open();								
					savedCursor.moveToPosition(position);
					SearchHistoryItem item = searchHistoryDBAdapter.getEntry(selectedRowId);
					if(priorityText.length() > 0)
						item.setPriority(String.format("%04d", Integer.parseInt(priorityText)));
					if(codeText.length() > 0)
						item.setCode(codeText);
					searchHistoryDBAdapter.updateEntry(selectedRowId, item);
					searchHistoryDBAdapter.close();
					updateHistoryList(lang, searchText.getText().toString().trim(), position);					
					dialog.dismiss();
				}
			}
		});
		dialog.show();
	}
	
	private void recallItemAt(int position) {
		searchHistoryDBAdapter.open();
				
		// prepare necessary information for retrieving the result from DB
		String lang = savedCursor.getString(SearchHistoryDBAdapter.LANG_COL);
		String keywords = savedCursor.getString(SearchHistoryDBAdapter.KEYWORDS_COL);
		String sCate = savedCursor.getString(SearchHistoryDBAdapter.SEL_CATE_COL);
		searchResultsDBAdapter.open();
		Cursor cursor = searchResultsDBAdapter.getEntries(lang, keywords, sCate);
		String content = "";
		String pClicked = "";
		String sClicked = "";
		String saved = "";
		String marked = "";
		
		// retrieve the result from DB
		if(cursor.getCount() > 0 && cursor.moveToFirst()) {
			content = cursor.getString(SearchResultsDBAdapter.CONTENT_COL);
			pClicked = cursor.getString(SearchResultsDBAdapter.PRIMARY_CLIKCED_COL);
			sClicked = cursor.getString(SearchResultsDBAdapter.SECONDARY_CLIKCED_COL);
			saved = cursor.getString(SearchResultsDBAdapter.SAVED_COL);
			marked = cursor.getString(SearchResultsDBAdapter.MARKED_COL);
		}
		
		// send the result to SearchActivity 
		if(content.length() > 0) {			
			// increase frequency of history item
			savedCursor.moveToPosition(position);
			long rowId = savedCursor.getInt(SearchHistoryDBAdapter.ID_COL);
			SearchHistoryItem item = searchHistoryDBAdapter.getEntry(rowId);
			item.setFrequency(item.getFrequency()+1);
			searchHistoryDBAdapter.updateEntry(rowId, item);
			
    		Intent intent = new Intent(context, SearchActivity.class);
    		Bundle dataBundle = new Bundle();
    		dataBundle.putString("LANG", lang);
    		dataBundle.putString("CONTENT", content);
    		dataBundle.putString("QUERY", keywords);
    		dataBundle.putString("PCLICKED", pClicked);
    		dataBundle.putString("SCLICKED", sClicked);
    		dataBundle.putString("SAVED", saved);
    		dataBundle.putString("MARKED", marked);
    		dataBundle.putString("SCATE", sCate);
    		
    		intent.putExtras(dataBundle);
    		context.startActivity(intent);
    		
			// update list
			updateHistoryList(lang, searchText.getText().toString().trim());    		
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(context.getString(R.string.data_not_found));
			builder.setMessage(context.getString(R.string.please_search_again));
			builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			
			builder.show();
		}
		
		cursor.close();
		searchResultsDBAdapter.close();				
		searchHistoryDBAdapter.close();
		
	}
	
	private void deleteItemAt(int _position) {
		final int position = _position;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);   
		builder.setTitle(context.getString(R.string.delete_item));
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage(context.getString(R.string.confirm_delete_item));
		
		builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// remove record from DB
				searchHistoryDBAdapter.open();								
				savedCursor.moveToPosition(position);
				int rowId = savedCursor.getInt(SearchHistoryDBAdapter.ID_COL);
				searchHistoryDBAdapter.removeEntry(rowId);								
				searchHistoryDBAdapter.close();
				
				// prepare data for updating the corresponding record in another DB
				String lang = savedCursor.getString(SearchHistoryDBAdapter.LANG_COL);
				String keywords = savedCursor.getString(SearchHistoryDBAdapter.KEYWORDS_COL);
				String sCate = savedCursor.getString(SearchHistoryDBAdapter.SEL_CATE_COL);
				int nSutsHistory = savedCursor.getInt(SearchHistoryDBAdapter.N_SUT_COL);
				
				// update the corresponding record
				searchResultsDBAdapter.open();
				Cursor cursor = searchResultsDBAdapter.getEntries(lang, keywords, sCate);
				if(cursor.getCount() > 0 && cursor.moveToFirst()) {
					int i;
					int nSutsResults;
					String suts;
					String [] tokens;
					while(!cursor.isAfterLast()) {
						i = cursor.getInt(SearchResultsDBAdapter.ID_COL);
						suts = cursor.getString(SearchResultsDBAdapter.SUTS_COL);
						tokens = suts.split(":");
						nSutsResults = Integer.parseInt(tokens[0]) + Integer.parseInt(tokens[1]) + Integer.parseInt(tokens[2]);
						if(nSutsResults == nSutsHistory) {
							searchResultsDBAdapter.removeEntry(i);
						}
						cursor.moveToNext();
					}
				}
				cursor.close();
				searchResultsDBAdapter.close();
				updateHistoryList(lang, searchText.getText().toString().trim(),position);
				return;
				
			}
		});
		builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
				
			}
		});		
		builder.show();
	
	}
	
	@Override
	public void dismiss() {
		//Toast.makeText(context, "Dismiss", Toast.LENGTH_SHORT).show();
		if(searchHistoryDBAdapter != null) {
			searchHistoryDBAdapter.close();
		}
		super.dismiss();
	}

	private void updateHistoryList(String _lang, String text, int position) {
		String code = codeText.getText().toString();
		String number = numberText.getText().toString();
		searchHistoryDBAdapter.open();
		if(savedCursor != null && !savedCursor.isClosed()) {
			savedCursor.close();
		}
		savedCursor = searchHistoryDBAdapter.getEntries(_lang, text, sortKey, isDesc, code, number);
        historyAdapter = new ResultsCursorAdapter(context, R.layout.history_item, savedCursor, 
        		new String[] {  SearchHistoryDBAdapter.KEY_KEYWORDS,
        						SearchHistoryDBAdapter.KEY_LINE1, 
        						SearchHistoryDBAdapter.KEY_LINE2, 
        						SearchHistoryDBAdapter.KEY_PRIORITY, SearchHistoryDBAdapter.KEY_CODE}, 
        		new int[] {R.id.hline1, R.id.hline2, R.id.hline3, R.id.priority_number, R.id.priority_code});
        historyList.setAdapter(historyAdapter);				
		searchHistoryDBAdapter.close();
		if(position > 0) {
			historyList.setSelected(true);
			historyList.setSelection(position-1);
		}
	}
	
	private void updateHistoryList(String _lang, String text) {
		String code = codeText.getText().toString();
		String number = numberText.getText().toString();		
		searchHistoryDBAdapter.open();
		if(savedCursor != null && !savedCursor.isClosed()) {
			savedCursor.close();
		}
		savedCursor = searchHistoryDBAdapter.getEntries(_lang, text, sortKey, isDesc, code, number);
        historyAdapter = new ResultsCursorAdapter(context, R.layout.history_item, savedCursor, 
        		new String[] {  SearchHistoryDBAdapter.KEY_KEYWORDS,
        						SearchHistoryDBAdapter.KEY_LINE1, 
        						SearchHistoryDBAdapter.KEY_LINE2, 
        						SearchHistoryDBAdapter.KEY_PRIORITY, SearchHistoryDBAdapter.KEY_CODE}, 
        		new int[] {R.id.hline1, R.id.hline2, R.id.hline3, R.id.priority_number, R.id.priority_code});
        historyList.setAdapter(historyAdapter);				
		searchHistoryDBAdapter.close();
	}

	private void updateHistoryList(String _lang) {
		String code = codeText.getText().toString();
		String number = numberText.getText().toString();	
		searchHistoryDBAdapter.open();
		if(savedCursor != null && !savedCursor.isClosed()) {
			savedCursor.close();
		}
		savedCursor = searchHistoryDBAdapter.getEntries(_lang, "", sortKey, isDesc, code, number);
        historyAdapter = new ResultsCursorAdapter(context, R.layout.history_item, savedCursor, 
        		new String[] {  SearchHistoryDBAdapter.KEY_KEYWORDS,
        						SearchHistoryDBAdapter.KEY_LINE1, 
        						SearchHistoryDBAdapter.KEY_LINE2, 
        						SearchHistoryDBAdapter.KEY_PRIORITY, SearchHistoryDBAdapter.KEY_CODE}, 
        		new int[] {R.id.hline1, R.id.hline2, R.id.hline3, R.id.priority_number, R.id.priority_code});
        historyList.setAdapter(historyAdapter);				
		searchHistoryDBAdapter.close();
	}
	
	public void updateHistoryList() {
		updateHistoryList(lang,searchText.getText().toString());
	}
	
	/*
	@Override
	public void cancel() {
		Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show();
		super.cancel();
	}
	*/
}