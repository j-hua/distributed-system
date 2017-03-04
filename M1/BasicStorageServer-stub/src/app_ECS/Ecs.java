package app_ECS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by warefhaque on 3/3/17.
 */
public class Ecs {

    String node1 =	"127.0.0.1 	50000";
    String node2 =	"127.0.0.1 	50001";
    String node3 =	"127.0.0.1 	50002";
    String node4 =	"127.0.0.1 	50003";
    String node5 =	"127.0.0.1 	50004";
    String node6 =	"127.0.0.1 	50005";
    String node7 =	"127.0.0.1 	50006";


    public Ecs(){

    }
    public void initService(){

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

        System.out.println(hashedServer.mHashedKeys.toString() +" "+hashedServer.mIpAndPort);
        System.out.println(hashedServer1.mHashedKeys.toString() +" "+hashedServer1.mIpAndPort);

    }

}
