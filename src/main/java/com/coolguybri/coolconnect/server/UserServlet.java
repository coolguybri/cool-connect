package com.coolguybri.coolconnect.server;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.Key;

import java.io.IOException;
import java.util.Properties;
import java.lang.String;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

//import com.coolguybri.coolconnect.PulseClient;

/*
*/
public class UserServlet extends HttpServlet 
{
	/* main http handler for the admin ui. */
  	@Override
  	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException 
  	{
  		/* see which client we want. */
  		String clientName = req.getParameter("client");
  		if (clientName == null)
  		{
  			sendError(req, resp);
  			return;
  		}
  		
  		/* prepare to return data. */
  		resp.setContentType("text/plain");
  		
  		/* query all outage records for this client. */
		Key<PulseClient> clientKey = Key.create(PulseClient.class, clientName);
		List<PulseClientOutage> outages = ofy().load().type(PulseClientOutage.class).ancestor(clientKey).order("-startTime").limit(100).list();
		if (outages.isEmpty()) 
		{
			resp.getWriter().println("No outages reported for " + clientName + ". \n\n");
		}
		else
		{
			SimpleDateFormat dateformat = new SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss");
			resp.getWriter().println("outages reported:\n");
			
			int i = 0;
			for (PulseClientOutage outage : outages) 
			{
				i++;
				resp.getWriter().println("outage " + i + ":");
				
				/* generate a pretty-printed start timw. */
				String startTimeString = dateformat.format(new Date(outage.startTime));
				String endTimeString = dateformat.format(new Date(outage.endTime));

				/* generate a pretty-printed duration. */
				long diff = outage.endTime - outage.startTime;
				int hours = (int)(diff / 3600000); diff = diff % 3600000;
				int mins = (int)(diff / 60000); diff = diff % 60000;
				int secs = (int)(diff / 1000); diff = diff % 1000;
				String durationString = String.format("%02d:%02d:%02d", hours, mins, secs);
				
				resp.getWriter().println("\t start=" + startTimeString + ", end=" + endTimeString + ", duration=" + durationString);
			}
		}
    }
    
    
    /* common error routine. */
	protected void sendError(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setContentType("text/plain");
		resp.getWriter().println("Error. \n\n");
		Properties p = System.getProperties();
		p.list(resp.getWriter());
	}
}