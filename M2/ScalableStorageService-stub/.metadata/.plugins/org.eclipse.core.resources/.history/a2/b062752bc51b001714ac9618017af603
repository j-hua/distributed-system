package testing;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Level;

import app_kvServer.KVServer;
import app_kvEcs.Ecs;
import junit.framework.Test;
import junit.framework.TestSuite;
import logger.LogSetup;
import org.apache.log4j.Logger;

public class AllTests {



	static {
		try {
			new LogSetup("logs/testing/test.log", Level.ERROR);

			//use a default of 128 keys, FIFO replacement
         /*   KVServer kvs1 = new KVServer(50010, "null", -1);
			KVServer kvs2 = new KVServer(50014, "null", -1);
			KVServer kvs3 = new KVServer(50011, "null", -1);
            testing.AdditionalTest.clearFile(50010);
			testing.AdditionalTest.clearFile(50014);
			testing.AdditionalTest.clearFile(50011);
            //new Thread(kvs1).start();
            kvs1.state = KVServer.SERVER_READY;
			kvs2.state = KVServer.SERVER_READY;
			kvs3.state = KVServer.SERVER_READY;
            kvs1.initKVServer(new String[]{"3ebf39bfa08189651d170e593782fea4,51feb15dc634eb38a2be7d96b6cf6fa5," +
					"127.0.0.1,50010,127.0.0.1,50014,127.0.0.1,50011 " +
					"51feb15dc634eb38a2be7d96b6cf6fa5,c98f070ad535b44eb9ff4469c98a00f3," +
					"127.0.0.1,50014,127.0.0.1,50011,127.0.0.1,50010 " +
					"c98f070ad535b44eb9ff4469c98a00f3,3ebf39bfa08189651d170e593782fea4," +
					"127.0.0.1,50011,127.0.0.1,50010,127.0.0.1,50014"}, 128, "fifo");
			kvs1.initKVServer(new String[]{"3ebf39bfa08189651d170e593782fea4,51feb15dc634eb38a2be7d96b6cf6fa5," +
					"127.0.0.1,50010,127.0.0.1,50014,127.0.0.1,50011 " +
					"51feb15dc634eb38a2be7d96b6cf6fa5,c98f070ad535b44eb9ff4469c98a00f3," +
					"127.0.0.1,50014,127.0.0.1,50011,127.0.0.1,50010 " +
					"c98f070ad535b44eb9ff4469c98a00f3,3ebf39bfa08189651d170e593782fea4," +
					"127.0.0.1,50011,127.0.0.1,50010,127.0.0.1,50014"}, 128, "fifo");
			kvs1.initKVServer(new String[]{"3ebf39bfa08189651d170e593782fea4,51feb15dc634eb38a2be7d96b6cf6fa5," +
					"127.0.0.1,50010,127.0.0.1,50014,127.0.0.1,50011 " +
					"51feb15dc634eb38a2be7d96b6cf6fa5,c98f070ad535b44eb9ff4469c98a00f3," +
					"127.0.0.1,50014,127.0.0.1,50011,127.0.0.1,50010 " +
					"c98f070ad535b44eb9ff4469c98a00f3,3ebf39bfa08189651d170e593782fea4," +
					"127.0.0.1,50011,127.0.0.1,50010,127.0.0.1,50014"}, 128, "fifo");
			kvs1.start();
			kvs2.start();
			kvs3.start();*/

         Ecs ecs = new Ecs();
         ecs.readFile();
         ecs.kvList = new ArrayList<>();
		 ecs.initService(8,128,"fifo");
		 ecs.start();

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
