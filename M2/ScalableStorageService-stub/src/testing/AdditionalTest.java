package testing;

import org.junit.Test;

import client.KVStore;
import junit.framework.TestCase;
import common.messages.KVMessage;
import common.messages.KVMessage.StatusType;


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
}
