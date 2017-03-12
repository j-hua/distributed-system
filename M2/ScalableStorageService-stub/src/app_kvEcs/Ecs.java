package app_kvEcs;

import client.TextMessage;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by warefhaque on 3/3/17.
 */
public class Ecs {

    List<String> mIpAndPorts;
    private static final String PROMPT = "ECS> ";
    private static final int ecsPort = 40000;
    
    private BufferedReader stdin;
    private boolean stop = false;
    private static final int BUFFER_SIZE = 1024;
    private static final int DROP_SIZE = 1024*BUFFER_SIZE;
    private static Logger logger = Logger.getRootLogger();
    List<String> participatingServers;

    public void run() {
        while(!stop) {
            stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(PROMPT);
            try {
                String cmdLine = stdin.readLine();
                this.handleCommand(cmdLine);
            } catch (IOException e) {
                stop = true;
                System.out.println("CLI does not respond - Application terminated ");
            }
        }
    }

    private void handleCommand(String cmdline){

        String[] tokens = cmdline.split("\\s+");

        if(tokens[0].equals("quit")) {
            stop = true;
            //	disconnect();
            System.out.println(PROMPT + "Application exit!");

        } else if (tokens[0].equals("connect")){
            if(tokens.length == 2) {
                int numberOfNodes = Integer.valueOf(tokens[1]);
                initService(numberOfNodes);
            }else{
                printError("Invalid number of parameters!");
            }
        } else  if (tokens[0].equals("add")) {
            if(tokens.length == 3) {
                int ipAddress = Integer.valueOf(tokens[1]);
                int port = Integer.valueOf(tokens[2]);
            } else {
                printError("Invalid number of parameters!");
            }

        }else if (tokens[0].equals("start")){
            if (tokens.length == 1){
                start();
            }
        }
    }

    public void start(){
        System.out.println("Participating servers are: "+participatingServers.size());

        for (int i =0; i<participatingServers.size();i++){
            String[] elements= mIpAndPorts.get(i).split(" ");
            String ip = elements[0];
            String port =elements[1];
            try {
                connect(ip,Integer.valueOf(port),"ecs start");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void initService(int numberOfNodes){
        readFile();
        ConsistentHashing consistentHashing = new ConsistentHashing(1,mIpAndPorts);
        StringBuilder sb = new StringBuilder("");
        participatingServers = new ArrayList<>();
        if (numberOfNodes<mIpAndPorts.size()){
            for (int i=0; i<numberOfNodes; i++) {

                participatingServers.add(mIpAndPorts.get(i));
                String[] elements= mIpAndPorts.get(i).split(" ");
                String ip = elements[0];
                String port =elements[1];
                System.out.println("ip passed in: "+ip+ " port: "+port);

                ConsistentHashing.HashedServer hashedServer= consistentHashing.get(mIpAndPorts.get(i));
                String start = Integer.toString(hashedServer.mHashedKeys[0]).trim();
                String end = Integer.toString(hashedServer.mHashedKeys[1]).trim();

				try {
					sb.append(start.trim()).append(",").append(end.trim()).append(",").append(InetAddress.getByName(ip).getHostName().trim()).append(",").append(port.trim()).append(" ");
					ssh(ip,port,"ecs initkvserver 0 fifo " + sb);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
            }
        }else{
            System.out.println("Number of nodes entered is larger than ones available");
        }
    }



    public void connect(String kvAddress, int kvPort, String metadata) throws Exception {
        boolean connected = false;
        
        while(!connected){
	    	try{
	            OutputStream output;
	            InputStream input;
	            //System.out.println("KVADDRESS: "+kvAddress+" PORT: "+kvPort);
	            Socket clientSocket = new Socket();
	            clientSocket.connect(new InetSocketAddress(kvAddress, kvPort));
	            
	            System.out.println("CONNECTED TO SERVER");
	            
	            connected = true;
	            
	            output = clientSocket.getOutputStream();
	            input = clientSocket.getInputStream();
            	
	            //send message to server
	            sendMessage(new TextMessage(metadata), output);
	            
	            String result = receiveMessage(input).getMsg();
	            
	            System.out.println("The result is : " + result);
	
	            disconnect(clientSocket,input,output,kvAddress,Integer.toString(kvPort));
	
	        }catch (Exception e){
	            //System.out.println(e.getMessage());
	        }
        }
    }


    public void disconnect(Socket clientSocket, InputStream input, OutputStream output, String kvAddress, String kvPort) {
        
    	System.out.println("Disconnecting " + kvAddress + ":" + kvPort);
        if(clientSocket != null){
            try{
                clientSocket.close();
                input.close();
                output.close();
            }catch (IOException e){
                e.printStackTrace();
            }

            clientSocket = null;
            System.out.println("Connection to " + kvAddress + ":" + kvPort + " closed");
        }
    }

    /**
     * Method sends a TextMessage using this socket.
     * @param msg the message that is to be sent.
     * @throws IOException some I/O error regarding the output stream
     */
    public void sendMessage(TextMessage msg, OutputStream output) throws IOException {
        byte[] msgBytes = msg.getMsgBytes();
        output.write(msgBytes, 0, msgBytes.length);
        output.flush();
    }

    public TextMessage receiveMessage(InputStream input) throws IOException {


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
//        System.out.print("Receive message:\t '" + msg.getMsg() + "'");

        return msg;
    }


    public void ssh(String ip, String port, String metadata){

        Process proc = null;
        String script = "src/app_kvEcs/script.sh";
        String[] cmd = {"sh", script, ip, port};

        Runtime run = 	Runtime.getRuntime();
        try {
            proc = run.exec(cmd);
            
            //wait till the server notifies that it is about to listen for connections, then proceed
            ServerSocket ss = new ServerSocket(ecsPort);
            ss.accept();
            
            ss.close();
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }finally {
            if (proc!=null){
                System.out.println("killing process!");
                proc.destroy();

                try {
                    System.out.println("SSH: "+ip+" PORT: "+ port);
                    connect(ip, Integer.parseInt(port), metadata);

                } catch (Exception e) {
                	System.out.println(e.getMessage());
                }
            }
        }
    }
    private void printError(String error){
        System.out.println(PROMPT + "Error! " +  error);
    }

    void readFile() {

        BufferedReader br = null;
        FileReader fr = null;
        String FILENAME = "src/app_kvEcs/ecs.conf";
        mIpAndPorts = new ArrayList<>();

        try {

            fr = new FileReader(FILENAME);
            br = new BufferedReader(fr);

            String sCurrentLine;

            br = new BufferedReader(new FileReader(FILENAME));

            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
                String[] splitString = sCurrentLine.split(" ");
                String ipAddress = splitString[1].trim();
                String port = splitString[2].trim();
                mIpAndPorts.add(ipAddress+" "+port);
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }
    }


    //test the consistent hashing;
    public void tester(){
        String node1 =	"127.0.0.1 	50000";
        String node2 =	"127.0.0.1 	50001";
        String node3 =	"127.0.0.1 	50002";
        String node4 =	"127.0.0.1 	50003";
        String node5 =	"127.0.0.1 	50004";
        String node6 =	"127.0.0.1 	50005";
        String node7 =	"127.0.0.1 	50006";
        List<String> initiation = new ArrayList<>();
        initiation.add(node1);
        initiation.add(node2);
        initiation.add(node3);
        initiation.add(node4);
        initiation.add(node5);
        initiation.add(node6);
        initiation.add(node7);

        ConsistentHashing consistentHashing = new ConsistentHashing(1, initiation);

        //test

        ConsistentHashing.HashedServer hashedServer = consistentHashing.get("one");
        ConsistentHashing.HashedServer hashedServer1 = consistentHashing.get("two");
        ConsistentHashing.HashedServer hashedServer2 = consistentHashing.get("three");
        ConsistentHashing.HashedServer hashedServer3 = consistentHashing.get("four");
        ConsistentHashing.HashedServer hashedServer4 = consistentHashing.get("five");




        System.out.println(Arrays.toString(hashedServer.mHashedKeys) +" "+hashedServer.mIpAndPort);
        System.out.println(Arrays.toString(hashedServer1.mHashedKeys) +" "+hashedServer1.mIpAndPort);
        System.out.println(Arrays.toString(hashedServer2.mHashedKeys) +" "+hashedServer2.mIpAndPort);
        System.out.println(Arrays.toString(hashedServer3.mHashedKeys) +" "+hashedServer3.mIpAndPort);
        System.out.println(Arrays.toString(hashedServer4.mHashedKeys) +" "+hashedServer4.mIpAndPort+"\n");



        System.out.println(consistentHashing.circle.keySet().toString());

        System.out.println("----------------NOW GONNA TEST THE REMOVE------------------\n");

        consistentHashing.remove(node1);
        consistentHashing.remove(node5);
        consistentHashing.remove(node7);

        ConsistentHashing.HashedServer hs = consistentHashing.get("one");
        ConsistentHashing.HashedServer hs2 = consistentHashing.get("two");
        ConsistentHashing.HashedServer hs3 = consistentHashing.get("three");
        ConsistentHashing.HashedServer hs4 = consistentHashing.get("four");
        ConsistentHashing.HashedServer hs5 = consistentHashing.get("five");


        System.out.println(Arrays.toString(hs.mHashedKeys) +" "+hs.mIpAndPort);
        System.out.println(Arrays.toString(hs2.mHashedKeys) +" "+hs2.mIpAndPort);
        System.out.println(Arrays.toString(hs3.mHashedKeys) +" "+hs3.mIpAndPort);
        System.out.println(Arrays.toString(hs4.mHashedKeys) +" "+hs4.mIpAndPort);
        System.out.println(Arrays.toString(hs5.mHashedKeys) +" "+hs5.mIpAndPort+"\n");


        System.out.println(consistentHashing.circle.keySet().toString());
    }



}


