/*
 * Copyright (c) 2005 Srit Devoteam. All rights reserved.
 */
package com.devoteam.srit.xmlloader.rtp.jmf;

/**
 * Floor event
 * 
 * @author JM. Auffret
 */
public class FloorEvent {
	
	/**
	 * Unknown event
	 */
	public static final int UNKNOWN_EVENT = -1;

	/**
	 * Error event
	 */
	public static final int EVENT_FLOOR_ERROR = -2;

	/**
	 * Event floor request
	 */
	public static final int EVENT_FLOOR_REQUEST = 0x00;
	
	/**
	 * Event floor grant
	 */
	public static final int EVENT_FLOOR_GRANT = 0x01;
	
	/**
	 * Event floor taken
	 */
	public static final int EVENT_FLOOR_TAKEN = 0x02;
	
	/**
	 * Event floor taken with ack
	 */
	public static final int EVENT_FLOOR_TAKEN_ACK = 0x12;

	/**
	 * Event floor deny
	 */
	public static final int EVENT_FLOOR_DENY = 0x03;

	/**
	 * Event floor release
	 */
	public static final int EVENT_FLOOR_RELEASE = 0x04;

	/**
	 * Event floor idle
	 */
	public static final int EVENT_FLOOR_IDLE = 0x05;

	/**
	 * Event floor revoke
	 */
	public static final int EVENT_FLOOR_REVOKE = 0x06;
	
	/**
	 * Event TBCP_ACK
	 */
	public static final int EVENT_ACK = 0x07;
	
	/**
	 * Type of event
	 */
	private int event = UNKNOWN_EVENT;

	/**
	 * Data associated to the event
	 */
	private Object data = null;

	/**
	 * Constructor
	 * 
	 * @param evt Type of event
	 * @param dataEvt Data associated to the event
	 */
	public FloorEvent(int evt, Object dataEvt) {
		this.event = evt;
		this.data = dataEvt;
	}

	/**
	 * Constructor without data
	 * 
	 * @param evt Type of event
	 */
	public FloorEvent(int evt) {
		this.event = evt;
		this.data = null;
	}

	/**
	 * Return the type of event
	 * 
	 * @return Type of event
	 */
	public int getEvent() {
		return event;
	}

	/**
	 * Return the data associated to the event
	 *
	 * @return Data associated to the event
	 */
	public Object getData() {
		return data;
	}
}