package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import common.messages.KVMessage;

public class KVStore implements KVCommInterface {

	private Socket clientSocket;
	//private OutputStream output;
	//private InputStream input;
	private String kvAddress;
	private int kvPort;
	private OutputStream output;
	private InputStream input;
	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	public KVStore(String address, int port) {
		this.kvAddress = address;
		this.kvPort = port;
		this.clientSocket = null;
		input = null;
		output = null;
	}
	
	@Override
	public void connect() throws Exception {
		// TODO Auto-generated method stub
		this.clientSocket = new Socket(kvAddress,kvPort);
		//output = kvStore.getSocket().getOutputStream();
		//input = kvStore.getSocket().getInputStream();
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public KVMessage put(String key, String value) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append("put ");
		sb.append(key);
		sb.append(" ");
		sb.append(value);

		TextMessage msg = new TextMessage(sb.toString());
		byte[] msgBytes = msg.getMsgBytes();
		output = clientSocket.getOutputStream();
		output.write(msgBytes, 0, msgBytes.length);
		output.flush();
		//	logger.info("Send message:\t '" + msg.getMsg() + "'");
		return null;
	}

	@Override
	public KVMessage get(String key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	public Socket getSocket(){	return clientSocket;	}
	
}
