package com.devoteam.srit.xmlloader.core.report;

import java.io.IOException;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.newstats.StatCounter;
import com.devoteam.srit.xmlloader.core.newstats.StatCounterConfigManager;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.report.derived.StatPercentage;

import junit.framework.TestCase;

public class ReportGeneratorNSTest extends TestCase {

	public ReportGeneratorNSTest(String name) {
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

	public void testProtocolPartGenerator() throws InterruptedException, IOException {
		StatPool statPool = StatPool.getInstance();
		
		StatCounter statCounter =  (StatCounter) statPool.get(new StatKey("/protocol/sip/invitesend/_transaction"));
		//StatCounter statCounter = new StatCounter(new StatKey("/protocol/sip/invitesend/_transaction"), System.currentTimeMillis(),StatCounterConfigManager.getInstance().getCounterParameters(new StatKey("/protocol/sip/invitesend/_transaction")));

		for (int i = 0; i < 100; i++) {
			statCounter.addValue(1);
			Thread.sleep(10);
		}
		
		System.out.println("Instant value: "+statCounter.getInstantValue());
		System.out.println("Instant value(2): "+statCounter.getInstantValue());
		for (int i = 0; i < 100; i++) {
			statCounter.addValue(1);
			Thread.sleep(10);
		}

		System.out.println("Instant value: "+statCounter.getInstantValue());
		System.out.println("Instant value(2): "+statCounter.getInstantValue());
		
		System.out.println("Finised");

		ProtocolReportGenerator rg = new ProtocolReportGenerator(statPool);
		
		String reportString = rg.protocolPartGenerator(new StatKey("/protocol"));
    	rg.NSwriteReportToFile("protocol", reportString);
		
	}

}
