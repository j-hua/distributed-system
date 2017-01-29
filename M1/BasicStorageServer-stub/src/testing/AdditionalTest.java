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
	public void getCheck(){
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
	public void getError(){
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