function JpsListCtrl($scope, $routeParams, $http) {
	$http.get('jps/pid').success(function(data) {
		$scope.list = data;
	});
	$scope.sampling = function(pid, duration, interval) {
		alert(pid+" "+duration+" "+interval);
		$http.post('jps/pid/'+pid+"/sampling").success(function(data) {
			alert(data);
		});
	}
}
function JpsDetailCtrl($scope, $routeParams, $http, $route) {
	$http.get('jps/pid/'+$routeParams.pid).success(function(data) {
		$scope.detail = data;
	});
	$scope.refresh = function() {
		$route.reload();
	}
}