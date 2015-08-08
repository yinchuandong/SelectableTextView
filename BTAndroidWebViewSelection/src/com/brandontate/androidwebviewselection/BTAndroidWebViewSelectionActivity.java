package com.brandontate.androidwebviewselection;

import org.json.JSONException;
import org.json.JSONObject;

import net.londatiga.android.QuickAction;
import net.londatiga.android.QuickAction.OnActionItemClickListener;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

public class BTAndroidWebViewSelectionActivity extends Activity {
	/** Called when the activity is first created. */

	private static final String TAG = "BTAndroidWebViewSelectionActivity";
	private BTWebView webView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		webView = (BTWebView) findViewById(R.id.webView);
		// Load up the android asset file
		final String filePath = "file:///android_asset/content.html";

		// Load the url
		webView.loadUrl(filePath);

		webView.setOnMenuActionItemClickListener(new OnActionItemClickListener() {

			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				if (actionId == 1) {
					// Do Button 1 stuff
					Log.i(TAG, "Hit Button 1: " + webView.getSelectedText());
				} else if (actionId == 2) {
					// Do Button 2 stuff
					Log.d(TAG, "Hit Button 2: ");
				} else if (actionId == 3) {
					// Do Button 3 stuff
					Log.i(TAG, "Hit Button 3");
				}
			}
		});
	}
	
	public Handler jsHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(webView == null) return;
			String url = msg.getData().getString("js");
			webView.loadUrl(url);
		}
		
	};

}