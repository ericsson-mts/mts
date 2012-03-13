/*
 * Copyright (c) 2005 Srit Devoteam. All rights reserved.
 */
package com.devoteam.srit.xmlloader.rtp.jmf;


/**
 * Floor timer
 * 
 * @author JM. Auffret
 */
public class FloorTimer extends javax.swing.Timer {
	
	/**
	 * Constructor
	 * 
	 * @param period Timer period
	 */
	public FloorTimer(int period) {
		super(period, null);
		setCoalesce(true);
		setRepeats(true);
	}

	/**
	 * Start the timer
	 */
	public void start() {
		super.start();
	}

	/**
	 * Stop the timer
	 */
	public void stop() {
		super.stop();
	}
}
