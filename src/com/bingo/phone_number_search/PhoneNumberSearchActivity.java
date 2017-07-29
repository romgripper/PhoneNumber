package com.bingo.phone_number_search;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class PhoneNumberSearchActivity extends Activity {
	
	private static final String LOG_TAG =
		PhoneNumberSearchActivity.class.getSimpleName();
	
	private static final int DIALOG_MOBILE_NUM_ERROR = 0;
	private static final String ADMOB_ID = "a14d0b78a808a87";
		
	private EditText phoneNumber;
	private Button search;
	private TextView provider;
	private TextView area;
	private ImageButton goto_map;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        setContentView(R.layout.main);
        
        initViews();
    }
    
    private void initViews() {
    	initAds();
    	phoneNumber = (EditText) findViewById(R.id.mobile);
    	search = (Button) findViewById(R.id.search);
    	search.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			searchNumber();
    		}
    	});
    	provider = (TextView) findViewById(R.id.provider);
    	area = (TextView) findViewById(R.id.area);
    	goto_map = (ImageButton) findViewById(R.id.go_to);
    }
    
    private void initAds() {		
    	FrameLayout layout = (FrameLayout) findViewById(R.id.ads_frame1);
	    AdView adView = new AdView(this, AdSize.BANNER, ADMOB_ID);
	    layout.addView(adView);
	    adView.loadAd(new AdRequest());
    	layout = (FrameLayout) findViewById(R.id.ads_frame2);
	    adView = new AdView(this, AdSize.BANNER, ADMOB_ID);
	    layout.addView(adView);
	    adView.loadAd(new AdRequest());
    }
    
    private void searchNumber() {
    	String number = phoneNumber.getText().toString();
    	if (number.startsWith("0086")) {
    		number = number.substring(4);
    	} else if (number.startsWith("+86")) {
    		number = number.substring(3);
    	} else if (number.startsWith("86")) {
    		number = number.substring(2);
    	}
    	
    	number = number.replace("-", "");
    	
    	if (number.length() < 7) {
    		showDialog(DIALOG_MOBILE_NUM_ERROR);
    		return;
    	}
    	char c = number.charAt(0);
		if (c > '9' || c < '0') {
    		showDialog(DIALOG_MOBILE_NUM_ERROR);
    		return;
		}
    	new SearchTask(number).execute();

    }
    
    private BufferedReader openNumberBookReader() {
    	try{  
    		AssetManager am = getAssets();
    		InputStream in = am.open("number_book");
    		ZipInputStream zis = new ZipInputStream(in);  
    		ZipEntry entry = zis.getNextEntry();
    		if (entry != null) {
    			return new BufferedReader(new InputStreamReader(zis));
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}  
		return null;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_MOBILE_NUM_ERROR) {
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert)
                .setTitle(R.string.phone_number_error)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        }
        return null;
    }
    
	private class SearchTask extends AsyncTask<Void, Void, String[]> {
		
		private String number;
		private boolean isMobile;
		
		public SearchTask(String number) {
			this.number = number;
		}

		@Override
		protected void onPreExecute() {
	    	search.setEnabled(false);
	    	setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected String[] doInBackground(Void... params) {
	    	String number1 = number.substring(0, 7);
	    	String number2 = " " + number.substring(0, 3) + " ";
	    	String number3 = " " + number.substring(0, 4) + " ";
	    	BufferedReader reader = openNumberBookReader();
	    	String line;
	    	try {
		    	while ((line = reader.readLine()) != null) {
		    		if (line.startsWith(number1) ) {
		    			isMobile = true;
		    			return line.split(" ");
		    		}
		    		if (line.contains(number2) || line.contains(number3)) {
		    			isMobile = false;
		    			return line.split(" ");
		    		}
		    	}
	    	} catch (Exception e) {    		
	    	}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... progress) {
		}

		@Override
		protected void onPostExecute(String[] result) {
	    	search.setEnabled(true);
	    	setProgressBarIndeterminateVisibility(false);
	    	
			if(result == null || result.length != 6) {
				provider.setText(R.string.not_found);
				area.setText("");
				goto_map.setVisibility(View.INVISIBLE);
				return;
			}
			
			if (! result[1].equals(result[3])) {
				result[1] += result[3];
			}
			
			if (isMobile) {
				provider.setText(getString(R.string.provider) + result[5]);
			} else {
				provider.setText(getString(R.string.provider) + getString(R.string.pstn));
			}
			
			final String geo = "geo:0,0?q=" + result[1];
			area.setText(Html.fromHtml(getString(R.string.area) + "<a href=\"geo:0,0?q="
					+ result[1] + "\">"	+ result[1] + "</a>"));
			final View.OnClickListener listener = new View.OnClickListener() {
				public void onClick(View view) {
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(geo));
					startActivity(intent);
				}
			};
			area.setOnClickListener(listener);
			goto_map.setVisibility(View.VISIBLE);
			goto_map.setOnClickListener(listener);
			
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(phoneNumber.getWindowToken(), 0);
		}
	}
}