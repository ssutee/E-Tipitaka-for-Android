package  com.watnapp.etipitaka;

import android.util.Log;

public class SearchHistoryItem {
	private String lang;
	private String keywords;
	private int nPage;
	private int nSut;
	private String sCate;
	private String line1;
	private String line2;
	private int freq;
	private String priority;
	private String code;
	
	public SearchHistoryItem(String _lang, String _keywords, int _nPage, int _nSut, String _sCate, 
			String _line1, String _line2) {
		lang = _lang;
		keywords = _keywords;
		nPage = _nPage;
		nSut = _nSut;
		sCate = _sCate;
		line1 = _line1;
		line2 = _line2;
		freq = 0;
		priority = "0";
		code = "A";
	}	

	public SearchHistoryItem(String _lang, String _keywords, int _nPage, int _nSut, String _sCate, 
			String _line1, String _line2, int _freq) {
		lang = _lang;
		keywords = _keywords;
		nPage = _nPage;
		nSut = _nSut;
		sCate = _sCate;
		line1 = _line1;
		line2 = _line2;
		freq = _freq;
		priority = "0";
		code = "A";		
	}	
	
	public SearchHistoryItem(String _lang, String _keywords, int _nPage, int _nSut, String _sCate, 
			String _line1, String _line2, int _freq, String _priority) {
		lang = _lang;
		keywords = _keywords;
		nPage = _nPage;
		nSut = _nSut;
		sCate = _sCate;
		line1 = _line1;
		line2 = _line2;
		freq = _freq;
		priority = _priority;
		code = "A";		
	}	

	public SearchHistoryItem(String _lang, String _keywords, int _nPage, int _nSut, String _sCate, 
			String _line1, String _line2, int _freq, String _priority, String _code) {
		lang = _lang;
		keywords = _keywords;
		nPage = _nPage;
		nSut = _nSut;
		sCate = _sCate;
		line1 = _line1;
		line2 = _line2;
		freq = _freq;
		priority = _priority;
		code = _code;
	}	
	
	
	public SearchHistoryItem(String s) throws Exception{
		String [] tokens = s.split(":");
		if(tokens.length == 7) {
			lang = tokens[0].trim();
			keywords = tokens[1].trim();
			nPage = Integer.parseInt(tokens[2].trim());
			nSut = Integer.parseInt(tokens[3].trim());
			sCate = tokens[4].trim();
			line1 = tokens[5].trim();
			line2 = tokens[6].trim();
			freq = 0;
		} else if(tokens.length == 8) {
			lang = tokens[0].trim();
			keywords = tokens[1].trim();
			nPage = Integer.parseInt(tokens[2].trim());
			nSut = Integer.parseInt(tokens[3].trim());
			sCate = tokens[4].trim();
			line1 = tokens[5].trim();
			line2 = tokens[6].trim();
			freq = Integer.parseInt(tokens[7].trim());			
		} else if(tokens.length == 9) {
			lang = tokens[0].trim();
			keywords = tokens[1].trim();
			nPage = Integer.parseInt(tokens[2].trim());
			nSut = Integer.parseInt(tokens[3].trim());
			sCate = tokens[4].trim();
			line1 = tokens[5].trim();
			line2 = tokens[6].trim();
			freq = Integer.parseInt(tokens[7].trim());
			priority = tokens[8].trim();
		} else if(tokens.length == 10) {
			lang = tokens[0].trim();
			keywords = tokens[1].trim();
			nPage = Integer.parseInt(tokens[2].trim());
			nSut = Integer.parseInt(tokens[3].trim());
			sCate = tokens[4].trim();
			line1 = tokens[5].trim();
			line2 = tokens[6].trim();
			freq = Integer.parseInt(tokens[7].trim());
			priority = tokens[8].trim();
			code = tokens[9].trim();
		}
		else {
			throw new Exception("History: Input format is invalid: "+tokens.length);
		}
	}
	
	@Override
	public String toString() {
		return String.format(" %s : %s : %d : %d : %s : %s : %s : %d : %s : %s ", lang, keywords, nPage, nSut, sCate, line1, line2, freq, priority, code);
	}
	
	public String getLanguage() {
		return lang;
	}
	
	public String getKeywords() {
		return keywords;
	}
	
	public int getNPage() {
		return nPage;
	}
	
	public int getNSut() {
		return nSut;
	}
	
	public String getSelectedCategories() {
		return sCate;
	}
	
	public String getLine1() {
		return line1;
	}

	public String getLine2() {
		return line2;
	}
	
	public int getFrequency() {
		return freq;
	}
	
	public String getPriority() {
		return priority;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setLangauge(String _lang) {
		lang = _lang;
	}
	
	public void setKeywords(String _keywords) {
		keywords = _keywords;
	}
	
	public void setNPage(int _nPage) {
		nPage = _nPage;
	}
	
	public void setNSut(int _nSut) {
		nSut = _nSut;
	}
	
	public void setSelectedCategories(String _sCate) {
		sCate = _sCate;
	}
	
	public void setLine1(String _line1) {
		line1 = _line1;
	}

	public void setLine2(String _line2) {
		line1 = _line2;
	}
	
	public void setFrequency(int _freq) {
		freq = _freq;
	}
	
	public void setPriority(String _priority) {
		priority = _priority;
	}
	
	public void setCode(String _code) {
		code = _code;
	}
	
}