package com.devoteam.srit.xmlloader.core.report;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.newstats.IStatCounter;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.stats.StatAverage;

import junit.framework.TestCase;

public class testReportGenerator extends TestCase {

	public testReportGenerator() {
		// TODO Auto-generated constructor stub
	}

	public testReportGenerator(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	protected void setUp() throws Exception {
		if (Tester.getInstance() == null) {
			System.out.println("Building a tester instance.");
			Tester.buildInstance(Tester.Mode.MODE_TEXT);
		}
	}

	protected void tearDown() throws Exception {
		StatPool.getInstance().reset();
	}

	public void NOtestNSgenerateeport2() throws Exception {
		
		
		//transactions
		System.out.println("Transactions flow");
		LinkedList<SequenceRunner> list = new LinkedList<SequenceRunner>();
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/sip/sendinvite/transaction/flowS"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/sip/receiveinvite/transaction/flowS"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/sip/sendack/transaction/flowS"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/sip/receiveack/transaction/flowS"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/aaa/sendmma/transaction/flowS"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/aaa/receivemma/transaction/flowS"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/aaa/sendudr/transaction/flowS"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/aaa/receiveudr/transaction/flowS"));
		
		ReportGenerator rg = ReportGenerator.getInstance();
    	
		rg.setTestBegin(System.currentTimeMillis());
    	long startTime = System.currentTimeMillis();int i=0;
    	for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			SequenceRunner sequenceRunner = (SequenceRunner) iterator.next();
			sequenceRunner.start();
			System.out.println("Sequence Runner number "+i+"started at:"+(System.currentTimeMillis()-startTime));
			Thread.sleep(100);
			i++;
		}
    	//waiting for all of the Sequence Runners to finish
    	for (SequenceRunner sequenceRunner : list) {
    		sequenceRunner.join();
    	}

    	//transaction response time
    	System.out.println("Transactions response time");
		LinkedList<SequenceRunner> list1 = new LinkedList<SequenceRunner>();
		list1.add(new SequenceRunner(100, 100, 10, "/protocol/sip/sendinvite/transactiontime/value"));
		list1.add(new SequenceRunner(100, 100, 10, "/protocol/sip/receiveinvite/transactiontime/value"));
		list1.add(new SequenceRunner(100, 100, 10, "/protocol/sip/sendack/transactiontime/value"));
		list1.add(new SequenceRunner(100, 100, 10, "/protocol/sip/receiveack/transactiontime/value"));
		list1.add(new SequenceRunner(100, 100, 10, "/protocol/aaa/sendmma/transactiontime/value"));
		list1.add(new SequenceRunner(100, 100, 10, "/protocol/aaa/receivemma/transactiontime/value"));
		list1.add(new SequenceRunner(100, 100, 10, "/protocol/aaa/sendudr/transactiontime/value"));
		list1.add(new SequenceRunner(100, 100, 10, "/protocol/aaa/receiveudr/transactiontime/value"));
    	
		i=0;
		for (Iterator iterator = list1.iterator(); iterator.hasNext();) {
			SequenceRunner sequenceRunner = (SequenceRunner) iterator.next();
			sequenceRunner.start();
			System.out.println("Sequence Runner number "+i+"started at:"+(System.currentTimeMillis()-startTime));
			Thread.sleep(100);
			i++;
		}
    	//waiting for all of the Sequence Runners to finish
    	for (SequenceRunner sequenceRunner : list1) {
    		sequenceRunner.join();
    	}

    	long stopTime = System.currentTimeMillis();
    	long delta = stopTime-startTime;
    	rg.setTestEnd(System.currentTimeMillis());
    	System.out.println("Test time:"+delta);

    	long reportStart = System.currentTimeMillis();
        //rg.NSgenerateReport2();
        System.out.println("Generation time:"+(System.currentTimeMillis()-reportStart));
        
        StatAverage sTemp= (StatAverage) StatPool.getInstance().get(new StatKey("/protocol/sip/sendinvite/transactiontime/value"));
    
        
        }

	
	public void NOtestNSRecursiveGenerator() throws Exception{
		
		StatPool p = StatPool.getInstance();
		
		
		//sip
		p.get("/protocol/sip/sendinvite/transaction/flow");
		p.get("/protocol/sip/sendack/transaction/flow");
		p.get("/protocol/sip/sendbye/transaction/flow");

		p.get("/protocol/sip/receiveinvite/transaction/flow");
		p.get("/protocol/sip/receiveack/transaction/flow");
		p.get("/protocol/sip/receivebye/transaction/flow");
		
		p.get("/protocol/sip/sendinvite/receive200/flow");
		p.get("/protocol/sip/sendack/receive200/flow");
		p.get("/protocol/sip/sendbye/receive200/flow");
		
		p.get("/protocol/sip/receiveinvite/send200/flow");
		p.get("/protocol/sip/receiveack/send200/flow");
		p.get("/protocol/sip/receivebye/send200/flow");
		
		
		//aaa
		p.get("/protocol/aaa/sendmar/transaction/flow");
		p.get("/protocol/aaa/sendsar/transaction/flow");
		p.get("/protocol/aaa/senduar/transaction/flow");
	
		p.get("/protocol/aaa/receivemar/transaction/flow");
		p.get("/protocol/aaa/receivesar/transaction/flow");
		p.get("/protocol/aaa/receiveuar/transaction/flow");
		
		p.get("/protocol/aaa/sendmar/receivemaa/transaction/flow");
		p.get("/protocol/aaa/sendsar/receivesaa/transaction/flow");
		p.get("/protocol/aaa/senduar/receiveuaa/transaction/flow");
	
		p.get("/protocol/aaa/receivemar/sendmaa/transaction/flow");
		p.get("/protocol/aaa/receivesar/sendsaa/transaction/flow");
		p.get("/protocol/aaa/receiveuar/senduaa/transaction/flow");
		
		ReportGenerator rg = ReportGenerator.getInstance();
		
		//rg.NSRecursiveGenerator(new StatKey("/protocol"), "send", 0);
		//rg.NSRecursiveGenerator2(new StatKey("/protocol"));
	}
	
	public void NOtestMatchPattern() throws Exception{
		ReportGenerator rg = ReportGenerator.getInstance();
		StatKey examinedStatKey = new StatKey("protocol", "sip");
		StatKey patternStatKey = new StatKey(".*",".*","send.*","_transaction","flow");
		
		//StatKey result = rg.matchTemplate(patternStatKey, examinedStatKey);
		//System.out.println(result);
		
		
	}
	
	public void testNSnewBetterGenerator() throws Exception{
		//transactions
		System.out.println("Transactions flow");
		LinkedList<SequenceRunner> list = new LinkedList<SequenceRunner>();
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/sip/sendinvite/_transaction/flow"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/sip/receiveinvite/_transaction/flow"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/sip/sendack/_transaction/flow"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/sip/receiveack/_transaction/flow"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/aaa/sendmma/_transaction/flow"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/aaa/receivemma/_transaction/flow"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/aaa/sendudr/_transaction/flow"));
		list.add(new SequenceRunner(100, 100, 1L, "/protocol/aaa/receiveudr/_transaction/flow"));
		
		ReportGenerator rg = ReportGenerator.getInstance();
    	
		rg.setTestBegin(System.currentTimeMillis());
    	long startTime = System.currentTimeMillis();int i=0;
    	for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			SequenceRunner sequenceRunner = (SequenceRunner) iterator.next();
			sequenceRunner.start();
			System.out.println("Sequence Runner number "+i+"started at:"+(System.currentTimeMillis()-startTime));
			Thread.sleep(100);
			i++;
		}
    	//waiting for all of the Sequence Runners to finish
    	for (SequenceRunner sequenceRunner : list) {
    		sequenceRunner.join();
    	}
    	
    	//String reportString = rg.protocolPartGenerator(new StatKey("/protocol"));
    	//rg.NSwriteReportToFile("protocol", reportString);
    	//rg.NSgenerateReport2(reportString);
		
	}
	
	//help functions / inner classes
	public void simulateFlowOfEvents(int numberOfEvents, long pauseBetweenEvents, long meanValue, String pathID) throws InterruptedException {
		for (int i = 0; i < numberOfEvents; i++) {
			StatPool.getInstance().get(new StatKey(pathID)).addValue(meanValue);
			Thread.sleep(pauseBetweenEvents);
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private class SequenceRunner extends Thread {
		private int numberOfEvents;
		private long period;
		private String pathID;

		private long meanValue;
		
		public SequenceRunner(int numberOfEvents, long period, long meanValue, String pathID) {
			this.numberOfEvents = numberOfEvents;
			this.period = period;
			this.pathID = pathID;
			this.meanValue=meanValue;
			
			//StatKey tempKey = new StatKey(pathID);
			//String type = tempKey.getAttribute(tempKey.getAllAttributes().length);
			
			//creating the counter (to solve some concurent exceptions in StatTimer)
			//StatPool.getInstance().get(new StatKey(pathID));
		}

		public void run() {
			try {
				simulateFlowOfEvents(numberOfEvents, period,meanValue, pathID);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
