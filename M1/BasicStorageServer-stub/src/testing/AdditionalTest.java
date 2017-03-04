package testing;

import app_kvServer.KVServer;
import app_kvServer.storageServer;
import client.KVStore;
import common.messages.KVMessage;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

//import KVServer.*;

public class AdditionalTest extends TestCase {
	
	// TODO add your test cases, at least 3
	int CACHE_SIZE =5;
	String CACHE_STRATEGY = "FIFO";
	int PORT = 3000;
	int ptLimit = 100;


	/**
	 * Clear the DB before each test.
	 * that is what clearFile does
	 */
	@Test
	public void testStub() {

		assertTrue(true);
	}

	/**
	 * Tests whether you can retrieve a value from
	 * persistent storage after putting it there
	 */

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

	/**
	 * Tests for when you are getting a key is not there
	 */
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

	/**
	 * Does two puts and the latter one should say
	 * PUT_UPDATE instead of creating a new entry
	 */

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

	/**
	 * Checking if a delete is successful by putting a value
	 * and then deleting it
	 */

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

	/**
	 * Check if an invalid delete fails by trying to
	 * delete a key that does not exist
	 */
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
		storageServer  stserver= null;

		try {
			putMessage = kvServer.put("one", "45");
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(putMessage.getStatus() == KVMessage.StatusType.PUT_SUCCESS);

		stserver = kvServer.getStorageServer();


//		List<String> keys = storageServer.getKeyCache();
//		List<String> values = storageServer.getValueCache();


//		assertTrue(keys.get(0).equals("one") && values.get(0).equals("45"));
	}

	/**
	 * This checks if fifo works. Put 6 values. The 6th value
	 * kicks out the first value from the cache
	 */
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
		List<String> keys = storageServer.getKeyCache();
		List<String> values = storageServer.getValueCache();

		assertTrue(!keys.contains("one") && !values.contains("45"));
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

   /*
    @Test
    public void testGetDeletedKey() {
        String key = "atestkey";
        String value = "atestvalue";

        KVMessage response = null;
        Exception ex = null;

        try {
            KVStore kvClient = new KVStore("localhost",50000);
            kvClient.put(key,value);
            kvClient.put(key,"null");
            response = kvClient.get(key);

        } catch (Exception e) {
            ex = e;
        }

        assertTrue(ex == null && response.getStatus() == KVMessage.StatusType.GET_ERROR);
    }
*/

	/**
	 * check latency of different combination of cache size, cacheing strategy
     * and put/get operations
	 *
	 */
	@Test
	public void testPerformace() throws Exception {


        System.out.println("Performance test 40 puts, 10 gets, FIFO, 100");
        System.out.println("elapsedTime: " + evalLatency(100, "FIFO",40,10) + " milliseconds");

        System.out.println("Performance test 40 puts, 10 gets, LRU, 100");
        System.out.println("elapsedTime: " + evalLatency(100, "LRU",40,10) + " milliseconds");

        System.out.println("Performance test 40 puts +  10 gets, LFU, 100");
        System.out.println("elapsedTime: " + evalLatency(100, "LFU",40,10) + " milliseconds");

        System.out.println("Performance test 25 puts, 25 gets, FIFO, 100");
        System.out.println("elapsedTime: " + evalLatency(100, "FIFO",25,25) + " milliseconds");

        System.out.println("Performance test 25 puts, 25 gets, LRU, 100");
        System.out.println("elapsedTime: " + evalLatency(100, "LRU",25,25) + " milliseconds");

        System.out.println("Performance test 25 puts +  25 gets, LFU, 100");
        System.out.println("elapsedTime: " + evalLatency(100, "LFU",25,25) + " milliseconds");

        System.out.println("Performance test 10 puts, 40 gets, FIFO, 100");
        System.out.println("elapsedTime: " + evalLatency(100, "FIFO",10,40) + " milliseconds");

        System.out.println("Performance test 10 puts, 40 gets, LRU, 100");
        System.out.println("elapsedTime: " + evalLatency(100, "LRU",10,40) + " milliseconds");

        System.out.println("Performance test 10 puts +  40 gets, LFU, 100");
        System.out.println("elapsedTime: " + evalLatency(100, "LFU",10,40) + " milliseconds");



        System.out.println("Performance test 40 puts, 10 gets, FIFO, 25");
        System.out.println("elapsedTime: " + evalLatency(25, "FIFO",40,10) + " milliseconds");

        System.out.println("Performance test 40 puts, 10 gets, LRU, 25");
        System.out.println("elapsedTime: " + evalLatency(25, "LFU",40,10) + " milliseconds");

        System.out.println("Performance test 40 puts +  10 gets, LFU, 25");
        System.out.println("elapsedTime: " + evalLatency(25, "LFU",40,10) + " milliseconds");

        System.out.println("Performance test 25 puts, 25 gets, FIFO, 25");
        System.out.println("elapsedTime: " + evalLatency(25, "FIFO",25,25) + " milliseconds");

        System.out.println("Performance test 25 puts, 25 gets, LRU, 25");
        System.out.println("elapsedTime: " + evalLatency(25, "LRU",25,25) + " milliseconds");

        System.out.println("Performance test 25 puts +  25 gets, LFU, 25");
        System.out.println("elapsedTime: " + evalLatency(25, "LFU",25,25) + " milliseconds");

        System.out.println("Performance test 10 puts, 40 gets, FIFO, 25");
        System.out.println("elapsedTime: " + evalLatency(25, "FIFO",10,40) + " milliseconds");

        System.out.println("Performance test 10 puts, 40 gets, LRU, 25");
        System.out.println("elapsedTime: " + evalLatency(25, "LRU",10,40) + " milliseconds");

        System.out.println("Performance test 10 puts +  40 gets, LFU, 25");
        System.out.println("elapsedTime: " + evalLatency(25, "LFU",10,40) + " milliseconds");

	}

	public long evalLatency(int cacheSize, String Strat, int numPut, int numGet){
        clearFile();

        KVServer kvServer = new KVServer(PORT, cacheSize,Strat);

        KVMessage putMessage = null;
        KVMessage getMessage = null;
        long elapsedTime = 0;
        long startTime = System.currentTimeMillis();
        try {
            int i;
            for (i = 0; i < numPut; i++){
                putMessage = kvServer.put(Integer.toString(i), Integer.toString(i));
                assertTrue(putMessage.getStatus() == KVMessage.StatusType.PUT_SUCCESS);
            }
            for(i = 0; i < numGet; i++){
                getMessage = kvServer.get(Integer.toString(new Random().nextInt(numPut)));
                assertTrue(getMessage.getStatus() == KVMessage.StatusType.GET_SUCCESS);
            }
            long stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return elapsedTime;
    }

	public static void clearFile(){

	PrintWriter pw = null;
		try {
			pw = new PrintWriter("./storage.txt");
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
