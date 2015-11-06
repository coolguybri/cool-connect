
/* create an unnamed function (to hide all data as private), and then invoke it. */
(function() {

	/* Define the module, give it a name, and define its dependencies (currently null list) */
	var app = angular.module('cool-monitor', [ ]);
	
	/* create our store controller. */
	app.controller('StoreController', function(){
		this.products = gems;
	});
	
	/* create our panel controller */
	app.controller('PanelController', function() {
		this.tab = 1;
		this.selectTab = function(setTab) {
			this.tab = setTab;
		};
		this.isSelected = function(checkTab) {
			return this.tab === checkTab;
		};
	});
	
	var gems = [
		{
			name: 'Dodecahedron',
			price: 2.95,
			description: '...',
			canPurchase: true,
		},
		{
			name: 'Penta Gem',
			price: 5.95,
			description: '...',
			canPurchase: false,
		},
	];
	
})();