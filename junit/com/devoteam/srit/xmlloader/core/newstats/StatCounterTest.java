package com.devoteam.srit.xmlloader.core.newstats;

import junit.framework.TestCase;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.newstats.*;
import com.devoteam.srit.xmlloader.core.report.ToStringHelper;

public class StatCounterTest extends TestCase {

	public StatCounterTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		if (Tester.getInstance() == null) {
            System.out.println("Building a tester instance.");
            Tester.buildInstance(Tester.Mode.MODE_TEXT);
        }
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testStatCounter() throws InterruptedException{
		
		StatPool statPool = StatPool.getInstance();
		StatCounter statCounter = (StatCounter) statPool.get(new StatKey("/protocol/sip/invitesend/numberofRequests"));
		
		//StatCounter statCounter = new StatCounter(new StatKey("/protocol/sip/invitesend/numberofRequests"), System.currentTimeMillis(),StatCounterConfigManager.getInstance().getCounterParameters(new StatKey("/protocol/sip/invitesend/numberofRequests")));

		for (int i = 0; i < 100; i++) {
			statCounter.addValue(1);
			Thread.sleep(10);
		}
		
		System.out.println("Instant value: "+statCounter.getInstantValue());
		System.out.println("Instant value(2): "+statCounter.getInstantValue());
		System.out.println("global flow avg "+statCounter.globalDataset.getFlowAvg());
		System.out.println("max flow"+statCounter.globalDataset.getFlowMax());
		for (int i = 0; i < 10000; i++) {
			statCounter.addValue(1);
			Thread.sleep(1);
		}

		System.out.println("Instant value: "+statCounter.getInstantValue());
		System.out.println("Instant value(2): "+statCounter.getInstantValue());
		System.out.println("global flow avg "+statCounter.globalDataset.getFlowAvg());
		System.out.println("max flow"+statCounter.globalDataset.getFlowMax());

		for (int i = 0; i < 100; i++) {
			statCounter.addValue(1);
			Thread.sleep(10);
		}

		System.out.println("Instant value: "+statCounter.getInstantValue());
		System.out.println("Instant value(2): "+statCounter.getInstantValue());
		System.out.println("global flow avg "+statCounter.globalDataset.getFlowAvg());
		System.out.println("max flow"+statCounter.globalDataset.getFlowMax());

		for (int i = 0; i < 100; i++) {
			statCounter.addValue(1);
			Thread.sleep(10);
		}

		System.out.println("Instant value: "+statCounter.getInstantValue());
		System.out.println("Instant value(2): "+statCounter.getInstantValue());
		System.out.println("global flow avg "+statCounter.globalDataset.getFlowAvg());
		System.out.println("max flow"+statCounter.globalDataset.getFlowMax());

		for (int i = 0; i < 100; i++) {
			statCounter.addValue(1);
			Thread.sleep(10);
		}

		System.out.println("Instant value: "+statCounter.getInstantValue());
		System.out.println("Instant value(2): "+statCounter.getInstantValue());
		System.out.println("global flow avg "+statCounter.globalDataset.getFlowAvg());
		System.out.println("max flow"+statCounter.globalDataset.getFlowMax());

		for (int i = 0; i < 100; i++) {
			statCounter.addValue(1);
			Thread.sleep(10);
		}

		System.out.println("Instant value: "+statCounter.getInstantValue());
		System.out.println("Instant value(2): "+statCounter.getInstantValue());
		System.out.println("global flow avg "+statCounter.globalDataset.getFlowAvg());
		System.out.println("max flow"+statCounter.globalDataset.getFlowMax());
		statCounter.addValue(1);
		System.out.println("Finised");
		
		
		System.out.println("tobesumedCounter"+ToStringHelper.toString(statCounter));		
		System.out.println("GlobalDataset"+ToStringHelper.toString(statCounter.globalDataset));
		System.out.println("GraphDataset"+ToStringHelper.toString(statCounter.graphDataset));
		
		StatCounter sumCounter = (StatCounter) StatPool.getInstance().sum(new StatKey("/protocol/lalala"), new StatKey("/.*/.*/.*/numberofRequests"));
		System.out.println("sumCounter"+ToStringHelper.toString(sumCounter));		
		System.out.println("GlobalDataset"+ToStringHelper.toString(sumCounter.globalDataset));
		System.out.println("GraphDataset"+ToStringHelper.toString(sumCounter.graphDataset));
		
	}
	
	public void testStatCounterStatKeyLongStatCounterParameters() {
		fail("Not yet implemented");
	}

	public void testGetInstantValue() {
		fail("Not yet implemented");
	}

	public void testGetInstantAverage() {
		fail("Not yet implemented");
	}

	public void testAddValue() {
		fail("Not yet implemented");
	}

	public void testSum() {
		fail("Not yet implemented");
	}

	public void testGetId() {
		fail("Not yet implemented");
	}

	public void testSetId() {
		fail("Not yet implemented");
	}

	public void testMain() {
		fail("Not yet implemented");
	}

	public void testShowArray() {
		fail("Not yet implemented");
	}

	public void testDivide() {
		fail("Not yet implemented");
	}

}
