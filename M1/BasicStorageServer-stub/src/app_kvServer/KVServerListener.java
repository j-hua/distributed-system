package app_kvServer;

/**
 * Created by haquewar on 1/26/17.
 */
public interface KVServerListener {
    void putMessage(String key, String value) throws Exception;
    void getMessage(String key) throws Exception;
}
