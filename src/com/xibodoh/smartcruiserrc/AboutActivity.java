/*
  ~ Copyright (c) 2014 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package com.xibodoh.smartcruiserrc;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

public class AboutActivity extends Activity {

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
