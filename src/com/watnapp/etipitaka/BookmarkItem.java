package  com.watnapp.etipitaka;

import android.util.Log;

public class BookmarkItem {
	private String lang;
	private int volumn;
	private int page;
	private int item;
	private String note;
	private String keywords;
	
	public BookmarkItem(String _lang, int _volumn, int _page, int _item, String _note, String _keyword) {
		lang = _lang;
		volumn = _volumn;
		page = _page;
		item = _item;
		note = _note;
		keywords = _keyword;
	}
	
	public BookmarkItem(String s) throws Exception{
		String [] tokens = s.split(":");		
		if(tokens.length == 5) {
			lang = tokens[0].trim();
			volumn = Integer.parseInt(tokens[1].trim());
			page = Integer.parseInt(tokens[2].trim());
			item = Integer.parseInt(tokens[3].trim());
			note = tokens[4].trim();
			keywords = "";
		} else if(tokens.length == 6) {
			lang = tokens[0].trim();
			volumn = Integer.parseInt(tokens[1].trim());
			page = Integer.parseInt(tokens[2].trim());
			item = Integer.parseInt(tokens[3].trim());
			note = tokens[4].trim();
			keywords = tokens[5].trim();
		} 
		else {
			throw new Exception("Bookmark: Input format is invalid: " + tokens.length);
		}
		
	}
	
	@Override
	public String toString() {
		return String.format(" %s : %d : %d : %d : %s : %s ", lang, volumn, page, item, note, keywords);
	}
	
	
	public String getLanguage() {
		return lang;
	}
	
	public int getVolumn() {
		return volumn;
	}
	
	public int getPage() {
		return page;
	}
	
	public int getItem() {
		return item;
	}
	
	public String getNote() {
		return note;
	}
	
	public String getKeywords() {
		return keywords;
	}
	
	public void setLangauge(String _lang) {
		lang = _lang;
	}
	
	public void setVolumn(int _volumn) {
		volumn = _volumn;
	}
	
	public void setPage(int _page) {
		page = _page;
	}
	
	public void setItem(int _item) {
		item = _item;
	}
	
	public void setNote(String _note) {
		note = _note;
	}
	
	public void setKeywords(String _keywords) {
		keywords = _keywords;
	}
}