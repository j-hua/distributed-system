package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import app_kvServer.KVMessageStorage;
import common.messages.KVMessage;

import javax.xml.soap.Text;

public class KVStore implements KVCommInterface {

	private Socket clientSocket;
	//private OutputStream output;
	//private InputStream input;
	private String kvAddress;
	private int kvPort;
	private OutputStream output;
	private InputStream input;

	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 1024*BUFFER_SIZE;
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
		System.out.println("try connecting");
		this.clientSocket = new Socket(kvAddress,kvPort);
		System.out.println("connected");
		input = clientSocket.getInputStream();
		output = clientSocket.getOutputStream();

		TextMessage res = receiveMessage();
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
	//	logger.info("tearing down the connection ...");
		if (clientSocket != null) {

			try {
				clientSocket.close();
				input.close();
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			clientSocket = null;
		//	logger.info("connection closed!");
		}
		
	}

	@Override
	public KVMessage put(String key, String value) throws Exception {
		// TODO Auto-generated method stub
		TextMessage msg = null;
		KVMessageStorage kvms = null;
		StringBuilder sb = new StringBuilder();

		sb.append("put ");
		sb.append(key);
		if(value.equals("null")){
			msg = new TextMessage(sb.toString());
			sendMessage(new TextMessage(sb.toString()));
			//	logger.info("Send message:\t '" + msg.getMsg() + "'");
			TextMessage res = receiveMessage();
			String[] tokens = res.getMsg().split("\\s+",2);
			kvms = new KVMessageStorage(tokens[1], null, StatusTypeLookup(tokens[0]));
		}else {
			sb.append(" ");
			sb.append(value);
			msg = new TextMessage(sb.toString());
			sendMessage(new TextMessage(sb.toString()));
			//	logger.info("Send message:\t '" + msg.getMsg() + "'");
			TextMessage res = receiveMessage();
			String[] tokens = res.getMsg().split("\\s+",3);
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
	//	logger.info("Send message:\t '" + msg.getMsg() + "'");
	}


	@Override
	public KVMessage get(String key) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("get ");
		sb.append(key);

		sendMessage(new TextMessage(sb.toString()));

		TextMessage res = receiveMessage();

		String[] tokens = res.getMsg().split("\\s+",3);

		//if not length != 2, general error?
		KVMessageStorage kvms = new KVMessageStorage(tokens[1],tokens[2], StatusTypeLookup(tokens[0]));

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
		//logger.info("Receive message:\t '" + msg.getMsg() + "'");
		System.out.println("Receive message:\t '" + msg.getMsg() + "'");
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
