package client;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JHUA on 2017-03-04.
 */
public class Metadata {
    Map m =  new HashMap();
    /**
     *
     *
     */
    public Metadata(){}

    public void put(MapKey mk,MapValue mv){
        m.put(mk,mv);
    }

    public void remove(MapKey mk){
        m.remove(mk);
    }
}
