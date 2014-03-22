package com.xibodoh.smartcruiserrc;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

public class AboutActivity extends Activity {//extends TabActivity {

	   @Override
	    protected void onCreate(Bundle savedInstanceState ) {
	        super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_about);
			WebView webView1 = (WebView) findViewById(R.id.webView1);
			webView1.loadUrl("file:///android_asset/about.html");
	    }

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			return false;
		}
}
