package app_kvServer;

import common.messages.KVMessage;

/**
 * Created by haquewar on 1/26/17.
 */
public interface KVServerListener {
    KVMessage put(String key, String value) throws Exception;
    KVMessage get(String key) throws Exception;
}
