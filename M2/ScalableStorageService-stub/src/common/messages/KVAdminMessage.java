package common.messages;

public interface KVAdminMessage {
	
    public enum StatusType {
    	INITIALIZATION_SUCCESS,         /* Initialization was successful*/
    	SERVER_STARTED,         /* Starting server was successful*/
    	SERVER_STOPPED,         /* Stopping server was successful*/
    	SERVER_SHUTDOWN,         /* Shutting down server was successful*/
    	LOCK_WRITE_SUCCESS,         /* Locking the write operation was successful*/
    	METADATA_UPDATE_SUCCESSFUL,         /* Updating the metadata on the server was successful*/
    	DATA_TRANSFER_SUCCESSFUL,         /* Data arrived at the target server successfully*/
    	DATA_TRANSFER_FAILED,         /* Data did not arrive at the server, try again*/
    	DELETING_KVPAIRS_FAILED,	/* The key-value pairs could not be all deleted*/
    	DELETING_KVPAIRS_SUCCESSFUL,	/* The key-value pairs were all deleted*/
	}

	/**
	 * @return a status string that is used to identify request types, 
	 * response types and error types associated to the message.
	 */
	public StatusType getStatus();
	
	/**
	 * @return a String with the appropriate data, if needed
	 */
	public String getData();
	
}


