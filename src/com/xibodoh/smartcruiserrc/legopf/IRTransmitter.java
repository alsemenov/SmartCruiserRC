/*
  ~ Copyright (c) 2014 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package com.xibodoh.smartcruiserrc.legopf;
/**
 * Generic IR transmitter interface.
 * @author semenov
 *
 */
public interface IRTransmitter {
	public void sendCommand(int command);
}
