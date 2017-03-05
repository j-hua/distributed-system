package testing;

import java.io.IOException;

import org.apache.log4j.Level;

import app_kvServer.KVServer;
import junit.framework.Test;
import junit.framework.TestSuite;
import logger.LogSetup;


public class AllTests {

	static {
		try {
			new LogSetup("logs/testing/test.log", Level.ERROR);
			//use a default of 128 keys, FIFO replacement
            KVServer kvs = new KVServer(50000);
            kvs.state = KVServer.SERVER_READY;
            kvs.initKVServer(new String[4], 128, "fifo");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static Test suite() {
		TestSuite clientSuite = new TestSuite("Basic Storage ServerTest-Suite");
		clientSuite.addTestSuite(ConnectionTest.class);
		clientSuite.addTestSuite(InteractionTest.class); 
		clientSuite.addTestSuite(AdditionalTest.class); 
		return clientSuite;
	}
	
}
