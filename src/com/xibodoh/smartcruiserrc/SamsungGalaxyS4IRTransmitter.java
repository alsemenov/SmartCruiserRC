/*
  ~ Copyright (c) 2014 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package com.xibodoh.smartcruiserrc;

import java.lang.reflect.Method;

import com.xibodoh.smartcruiserrc.legopf.IRController;
import com.xibodoh.smartcruiserrc.legopf.IRTransmitter;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class SamsungGalaxyS4IRTransmitter implements IRTransmitter {

	private static final String IN_STATE = "OUDB";
	
	private Object irdaService;
    private Method irWrite;
    private Context context;
	
	public SamsungGalaxyS4IRTransmitter(Context context) {
		this.context = context;  
		irdaService = context.getSystemService("irda");
        try {
        	irWrite = irdaService.getClass().getMethod("write_irsend", new Class[]{String.class});
        } catch (NoSuchMethodException e) {
        	throw new RuntimeException(e);
        }		
		
	}
	
	private String encode(int command) {
		StringBuilder sb = new StringBuilder("38000,6,39");
		int mask = 0x8000;
		while (mask!=0){
			if ((command&mask)!=0) {
				sb.append( ",6,21");
			} else {
				sb.append(",6,10");
			}
			mask = mask>>1;
		}
		sb.append(",6,39");
		return sb.toString();
	}
	
	private String decode(int command) {
		// decode command
		if ((command & IRController.NO_COMMAND_MASK)!=0){
			return "no command";
		} else {
			int channel = ((0x3<<12)&command)>>12;
			int mode = ((0xF<<8)&command)>>8;
			int red = ((0x3<<4)&command)>>4;
			int blue = ((0x3<<6)&command)>>6;
			return "C"+channel+"R"+IN_STATE.charAt(red)+"B"+IN_STATE.charAt(blue);
		}		
	}
	
	@Override
	public void sendCommand(int command) {
		try {
			String encodedCommand = encode(command);			
			irWrite.invoke(irdaService, encodedCommand);
        	Log.d("Perf", "transmitted command: "+decode(command));
			//Toast.makeText(context, encodedCommand, Toast.LENGTH_SHORT).show();
		} catch (Exception e){
			throw new RuntimeException(e);
		}

	}

}
