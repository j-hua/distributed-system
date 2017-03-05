package app_kvServer;

import common.messages.KVMessage;
import common.messages.KVAdminMessage;
import logger.LogSetup;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class KVServer implements Runnable {

    public static final String FIFO = "fifo";
    public static final String LRU = "lru";
    public static final String LFU = "lfu";
    public static final String DELETE = "delete";
    public static final String GET = "get";
    public static final String PUT = "put";

    public static final String INIT = "initkvserver";
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String SHUTDOWN = "shutdown";
    public static final String LOCKWRITE = "lockwrite";
    public static final String UNLOCKWRITE = "unlockwrite";
    public static final String MOVEDATA = "movedata";
    public static final String UPDATE = "update";

   	public static final int SERVER_STOPPED = 0;
    public static final int SERVER_READY = 1;

    public static final int BAN_WRITE = 0;
    public static final int ALLOW_WRITE = 1;

    //lock for requesting client's array
    private final Object lock = new Object();
    private final Object writeLock = new Object();
    private List<Integer> clientsInRequest = new ArrayList<Integer>(); 
    private List<Integer> writeRequests = new ArrayList<Integer>();

    private static Logger logger = Logger.getRootLogger();

    private int port;
    private ServerSocket serverSocket;
    private boolean running;
    public int state;
    public int banWrite;

	private storageServer mStorage = null;
	private List<String> metadata = new ArrayList<String>();

	 /* Start KV Server at given port
	 * @param port given port for storage server to operate
	 * @param cacheSize specifies how many key-value pairs the server is allowed 
	 *           to keep in-memory
	 * @param strategy specifies the cache replacement strategy in case the cache 
	 *           is full and there is a GET- or PUT-request on a key that is 
	 *           currently not contained in the cache. Options are "FIFO", "LRU", 
	 *           and "LFU".
	 */
	public KVServer(int port) {
        this.port = port;
    	state = SERVER_STOPPED;
    	banWrite = ALLOW_WRITE;
	}
    /**
     * Initializes and starts the server.
     * Loops until the the server should be closed.
     */
    public void run() {
        running = initializeServer();

        if(serverSocket != null) {
            while(isRunning()){
                try {
                    Socket client = serverSocket.accept();
                   ClientConnectionKVServer connection = new ClientConnectionKVServer(client);
                   connection.setKVServerListener(this);
                    new Thread(connection).start();

                    logger.info("Connected to "
                            + client.getInetAddress().getHostName()
                            +  " on port " + client.getPort());
                } catch (IOException e) {
                    logger.error("Error! " +
                            "Unable to establish connection. \n", e);
                }
            }
        }
        logger.info("Server Shutdown");
    }

    public boolean isRunning() {
        return this.running;
    }

    //****************************CLIENT REQUEST METHODS****************************//

    /**
     * Inserts a key-value pair into the KVServer.
     * @param key the key that identifies the given value.
     * @param value the value that is indexed by the given key.
     * @return a message that confirms the insertion of the tuple or an error.
     * @throws Exception if put command cannot be executed
     *     (e.g. not connected to any KV server).
     */

	public KVMessage put(String key, String value) throws Exception {
		logger.info("Putting  "+ " "+ "key: " + key + " "+"value: " + value);
		
		//add this request to the number of clients requesting list
		synchronized(lock){
			clientsInRequest.add(0);
		}
		synchronized(writeLock){
			writeRequests.add(0);
		}
		
		KVMessage kvm = mStorage.put(key.trim(),value);

		//once done remove client's request from request list
		synchronized(lock){
			clientsInRequest.remove(0);
		}
		synchronized(writeLock){
			writeRequests.remove(0);
		}

        return kvm;
	}

    /**
     * Retrieves the value for a given key from the KVServer.
     * @param key the key that identifies the value.
     * @return the value, which is indexed by the given key.
     * @throws Exception if put command cannot be executed
     *     (e.g. not connected to any KV server).
     */

	public KVMessage get(String key) throws Exception {
		logger.info("Getting "+"key: " +key);

		//add this request to the number of clients requesting list
		synchronized(lock){
			clientsInRequest.add(0);
		}

		KVMessage kvm = mStorage.get(key.trim());

		//once done remove client's request from request list
		synchronized(lock){
			clientsInRequest.remove(0);
		}

        return kvm;
	}

	//****************************ADMIN ECS REQUEST METHODS****************************//

	public KVAdminMessage initKVServer(String[] metadata, int cacheSize, String replacementStrategy){
		//parse metadata and store in 
		logger.info("Initializing Server: No Client Requests Allowed");
		for (String server : metadata){
			this.metadata.add(server);
		}

		mStorage = new storageServer(replacementStrategy.toUpperCase(), cacheSize, serverSocket.getLocalPort());

		return new KVAdminMessageStorage(KVAdminMessage.StatusType.INITIALIZATION_SUCCESS);
	}

	public KVAdminMessage start(){
		logger.info("Starting Server: Client Requests Allowed");
		state = SERVER_READY;
		return new KVAdminMessageStorage(KVAdminMessage.StatusType.SERVER_STARTED);
	}

	public KVAdminMessage stop(){
		logger.info("Stopping Server: No Client Requests Allowed");
		state = SERVER_STOPPED;

		boolean stopPossible = false;

		while(!stopPossible){
			synchronized(lock){
				if(clientsInRequest.size() == 0){
					stopPossible = true;
				}
			}
		}

		return new KVAdminMessageStorage(KVAdminMessage.StatusType.SERVER_STOPPED);
	}

	public void shutDown(){
		logger.info("Shutting Down Server: Will Process Current Requests Before Exiting");
		
		state = SERVER_STOPPED;

		boolean sdPossible = false;

		while(!sdPossible){
			synchronized(lock){
				if(clientsInRequest.size() == 0){
					sdPossible = true;
				}
			}
		}

		logger.info("All Requests Complete, Shutting Down Server");

		System.exit(1);
	}

	public KVAdminMessage lockWrite(){
		//cannot notify the ECS Server until every ongoing write request is complete
		//setting banWrite will prevent other clients from making a PUT Request
		logger.info("Banning Put Operations: Setting Lock Variable");
		banWrite = BAN_WRITE;
		boolean lockPossible = false;

		while(!lockPossible){
			synchronized(writeLock){
				if(writeRequests.size() == 0){
					lockPossible = true;
				}
			}
		}

		return new KVAdminMessageStorage(KVAdminMessage.StatusType.LOCK_WRITE_SUCCESS);
	}

	public KVAdminMessage unLockWrite(){
		logger.info("Allowing Put Operations by Clients");
		banWrite = ALLOW_WRITE;
		return new KVAdminMessageStorage(KVAdminMessage.StatusType.SERVER_STARTED);
	}

	public KVAdminMessage moveData(){
		return null;
	}

	//****************************SERVER METHODS****************************//

	private boolean initializeServer() {
		logger.info("Setting up server socket ...");
		try {
			serverSocket = new ServerSocket(port);
			logger.info("Server listening on port: "
					+ serverSocket.getLocalPort());
			return true;

		} catch (IOException e) {
			logger.error("Error! Cannot open server socket:");
			if(e instanceof BindException){
				logger.error("Port " + port + " is already bound!");
			}
			return false;
		}
	}

	/**
	 * Main entry point for the echo server application.
	 * @param args contains the port number at args[0].
	 */
	public static void main(String[] args) {
		try {
			new LogSetup("logs/server.log", Level.ALL);
			logger.error("LENGTH "+ args.length);
			if(args.length != 1) {
				System.out.println("Error! Invalid number of arguments!");
				System.out.println("Usage: Server <port> <cache_size> <cache_strategy> !");
			} else {

				int port = Integer.parseInt(args[0]);

				KVServer kvs = new KVServer(port);
				new Thread(kvs).start();
			}
		} catch (IOException e) {
			System.out.println("Error! Unable to initialize logger!");
			e.printStackTrace();
			System.exit(1);
		} catch (NumberFormatException nfe) {
			System.out.println("Error! Please check whether the <port and <cache_size> are numbers");
			System.out.println("Usage: Server <port> <cache_size> <cache_strategy> !");
			System.exit(1);
		}
	}

	public storageServer getStorageServer(){
		return mStorage;
	}
}
