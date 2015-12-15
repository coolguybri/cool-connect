package com.coolguybri.coolconnect.server;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;
 

/*
*/
public class PulseServlet extends HttpServlet 
{
	/* */
	protected int parseInterval(HttpServletRequest req)
	{
		String intervalString = req.getParameter("interval");
		if (intervalString == null) 
			intervalString = "60";
			
		int intervalMillis = 0;
		try
		{
			intervalMillis = Integer.parseInt(intervalString) * 1000;
		}
		catch (Exception e)
		{
			System.out.println("PulseServlet: exception parsing interval " + intervalString);
		}
		
		return intervalMillis;
	}
	
	/* main http handler for the pulse from a client. */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException 
	{
		/* get the required input params, and fill with defaults. */
		String clientName = req.getParameter("client");
		String secretKey = req.getParameter("key");
		if (clientName == null)
		{
			System.out.println("PulseServlet: error parsing input params: no client");
			sendError(req, resp);
			return;
		}
		
		/* sanity check and parse some of the params. */
		int intervalMillis = parseInterval(req);
		if (intervalMillis <= 0)
		{
			System.out.println("PulseServlet: error parsing input params: illegal interval");
			sendError(req, resp);
			return;
		}
		
		/* pulseSerial: the serial number of the current pulse. 0 means the agent just restarted. */
		int pulseSerial = -1;
		try
		{
			String pulseSerialString = req.getParameter("pulse");
			pulseSerial = Integer.parseInt(pulseSerialString);
		}
		catch (Exception e)
		{
			System.out.println("PulseServlet: exception parsing pulseSerial");
		}
		
		/* calculate the failure delta (number of client failures since the last time it connected). */
		int failureDelta = 0;
		try
		{
			String failureDeltaString = req.getParameter("failures");
			if (failureDeltaString != null)
				failureDelta = Integer.parseInt(failureDeltaString);
		}
		catch (Exception e)
		{
			System.out.println("PulseServlet: exception parsing pulseSerial");
		}
		
		/* BCTODO: we are about to use the client name in the database. */
		
		/* grab the entry from the db, and create it if it doesn't exist. */
		PulseClient client = ofy().load().type(PulseClient.class).id(clientName).now();
		if (client == null)
		{
			System.out.println("PulseServlet: auto-creating client record for " + clientName);
			client = new PulseClient(clientName);
		}
		
		/* update when our latest pulse came in (which is now), and when we should get the next one. */
		long now = System.currentTimeMillis();
		long expectedPulse = client.nextPulseTime;
		client.lastPulseTime = now;
		client.nextPulseTime = now + intervalMillis;
		client.clientFailures += failureDelta;
		client.clientPulseSerial = pulseSerial;
		if (pulseSerial == 0)
		{
			client.firstPulseTime = now;
		}
		
		/* are we late with this pulse? We'll let them be two intervals late before we complain. 
			Make sure to not include the first pulse from an agent (the time before it was not an outage, the agent was down),
			as well as when we just created the client in the database. */
		long expectedPulseFuzzy = expectedPulse + intervalMillis;
		System.out.println("PulseServlet: evaluating pulse (" + pulseSerial + ") with now=" + now + ", expected=" + expectedPulse + 
			", expectedFuzzy=" + expectedPulseFuzzy + ", failureCount=" + client.clientFailures);
		if ((pulseSerial != 0) && (expectedPulse != 0) && (now > expectedPulseFuzzy))
		{
			/* late! */
			System.out.println("PulseServlet: LATE pulse received from " + client.name);
			client.goodCount = 0;
			client.status = "shady";
			
			/* record the out-interval in the database. */
			PulseClientOutage outage = new PulseClientOutage(client, expectedPulse, now);
			ofy().save().entity(outage).now();
		}
		else
		{
			/* on time! increment the good count. */
			System.out.println("PulseServlet: on-time pulse received from " + client.name);
			client.goodCount++;
			if (client.goodCount == 3)
			{
				client.status = "connected";
			}
		}
		
		/* save the updated record. */
		/* BCTODO: do this at the same time as updating the outage record. */
		ofy().save().entity(client).now();    // async without the now()

		/* if we got here, we are cool. */
		sendSuccess(req, resp, client);
	}


	/* common error routine. */
	protected void sendError(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setContentType("text/plain");
		resp.getWriter().println("Error. \n\n");
		Properties p = System.getProperties();
		p.list(resp.getWriter());
	}
	
	
	/* common error routine. */
	protected void sendSuccess(HttpServletRequest req, HttpServletResponse resp, PulseClient client) throws IOException
	{
		resp.setContentType("application/json");
	
		String response = "{ \"client\":\"" + client.name + "\", \"status\":\"" + client.status + "\", \"count\":\"" + client.goodCount + "\" }";
		resp.getWriter().println("{ \"status\":\"success\", \"response\":" + response + " }");
	}
  
}