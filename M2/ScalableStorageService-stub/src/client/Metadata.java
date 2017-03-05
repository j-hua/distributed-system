package client;

import javafx.scene.control.cell.MapValueFactory;
import org.omg.PortableInterceptor.ServerRequestInfo;

import java.util.*;

/**
 * Created by JHUA on 2017-03-04.
 */
public class Metadata {
    NavigableMap<String, MapValue> mList = new TreeMap<>();

    public Metadata(){}

    public void add(String k, MapValue v){
        mList.put(k,v);
    }

    /**
     * lookup function for metadata
     * if entry does not exist, returns null
     * otherwise return object that contains hashEnd, ip address and port number
     * @param hashedKey
     * @return
     */
    public MapValue lookup(String hashedKey){

        /**
         * floorEntry(K key):
         * returns a key-value mapping entry which is associated with the greatest key less than or equal to the given key.
         */
         Map.Entry<String,MapValue> floorEntry = mList.floorEntry(hashedKey);
         String floorKey = mList.floorKey(hashedKey);

         if(floorEntry.getValue().getHashEnd().compareTo(hashedKey) >= 0 ){
            return floorEntry.getValue();
         }else{
            //Entry not found
            return null;
         }
    }


    /**
     * This function updates entire mList based on md
     * md contains completed information of mList, in the format of string
     * @param md
     */
    public void updateMetadata(String md){
        String[] entry = md.split("\\s+");

        int i;
        for(i=0;i<entry.length;i++){
            String[] element = entry[i].split(",");

            //if hashStart > hashEnd
            if(element[0].compareTo(element[1]) > 0) {
                add(element[0], new MapValue("ffffffffffffffff", element[2], Integer.parseInt(element[3])));
                add("0000000000000000", new MapValue(element[1], element[2], Integer.parseInt(element[3])));
            }else{
                add(element[0],new MapValue(element[1],element[2],Integer.parseInt(element[3])));
            }

        }

    }
}
