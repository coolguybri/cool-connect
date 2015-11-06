package com.coolguybri.coolconnect.agent;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.net.URL;
import java.net.URLConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URLEncoder;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;


/*
*/
public class PulseAgent 
{
	/* constants. */
	protected static final int INTERVAL_LOCAL = 5000;
	protected static final int INTERVAL_CLOUD = 60000;
	protected static final String SERVER_LOCAL = "http://127.0.0.1:8080/pulse";
	protected static final String SERVER_CLOUD = "https://cool-connect.appspot.com/pulse";
	
	/* members  */
	String 	agentName;
	String	serverUrl;
	int		intervalMillis;
	int		failureCount;
	int		failureCountTotal;
	Timer 	timer;
	
	
	/* nested class: our timer task. */
	class PulseTask extends TimerTask 
	{
    	public void run() 
    	{
      		System.out.println("sending pulse...");
      		sendPulse();
     	 	System.out.println("pulse complete");
     	 	
     	 	/* schedule the next timer. */
     	 	timer.schedule(new PulseTask(), intervalMillis);
    	}
  	}
  

	/*
		global entry point for the agent
	*/
    public static void main(String[] args) 
    {
    	/* parse the command line args. BCTODO: find a sexier way to do this. */
    	int 	interval = INTERVAL_CLOUD;
    	String	serverUrl = SERVER_CLOUD;
    	String	mode = "cloud";
    	for (String arg : args)
    	{
    		if (arg.equals("local"))
    		{
    			interval = INTERVAL_LOCAL;
    			serverUrl = SERVER_LOCAL;
    			mode = "local";
    		}
    	}
    	
    	/* create the new agent with the given params. */
    	System.out.println("starting agent in \"" + mode + "\" mode...");
        PulseAgent agent = new PulseAgent(interval, serverUrl); 
    }
    
    /*
    */
    public PulseAgent(int ival, String srvr)
    {
    	/* init. */
    	agentName = getComputerName();
    	serverUrl = srvr;
    	intervalMillis = ival;
    	failureCount = 0;
    	failureCountTotal = 0;
    	
    	/* register our first timer. */
        timer = new Timer();
        timer.schedule(new PulseTask(), intervalMillis);
        
        System.out.println("initialized with agent=\"" + agentName + "\", interval=" + intervalMillis + ", server=\"" + serverUrl + "\"");
    }
    
    /*
    */
    protected boolean sendPulse()
    {
    	try
    	{
    		/* build the query string. */
    		String encoder = "UTF-8";
    		String query = "?client=" + URLEncoder.encode(agentName, encoder) + "&interval=" + (intervalMillis / 1000);
    	
    		/* synchronously open the connection. */
    		System.out.println("connecting to \"" + serverUrl + query + "\"...");	
     		URL url = new URL(serverUrl + query);
        	URLConnection urlc = url.openConnection();
        	urlc.setAllowUserInteraction(false);

        	/* synchronously block waiting for a response. */
        	BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
        	String l = null;
        	while ((l = br.readLine()) != null) 
        	{
            	System.out.println(l);
        	}
        	br.close();
        }
        catch (Exception e)
        {
        	System.out.println("pulse failure, now with " + failureCount + "/" + failureCountTotal + " failures");
        	e.printStackTrace();
        	failureCount++;
        	failureCountTotal++;
        	return false;
        }
        
        /* if we got here, we are cool. */
        System.out.println("pulse successful");
        failureCount = 0;
        return true;
    }
    
    
    private String getComputerNameOld()
	{
		Map<String, String> env = System.getenv();
		if (env.containsKey("COMPUTERNAME"))
			return env.get("COMPUTERNAME");
		else if (env.containsKey("HOSTNAME"))
			return env.get("HOSTNAME");
		else
			return "Unknown Computer";
	}
	
	private String getComputerName()
	{
		String hostname = "Unknown";

		try
		{
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		}
		catch (UnknownHostException ex)
		{
			System.out.println("Hostname can not be resolved");
		}
		
		return hostname;
	}
    
}