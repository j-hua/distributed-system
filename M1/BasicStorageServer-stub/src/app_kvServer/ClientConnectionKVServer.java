package app_kvServer;

import common.messages.KVMessage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a connection end point for a particular client that is 
 * connected to the server. This class is responsible for message reception 
 * and sending. 
 * The class also implements the echo functionality. Thus whenever a message 
 * is received it is going to be echoed back to the client.
 */
public class ClientConnectionKVServer implements Runnable {

	private static Logger logger = Logger.getRootLogger();
	
	private boolean isOpen;
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 128 * BUFFER_SIZE;
	
	private Socket clientSocket;
	private InputStream input;
	private OutputStream output;
	public KVServerListener kvServerListener;
	public static final String FORMAT_ERROR = "Error: The format you entered is incorrect. Type 'help' to see possible options";

	/**
	 * Constructs a new CientConnection object for a given TCP socket.
	 * @param clientSocket the Socket object for the client connection.
	 */
	public ClientConnectionKVServer(Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.isOpen = true;
	}

	/**
	 * set the listener to tell te KVServer the parsed message
	 * @param kvServerListener
	 */
	public void setKVServerListener(KVServerListener kvServerListener){
		this.kvServerListener = kvServerListener;
	}

	/**
	 * Initializes and starts the client connection. 
	 * Loops until the connection is closed or aborted by the client.
	 */
	public void run() {
		try {
			output = clientSocket.getOutputStream();
			input = clientSocket.getInputStream();
		
			sendMessage(new TextMessageKVServer(
					"Connection to MSRG Echo server established: " 
					+ clientSocket.getLocalAddress() + " / "
					+ clientSocket.getLocalPort()));
			
			while(isOpen) {
				try {
					TextMessageKVServer latestMsg = receiveMessage();
					sendMessage(latestMsg);
					
				/* connection either terminated by the client or lost due to 
				 * network problems*/	
				} catch (IOException ioe) {
					logger.error("Error! Connection lost!");
					isOpen = false;
				}				
			}
			
		} catch (IOException ioe) {
			logger.error("Error! Connection could not be established!", ioe);
			
		} finally {
			
			try {
				if (clientSocket != null) {
					input.close();
					output.close();
					clientSocket.close();
				}
			} catch (IOException ioe) {
				logger.error("Error! Unable to tear down connection!", ioe);
			}
		}
	}
	
	/**
	 * Method sends a TextMessage using this socket.
	 * @param msg the message that is to be sent.
	 * @throws IOException some I/O error regarding the output stream 
	 */
	public void sendMessage(TextMessageKVServer msg) throws IOException {
		byte[] msgBytes = msg.getMsgBytes();
		output.write(msgBytes, 0, msgBytes.length);
		output.flush();
		logger.info("SEND \t<" 
				+ clientSocket.getInetAddress().getHostAddress() + ":" 
				+ clientSocket.getPort() + ">: '" 
				+ msg.getMsg() +"'");
    }
	
	
	private TextMessageKVServer receiveMessage() throws IOException {
		
		int index = 0;
		byte[] msgBytes = null, tmp = null;
		byte[] bufferBytes = new byte[BUFFER_SIZE];
		
		/* read first char from stream */
		byte read = (byte) input.read();	
		boolean reading = true;
		
//		logger.info("First Char: " + read);
//		Check if stream is closed (read returns -1)
//		if (read == -1){
//			TextMessage msg = new TextMessage("");
//			return msg;
//		}

		while(/*read != 13  && */ read != 10 && read !=-1 && reading) {/* CR, LF, error */
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
			
			/* only read valid characters, i.e. letters and constants */
			bufferBytes[index] = read;
			index++;
			
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
		TextMessageKVServer msg = new TextMessageKVServer(msgBytes);

		TextMessageKVServer response = new TextMessageKVServer(processMessage(msg.getMsg()).getBytes());

		return response;

    }

    public String processMessage(String message){
		String[] messageArray = message.split(" ");
		String status = "";
		/**
		 * length <=12 means:
		 * the server will only allow 10 space delimited value strings.
		 * In other words 10 columns of data per row or per object saved.
		 */
		if (messageArray.length>=2){
			try {
				//determining the action type based on the length of the message
				//this is under the assumption that the key will not have spaces
				String action = messageArray[0].trim();
				logger.info("ACTION: " + action);
				String key= messageArray[1];

				if (messageArray.length>2){

					StringBuilder sb = new StringBuilder("");
					for (int i = 2; i< messageArray.length; i++) {

						sb.append(messageArray[i]).append(" ");
					}

					KVMessage kvMessage =kvServerListener.put(key, sb.toString().trim() );
					status = String.valueOf(kvMessage.getStatus());


				}else if (messageArray.length==2){
					//must be a get
					if (action.equals(KVServer.GET)){
						KVMessage kvMessage = kvServerListener.get(key);
						status = String.valueOf(kvMessage.getStatus())+" "+kvMessage.getValue();

					}else{
						//
						KVMessage kvMessage = kvServerListener.put(key, null);
						status = String.valueOf(kvMessage.getStatus());

					}
				}else{
					logger.error("message array length not greater than or equal to 2 (inner if).");
					return FORMAT_ERROR;
				}


				/* 0 and 1 hold the action and the key*/
				logger.info("RECEIVE \t<"
						+ clientSocket.getInetAddress().getHostAddress() + ":"
						+ clientSocket.getPort() + ">: '"
						+ message.trim() + "'");
				//must be success here
				return status;
			} catch (Exception e) {
				// TODO: 1/27/17 (Talk to ALI) make sure this returns all of the errors from PUT_ERROR, GET_ERROR etc.
				logger.error(e.getMessage());
				return e.getMessage();
			}

		}else{
			logger.error("message array length not greater than or equal to 2 OR greater than 12 (outer if)");
			return  FORMAT_ERROR;
		}}

}
