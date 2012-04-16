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

package com.devoteam.srit.xmlloader.rtp.jmf;

/**
 * Observer of floor events
 * 
 * @author JM. Auffret
 */
public class FloorObserver {
	
	/**
	 * List of listeners
	 */
	private java.util.Vector listeners = new java.util.Vector();

	/**
	 * Add a new listener
	 * 
	 * @param obj Listener to be added
	 */
	public void addFloorMessageListener(FloorListener obj) {
		listeners.addElement(obj);
	}

	/**
	 * Remove a listener
	 * 
	 * @param obj Listener to be removed
	 */
	public void removeFloorMessageListener(FloorListener obj) {
		listeners.removeElement(obj);
	}

	/**
	 * Notify all listeners that a new floor event has arrived
	 * 
	 * @param event Floor event object
	 */
	public void notifyFloorListeners(FloorEvent event) {
		FloorListener obj;
		for (int i = 0; i < listeners.size(); i++) {
			obj = (FloorListener) listeners.elementAt(i);
			if (obj != null)
				obj.floorCallback(event);
		}
	}
}