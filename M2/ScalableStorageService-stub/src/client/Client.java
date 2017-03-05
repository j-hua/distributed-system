package client;

import common.messages.KVMessage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by JHUA on 2017-03-04.
 */
public class Client extends Thread{

    private boolean running;
    private KVStore kvStore;
    private static Logger logger = Logger.getRootLogger();
    private Metadata mData;
    /**
     *  Client constructor
     */
    public Client(String address, int port) throws UnknownHostException, IOException{
        kvStore = new KVStore(address, port);
        try{
            kvStore.connect();
            setRunning(kvStore.getConnected());
        }catch(Exception e){
            e.printStackTrace();
        }

        logger.info("Connection established");
    }

    /**
     * Initializes and starts the client connection.
     * Loops until the connection is closed or aborted by the client.
     */
    public void run(){
        logger.info("Client thread starts running");
        try{
            //currently it does nothing

            //if SERVER NOT RESPONSIBLE
                //update ListMetadata
                //create new kvStore

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isRunning(){
        return running;
    }

    public  void setRunning(boolean run){
        running = run;
    }


    public void putMessage(String key, String value) throws Exception {
        KVMessage kvm = kvStore.put(key,value);

        while(kvm.getStatus() == KVMessage.StatusType.SERVER_NOT_RESPONSIBLE){

            //update metadata
            mData.updateMetadata(kvm.getValue());

            //lookup
            Address address = mData.lookup(key);

            //close the wrong server
            kvStore.disconnect();

            //retry
            kvm = kvStore.put(key,value);
        }
    }

    public void getMessage(String key) throws Exception {
        KVMessage kvm = kvStore.get(key);

        while(kvm.getStatus() == KVMessage.StatusType.SERVER_NOT_RESPONSIBLE){

            //update metadata
            mData.updateMetadata(kvm.getValue());

            //lookup
            Address address = mData.lookup(key);

            //close the wrong server
            kvStore.disconnect();

            //retry
            kvm = kvStore.get(key);
        }
    }

    public void disconnect() throws IOException {
        kvStore.disconnect();
        setRunning(false);
    }
}
