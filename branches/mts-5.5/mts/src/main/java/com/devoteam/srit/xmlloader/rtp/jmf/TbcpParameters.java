/*
 * Created on 27 sept. 2005
 *
*/
package com.devoteam.srit.xmlloader.rtp.jmf;

/**
 * @author ma007141
 *
 * TBCP parameters
 */
public class TbcpParameters {
	
    /**
	 * Queuing tag
	 */
    private final static String QUEUING = "queuing=";

    /**
	 * Priority tag
	 */
    private final static String PRIORITY = "tb_priority=";

    /**
	 * Timestamp tag
	 */
    private final static String TIMESTAMP = "timestamp=";

    /**
	 * TB granted tag
	 */
    private final static String TB_GRANTED = "tb_granted=";
    
    /**
	 * Queuing flag
	 */
	private boolean queuingEnabled = false;
	
	/**
	 * Timestamp flag
	 */
	private boolean timestampEnabled = false;
	
	/**
	 * TB granted flag
	 */
	private boolean tbGrantedReceived = false;
	
	/**
	 * Priority level
	 */
	private int priority = 1;	

	/**
	 * Constructor
	 */
	public TbcpParameters() {
		queuingEnabled = false;
		timestampEnabled = false;
		tbGrantedReceived = false;
		priority = 1;	
	}

	/**
	 * Returns the priority level
	 * 
	 * @return Level
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * Set the priority level
	 * 
	 * @param priority The priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	/**
	 * Is queuing activated
	 * 
	 * @return Boolean
	 */
	public boolean isQueuingEnabled() {
		return queuingEnabled;
	}
	
	/**
	 * Set the queuing mode
	 * 
	 * @param queuingEnabled The queuing mode
	 */
	public void setQueuingEnabled(boolean queuingEnabled) {
		this.queuingEnabled = queuingEnabled;
	}
	
	/**
	 * Is queuing activated
	 * 
	 * @return Boolean
	 */
	public boolean isTbGrantedReceived() {
		return tbGrantedReceived;
	}
	
	/**
	 * Set the TB granted mode
	 * 
	 * @param tbGrantedReceived The TB granted mode.
	 */
	public void setTbGrantedReceived(boolean tbGrantedReceived) {
		this.tbGrantedReceived = tbGrantedReceived;
	}
	
	/**
	 * Is timestamp activated
	 * 
	 * @return Boolean.
	 */
	public boolean isTimestampEnabled() {
		return timestampEnabled;
	}
	
	/**
	 * Set the timestamp flag
	 * 
	 * @param timestampEnabled The timestamp mode.
	 */
	public void setTimestampEnabled(boolean timestampEnabled) {
		this.timestampEnabled = timestampEnabled;
	}

	/**
     * Extract queuing parameter
     * 
     * @param line Line to parse
     * @return Boolean
     */
    final private static boolean extractQueuing(String line)  {
        boolean res = false;
        int index = line.indexOf(QUEUING);
        if (index != -1) {
            int start = QUEUING.length() + index;
            String value = line.substring(start, start + 1);
            res = (Integer.parseInt(value) == 1);
        }
        return res;
    }

    /**
     * Extract timestamp parameter
     * 
     * @param line Line to parse
     * @return Boolean
     */
    final private static boolean extractTimestamp(String line) {
        boolean res = false;
        int index = line.indexOf(TIMESTAMP);
        if (index != -1) {
            int start = TIMESTAMP.length() + index;
            String value = line.substring(start, start + 1);
            res = (Integer.parseInt(value) == 1);
        }
        return res;
    }

    /**
     * Extract timestamp parameter
     * 
     * @param line Line to parse
     * @return Boolean
     */
    final private static boolean extractTbGranted(String line) {
        boolean res = false;
        int index = line.indexOf(TB_GRANTED);
        if (index != -1) {
            int start = TB_GRANTED.length() + index;
            String value = line.substring(start, start + 1);
            res = (Integer.parseInt(value) == 1);
        }
        return res;
    }

    /**
     * Extract level priority
     * 
     * @param line Line to parse
     * @return Integer
     */
    final private static int extractPriority(String line) {
        int res = 1;
        int index = line.indexOf(PRIORITY);
        if (index != -1) {
            int start = PRIORITY.length() + index;
            String value = line.substring(start, start + 1);
            res = Integer.parseInt(value);
        }
        return res;
    }
	
    /**
     * Parse the TBCP parameters from SDP.
     * 
     * @param line Line to parse
     * @return Tbcp parameters
     */
    public static TbcpParameters parse(String line) {
    	TbcpParameters tbcp = new TbcpParameters();
		tbcp.setQueuingEnabled(extractQueuing(line));
		tbcp.setPriority(extractPriority(line));
        tbcp.setTimestampEnabled(extractTimestamp(line));
        tbcp.setTbGrantedReceived(extractTbGranted(line));
        return tbcp;
    }
}

