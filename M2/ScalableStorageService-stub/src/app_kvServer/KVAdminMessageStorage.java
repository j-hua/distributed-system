package app_kvServer;

import common.messages.KVAdminMessage;

public class KVAdminMessageStorage implements KVAdminMessage{

    private StatusType status;


    public KVAdminMessageStorage(StatusType status){

        this.status = status;
    }

    @Override
    public StatusType getStatus() {
        return status;
    }

}
