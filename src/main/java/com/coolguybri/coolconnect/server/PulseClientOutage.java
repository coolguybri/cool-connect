package com.coolguybri.coolconnect.server;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.Key;

/* 
	This is an Objectify model class
*/
@Entity
public class PulseClientOutage 
{
	/* members */
	@Parent Key<PulseClient> 		client;
	@Id public Long 				id;
	@Index public long 				startTime;
	public long 					endTime;

	public PulseClientOutage() 
	{
	}

	public PulseClientOutage(PulseClient c, long s, long e) 
	{
		client = Key.create(PulseClient.class, c.name);  // Creating the Ancestor 
		startTime = s;
		endTime = e;
	}
}