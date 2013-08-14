function JpsListCtrl($scope, $routeParams, JpsList, $http) {
	$scope.list = JpsList.query();
	$scope.sampling = function(pid, duration, interval) {
		alert(pid + " " + duration + " " + interval);
		$http.post('jps/pid/' + pid + "/sampling").success(function(data) {
			alert(data);
		});
	}
}
function JpsDetailCtrl($scope, $routeParams, Jps, $route) {
	$scope.detail = Jps.get({
		pid : $routeParams.pid
	})
	$scope.refresh = function() {
		$scope.detail = Jps.get({
			pid : $routeParams.pid
		})
	}
}