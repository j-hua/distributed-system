package testing;

import org.junit.Test;

import junit.framework.TestCase;
//import KVServer.*;

public class AdditionalTest extends TestCase {
	
	// TODO add your test cases, at least 3
	
	@Test
	public void testStub() {
		assertTrue(true);
	}

/*
	@Test
	public void getCheck(){
		KVServer kvServer = new KVServer(PORT,CACHE_SIZE,CACHE_STRATEGY);
		Exception putEx=null;
		Exception getEx = null;
		KVMessage kvMessage = null;
		KVMessage getMessage = null;

		try {
			kvMessage = kvServer.put("one","52");
		} catch (Exception e) {
			putEx = e;
		}

		assertTrue(putEx == null && kvMessage.getStatus() == KVMessage.StatusType.PUT_SUCCESS);
		try {
			getMessage = kvServer.get("one");
		} catch (Exception e) {
			getEx = e;
		}

		assertTrue(getEx == null && getMessage.getStatus() == KVMessage.StatusType.GET_SUCCESS);

	}*/
}

