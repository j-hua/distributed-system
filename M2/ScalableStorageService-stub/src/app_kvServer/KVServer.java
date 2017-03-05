package app_kvServer;


<<<<<<< HEAD
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

public class KVServer extends Thread {

    public static final String FIFO = "fifo";
    public static final String LRU = "lru";
    public static final String LFU = "lfu";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String GET = "get";
    public static final String PUT = "put";

    public static final String INIT = "initkvserver";
    public static final String START = "start";

   	public static final int SERVER_STOPPED = 0;
    public static final int SERVER_READY = 1;

    private static Logger logger = Logger.getRootLogger();

    private int port;
    private ServerSocket serverSocket;
    private boolean running;
    public int state;
    public String ecsAddr;
    public int ecsPort;

	private storageServer mStorage = null;
	private List<String> metadata = new ArrayList<String>();

	
public class KVServer  {
	 /* Start KV Server at given port
	 * @param port given port for storage server to operate
	 * @param cacheSize specifies how many key-value pairs the server is allowed 
	 *           to keep in-memory
	 * @param strategy specifies the cache replacement strategy in case the cache 
	 *           is full and there is a GET- or PUT-request on a key that is 
	 *           currently not contained in the cache. Options are "FIFO", "LRU", 
	 *           and "LFU".
	 */
	public KVServer(int port, String ecsAddr, int ecsPort) {
        this.port = port;
        this.ecsAddr = ecsAddr;
        this.ecsPort = ecsPort;
    	this.state = SERVER_STOPPED;
	}
    /**
     * Initializes and starts the server.
     * Loops until the the server should be closed.
     */
    @Override
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
        logger.info("Server stopped.");
    }

    public boolean isRunning() {
        return this.running;
    }

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

		// TODO: 1/23/17 Save the tuple to DB here
        return mStorage.put(key.trim(),value);
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

        return mStorage.get(key.trim());
	}

	public KVAdminMessage initKVServer(String[] metadata, int cacheSize, String replacementStrategy){
		//parse metadata and store in list
		for (String server : metadata){
			this.metadata.add(server);
		}

		mStorage = new storageServer(replacementStrategy.toUpperCase(), cacheSize);

		return new KVAdminMessageStorage(KVAdminMessage.StatusType.INIT_SUCCESS);
	}

	private boolean initializeServer() {
		logger.info("Initialize server ...");
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
			if(args.length != 3) {
				System.out.println("Error! Invalid number of arguments!");
				System.out.println("Usage: Server <port> <cache_size> <cache_strategy> !");
			} else {

				int port = Integer.parseInt(args[0]);
				String ecsAddr = args[1];
				int ecsPort = Integer.parseInt(args[2]);

				new KVServer(port, ecsAddr, ecsPort).start();
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
