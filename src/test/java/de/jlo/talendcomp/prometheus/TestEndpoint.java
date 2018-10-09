package de.jlo.talendcomp.prometheus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestEndpoint {
	
	Endpoint e;
	int port = 12345;

	@Before
	public void setUp() throws Exception {
		e = new Endpoint();
		e.startServer(port);
	}

	@After
	public void shutdown() {
		try {
			Thread.sleep(1000*30);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("stop server");
		e.stopServer();
	}
	
	@Test
	public void testSetGaugeValue() throws Exception {
		String metricName = "test";
		String label = "method";
		String help = "Test metrik";
		e.registerGauge(metricName, help, label);
		e.setGaugeValue(metricName, help, "post", 10d);
	}
	
}
