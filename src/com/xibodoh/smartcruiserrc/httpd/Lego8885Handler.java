/*
  ~ Copyright (c) 2014 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package com.xibodoh.smartcruiserrc.httpd;

import com.xibodoh.smartcruiserrc.httpd.HTTPServer.RequestHandler;
import com.xibodoh.smartcruiserrc.legopf.IRController;

import android.net.wifi.WifiConfiguration.Status;


import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class Lego8885Handler implements RequestHandler {

	private static final String _8885 = "/8885/";
	private static final String _8885_STATE = _8885 +"state/";

	private final IRController controller;
	
	public Lego8885Handler(IRController controller) {
		this.controller = controller;
	}


	@Override
	public Response serve(IHTTPSession session) {
		long start = System.currentTimeMillis();
		String uri = session.getUri();
		if (uri.startsWith(_8885)){
			if (uri.startsWith(_8885_STATE)){
				String state = uri.substring(_8885_STATE.length());
				controller.sendCommand(decode(state));
				long finish = System.currentTimeMillis();
				return new Response(Response.Status.OK, "text/plain", "" +(finish-start));
//				System.out.println(Thread.currentThread().getId()+" Lego8885Handler: receive state: "+state);
			}
		}
		return null;
	}

	private static final String IN_STATE = "OUDB";
	private final static int UP = 1;
	private final static int DOWN = 2;
	
	private int decode(String state){
		// channel
		if (state==null || state.length()!=6 
				|| state.charAt(0)!='C' 				
				|| state.charAt(2)!='R' || state.charAt(4)!='B'){
			return IRController.NO_COMMAND_MASK;
		}
		
		int channel = state.charAt(1) - '1';
		if (channel<0 || channel>3){
			return IRController.NO_COMMAND_MASK;
		}
		int red = IN_STATE.indexOf(state.charAt(3));
		int blue = IN_STATE.indexOf(state.charAt(5));
		if (red<0 || blue<0){
			return IRController.NO_COMMAND_MASK;
		}
		
		int nibble1 = 0x3 & channel; // 00CC
		int nibble2 = 1; // 0001 (combo direct mode)
		int nibble3 = blue<<2 | red; 
		int lrc = 0x0F ^ nibble1 ^ nibble2 ^ nibble3;
		
		int command = lrc | (nibble3<<4) | (nibble2<<8) | (nibble1<<12);
		if (blue==UP || blue==DOWN || red==UP || red==DOWN){
			// append timeout
			command = command | (200<<IRController.TIMEOUT_SHIFT);
		}
		return command;
	}
	

	@Override
	public void start() {
		controller.start();
	}

	@Override
	public void stop() {
		controller.stop();
	}
	
}
