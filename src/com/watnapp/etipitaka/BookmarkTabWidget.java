package  com.watnapp.etipitaka;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class BookmarkTabWidget extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookmark);
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		intent = new Intent().setClass(this, BookmarkThaiActivity.class);
		spec = tabHost.newTabSpec("thai").setIndicator(getString(R.string.th_lang)).setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, BookmarkPaliActivity.class);
		spec = tabHost.newTabSpec("pali").setIndicator(getString(R.string.pl_lang)).setContent(intent);
		tabHost.addTab(spec);
		if(BookmarkTabWidget.this.getIntent().getExtras() != null) {
			Bundle dataBundle = BookmarkTabWidget.this.getIntent().getExtras();
			String lang = dataBundle.getString("LANG");
			if(lang.equals("thai")) {
				tabHost.setCurrentTab(0);
			}
			else if(lang.equals("pali")) {
				tabHost.setCurrentTab(1);
			}
			
		}
		
	}
}