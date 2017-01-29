package testing;

import app_kvServer.KVServer;
import org.junit.Test;

import client.KVStore;
import junit.framework.TestCase;
import common.messages.KVMessage;
import common.messages.KVMessage.StatusType;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import app_kvServer.storageServer;


public class InteractionTest extends TestCase {

	private KVStore kvClient;
	int CACHE_SIZE =5;
	String CACHE_STRATEGY = "FIFO";
	int PORT = 3000;
	
	public void setUp() {
		kvClient = new KVStore("localhost", 50000);
		try {
			kvClient.connect();
		} catch (Exception e) {
		}
	}

	public void tearDown() {
		kvClient.disconnect();
	}
	
	
	@Test
	public void testPut() {
		String key = "foo";
		String value = "bar";
		KVMessage response = null;
		Exception ex = null;

		try {
			response = kvClient.put(key, value);
		} catch (Exception e) {
			ex = e;
		}

		assertTrue(ex == null && response.getStatus() == StatusType.PUT_SUCCESS);
	}
	
	@Test
	public void testPutDisconnected() {
		kvClient.disconnect();
		String key = "foo";
		String value = "bar";
		Exception ex = null;

		try {
			kvClient.put(key, value);
		} catch (Exception e) {
			ex = e;
		}

		assertNotNull(ex);
	}

	@Test
	public void testUpdate() {
		String key = "updateTestValue";
		String initialValue = "initial";
		String updatedValue = "updated";
		
		KVMessage response = null;
		Exception ex = null;

		try {
			kvClient.put(key, initialValue);
			response = kvClient.put(key, updatedValue);
			
		} catch (Exception e) {
			ex = e;
		}

		assertTrue(ex == null && response.getStatus() == StatusType.PUT_UPDATE
				&& response.getValue().equals(updatedValue));
	}
	
	@Test
	public void testDelete() {
		String key = "deleteTestValue";
		String value = "toDelete";
		
		KVMessage response = null;
		Exception ex = null;

		try {
			kvClient.put(key, value);
			response = kvClient.put(key, "null");
			
		} catch (Exception e) {
			ex = e;
		}

		assertTrue(ex == null && response.getStatus() == StatusType.DELETE_SUCCESS);
	}
	
	@Test
	public void testGet() {
		// String key = "foo";
		// String value = "bar";
		// KVMessage response = null;
		// Exception ex = null;

		// 	try {
		// 		kvClient.put(key, value);
		// 		response = kvClient.get(key);
		// 	} catch (Exception e) {
		// 		ex = e;
		// 	}
		
		// assertTrue(ex == null && response.getValue().equals("bar"));

		clearFile();

		KVServer kvServer = new KVServer(PORT,CACHE_SIZE,CACHE_STRATEGY);
		KVMessage getErrorMessage = null;

		try{
			getErrorMessage = kvServer.get("nullKey");
		}catch(Exception e){
			e.printStackTrace();
		}

		assertTrue(getErrorMessage.getStatus() == KVMessage.StatusType.GET_ERROR);

	}

	@Test
	public void testGetUnsetValue() {
		String key = "anunsetvalue";
		KVMessage response = null;
		Exception ex = null;

		try {
			response = kvClient.get(key);
		} catch (Exception e) {
			ex = e;
		}

		assertTrue(ex == null && response.getStatus() == StatusType.GET_ERROR);
	}


public void clearFile(){
	PrintWriter pw = null;
		try {
			pw = new PrintWriter("./storage.txt");
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

}

}
