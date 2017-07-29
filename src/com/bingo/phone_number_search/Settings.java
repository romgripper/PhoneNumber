package com.bingo.phone_number_search;

import java.util.Random;

import android.util.Log;

public class Settings {
	
	private static final String LOG_TAG =
			Settings.class.getSimpleName();
	
	public static final boolean DEBUG = true;
	
	public static final boolean GOOGLE_ADS_TEST = true;
	
	public static final boolean MOBCLIX_ADS_TEST = false;
	
	public static final int PROVIDER_ADMOB = 0;
	
	public static final int PROVIDER_MOBCLIX = 1;
	
	private static String appDescription = "电话号码归属地查询";
    
    private static Random random = new Random();
	
    private static String[] adKeywords = {
    	"固定电话",
	    "移动电话",
	    "手机",
	    "电话号码归属地",
	};
    
    public static String[] getAdKeywords() {
		return adKeywords;
	}

	public static String getNextKeyword() {
    	String[] keywords = getAdKeywords();
    	int index = random.nextInt(keywords.length);
    	if (DEBUG) {
    		Log.d(LOG_TAG, "getNextKeyword: " + keywords[index]);
    	}
    	return keywords[index];
    }
    
    public static String getAppDescription() {
		return appDescription;
	}
}

