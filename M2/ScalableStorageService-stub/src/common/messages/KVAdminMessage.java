package common.messages;

public interface KVAdminMessage {
	
    public enum StatusType {
    	INIT_SUCCESS,         /* Initialization was successful*/
}

	/**
	 * @return a status string that is used to identify request types, 
	 * response types and error types associated to the message.
	 */
	public StatusType getStatus();
	
}


