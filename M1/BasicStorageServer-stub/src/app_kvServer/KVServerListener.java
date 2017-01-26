package app_kvServer;

/**
 * Created by haquewar on 1/26/17.
 */
public interface KVServerListener {
    void parsedMessage(String action, String key, String value) throws Exception;
}
