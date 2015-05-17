package com.sandy.jovenotes.processor.async;

import java.io.Serializable;

/**
 * Superclass of all command data which are stored and processed via 
 * PersistentQueue.
 * 
 * There are certain activities like downloading sound clips, word meanings
 * and pronounciation which can't be guaranteed to complete in a standard run.
 * For example, if the network connection is patchy or the server is not available.
 * 
 * To solve this design challenge, we convert such activities into persisted
 * commands and process them via an asynchronous processor feeding off the 
 * persisted queue. This way, if these commands don't get processed during a
 * standard run, they will be picked up in the next run.
 * 
 * @author Sandeep
 */
public abstract class PersistedCmd implements Serializable {

	private static final long serialVersionUID = -1919148238331312275L;

	public abstract void execute() throws Exception ;
}
