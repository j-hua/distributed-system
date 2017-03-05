package client;

/**
 * Created by JHUA on 2017-03-04.
 */
public class MapValue {
    private String hashEnd;
    private String ipAddr;
    private int port;

    public MapValue(String hEnd, String iAddr, int p){
        hashEnd = hEnd;
        ipAddr = iAddr;
        port = port;
    }

    public String getHashEnd(){return hashEnd;}

    public String getIpAddr(){return ipAddr;}

    public int getPort(){return port;}
}
