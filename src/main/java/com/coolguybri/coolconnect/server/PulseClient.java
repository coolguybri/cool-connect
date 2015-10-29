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
public class PulseClient 
{
	/* members */
    @Id public String 		name;
    public long				lastPulseTime;
    public long				nextPulseTime;
    public long				goodCount;
    public String			status;
    
    public PulseClient(String n)
    {
    	name = n;
    	lastPulseTime = 0;
    	nextPulseTime = 0;
    	goodCount = 0;
    	status = "created";
    }
    
     public PulseClient()
     {
     }
}