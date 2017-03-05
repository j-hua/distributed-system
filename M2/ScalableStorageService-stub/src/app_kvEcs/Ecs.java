package app_kvEcs;

import com.sun.org.apache.bcel.internal.generic.LUSHR;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by warefhaque on 3/3/17.
 */
public class Ecs {

    List<String> mIpAndPorts;
    private static final String PROMPT = "ECS> ";
    private BufferedReader stdin;
    private boolean stop = false;
    String privateKey = "/homes/h/haquewar/.ssh/id_rsa";


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

        }else if (tokens[0].equals("remove")){
            if (tokens.length == 3){

            }
        }
    }
    public void initService(int numberOfNodes){
        readFile();
        if (numberOfNodes<mIpAndPorts.size()){
            for (int i=0; i<numberOfNodes; i++) {
                String[] elements= mIpAndPorts.get(i).split(" ");
                String ip = elements[0];
                String port =elements[1];
                System.out.println("ip passed in: "+ip+ " port: "+port);
                ssh(ip,port);
            }
        }else{
            System.out.println("Number of nodes entered is larger that ones available");
        }

    }

    public void ssh(String ipAddress, String port){

        Process proc = null;
        String script = "src/app_ECS/script.sh";
        String[] cmd = {"sh", script, port,ipAddress,"40000"};

        Runtime run = 	Runtime.getRuntime();
        try {
            proc = run.exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (proc!=null){
                System.out.println("killing process!");
                proc.destroy();
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


