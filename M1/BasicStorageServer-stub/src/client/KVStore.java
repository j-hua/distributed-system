package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import app_kvServer.KVMessageStorage;
import common.messages.KVMessage;

import javax.xml.soap.Text;
import org.apache.log4j.Logger;

public class KVStore implements KVCommInterface {

	private Socket clientSocket;
	//private OutputStream output;
	//private InputStream input;
	private String kvAddress;
	private int kvPort;
	private OutputStream output;
	private InputStream input;
	private static Logger logger = Logger.getRootLogger();
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 1024*BUFFER_SIZE;
	private boolean connected;
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
		connected = false;
	}
	
	@Override
	public void connect() throws Exception {
		// TODO Auto-generated method stub
		try{
			this.clientSocket = new Socket();
			this.clientSocket.connect(new InetSocketAddress(kvAddress,kvPort),100);
			input = clientSocket.getInputStream();
			output = clientSocket.getOutputStream();
			connected = true;
			TextMessage res = receiveMessage();
		}catch (SocketTimeoutException e){
			System.out.println("connection failed, please try again");
			connected = false;
		}

	}


	public boolean getConnected() {
		return connected;
	}
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		logger.info("disconnect ...");
		if (clientSocket != null) {

			try {
				clientSocket.close();
				input.close();
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			clientSocket = null;
			logger.info("connection closed!");
		}
		
	}

	@Override
	public KVMessage put(String key, String value) throws Exception {
		// TODO Auto-generated method stub
		TextMessage msg = null;
		KVMessageStorage kvms = null;
		StringBuilder sb = new StringBuilder();

		sb.append("put ");
		if(key.getBytes().length <= 20){
			sb.append(key);
		}else{
			logger.info("key exceeds max length 20 Bytes");
			System.out.println("key exceeds max length 20 bytes");
			kvms = new KVMessageStorage(null, null, StatusTypeLookup("PUT_ERROR"));
			return kvms;
		}
		
		if(value.equals("null")){
			logger.info("trying to delete key " + key);
			System.out.println("Deleting key " + key.trim());
			msg = new TextMessage(sb.toString());
			sendMessage(new TextMessage(sb.toString()));
			TextMessage res = receiveMessage();
			String[] tokens = res.getMsg().split("\\s+",2);
			System.out.println("KEY: " + key.trim());
			System.out.println("VALUE: " + value.trim());
			System.out.println("STATUS: " + tokens[0].trim());
			kvms = new KVMessageStorage(tokens[1], null, StatusTypeLookup(tokens[0]));
		}else{
			if(value.getBytes().length <= 120000){
				sb.append(" ");
				sb.append(value);
			}else{
				logger.info("value exceeds max length 120kBytes");
				System.out.println("value exceeds max length 120 kbytes");
				kvms = new KVMessageStorage(null,null, StatusTypeLookup("PUT_ERROR"));
				return kvms;
			}
			msg = new TextMessage(sb.toString());
			sendMessage(new TextMessage(sb.toString()));
			TextMessage res = receiveMessage();
			String[] tokens = res.getMsg().split("\\s+",3);
			System.out.println("KEY: " + key.trim());
			System.out.println("VALUE: " + value.trim());
			System.out.println("STATUS: " + tokens[0].trim());
			kvms = new KVMessageStorage(tokens[1], tokens[2], StatusTypeLookup(tokens[0]));
		}


		return kvms;
	}

	/**
	 * Method sends a TextMessage using this socket.
	 * @param msg the message that is to be sent.
	 * @throws IOException some I/O error regarding the output stream
	 */
	public void sendMessage(TextMessage msg) throws IOException {
		byte[] msgBytes = msg.getMsgBytes();
		output.write(msgBytes, 0, msgBytes.length);
		output.flush();
		logger.info("Send message:\t '" + msg.getMsg() + "'");
	}


	@Override
	public KVMessage get(String key) throws Exception {

		KVMessageStorage kvms = null;
		StringBuilder sb = new StringBuilder();
		sb.append("get ");
		if(key.getBytes().length <= 20){
			sb.append(key);
		}else{
			logger.info("key exceeds max length 20 bytes");
			System.out.println("key exceeds max length 20 bytes");
			kvms = new KVMessageStorage(null, null, StatusTypeLookup("GET_ERROR"));
			return kvms;
		}
		
		sendMessage(new TextMessage(sb.toString()));

		TextMessage res = receiveMessage();

		String[] tokens = res.getMsg().split("\\s+",3);
		kvms = new KVMessageStorage(tokens[1],tokens[2], StatusTypeLookup(tokens[0]));
		System.out.println("KEY: " + tokens[1]);
		System.out.println("VALUE: " + tokens[2]);
		System.out.println("STATUS: " +  tokens[0]);
		return kvms;
	}

	public TextMessage receiveMessage() throws IOException {


		int index = 0;
		byte[] msgBytes = null, tmp = null;
		byte[] bufferBytes = new byte[BUFFER_SIZE];

		/* read first char from stream */
		byte read = (byte) input.read();
		boolean reading = true;

		while(read != 13 && reading) {/* carriage return */
			/* if buffer filled, copy to msg array */
			if(index == BUFFER_SIZE) {
				if(msgBytes == null){
					tmp = new byte[BUFFER_SIZE];
					System.arraycopy(bufferBytes, 0, tmp, 0, BUFFER_SIZE);
				} else {
					tmp = new byte[msgBytes.length + BUFFER_SIZE];
					System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
					System.arraycopy(bufferBytes, 0, tmp, msgBytes.length,
							BUFFER_SIZE);
				}

				msgBytes = tmp;
				bufferBytes = new byte[BUFFER_SIZE];
				index = 0;
			}

			/* only read valid characters, i.e. letters and numbers */
			if((read > 31 && read < 127)) {
				bufferBytes[index] = read;
				index++;
			}

			/* stop reading is DROP_SIZE is reached */
			if(msgBytes != null && msgBytes.length + index >= DROP_SIZE) {
				reading = false;
			}

			/* read next char from stream */
			read = (byte) input.read();
		}

		if(msgBytes == null){
			tmp = new byte[index];
			System.arraycopy(bufferBytes, 0, tmp, 0, index);
		} else {
			tmp = new byte[msgBytes.length + index];
			System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
			System.arraycopy(bufferBytes, 0, tmp, msgBytes.length, index);
		}

		msgBytes = tmp;

		/* build final String */
		TextMessage msg = new TextMessage(msgBytes);
		logger.info("Receive message:\t '" + msg.getMsg() + "'");

		return msg;
	}

	public KVMessage.StatusType StatusTypeLookup(String status){
		KVMessage.StatusType st = null;
		//need a default error; add one more error type: GENERAL_ERROR;
		switch (status){
			case "GET":
				st = KVMessage.StatusType.GET;
				break;
			case "GET_ERROR":
				st = KVMessage.StatusType.GET_ERROR;
				break;
			case "GET_SUCCESS":
				st = KVMessage.StatusType.GET_SUCCESS;
				break;
			case "PUT":
				st = KVMessage.StatusType.PUT;
				break;
			case "PUT_SUCCESS":
				st = KVMessage.StatusType.PUT_SUCCESS;
				break;
			case "PUT_UPDATE":
				st = KVMessage.StatusType.PUT_UPDATE;
				break;
			case "PUT_ERROR":
				st = KVMessage.StatusType.PUT_ERROR;
				break;
			case "DELETE_SUCCESS":
				st = KVMessage.StatusType.DELETE_SUCCESS;
				break;
			case "DELETE_ERROR":
				st = KVMessage.StatusType.DELETE_ERROR;
				break;
		}
		return st;
	}

	public Socket getSocket(){	return clientSocket;	}
	
}
