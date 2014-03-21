package com.xibodoh.smartcruiserrc;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TabHost;

public class AboutActivity extends TabActivity {

	   @Override
	    protected void onCreate(Bundle savedInstanceState ) {
	        super.onCreate(savedInstanceState);
	        setTitle("Smart Cruiser RC ("+getAppVersion(this)+")");

	        addTab("about", R.string.about);
	        addTab("whatsnew", R.string.whats_new);
	        addTab("gpl-2.0-standalone", R.string.license);
	    }

	    private void addTab(String name, int titleId) {
	        Intent intent = new Intent(this, WebViewActivity.class);
	        intent.putExtra(WebViewActivity.FILENAME, name);
	        TabHost tabHost = getTabHost();
	        tabHost.addTab(tabHost.newTabSpec(name)
	                .setIndicator(getString(titleId), null /*getResources().getDrawable(R.drawable.ic_tab_about)*/)
	                .setContent(intent));
	    }

	    public static String getAppVersion(Context context) {
	        try {
	            PackageManager manager = context.getPackageManager();                         
	    		return "v. "+manager.getPackageInfo(context.getPackageName(), 0).versionName;    
	        } catch (PackageManager.NameNotFoundException e) {
	            return "";
	        }
	    }

}
