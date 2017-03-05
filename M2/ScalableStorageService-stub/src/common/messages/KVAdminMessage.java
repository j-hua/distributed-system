package common.messages;

public interface KVAdminMessage {
	
    public enum StatusType {
    	INITIALIZATION_SUCCESS,         /* Initialization was successful*/
    	SERVER_STARTED,         /* Starting server was successful*/
    	SERVER_STOPPED,         /* Stopping server was successful*/
    	SERVER_SHUTDOWN,         /* Shutting down server was successful*/
    	LOCK_WRITE_SUCCESS,         /* Locking the write operation was successful*/
	}

	/**
	 * @return a status string that is used to identify request types, 
	 * response types and error types associated to the message.
	 */
	public StatusType getStatus();
	
}


