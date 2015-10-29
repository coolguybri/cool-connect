package com.coolguybri.coolconnect.server;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * OfyHelper, a ServletContextListener, is setup in web.xml to run before a JSP is run.  This is
 * required to let JSP's access Ofy.
 **/
public class OfyHelper implements ServletContextListener 
{
	/* this gets called right before the first request, before the servlets are initialized. */
	public void contextInitialized(ServletContextEvent event) 
	{
	  	ObjectifyService.register(PulseClient.class);
	  	ObjectifyService.register(PulseClientOutage.class);
	}

	/* gets called when servlets are coming down. */
	public void contextDestroyed(ServletContextEvent event) 
	{
		// App Engine does not currently invoke this method.
	}
}