/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.core.hybridnio;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 *
 * @author gpasquiers
 */
public interface IOHandler
{
	/**
	 * @param selectionKey
	 * @param selectableChannel (should be same as selectionKey.channel() ?)
	 */
    public void onIorInit(SelectionKey selectionKey, SelectableChannel selectableChannel);

	/**
     * triggered when readable (there is data to read)
	 */
    public void onIorInputReady();

	/**
     * triggered when writable (it is possible to write data)
	 */
    public void onIorOutputReady();

	/**
     * triggered on connect event (channel has either finished, or failed to finish, its socket-connection operation)
	 */
    public void onIorConnectReady();

	/**
	 * triggered on accept event (channel is ready to accept a new socket connection)
	 */
    public void onIorAcceptReady();
}
