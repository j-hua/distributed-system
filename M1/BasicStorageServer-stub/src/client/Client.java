package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

//import client.ClientSocketListener.SocketStatus;

public class Client extends Thread{

	//private Set<ClientSocketListener> listeners;
	private boolean running;
	private KVStore kvStore;


	
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 1024*BUFFER_SIZE;

	public Client(String address, int port) throws UnknownHostException, IOException {
		
		kvStore = new KVStore(address, port);
		System.out.println("Client created");
		setRunning(false);
	}

	/**
	 * Initializes and starts the client connection. 
	 * Loops until the connection is closed or aborted by the client.
	 */
	public void run() {
		System.out.println("Client thread starts running");
		setRunning(true);

		try {
			kvStore.connect();
		//	output = kvStore.getSocket().getOutputStream();
		//	input = kvStore.getSocket().getInputStream();


			
/*
			while(isRunning()) {
				try {
					TextMessage latestMsg = receiveMessage();
					for(ClientSocketListener listener : listeners) {
						listener.handleNewMessage(latestMsg);
					}
				} catch (IOException ioe) {
					if(isRunning()) {
						logger.error("Connection lost!");
						try {
							tearDownConnection();
							for(ClientSocketListener listener : listeners) {
								listener.handleStatus(
										SocketStatus.CONNECTION_LOST);
							}
						} catch (IOException e) {
							logger.error("Unable to close connection!");
						}
					}
				}				
			}*/
		} catch (IOException ioe) {
		//	logger.error("Connection could not be established!");
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//if(isRunning()) {
			//	closeConnection();
			//}
		}
	}

	public boolean isRunning() {
		return running;
	}
	
	public void setRunning(boolean run) {
		running = run;
	}
	
	
	/**
	 * Method sends a TextMessage using this socket.
	 * @param msg the message that is to be sent.
	 * @throws IOException some I/O error regarding the output stream
	 */
	public void putMessage(String key, String value) throws Exception {
		kvStore.put(key,value);
    }

}
