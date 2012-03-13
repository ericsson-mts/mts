/*
 * Copyright (c) 2005 Srit Devoteam. All rights reserved.
 */
package com.devoteam.srit.xmlloader.rtp.jmf;

/**
 * Listener of floor events
 * 
 * @author JM. Auffret
 */
public interface FloorListener {
	
	/**
	 * Call-back method called when a floor event occurs
	 * 
	 * @param event Floor event object
	 */
	void floorCallback(FloorEvent event);
}