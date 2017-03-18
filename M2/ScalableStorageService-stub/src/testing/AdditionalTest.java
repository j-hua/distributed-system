package testing;

import app_kvEcs.Ecs;
import app_kvServer.KVServer;
import org.junit.Test;

import client.KVStore;
import junit.framework.TestCase;
import common.messages.KVMessage;

import java.io.IOException;
import java.io.PrintWriter;


public class AdditionalTest extends TestCase {
	
	// TODO add your test cases, at least 3
	int CACHE_SIZE =5;
	String CACHE_STRATEGY = "FIFO";
	int PORT = 3000;
	int ptLimit = 100;

	@Test
	public void testStub() {
		assertTrue(true);
	}

	/**
	 * Test whether you can retrieve a value from
	 * persistent storage after inserting it
	 */

	@Test
	public void testPutGet(){
		clearFile(50000);

		KVServer kvs = new KVServer(50000, "null", -1);
		new Thread(kvs).start();
		kvs.state = KVServer.SERVER_READY;
		kvs.initKVServer(new String[4], 128, "fifo");

		Exception putEx = null;
		Exception getEX = null;
		KVMessage kvMessage = null;
		KVMessage getMessage = null;

		try{
			kvMessage = kvs.put("one","52");
		}catch(Exception e){
			e.printStackTrace();
			putEx = e;
		}

		assertTrue(putEx == null && kvMessage.getStatus()==KVMessage.StatusType.PUT_SUCCESS);

		try{
			getMessage = kvs.get("one");
		}catch (Exception e){
			e.printStackTrace();
			getEX = e;
		}

		assertTrue(getEX == null && getMessage.getStatus() == KVMessage.StatusType.GET_SUCCESS);
	}


	/**
	 * put <key><value>, <key> has a max length of 20 bytes
	 * <value> has a max length of 120kBytes
	 * check if a put-operation with a large key returns error
	 *
	 */
	@Test
	public void testPutLargeKey() {
		String key = "averylargekeythatisbiggerthantwentybytes";
		String value = "ok";

		KVMessage response = null;
		Exception ex = null;

		try {
			KVStore kvClient = new KVStore("localhost",50000);
			response = kvClient.put(key, value);

		} catch (Exception e) {
			ex = e;
		}

		assertTrue(ex == null && response.getStatus() == KVMessage.StatusType.PUT_ERROR);
	}

	/**
	 * get <key>, <key> has a max length of 20 bytes
	 * check if a get-operation with a large key returns error
	 *
	 */
	@Test
	public void testGetLargeKey() {
		String key = "averylargekeythatisbiggerthantwentybytes";
		String value = "ok";

		KVMessage response = null;
		Exception ex = null;

		try {
			KVStore kvClient = new KVStore("localhost",50000);
			response = kvClient.get(key);

		} catch (Exception e) {
			ex = e;
		}

		assertTrue(ex == null && response.getStatus() == KVMessage.StatusType.GET_ERROR);
	}

	// @Test
	// public void testConsistentHashing(){
	// 	//Ecs.tester();

	// 	assertTrue(true);
	// }


	public static void clearFile(int port){

		PrintWriter pw = null;
		try {
			String fileName = "./data/storage" + Integer.toString(port) + ".txt";
			pw = new PrintWriter(fileName);
			pw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
