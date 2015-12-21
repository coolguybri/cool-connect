
/* create an unnamed function (to hide all data as private), and then invoke it. */
(function() {

	/* constants. */
	var TABLE_ELEMENT = 'outages-timeline';
	
	/* first thing: load up the graph library. */
	console.log("viz: loading lib...");
	google.load("visualization", "1.1", {packages:["timeline"]});
	google.setOnLoadCallback(graphLibraryLoaded);
				
	/* called when the google-graph library is loaded. */
	function graphLibraryLoaded() {
		console.log("viz: load complete");
	};
	
	/* this is called to allocate the meta-data and structure of the data table. */
	function initDataTable(curr_week_begin) {
		var dataTable = new google.visualization.DataTable();
		
		/* label all the columns. */
		dataTable.addColumn({ type: 'string', id: 'client' });
		dataTable.addColumn({ type: 'string', id: 'outage' });
		//dataTable.addColumn({ type: 'string', id: 'tooltip' });
		dataTable.addColumn({ type: 'date', id: 'start' });
		dataTable.addColumn({ type: 'date', id: 'end' });
		
		/* Add the day-of-the-week stuff; these are 7 artificial cells used to label the days, and also to give us custom control
			on how big the graph is (the x axis). */
		var daysOfWeek = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
		var dateIter = new Date(curr_week_begin);
		for (var i = 0 ; i < 7 ; i++) {
			var startDate = dateIter;
			var endDate = new Date(startDate);
			endDate.setDate(endDate.getDate() + 1);
			console.log("adding meta-data: (" + daysOfWeek[i] + ") [" + startDate + "], [" + endDate + "]");
			dataTable.addRow([ 'outages', daysOfWeek[i], /*null,*/ startDate, endDate ]);
			dateIter = endDate;
		}
    	 
		return dataTable;
	};
	
	/* */
	function drawDataTable(graphElemName, dataTable) {
		console.log("viz: drawDataTable for \"" + graphElemName + "\", with " + 
			dataTable.getNumberOfColumns() + " cols, and " + dataTable.getNumberOfRows() + " rows...");
		var container = document.getElementById(graphElemName);
		var chart = new google.visualization.Timeline(container);
		
  		 var options = {
    		timeline: { colorByRowLabel: true, singleColor: '#fc6e0a' }
  		};
  
		chart.draw(dataTable, options);
		console.log("viz: drawDataTable complete");
	};
	
	/* */
	function eraseDataTable(graphElemName, displayText) {
		console.log("viz: eraseDataTable for \"" + graphElemName + "\"");
		var container = document.getElementById(graphElemName);
		var fc = container.firstChild;
		while (fc) {
    		container.removeChild(fc);
    		fc = container.firstChild;
		}
		
		if (displayText) {
			container.innerHTML = displayText;
		}
		
		console.log("viz: eraseDataTable complete");
	};
	
	/* */
	function getWeekLabel(currWeek) {
		if (currWeek == 0) {
			weekName = 'this week';
		} else if (currWeek == -1) {
			weekName = 'last week';
		} else if (currWeek == -1) {
			weekName = 'week before last';
		} else if (currWeek == -2) {
			weekName = 'like, 3 weeks ago';
		} else {
			weekName = 'week ' + currWeek;
		}
		return weekName;
	}
	
	/* called on page load. */
	function updateGraph(graphElemName, outages) {
	
		/* if no outages, just clear the table. */
		if (outages.outages.length < 1) {
			eraseDataTable(graphElemName, "no data available");
			return;
		}
			
		/* star the calender with this week; go to sunday of this week. */
		var now = new Date();
  		var day = now.getDay(); // 0=sunday...6=saturday
      	var day_diff = now.getDate() - day;
  		var curr_week_begin = new Date(now);
  		curr_week_begin.setDate(day_diff);
  		curr_week_begin.setHours(0,0,0,0);
  		
  		/* build the initial data table, with all the columns and metadata defined. */
  		var dataTable = initDataTable(curr_week_begin);

		/* now add the appropriate rows. */
		var currWeek = 0;
		var maxWeek = -3;
		var weekName = getWeekLabel(currWeek);
		for (var currOutage = 0 ; (currOutage < outages.outages.length) ; currOutage++) {
		
			var startDate = new Date(outages.outages[currOutage].startTime);
			var endDate = new Date(outages.outages[currOutage].endTime);
			console.log("processing outage on [" + startDate + "][" + endDate + "] with start-week=[" + curr_week_begin + "]");
			
			/* make sure we are drawing in the correct week. */
			while ((currWeek >= maxWeek) && (startDate < curr_week_begin)) {
				/* update the beginning of the week. */
				currWeek--;
				weekName = getWeekLabel(currWeek);
				curr_week_begin.setDate(curr_week_begin.getDate() - 7);
				console.log("processing outage " + currOutage + ", hit new week-begin (" + currWeek + ")(" + weekName + ") of " + curr_week_begin);
			}
			if (currWeek < maxWeek) {
				/* if we got to the end of the max-weeks, just break out and stop adding data. */
				console.log("hit max week!");
				break;
			}
			
			/* special case: if the endDate goes outside of the week, truncate it. A better approach is to take the outage and currently split it across the 
				current week-set. */
			var endOfWeek = new Date(curr_week_begin);
			endOfWeek.setDate(endOfWeek.getDate() + 7);
			if (endDate > endOfWeek)
				endDate = endOfWeek;
			
			/* manipulate the date - everything is delta of the current week. */
			var dayDiff = (7 * currWeek)
			startDate.setDate(startDate.getDate() - dayDiff);
			endDate.setDate(endDate.getDate() - dayDiff);
			
			/* just add it in. */
			var label = startDate.getHours() + ':' + startDate.getMinutes();
			var tooltip = "tooltip";
			console.log("adding data for label=" + label + ": [" + startDate + "][" + endDate + "]");
			dataTable.addRow([ weekName, label, /*tooltip,*/ startDate, endDate ]);
		}
		
		/* render the last week. */
		drawDataTable(graphElemName, dataTable);
	};
			  
			  
	/* Define the module, give it a name, and define its dependencies (currently null list) */
	var coolApp = angular.module('cool-monitor', [ ]);
	
	/* create the client service, which retrieves the list of available clients. */
	coolApp.factory('clientService', ['$http', function($http) {
		return function() {
			console.log("calling clientService...");
    		return $http({ method: 'GET', url: '/api/clients', });
     	};
    }]);
    
    /* create the outage service, which retrieves the outages for a specific client. */
	coolApp.factory('outageService', ['$http', function($http) {
		return function(clientName) {
			console.log("calling outageService for " + clientName + "...");
    		return $http({ method: 'GET', url: '/api/outages', params: { 'client': clientName } });
     	};
    }]); 
    
	/* create our clients controller. */
	coolApp.controller('ClientsController', ['$scope', '$rootScope', 'clientService', 'outageService', 
								function($scope, $rootScope, clientService, outageService) {
		
		/* init to empty list. */
		console.log('clientcontroller initializing...');	
		var clientList = this;
		clientList.clients = {};
		
		/* make initial fetch to get the list of clients. */
    	clientService().then(function(dataResponse) {
    		console.log('client service returned');
    		console.log(dataResponse);
    		console.log(dataResponse.data.response.clients[0]);
        	clientList.clients = dataResponse.data.response.clients;
   		});
   		
   		/* called when the client name is clicked on. */ 
   		this.updateOutages = function(clientName) {
   			console.log("updateOutages called for " + clientName + "...");
   			outageService(clientName).then(function(dataResponse) {
				console.log('outages service returned');
				console.log(dataResponse);
				console.log(dataResponse.data);
				console.log(dataResponse.data.response);
				var outages = dataResponse.data.response.outages;
				updateGraph(TABLE_ELEMENT, dataResponse.data.response);
			});
   		};
	}]);
	
	
	/* create our outages controller. */
	coolApp.controller('OutagesController', function($scope, outageService) {
		/* init to empty list. 
		var outagesList = this;
		outagesList.outages = {};
		outagesList.clientName = "bconnolly-macbookpro.roam.corp.google.com";
	
		outageService.getData(outagesList.clientName).then(function(dataResponse) {
			console.log('outages service returned');
			console.log(dataResponse);
			console.log(dataResponse.data);
			console.log(dataResponse.data.response);
			outagesList.outages = dataResponse.data.response;
			updateGraph('outages-timeline', outagesList.outages);
		}); */
	});


	/* create our panel controller */
	coolApp.controller('PanelController', function() {
		this.tab = 1;
		this.selectTab = function(setTab) {
			this.tab = setTab;
		};
		this.isSelected = function(checkTab) {
			return this.tab === checkTab;
		};
	});
	
})();