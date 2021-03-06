package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import common.messages.KVMessage;
import org.apache.log4j.Logger;

import client.ClientSocketListener.SocketStatus;


public class Client extends Thread{

	private Set<ClientSocketListener> listeners;
	private boolean running;
	private KVStore kvStore;
	private static Logger logger = Logger.getRootLogger();

	public Client(String address, int port) throws UnknownHostException, IOException {
		
		kvStore = new KVStore(address, port);
		try{
			kvStore.connect();
			setRunning(kvStore.getConnected());
		}catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("Connection established");
	}

	/**
	 * Initializes and starts the client connection. 
	 * Loops until the connection is closed or aborted by the client.
	 */
	public void run() {
		logger.info("Client thread starts running");


		try {

/*
			while(isRunning()) {
				try {
					TextMessage latestMsg = kvStore.receiveMessage();
					for(ClientSocketListener listener : listeners) {
						listener.handleNewMessage(latestMsg);
					}
				} catch (IOException ioe) {
					if(isRunning()) {
						//logger.error("Connection lost!");
						try {
							//tearDownConnection();
							for(ClientSocketListener listener : listeners) {
								listener.handleStatus(
										SocketStatus.CONNECTION_LOST);
							}
						} catch (IOException e) {
							//logger.error("Unable to close connection!");
						}
					}
				}				
			}*/
		}  catch (Exception e) {
			e.printStackTrace();
		} finally {
			/*if(isRunning()) try {
				disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
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
	 * @throws IOException some I/O error regarding the output stream
	 */
	public void putMessage(String key, String value) throws Exception {
		KVMessage kvm = kvStore.put(key,value);

	}

	public void getMessage(String key) throws Exception {
		KVMessage kvm = kvStore.get(key);
	}

	public void disconnect() throws IOException {
		kvStore.disconnect();
		setRunning(false);
	}
}
