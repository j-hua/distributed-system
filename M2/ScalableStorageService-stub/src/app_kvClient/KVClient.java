package app_kvClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import client.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import logger.LogSetup;

public class KVClient implements ClientSocketListener {
    private static Logger logger = Logger.getRootLogger();
    private static final String PROMPT = "KVClient> ";
    private BufferedReader stdin;
    private boolean stop = false;
    private Client client;
    private String serverAddress;
    private int serverPort;

    public void run() {
        while(!stop) {
            stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(PROMPT);

            try {
                String cmdLine = stdin.readLine();
                this.handleCommand(cmdLine);
            } catch (IOException e) {
                stop = true;
                printError("CLI does not respond - Application terminated ");
            }
        }
    }

    private void handleCommand(String cmdLine) {
        String[] tokens = cmdLine.split("\\s+");

        if(tokens[0].equals("quit")) {
            stop = true;
            //	disconnect();
            System.out.println(PROMPT + "Application exit!");

        } else if (tokens[0].equals("connect")){
            if(tokens.length == 3) {
                try{
                    serverAddress = tokens[1];
                    serverPort = Integer.parseInt(tokens[2]);
                    System.out.println("connecting to " + serverAddress + " " + serverPort );
                    connect(serverAddress, serverPort);
                } catch(NumberFormatException nfe) {
                    printError("No valid address. Port must be a number!");
                    logger.info("Unable to parse argument <port>", nfe);
                } catch (UnknownHostException e) {
                    printError("Unknown Host!");
                    //		logger.info("Unknown Host!", e);
                } catch (IOException e) {
                    printError("Could not establish connection!");
                    //		logger.warn("Could not establish connection!", e);
                }
            } else {
                printError("Invalid number of parameters!");
            }

        } else  if (tokens[0].equals("put")) {
            if(tokens.length >= 2) {
                String[] kvPair = cmdLine.split("\\s+", 3);
                //	System.out.println(kvPair[0] + kvPair[1] + kvPair[2]);
                //kvPair = [put][key][value]
                if(kvPair.length == 3){
                    //		System.out.println("KEY: " + kvPair[1].trim());
                    //		System.out.println("VALUE: " + kvPair[2].trim());

                    if(client != null && client.isRunning()){

                        putMessage(kvPair[1].trim(),kvPair[2].trim());

                    } else {
                        printError("Not connected!");
                    }
                }else{
                    printError("Invalid number of parameters!");
                }
            } else {
                printError("No message was passed");
            }

        } else if(tokens[0].equals("disconnect")) {
            try {
                disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(tokens[0].equals("logLevel")) {
            if(tokens.length == 2) {
                String level = setLevel(tokens[1]);
                if(level.equals(LogSetup.UNKNOWN_LEVEL)) {
                    printError("No valid log level!");
                    printPossibleLogLevels();
                } else {
                    System.out.println(PROMPT +
                            "Log level changed to level " + level);
                }
            } else {
                printError("Invalid number of parameters!");
            }

        } else if(tokens[0].equals("help")) {
            printHelp();
        } else if(tokens[0].equals("get")){
            if(tokens.length == 2){
                if(client != null && client.isRunning()){
                    getMessage(tokens[1].trim());
                } else {
                    printError("Not connected!");
                }
            } else {
                printError("Invalid number of parameters");
            }
        } else {
            printError("Unknown command");
            printHelp();
        }
    }


    private void connect(String address, int port)
            throws UnknownHostException, IOException {
        client = new Client(address, port);
        //client.addListener(this);
        client.start();
    }

    private void disconnect() throws IOException {
        if(client != null) {
            client.disconnect();
            client = null;
        }
    }

    private void putMessage(String key, String value){
        try {
            client.putMessage(key,value);
            //client.putMessage(new TextMessage(msg));
        } catch (Exception e) {
            printError("Unable to send message!");
            e.printStackTrace();
        }
    }

    private void getMessage(String key){
        try {
            client.getMessage(key);
            //client.putMessage(new TextMessage(msg));
        } catch (Exception e) {
            printError("Unable to get message!");
            e.printStackTrace();
        }
    }


    private void printPossibleLogLevels() {
        System.out.println(PROMPT
                + "Possible log levels are:");
        System.out.println(PROMPT
                + "ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF");
    }


    private String setLevel(String levelString) {

        if(levelString.equals(Level.ALL.toString())) {
            logger.setLevel(Level.ALL);
            return Level.ALL.toString();
        } else if(levelString.equals(Level.DEBUG.toString())) {
            logger.setLevel(Level.DEBUG);
            return Level.DEBUG.toString();
        } else if(levelString.equals(Level.INFO.toString())) {
            logger.setLevel(Level.INFO);
            return Level.INFO.toString();
        } else if(levelString.equals(Level.WARN.toString())) {
            logger.setLevel(Level.WARN);
            return Level.WARN.toString();
        } else if(levelString.equals(Level.ERROR.toString())) {
            logger.setLevel(Level.ERROR);
            return Level.ERROR.toString();
        } else if(levelString.equals(Level.FATAL.toString())) {
            logger.setLevel(Level.FATAL);
            return Level.FATAL.toString();
        } else if(levelString.equals(Level.OFF.toString())) {
            logger.setLevel(Level.OFF);
            return Level.OFF.toString();
        } else {
            return LogSetup.UNKNOWN_LEVEL;
        }
    }

    private void printHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append(PROMPT).append("ECHO CLIENT HELP (Usage):\n");
        sb.append(PROMPT);
        sb.append("::::::::::::::::::::::::::::::::");
        sb.append("::::::::::::::::::::::::::::::::\n");
        sb.append(PROMPT).append("connect <host> <port>");
        sb.append("\t\t establishes a connection to a server\n");
        sb.append(PROMPT).append("put <key,value>");
        sb.append("\t\t sends a <key,value> pair to the server \n");
        sb.append(PROMPT).append("get <key>");
        sb.append("\t\t\t sends a <key> to the server and returns a value if key found\n");
        sb.append(PROMPT).append("disconnect");
        sb.append("\t\t\t disconnects from the server \n");

        sb.append(PROMPT).append("logLevel");
        sb.append("\t\t\t changes the logLevel \n");
        sb.append(PROMPT).append("\t\t\t\t ");
        sb.append("ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF \n");

        sb.append(PROMPT).append("quit ");
        sb.append("\t\t\t\t exits the program");
        System.out.println(sb.toString());
    }

    private void printError(String error){
        System.out.println(PROMPT + "Error! " +  error);
    }

    public void handleNewMessage(TextMessage msg) {
        if(!stop) {
            System.out.println(msg.getMsg());
            System.out.print(PROMPT);
        }
    }

    public void handleStatus(ClientSocketListener.SocketStatus status) {
        if(status == ClientSocketListener.SocketStatus.CONNECTED) {

        } else if (status == ClientSocketListener.SocketStatus.DISCONNECTED) {
            System.out.print(PROMPT);
            System.out.println("Connection terminated: "
                    + serverAddress + " / " + serverPort);

        } else if (status == ClientSocketListener.SocketStatus.CONNECTION_LOST) {
            System.out.println("Connection lost: "
                    + serverAddress + " / " + serverPort);
            System.out.print(PROMPT);
        }

    }

    /**
     * Main entry point for the echo server application.
     * @param args contains the port number at args[0].
     */
    public static void main(String[] args) {
        try {
            new LogSetup("logs/client.log", Level.OFF);
            KVClient app = new KVClient();
            app.run();
        } catch (IOException e) {
            System.out.println("Error! Unable to initialize logger!");
            e.printStackTrace();
            System.exit(1);
        }
    }



}