package app_ECS;

import org.omg.PortableInterceptor.INACTIVE;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by warefhaque on 3/3/17.
 */
public class ConsistentHashing {
    private int numberOfReplicas;
    public SortedMap<Integer, HashedServer> circle = new TreeMap<Integer, HashedServer>();

    ConsistentHashing(int numberOfReplicas, List<String> nodes) {

        this.numberOfReplicas = numberOfReplicas;

        for (String node : nodes) {
            add(node);
        }
    }


    /**
     * Add a server to the circle
     * @param node IP + Port No.
     */
    public void add(String node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            String hashString = node.replaceAll("\\s","");
            circle.put(hashFunction(hashString + i), new HashedServer(node));
        }
    }

    /**
     * Removes a node from the cirlce
     * @param node IP + Port
     */
    public void remove(String node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            String hashString = node.replaceAll("\\s","");
            circle.remove(hashFunction(hashString + i));
        }
    }

    /**
     * Gets a string for the IP ADDRESS + PORT of the server
     * associated with the KEY provided
     * @param key
     * @return
     */
    public HashedServer get(String key) {
        if (circle.isEmpty()) {
            return null;
        }
        int hash = hashFunction(key);
        int serverhash = 0;
        if (!circle.containsKey(hash)) {

            SortedMap<Integer, HashedServer> tailMap = circle.tailMap(hash);

            serverhash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();

            //store the list of hashed key values the server is responsible for
            circle.get(serverhash).addHash(hash);
        }
        return circle.get(serverhash);
    }


    /**
     * Hashes a string value and returns an integer
     * to be put on the circle
     * @param ipAndPort IP + Port number of a server
     * @return Hashed Value
     */
    public int hashFunction(String ipAndPort){

        try {
            MessageDigest md = null;
            md = MessageDigest.getInstance("MD5");
            md.update(ipAndPort.getBytes());
            byte[] digest = md.digest();
            int integer = new BigInteger(1, digest).intValue();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            System.out.println("original:" + ipAndPort);
            System.out.println("digested(hex):" + Integer.toString(integer));
            return integer;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1;
        }

    }

    class HashedServer {
        List<Integer> mHashedKeys;
        String mIpAndPort;

        HashedServer(String ipAndPort){
            mHashedKeys = new ArrayList<>();
            mIpAndPort = ipAndPort;
        }

        boolean addHash (Integer hashValue){
            if (!mHashedKeys.contains(hashValue)){
                mHashedKeys.add(hashValue);
                return true;
            }else{
                return false;
            }
        }

    }
}
