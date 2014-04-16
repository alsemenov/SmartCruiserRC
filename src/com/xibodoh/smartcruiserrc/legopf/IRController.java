/*
  ~ Copyright (c) 2014 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package com.xibodoh.smartcruiserrc.legopf;

import java.util.concurrent.atomic.AtomicInteger;

public class IRController  {

	public static final int NO_COMMAND_MASK = 0x80000000;
	public static final int COMMAND_MASK = 0xFFFF;
	public static final int TIMEOUT_SHIFT = 16;
	
	public static final int NONE = 0;
	public static final int BREAK = 3;
	
	private final IRTransmitter transmitter;
	private final IRControllerThread thread;
	
	private AtomicInteger nextCommand;
	
	private final Object lock = new Object();
	
	private class IRControllerThread extends Thread {

		@Override
		public void run() {
			while(!isInterrupted()){
				int commandAndTimeout = nextCommand.get();
				int nextCommandAndTimeout = commandAndTimeout;
				long timeout = Long.MAX_VALUE;
				if ((commandAndTimeout&NO_COMMAND_MASK)==0){
					transmitter.sendCommand(commandAndTimeout&COMMAND_MASK);
					transmitter.sendCommand(commandAndTimeout&COMMAND_MASK);
					transmitter.sendCommand(commandAndTimeout&COMMAND_MASK);

					timeout = commandAndTimeout>>TIMEOUT_SHIFT;
					if (timeout<=0){
						timeout = Long.MAX_VALUE;
					}
					
					//    modify command break =>none
					int red = ((0x3<<4)&commandAndTimeout)>>4;
					int blue = ((0x3<<6)&commandAndTimeout)>>6;
					if (red==BREAK){
						red = NONE;
						nextCommandAndTimeout = nextCommandAndTimeout & (~(0x3<<4)); // convert to none
					}
					if (blue==BREAK){
						blue = NONE;
						nextCommandAndTimeout = nextCommandAndTimeout & (~(0x3<<6)); // convert to none
					}					
					if (red==NONE && blue==NONE){
						nextCommandAndTimeout = NO_COMMAND_MASK;
						timeout = Long.MAX_VALUE;
					}
				}
				if (nextCommand.compareAndSet(commandAndTimeout, nextCommandAndTimeout)){
					synchronized (lock) {
						try {
							lock.wait(timeout);
						} catch (InterruptedException e) {
						}
					}
				}
				
//				int commandAndTimeout = nextCommand.get();
//				if ((commandAndTimeout&NO_COMMAND_MASK)==0){
//					sendCommand(commandAndTimeout);
//				}
//				try {
//					sleep(500);
//				} catch (InterruptedException e) {
//
//				}
			}
		}
		
	}
	
	
	public IRController(IRTransmitter transmitter){
		this.transmitter = transmitter;
		this.nextCommand = new AtomicInteger(NO_COMMAND_MASK);
		
		thread = new IRControllerThread();
		//thread.setDaemon(true);
	//	thread.start();
	}
	
	
	public void start() {
		if (!thread.isAlive()){
			thread.start();
		}
	}
	
	
	public void stop() {
		thread.interrupt();
	}
	
	public void sendCommand(int commandAndTimeout){
		nextCommand.set(commandAndTimeout);
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
//	public void sendCommand(int commandAndTimeout){
//		// if command is not empty
//		if ((commandAndTimeout&NO_COMMAND_MASK)==0){
//			//   send immediately
//			transmitter.sendCommand(commandAndTimeout&COMMAND_MASK);
//			//   TODO modify command break =>none
//			int red = ((0x3<<4)&commandAndTimeout)>>4;
//			int blue = ((0x3<<6)&commandAndTimeout)>>6;
//			if (red==BREAK){
//				red = NONE;
//				commandAndTimeout = commandAndTimeout & (~(0x3<<4)); // convert to none
//			}
//			if (blue==BREAK){
//				blue = NONE;
//				commandAndTimeout = commandAndTimeout & (~(0x3<<6)); // convert to none
//			}
//			
//			// if command has timeout
//			int timeout = commandAndTimeout>>TIMEOUT_SHIFT;
//			if (timeout>0){
//				//   set nextCommand to this command
//				nextCommand.set(commandAndTimeout);
//			} else {
//				//   set nextCommand to no command
//				nextCommand.set(NO_COMMAND_MASK);
//			}
//		} else {
//			nextCommand.set(NO_COMMAND_MASK);
//		}
//	}
	
}
