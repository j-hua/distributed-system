package testing;

import app_kvServer.KVServer;
import app_kvServer.storageServer;
import common.messages.KVMessage;
import logger.LogSetup;
import org.apache.log4j.Level;
import org.junit.Test;

import junit.framework.TestCase;
//import KVServer.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class AdditionalTest extends TestCase {
	
	// TODO add your test cases, at least 3
	int CACHE_SIZE =5;
	String CACHE_STRATEGY = "FIFO";
	int PORT = 3000;

	/**
	 * Clear the DB before perming tests to avoid
	 * previous data hampering tests
	 */
	@Test
	public void testStub() {

		assertTrue(true);
	}

	@Test
	public void testGetCheck(){
		clearFile();

		KVServer kvServer = new KVServer(PORT,CACHE_SIZE,CACHE_STRATEGY);
		Exception putEx=null;
		Exception getEx = null;
		KVMessage kvMessage = null;
		KVMessage getMessage = null;

		try {
			kvMessage = kvServer.put("one","52");
		} catch (Exception e) {
			e.printStackTrace();
			putEx = e;
		}

		assertTrue(putEx == null && kvMessage.getStatus()== KVMessage.StatusType.PUT_SUCCESS);
		try {
			getMessage = kvServer.get("one");
		} catch (Exception e) {
			e.printStackTrace();
			getEx = e;
		}

		assertTrue(getEx == null && getMessage.getStatus() == KVMessage.StatusType.GET_SUCCESS);

	}
	@Test
	public void testGetError(){
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
	public void testUpdateCheck(){
		clearFile();

		KVServer kvServer = new KVServer(PORT,CACHE_SIZE,CACHE_STRATEGY);
		KVMessage putMessage = null;
		KVMessage updateMessage = null;

		try {
			putMessage = kvServer.put("one","54");
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(putMessage.getStatus()== KVMessage.StatusType.PUT_SUCCESS);

		try {
			updateMessage = kvServer.put("one","55");
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(updateMessage.getStatus()== KVMessage.StatusType.PUT_UPDATE && updateMessage.getValue().equals("55"));
	}

	@Test
	public void testDeleteCheck(){
		clearFile();

		KVServer kvServer = new KVServer(PORT, CACHE_SIZE,CACHE_STRATEGY);

		KVMessage putMessage = null;
		KVMessage deleteMessage = null;

		try {
			putMessage = kvServer.put("one", "52");
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(putMessage.getStatus() == KVMessage.StatusType.PUT_SUCCESS);

		try {
			deleteMessage = kvServer.put("one",null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue( deleteMessage.getStatus() == KVMessage.StatusType.DELETE_SUCCESS);
	}

	@Test
	public  void testDeleteError(){
		clearFile();

		KVServer kvServer = new KVServer(PORT, CACHE_SIZE,CACHE_STRATEGY);

		KVMessage deleteMessage = null;

		try {
			deleteMessage = kvServer.put("one",null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue( deleteMessage.getStatus() == KVMessage.StatusType.DELETE_ERROR);
	}

	/**
	 * Function puts a value in the cache and the DB
	 * and then checks specifically in the cache to check if the
	 * value was retrieved from the cache
	 */
	@Test
	public void testCacheCheck(){
		clearFile();

		KVServer kvServer = new KVServer(PORT, CACHE_SIZE,CACHE_STRATEGY);

		KVMessage putMessage = null;
		storageServer  storageServer= null;

		try {
			putMessage = kvServer.put("one", "45");
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(putMessage.getStatus() == KVMessage.StatusType.PUT_SUCCESS);

		storageServer = kvServer.getStorageServer();

		List<String> keys = storageServer.keyCache;
		List<String> values = storageServer.valueCache;

		assertTrue(keys.get(0).equals("one") && values.get(0).equals("45"));
	}

	@Test
	public void testCheckFifo(){
		clearFile();

		KVServer kvServer = new KVServer(PORT, CACHE_SIZE,CACHE_STRATEGY);

		KVMessage putMessage = null;
		KVMessage putMessage2 = null;
		KVMessage putMessage3 = null;
		KVMessage putMessage4 = null;
		KVMessage putMessage5 = null;
		KVMessage putMessage6 = null;

		storageServer  storageServer= null;

		try {
			putMessage = kvServer.put("one", "45");
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(putMessage.getStatus() == KVMessage.StatusType.PUT_SUCCESS);

		try {
			putMessage2 = kvServer.put("two", "46");
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(putMessage2.getStatus() == KVMessage.StatusType.PUT_SUCCESS);
		try {
			putMessage3 = kvServer.put("three", "47");
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(putMessage3.getStatus() == KVMessage.StatusType.PUT_SUCCESS);
		try {
			putMessage4 = kvServer.put("four", "48");
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(putMessage4.getStatus() == KVMessage.StatusType.PUT_SUCCESS);
		try {
			putMessage5 = kvServer.put("five", "49");
		} catch (Exception e) {
			e.printStackTrace();
		}

		/**
		 * At this point the cache is full
		 * The next value should kick the first entry out
		 * and put in <six,1000000>
		 */
		assertTrue(putMessage5.getStatus() == KVMessage.StatusType.PUT_SUCCESS);
		try {
			putMessage6 = kvServer.put("six", "1000000");
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(putMessage6.getStatus() == KVMessage.StatusType.PUT_SUCCESS);

		storageServer = kvServer.getStorageServer();

		/**
		 * Check if the last value was entered in the first slot of the cache
		 * hence confirming FIFO implementation
		 */
		List<String> keys = storageServer.keyCache;
		List<String> values = storageServer.valueCache;

		assertTrue(!keys.contains("one") && !values.contains("45"));
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