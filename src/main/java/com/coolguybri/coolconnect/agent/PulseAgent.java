package com.coolguybri.coolconnect.agent;

import java.util.Timer;
import java.util.TimerTask;
import java.net.URL;
import java.net.URLConnection;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/*
*/
public class PulseAgent 
{
	/* members  */
	Timer 	timer;
	int		intervalMillis;
	int		failureCount;
	int		failureCountTotal;
	
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
        PulseAgent agent = new PulseAgent();  
    }
    
    /*
    */
    public PulseAgent()
    {
    	failureCount = 0;
    	failureCountTotal = 0;
    	intervalMillis = 5000; /* 60000 */
    	System.out.println("initializing with interval=" + intervalMillis);
    	
    	/* register our first timer. */
        timer = new Timer();
        timer.schedule(new PulseTask(), intervalMillis);
    }
    
    /*
    */
    public boolean sendPulse()
    {
    	try
    	{
     		URL url = new URL("http://127.0.0.1:8080/pulse?client=mxyzptlk&interval=60");
        	String query = "client=mxyzptlk&interval=60";
        	
        	//use post mode
        	URLConnection urlc = url.openConnection();
        	//urlc.setDoOutput(true);
        	urlc.setAllowUserInteraction(false);

        	/* send query
        	PrintStream ps = new PrintStream(urlc.getOutputStream());
        	ps.print(query);
        	ps.close(); */

        	//get result
        	BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
        	String l = null;
        	while ((l=br.readLine())!=null) 
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
}