package com.xibodoh.smartcruiserrc;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final String LOG_TAG = "MainActivity";
	
	private boolean serviceStarted = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setServiceStarted(isHTTPServiceAlive());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public void onPowerButtonClick(View view){
		if (serviceStarted) {
			stopService(new Intent(this, HTTPService.class));
		} else {
			startService(new Intent(this, HTTPService.class));
		}
		setServiceStarted(!serviceStarted);
	}
	
	private void setServiceStarted(boolean started){
		this.serviceStarted = started;
		TextView textView = (TextView) findViewById(R.id.address);;
		ImageButton powerButton = (ImageButton) findViewById(R.id.powerButton);
		ImageButton browserButton = (ImageButton) findViewById(R.id.inBrowserButton);
		browserButton.setEnabled(serviceStarted);
		if (!serviceStarted) {
			Log.d(LOG_TAG, "before stop service");
			powerButton.setImageResource(R.drawable.red_off);		
			textView.setText(getString(R.string.inactive));
		} else {
			Log.d(LOG_TAG, "before start service");
			powerButton.setImageResource(R.drawable.green_on);		
	        String ipAddr = getLocalIpAddress();
			textView.setText("http://"+ipAddr+":7070/");
		}		
	}
	
	private boolean isHTTPServiceAlive() {
	    // TODO use something more correct
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (HTTPService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	
	public void onOpenButtonClick(View view){
		Log.d(LOG_TAG, "before open in browser");
        String ipAddr = getLocalIpAddress();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://"+ipAddr+":7070/")));
	}
	
	
	// TODO move to HTTPserver
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    //if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress() ) {
                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()) ) {
                        String ipAddr = inetAddress.getHostAddress();
                        return ipAddr;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.d(LOG_TAG, ex.toString());
        }
        return null;
    }
}
