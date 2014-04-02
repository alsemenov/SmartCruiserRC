package com.xibodoh.smartcruiserrc;

import java.io.IOException;

import com.xibodoh.smartcruiserrc.httpd.AssetHandler;
import com.xibodoh.smartcruiserrc.httpd.DebugHandler;
import com.xibodoh.smartcruiserrc.httpd.HTTPServer;
import com.xibodoh.smartcruiserrc.httpd.Lego8885Handler;
import com.xibodoh.smartcruiserrc.legopf.IRController;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;



public class HTTPService extends Service {

	private final String LOG_TAG = "HTTPService";
	
	private HTTPServer httpd;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		// TODO HTTPServer configuration: port, handlers
		httpd = new HTTPServer(7070);
//		Lego8885Handler2 lego8885Handler2 = new Lego8885Handler2(new SamsungGalaxyS4IRTransmitter(this));
//		httpd.setAsyncRunner(lego8885Handler2);
//		httpd.addRequestHandler(lego8885Handler2);
		httpd.addRequestHandler(new Lego8885Handler(new IRController(new SamsungGalaxyS4IRTransmitter(this))));
		httpd.addRequestHandler(new AssetHandler(this.getAssets()));
		httpd.addRequestHandler(new DebugHandler());
		try {
			httpd.start();
//			TODO httpd.setAsyncRunner(asyncRunner);
		} catch (IOException e) {
			//TODO e.printStackTrace();
		}
		Log.d(LOG_TAG, "Service started");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		httpd.stop();
		Log.d(LOG_TAG, "Service stopped");
	}
    
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "onStartCommand");
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
}
