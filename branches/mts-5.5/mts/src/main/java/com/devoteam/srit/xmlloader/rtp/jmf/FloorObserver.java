/*
 * Copyright (c) 2005 Srit Devoteam. All rights reserved.
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