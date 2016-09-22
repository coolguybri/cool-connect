# cool-connect

cool-connect has three components:

(1) a Java-based server that runs on Google App Engine (https://cool-connect.appspot.com/). 

(2) a Javascript frontend, that is bundled with the server and downloaded to the webbrowser when they connect.

(3) a Java-based agent to be deployed inside a home network.

Once the server is deployed, the agents are run on one or more machines running on the home network. These agents will send pulses to the server every interval. The server will figure out when pulses are missing, and record in its database all the outage periods. The javascript front-end will display the outages for the last few weeks.
