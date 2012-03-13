/*
 * Created on Feb 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.devoteam.srit.xmlloader.rtp.jmf;

/**
 * @author pn007888
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FloorState {
    
    int state;
    private FloorState(int i) {
        state=i;
    }
    
    public boolean equals(Object o) {
        return (o instanceof FloorState) && (((FloorState)o).state == state);
    }
	/**
	 * State "has no floor"
	 */
	public final static FloorState HAS_NO_FLOOR = new FloorState(0);

	/**
	 * State "request pending"
	 */
	public final static FloorState REQ_PENDING = new FloorState(1);

	/**
	 * State "has floor"
	 */
	public final static FloorState HAS_FLOOR = new FloorState(2);

	/**
	 * State "release pending"
	 */
	public final static FloorState REL_PENDING = new FloorState(3);
}
