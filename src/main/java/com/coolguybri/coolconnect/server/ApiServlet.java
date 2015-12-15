package com.coolguybri.coolconnect.server;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.Key;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Properties;
import java.lang.String;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;


/*
*/
public class ApiServlet extends HttpServlet 
{
	/* Constants */
	protected static final int DEFAULT_LIST_LIMIT = 100;
	
	static protected class ErrorMesg
	{
		private String message;
		
		public ErrorMesg(String m)
		{
			message = m;
		}
	}
	
	static protected class Response
	{
		private boolean 	success;
		private Object 		response;
		private ErrorMesg	error;
		
		public Response(boolean s)
		{
			success = s;
		}
		
		public Response(Object r)
		{
			success = true;
			response = r;
		}
		
		public Response(String m)
		{
			success = false;
			error = new ErrorMesg(m);
		}
	}
	
	
	/* main http handler. */
  	@Override
  	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException 
  	{
  		/* every single response will always be JSON. */
  		resp.setContentType("application/json");
  		
  		/* parse the url, so we can get the command part out. */
  		String[] cmds = parseFullUrl(req);
  		if (cmds.length < 1)
  		{
  			sendError(req, resp, "no command requested");
  			return;
  		}
  		
  		/* dispatch to the correct command. */
  		if (cmds[0].equals("clients"))
  		{
  			/* we just want the list of all the clients that have pulsed in. */
  			processClientList(req, resp);
  		}
  		else if (cmds[0].equals("outages"))
  		{
  			/* we just want the list of all the outages. */
  			processOutageList(req, resp);
  		}
  		else
  		{
  			/* unrecognized command. */
  			sendError(req, resp, "unrecognized command requested");
  		}
    }
    
    
    /*
    */
    static protected class ClientListResponse
	{
		private static transient SimpleDateFormat 	df;
		private Vector 								clients;
		
		static protected class Client
		{
			private String 	name;
			private String 	lastPulse;
			
			public Client(PulseClient client)
			{
				name = client.name;
				lastPulse = (client.lastPulseTime == 0) ? null : df.format(new Date(client.lastPulseTime));
			}
		}
		
		public ClientListResponse()
		{
			// "2012-04-23T18:25:43.511Z"
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			clients = new Vector();
		}
		
		public void AddClient(PulseClient client)
		{
			clients.add(new Client(client));
		}
	}
	
	
    /*
    */
    public void processClientList(HttpServletRequest req, HttpServletResponse resp) throws IOException 
    {
    	Gson gson = new Gson();
    	ClientListResponse r = new ClientListResponse();
    	
    	/* query all client records. */
		List<PulseClient> clients = ofy().load().type(PulseClient.class).limit(DEFAULT_LIST_LIMIT).list();
		for (PulseClient client : clients) 
		{
			r.AddClient(client);
		} 
		
		Response rr = new Response(r);
		resp.getWriter().println(gson.toJson(rr));
		System.out.println("ApiServlet:processClientList: " + gson.toJson(rr));
    }
    
			
	/*
    */
    static protected class OutageResponse
	{
		private static transient SimpleDateFormat 	df;
		private String								client;
		private Vector 								outages;
		
		static protected class Outage
		{
			private String 	startTime;
			private String 	endTime;
			
			public Outage(PulseClientOutage outage)
			{
				startTime = df.format(new Date(outage.startTime));
				endTime = df.format(new Date(outage.endTime));
			}
		}
		
		public OutageResponse(String c)
		{
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			client = c;
			outages = new Vector();
		}
		
		public void AddOutage(PulseClientOutage outage)
		{
			outages.add(new Outage(outage));
		}
	}
    
    /*
    */
    public void processOutageList(HttpServletRequest req, HttpServletResponse resp) throws IOException 
    {
    	/* see which client we want. */
		String clientName = req.getParameter("client");
		if (clientName == null)
		{
			sendError(req, resp, "no client specified");
			return;
		}
		
    	Gson gson = new Gson();
    	OutageResponse r = new OutageResponse(clientName);
    	
    	/* query all outage records for this client. */
		Key<PulseClient> clientKey = Key.create(PulseClient.class, clientName);
		List<PulseClientOutage> outages = ofy().load().type(PulseClientOutage.class).ancestor(clientKey).order("-startTime").limit(DEFAULT_LIST_LIMIT).list();
		for (PulseClientOutage outage : outages) 
		{
			r.AddOutage(outage);
		}
    	
    	Response rr = new Response(r);
		resp.getWriter().println(gson.toJson(rr));	
		System.out.println("ApiServlet:processOutageList: " + gson.toJson(rr));
    }
    
    
    /* break the requested url into its components. */
    protected static String[] parseFullUrl(HttpServletRequest req) 
    {
    	try 
    	{
    		String pathAfterContext = req.getRequestURI().substring(req.getContextPath().length() + req.getServletPath().length() + 1);
    	
    		Vector vec = new Vector();
    		for (String val : pathAfterContext.split("/")) {
        		vec.add(URLDecoder.decode(val, "UTF-8"));
    		}
    	
    		String[] ret = new String[vec.size()];
    		vec.toArray(ret);
    		return ret;
    	}
    	catch (Exception e)
    	{
    		return new String[0];
    	}
	}
    
    
    /* common error routine. */
	protected void sendError(HttpServletRequest req, HttpServletResponse resp, String mesg) throws IOException
	{
		Gson gson = new Gson();
		Response rr = new Response(mesg);
		resp.getWriter().println(gson.toJson(rr));
		System.out.println("ApiServlet:sendError: " + gson.toJson(rr));
	}
}