package app_kvServer;

import common.messages.KVMessage;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class storageServer {
    //this is going to be used for all replacement policies
    public List<String> keyCache = new ArrayList<String>();
    public List<String> valueCache = new ArrayList<String>();

    private  String replacementPolicy = null;
    private int limit = 0;
    private static Logger logger = Logger.getRootLogger();

    storageServer(String replacementPolicy, int limit){
        this.replacementPolicy = replacementPolicy;
        this.limit = limit;
    }

    public synchronized KVMessage put(String key, String value) throws Exception {
        logger.info("Putting  "+ " "+ "key: " + key + " "+"value: " + value);

        try {
            //writer for the original file
            FileWriter write = new FileWriter("./storage.txt", true);
            PrintWriter printWrite = new PrintWriter(write);
            //writer for a temp file that could replace the original file
            FileWriter writeTemp = new FileWriter("./temp.txt", true);
            PrintWriter printTemp = new PrintWriter(writeTemp);

            KVMessage.StatusType status = null;

            if(value != null){
                //read file for this key, maybe its an update
                File inputFile = new File("./storage.txt");
                BufferedReader br = new BufferedReader(new FileReader(inputFile));

                String line;
                boolean replaced = false;

                while((line = br.readLine()) != null){
                    if(line.length() != 0){
                        String[] kv = line.split(" ");
                        if(kv[0].equals(key)){
                            //replace this line
                            replaced = true;
                            printTemp.println(key+" "+value);
                        }else{
                            printTemp.println(line);
                        }
                    }
                }

                printTemp.close();
                File temp = new File("./temp.txt");

                if(!replaced){
                    //delete temp file
                    logger.info("Temp Deletion: " + temp.delete());

                    printWrite.println(key+" "+value);
                    printWrite.close();

                    status = KVMessage.StatusType.PUT_SUCCESS;
                } else {
                    //delete original storage.txt and rename the temp file
                    logger.info("Original Deletion: " + inputFile.delete());
                    logger.info("Renaming of Temp: " + temp.renameTo(inputFile));
                    status = KVMessage.StatusType.PUT_UPDATE;
                }

                if(replacementPolicy.equals("FIFO")){
                    //FIFO CACHE----------------------------------------------------------------------------
                    //add key to cache if not already there
                    if(!keyCache.contains(key)){
                        //not there, add to list
                        //check if array is full
                        if(keyCache.size() == limit){
                            //remove the first element by virtue of FIFO
                            logger.info("Key: " + keyCache.get(0) + " Value: " + valueCache.get(0) + " REMOVED FROM CACHE");
                            keyCache.remove(0);
                            valueCache.remove(0);
                        }
                        logger.info("Key: " + key + " Value: " + value + " ADDED TO CACHE");
                        keyCache.add(key);
                        valueCache.add(value);
                    } else if(status.equals(KVMessage.StatusType.PUT_UPDATE)){
                        logger.info("PUT UPDATE OCCURRED, MODIFYING CACHE WITH NEW VALUE");
                        int index = valueCache.indexOf(valueCache.get(keyCache.indexOf(key)));
                        valueCache.set(index, value);

                        logger.info(keyCache.toString());
                        logger.info(valueCache.toString());
                    }
                    //---------------------------------------------------------------------------------------
                } else if (replacementPolicy.equals("LRU")){
                    //LRU CACHE----------------------------------------------------------------------------
                    //add key to cache if not already there
                    if(!keyCache.contains(key)){
                        //not there, add to end of list, indicates most recently used
                        //check if array is full
                        if(keyCache.size() == limit){
                            //remove the first element by virtue of LRU (first is the least recently used)
                            logger.info("Key: " + keyCache.get(0) + " Value: " + valueCache.get(0) + " REMOVED FROM CACHE");
                            keyCache.remove(0);
                            valueCache.remove(0);
                        }
                        logger.info("Key: " + key + " Value: " + value + " ADDED TO CACHE");
                        keyCache.add(key);
                        valueCache.add(value);
                    } else {
                        //the key cache contains the key, thus a put update happened, remove key and value from cache
                        logger.info("Removing " + keyCache.get(keyCache.indexOf(key)) + " and " + valueCache.get(keyCache.indexOf(key)));
                        valueCache.remove(keyCache.indexOf(key));
                        keyCache.remove(keyCache.indexOf(key));

                        //add to end of list, that means it was the most recently used
                        keyCache.add(key);
                        valueCache.add(value);

                        logger.info(key + " " + value + " added to end of list");
                        logger.info(keyCache.toString());
                        logger.info(valueCache.toString());
                    }
                    //---------------------------------------------------------------------------------------
                }
                br.close();

            } else {
                //delete the corresponding key value pair in the file
                File inputFile = new File("./storage.txt");
                BufferedReader br = new BufferedReader(new FileReader(inputFile));

                String line;
                boolean deleted = false;

                while((line = br.readLine()) != null){
                    if(line.length() != 0){
                        String[] kv = line.split(" ");
                        if(kv[0].equals(key)){
                            //delete this line
                            deleted = true;
                        }else{
                            printTemp.println(line);
                        }
                    }
                }

                printTemp.close();
                File temp = new File("./temp.txt");

                if(!deleted){
                    //delete temp file
                    logger.error("Temp Deletion: " + temp.delete());

                    printWrite.close();
                    status = KVMessage.StatusType.DELETE_ERROR;
                } else {
                    //delete original storage.txt and rename the temp file
                    logger.info("Original Deletion: " + inputFile.delete());
                    logger.info("Renaming of Temp: " + temp.renameTo(inputFile));
                    status = KVMessage.StatusType.DELETE_SUCCESS;

                    if(replacementPolicy.equals("FIFO") || replacementPolicy.equals("LRU")){
                        //FIFO OR LRU--------------------------------------------------------
                        //If in cache, remove it
                        if(keyCache.contains(key)){
                            logger.info("Removing " + keyCache.get(keyCache.indexOf(key)) + " and " + valueCache.get(keyCache.indexOf(key)));
                            valueCache.remove(keyCache.indexOf(key));
                            keyCache.remove(keyCache.indexOf(key));

                            logger.info(keyCache.toString());
                            logger.info(valueCache.toString());
                        }
                        //-------------------------------------------------------------------
                    }
                }

                br.close();
            }

            KVMessageStorage kvms = new KVMessageStorage(key, value, status);
            return kvms;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new Exception("PUT_ERROR");
//            KVMessage.StatusType status = KVMessage.StatusType.PUT_ERROR;
//            KVMessageStorage kvms = new KVMessageStorage(key, value, status);
//            return kvms;
        }

    }

    public synchronized KVMessage get(String key) throws Exception {
        logger.info("Getting "+"key: " +key);

        //get the value for the corresponding key if the key exists in file
        try {
            File inputFile = new File("./storage.txt");
            BufferedReader br = new BufferedReader(new FileReader(inputFile));

            String line;
            KVMessage.StatusType status = null;

            if(replacementPolicy.equals("FIFO")){
                //FIFO--------------------------------------------------------------------
                //First check if it is in the cache
                if(keyCache.contains(key)){
                    logger.info("KEY FOUND IN CACHE");
                    br.close();
                    status = KVMessage.StatusType.GET_SUCCESS;
                    KVMessageStorage kvms = new KVMessageStorage(key, valueCache.get(keyCache.indexOf(key)), status);
                    return kvms;
                }
                //------------------------------------------------------------------------
            } else if (replacementPolicy.equals("LRU")){
                //LRU---------------------------------------------------------------------
                //First check if it is in the cache
                if(keyCache.contains(key)){
                    logger.info("KEY FOUND IN CACHE");
                    String value = valueCache.get(keyCache.indexOf(key));

                    //the key cache contains the key, thus a put update happened, remove key and value from cache
                    logger.info("Removing " + keyCache.get(keyCache.indexOf(key)) + " and " + valueCache.get(keyCache.indexOf(key)));
                    valueCache.remove(keyCache.indexOf(key));
                    keyCache.remove(keyCache.indexOf(key));

                    //add to end of list, that means it was the most recently used
                    keyCache.add(key);
                    valueCache.add(value);

                    logger.info(key + " " + value + " added to end of list");
                    logger.info(keyCache.toString());
                    logger.info(valueCache.toString());

                    br.close();
                    status = KVMessage.StatusType.GET_SUCCESS;
                    KVMessageStorage kvms = new KVMessageStorage(key, value, status);
                    return kvms;
                }
                //------------------------------------------------------------------------
            }

            while((line = br.readLine()) != null){
                if(line.length() != 0){
                    String[] kv = line.split(" ");
                    if(kv[0].equals(key)){
                        //skip the key and the first part of the value because its already been stored in value
                        boolean skipFirst = false;
                        boolean skipSecond = false;
                        String value = kv[1];

                        //concatenate all the other parts of the value
                        for (String part : kv){
                            if(!skipFirst){
                                skipFirst = true;
                            } else if(!skipSecond){
                                skipSecond = true;
                            } else{
                                value = value + " " + part;
                            }
                        }
                        br.close();

                        if(replacementPolicy.equals("FIFO") || replacementPolicy.equals("LRU")){
                            //FIFO or LRU--------------------------------------------------
                            //Add to cache because it is not there
                            //check if array is full
                            logger.info("Entered");
                            if(keyCache.size() == limit){
                                //remove the first element by virtue of FIFO
                                //or remove by virtue of being the least recently used (LRU)
                                logger.info("Key: " + keyCache.get(0) + " Value: " + valueCache.get(0) + " REMOVED FROM CACHE");
                                keyCache.remove(0);
                                valueCache.remove(0);
                            }
                            logger.info("Key: " + key + " Value: " + value + " ADDED TO CACHE");
                            keyCache.add(key);
                            valueCache.add(value);
                            //------------------------------------------------------------
                        }

                        status = KVMessage.StatusType.GET_SUCCESS;
                        KVMessageStorage kvms = new KVMessageStorage(key, value, status);
                        return kvms;
                    }
                }
            }
            br.close();

            status = KVMessage.StatusType.GET_ERROR;
            logger.error("GET_ERROR");
            KVMessageStorage kvms = new KVMessageStorage(key, null, status);
            return kvms;
        } catch (IOException e) {
            logger.error("EROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOR");
            throw new Exception("GET_ERROR");
//            KVMessage.StatusType status = KVMessage.StatusType.GET_ERROR;
//            KVMessageStorage kvms = new KVMessageStorage(key, null, status);
//            return kvms;
        }
    }

}
