package com.sun.media.rtp;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Random;

import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.Log;
import com.sun.media.rtp.util.RTPMediaThread;

// Referenced classes of package com.sun.media.rtp:
//            RTCPTransmitter, SSRCCache, SSRCInfo

public class RTCPReporter
    implements Runnable
{
	// PTT adaptation
	public static boolean PERIODIC_SR_ENABLED = false;
	// End PTT adaptation

    RTCPTransmitter transmit;
    SSRCCache cache;
    RTPMediaThread reportthread;
    Random myrand;
    boolean restart;
    boolean closed;
    InetAddress host;
    String cname;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];

    public RTCPReporter(SSRCCache ssrccache, RTCPTransmitter rtcptransmitter)
    {
        restart = false;
        closed = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        cache = ssrccache;
        setTransmitter(rtcptransmitter);
        reportthread = new RTPMediaThread(this, "RTCP Reporter");
        reportthread.useControlPriority();
        reportthread.setDaemon(true);
        reportthread.start();
    }

    public void close(String s)
    {
        synchronized(reportthread)
        {
            closed = true;
            reportthread.notify();
        }
        releasessrc(s);
        transmit.close();
    }

    public void releasessrc(String s)
    {
        transmit.bye(s);
        transmit.ssrcInfo.setOurs(false);
        transmit.ssrcInfo = null;
    }

    public void run()
    {
        if(restart)
        {
            restart = false;
        }
        do
        {
            double d = cache.calcReportInterval(cache.ourssrc.sender, false);
            synchronized(reportthread)
            {
                try
                {
                    reportthread.wait((long)d);
                }
                catch(InterruptedException interruptedexception)
                {
                    Log.dumpStack(interruptedexception);
                }
            }
            if(closed)
            {
                return;
            }
            if(!restart)
            {
            	// PTT adaptation
            	if (PERIODIC_SR_ENABLED) {
	                transmit.report();
            	}            
			    // End PTT adaptation
            } else
            {
                restart = false;
            }
        } while(true);
    }

    public void setTransmitter(RTCPTransmitter rtcptransmitter)
    {
        transmit = rtcptransmitter;
    }

    static 
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException _ex) { }
    }
}
