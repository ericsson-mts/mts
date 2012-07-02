package com.devoteam.srit.xmlloader.core.newstats;
/*
 * StatPoolTest.java
 * JUnit based test
 *
 * Created on 31 janvier 2008, 17:50
 */

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.utils.Config;
import junit.framework.*;
import java.lang.RuntimeException;
import java.lang.RuntimeException;
import java.lang.RuntimeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.stats.StatCounter;
import com.devoteam.srit.xmlloader.core.stats.StatAverage;
import com.devoteam.srit.xmlloader.core.stats.StatEnumeration;
import com.devoteam.srit.xmlloader.core.stats.StatTimer;
import com.devoteam.srit.xmlloader.core.stats.StatValue;

/**
 *
 * @author mjagodzinski
 */
public class StatPoolTest extends TestCase {
    
    public StatKey sk[];
    
    public StatPoolTest(String testName) {
        super(testName);

    }
    
    protected void setUp() throws Exception {
        if (Tester.getInstance() == null) {
            System.out.println("Building a tester instance.");
            Tester.buildInstance(Tester.Mode.MODE_TEXT);
        }
        sk = new StatKey[18];
        
        sk[0] = new StatKey(StatPool.PREFIX_PROTOCOL,"numberOfProtocolsUsed","counter");
        sk[1] = new StatKey(StatPool.PREFIX_PROTOCOL,"transaction","flow");
        sk[2] = new StatKey(StatPool.PREFIX_PROTOCOL,"responseTime","value");
        sk[3] = new StatKey(StatPool.PREFIX_PROTOCOL,"retransmision","counter");
        sk[4] = new StatKey(StatPool.PREFIX_PROTOCOL,"transactionClient","flow");
        sk[5] = new StatKey(StatPool.PREFIX_PROTOCOL,"transactionClient","counter");
        sk[6] = new StatKey(StatPool.PREFIX_PROTOCOL,"counter");
        sk[7] = new StatKey(StatPool.PREFIX_PROTOCOL,"number","flow");
        sk[8] = new StatKey(StatPool.PREFIX_PROTOCOL,"number","counter");
        sk[9] = new StatKey(StatPool.PREFIX_TEST,"success","counter");
        sk[10] = new StatKey(StatPool.PREFIX_TEST,"success","flow");
        sk[11] = new StatKey(StatPool.PREFIX_TEST,"total","counter");
        sk[12] = new StatKey(StatPool.PREFIX_TESTCASE,"success","counter");
        sk[13] = new StatKey(StatPool.PREFIX_TESTCASE,"success","flow");
        sk[14] = new StatKey(StatPool.PREFIX_PROTOCOL,"sip","invite","counter");
        sk[15] = new StatKey(StatPool.PREFIX_PROTOCOL,"sip","invite","flow");
        sk[16] = new StatKey(StatPool.PREFIX_PROTOCOL,"sip","bye","flow");
        sk[17] = new StatKey(StatPool.PREFIX_PROTOCOL,"sip","bye","counter");
        
    }
    
    protected void tearDown() throws Exception {
        int i =1;
        while (i < sk.length){
            sk[i] =null;
            i++;
        }
        sk = null;
        

        StatPool.getInstance().reset();
    }

/*    public void testGet1() throws Exception{
 
        System.out.println("get-StatKey");
        
        int i =0;
        while (i < sk.length){
            System.out.println(i+"Sk is null?"+sk[i].toString());
            StatPool.getInstance().get(sk[i]).addValueNS(i);
            i++;
        }
        
        //testing
        
        //obtaining a counter that exists
        StatKey statKeyReq1 = new StatKey(StatPool.PREFIX_OPERATION,"number","flowS");
        IStatCounter counter1 = StatPool.getInstance().get(statKeyReq1);
        assertTrue("/operation/number/flowS".equals(counter1.toString()));
        
        //obtaing a value of a counter that doesn't exist (if it doesn't exist - it will be created anyway by the StatPool
        // but it's value will be 0 and thats what we're testing
        StatKey statKeyReq2 = new StatKey(StatPool.PREFIX_OPERATION,"numberr","flowS");
        IStatCounter counter2 = StatPool.getInstance().get(statKeyReq2);
        assertEquals(counter2.getLastValueNS(),0);
        
    }*/
    
    //a help method
    public boolean containsElementAndValue(StatKey key,long value, List<IStatCounter> list){
        
        for (Iterator<IStatCounter> it = list.iterator(); it.hasNext();) {
            IStatCounter elem = it.next();
            if(elem.toString().equals(key.toString())){
                return true;
            }
        }
        return false;
    }
    
    //a help method
    public boolean containsElement(Object element, List list) {
        
        for (Iterator it = list.iterator(); it.hasNext();) {
            Object elem = (Object) it.next();
            if (elem.equals(element)) {return true;}
        }
        return false;
    }
    
    /**
     * Test of findStatKey method, of class com.devoteam.srit.xmlloader.core.newstats.StatPool.
     * @throws InterruptedException 
     */
 /*   public void testFindStatKey() throws Exception {

              
        int i =0;
        while (i < sk.length){
            StatPool.getInstance().get(sk[i]).addValueNS(i);
            i++;
            
        }
        
        System.out.println("findStatKey");
        
        //tests
        
        //variables
        StatKey searchPatternStattKey;
        StatPool instance = StatPool.getInstance();
        List<StatKey> result;
        
        
        
        System.out.println("   Searching for all keys that start with protocol/sip");
        searchPatternStattKey = new StatKey("protocol","sip");
        result = instance.findStatKey(searchPatternStattKey);
        this.assertEquals(true,containsElement(sk[14],result) );
        this.assertEquals(true,containsElement(sk[15],result) );
        this.assertEquals(true,containsElement(sk[16],result) );
        this.assertEquals(true,containsElement(sk[17],result) );
        this.assertEquals(4,result.size());
        
        
        System.out.println("    Searching - leaving some middle element as a wildcard");
        searchPatternStattKey = new StatKey("protocol","sip",".*","flowS");
        result = instance.findStatKey(searchPatternStattKey);
        this.assertEquals(true,containsElement(sk[15],result) );
        this.assertEquals(true,containsElement(sk[16],result) );
        this.assertEquals(2,result.size());
        
        System.out.println("    Searching - one of the attributes in the key are empty (shouldn't match anything)");
        searchPatternStattKey = new StatKey("protocol","sip","","flowS");
        result = instance.findStatKey(searchPatternStattKey);
        this.assertEquals(0,result.size());
        
        System.out.println("    Searching - one of the attributes in the key is null - shouldn't match anything");
        searchPatternStattKey = new StatKey("protocol","sip",null,"flowS");
        result = instance.findStatKey(searchPatternStattKey);
        this.assertEquals(0,result.size());
        
        
        System.out.println("    Searching '/' - means give me everything you've got");
        searchPatternStattKey = new StatKey("/");
        result = instance.findStatKey(searchPatternStattKey);
        instance.showList(result);
        this.assertEquals(18,result.size());
        
        System.out.println("    Searching - no param given");
        searchPatternStattKey = new StatKey();
        result = instance.findStatKey(searchPatternStattKey);
        this.assertEquals(0,result.size());
        
        System.out.println("    Searching - null StatKey passed.");
        searchPatternStattKey = null;
        result = instance.findStatKey(searchPatternStattKey);
        this.assertEquals(0,result.size());
        
        System.out.println("    Searching - giving more elements that we find in the keys kept in the pool");
        searchPatternStattKey = new StatKey("protocol","numberOfProtocolsUsed","counter","aaaaa");
        result = instance.findStatKey(searchPatternStattKey);
        this.assertEquals(0,result.size());
    }*/
    
  /*  public void testFindStatValue() throws Exception{
        //this.tearDown();this.setUp();
        int i =0;
        while (i < sk.length){
            StatPool.getInstance().get(sk[i]).addValueNS(i);
            i++;
            
        }
        
        System.out.println("findStatValue");
        
        //tests
        
        //variables
        StatKey searchPatternStattKey;
        StatPool instance = StatPool.getInstance();
        List<IStatCounter> result;
        
        System.out.println("   Searching for all keys that start with protocol/sip");
        searchPatternStattKey = new StatKey("protocol","sip");
        result = instance.findStatValue(searchPatternStattKey);
        this.assertEquals(true,containsElementAndValue(sk[14],14L,result) );
        this.assertEquals(true,containsElementAndValue(sk[15],15L,result) );
        this.assertEquals(true,containsElementAndValue(sk[16],16L,result) );
        this.assertEquals(true,containsElementAndValue(sk[17],17L,result) );
        this.assertEquals(4,result.size());
        
        
        System.out.println("    Searching - leaving some middle element as a wildcard");
        searchPatternStattKey = new StatKey("protocol","sip",".*","flowS");
        result = instance.findStatValue(searchPatternStattKey);
        this.assertEquals(true,containsElementAndValue(sk[15],15L,result) );
        this.assertEquals(true,containsElementAndValue(sk[16],16L,result) );
        this.assertEquals(2,result.size());
        
        System.out.println("    Searching - one of the attributes in the key are empty (shouldn't match anything)");
        searchPatternStattKey = new StatKey("protocol","sip","","flowS");
        result = instance.findStatValue(searchPatternStattKey);
        this.assertEquals(0,result.size());
        
        System.out.println("    Searching - one of the attributes in the key is null - shouldn't match anything");
        searchPatternStattKey = new StatKey("protocol","sip",null,"flowS");
        result = instance.findStatValue(searchPatternStattKey);
        this.assertEquals(0,result.size());
        
        
        System.out.println("    Searching '/' - means give me everything you've got");
        searchPatternStattKey = new StatKey("/");
        result = instance.findStatValue(searchPatternStattKey);
        instance.showList(result);
        this.assertEquals(18,result.size());
        
        System.out.println("    Searching - no param given");
        searchPatternStattKey = new StatKey();
        result = instance.findStatValue(searchPatternStattKey);
        this.assertEquals(0,result.size());
        
        System.out.println("    Searching - null StatKey passed.");
        searchPatternStattKey = null;
        result = instance.findStatValue(searchPatternStattKey);
        this.assertEquals(0,result.size());
        
        System.out.println("    Searching - giving more elements that we find in the keys kept in the pool");
        searchPatternStattKey = new StatKey("protocol","numberOfProtocolsUsed","counter","aaaaa");
        result = instance.findStatValue(searchPatternStattKey);
        this.assertEquals(0,result.size());
        
    }*/
    
    public void NOtestFindMatchingKeyStrict() throws Exception{
    	
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/_event/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/_event/flow"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/_retransmission/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/_retransmission/flow"));

    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/200/_event/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/200/_event/flow"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/200/_responseTime/value"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/200/_retransmission/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/200/_retransmission/flow"));

    	
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/180/_event/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/180/_event/flow"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/180/_responseTime/value"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/180/_retransmission/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/180/_retransmission/flow"));


    	
    	List<StatKey> list = StatPool.getInstance().findMatchingKeyStrict(new StatKey("/protocol/sip/invite/^[_].+"));
    	System.out.println("showing the result");
    	StatPool.showList(list);
    	
    }
    
    
    public void testRemove() throws Exception{
       	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/_event/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/_event/flow"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/_retransmission/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/_retransmission/flow"));

    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/200/_event/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/200/_event/flow"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/200/_responseTime/value"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/200/_retransmission/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/200/_retransmission/flow"));

    	
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/180/_event/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/180/_event/flow"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/180/_responseTime/value"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/180/_retransmission/counter"));
    	StatPool.getInstance().get(new StatKey("/protocol/sip/invite/180/_retransmission/flow"));
    	
    	
    	StatPool sp = StatPool.getInstance();
    	Logger.getLogger(StatPool.class).setLevel(Level.INFO);
    	sp.remove(new StatKey("protocol","sip","invite","180"));
    	
    	System.out.println("test Remove");
    	List<StatKey> list = sp.findStatKey(new StatKey("/protocol"));
    	
    	StatPool.showList(list);
    	
    	
    }
    
    public void simulateFlowOfEvents(int numberOfEvents, long pauseBetweenEvents, String pathID) throws InterruptedException {
    	for (int i = 0; i < numberOfEvents; i++) {
    		StatPool.getInstance().get(new StatKey(pathID)).addValue(1L);
    		Thread.sleep(pauseBetweenEvents);
		}
    }
    public void simulateFlowOfEventsOldStats(int numberOfEvents, long pauseBetweenEvents) throws InterruptedException {
    	for (int i = 0; i < numberOfEvents; i++) {
    		StatEnumeration.STAT_INVITE.increaseValue();
    		Thread.sleep(pauseBetweenEvents);
		}
    }
    public void NOtestSequenceOfEvents() throws Exception{
    	
    	SequenceRunner seqrunner1 = new SequenceRunner(100,100,"/protocol/sip/client/messages/invite/flowS");
    	SequenceRunner seqrunner2 = new SequenceRunner(100,100,"/protocol/sip/client/messages/bye/flowS");
    	SequenceRunner seqrunner3 = new SequenceRunner(100,100,"/protocol/sip/client/messages/cancel/flowS");
    	SequenceRunner seqrunner4 = new SequenceRunner(100,100,"/protocol/sip/client/messages/ack/flowS");
    	SequenceRunnerOldStats seqrunner5 = new SequenceRunnerOldStats(100,100);
    	ReportGenerator rg = ReportGenerator.getInstance();
    	
    	rg.setTestBegin(System.currentTimeMillis());
    	long startTime = System.currentTimeMillis();
    	seqrunner1.start();Thread.sleep(10);seqrunner2.start();Thread.sleep(10);seqrunner3.start();Thread.sleep(10);seqrunner4.start();
    	Thread.sleep(10);seqrunner5.run();
    	while(seqrunner1.isAlive() ||seqrunner2.isAlive() || seqrunner3.isAlive() || seqrunner4.isAlive() || seqrunner5.isAlive() ){
    	Thread.sleep(100);
    	}
    	long stopTime = System.currentTimeMillis();
    	long delta = stopTime-startTime;
    	rg.setTestEnd(System.currentTimeMillis()+2000);
    	System.out.println("Test time:"+delta);
    	
    	
    	
    	StatPool statPool = StatPool.getInstance();
    	StatCounter counter = (StatCounter) statPool.get("protocol","sip","client","messages","invite","counter");
        System.out.println("Counter to String: "+counter);
        System.out.println("Counter avg(float): "+counter.getAvgValue());
        float[] reportArray = counter.getReportArray();
        System.out.println("ReportArray size "+reportArray.length);
        
        
        
        StatEnumeration sumStat = (StatEnumeration) StatPool.getInstance().sum(new StatKey("/protocol/sip/client/messages/sum/flowS"), new StatKey("protocol/sip/client/messages/.*/flowS"));
        
        //rg.NSgenerateReport();
        
    }
    
     
    
    
    public static void main(String[] args) throws Exception{

        
    }
    

    /*Running events in separate threads helper method*/
    
    private  class SequenceRunner extends Thread{
    	private int numberOfEvents;
    	private long period;
    	private String pathID;
    	
    	
    	
    	
    	public SequenceRunner(int numberOfEvents, long period, String pathID){
    		this.numberOfEvents=numberOfEvents;
    		this.period=period;
    		this.pathID=pathID;
    	}
    	
    	public void run(){
    		try {
				simulateFlowOfEvents(numberOfEvents, period, pathID);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    }
    private  class SequenceRunnerOldStats extends Thread{
    	private int numberOfEvents;
    	private long period;
    	private String pathID;
    	
    	
    	
    	
    	public SequenceRunnerOldStats(int numberOfEvents, long period){
    		this.numberOfEvents=numberOfEvents;
    		this.period=period;
    		this.pathID=pathID;
    	}
    	
    	public void run(){
    		try {
				simulateFlowOfEventsOldStats(numberOfEvents, period);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    }
}
