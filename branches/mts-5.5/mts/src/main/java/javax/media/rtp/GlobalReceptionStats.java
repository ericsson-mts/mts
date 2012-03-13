package javax.media.rtp;

/**
 * Adaptation of the JMF implementation to support new statistic counters
 *  
 * @author JM. Auffret
 */
public interface GlobalReceptionStats {

	public abstract int getBadRTCPPkts();

	public abstract int getBadRTPkts();

	public abstract int getBytesRecd();

	public abstract int getRtcpBytesRecd();

	public abstract int getLocalColls();

	public abstract int getMalformedBye();

	public abstract int getMalformedRR();

	public abstract int getMalformedSDES();

	public abstract int getMalformedSR();

	public abstract int getPacketsLooped();

	public abstract int getPacketsRecd();

	public abstract int getRTCPRecd();

	public abstract int getRemoteColls();

	public abstract int getSRRecd();

	public abstract int getTransmitFailed();

	public abstract int getUnknownTypes();
}
