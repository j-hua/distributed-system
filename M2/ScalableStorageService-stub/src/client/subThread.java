package client;

import app_kvServer.ClientConnectionKVServer;
import app_kvServer.TextMessageKVServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 * Created by JHUA on 2017-04-12.
 */

public class subThread extends Thread {

    private String subAddress;
    private int subPort;
    private ServerSocket serverSocket;
    private InputStream input;
    private OutputStream output;
    private Logger logger;
    private static final int BUFFER_SIZE = 1024;
    private static final int DROP_SIZE = 128 * BUFFER_SIZE;


    /**
     * constructor
     */
    public subThread(int port){
        this.subPort = port;
        this.serverSocket = null;
        this.logger = Logger.getRootLogger();
    }

    public void run(){
        try {
            //not bound yet
            if(serverSocket == null){
                serverSocket = new ServerSocket(this.subPort);
                Socket subscribeSocket = serverSocket.accept();
                input = subscribeSocket.getInputStream();
                output = subscribeSocket.getOutputStream();

                TextMessage tm = receiveMessage();
                System.out.println("Key updated: " + tm.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() throws IOException {
        if(serverSocket != null){
            input.close();
            output.close();
            serverSocket.close();
        }
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


}