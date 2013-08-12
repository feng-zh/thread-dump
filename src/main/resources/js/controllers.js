function JpsListCtrl($scope, $routeParams, $http) {
	$http.get('jps/pid').success(function(data) {
		$scope.list = data;
	});
}
function JpsDetailCtrl($scope, $routeParams, $http) {
	$http.get('jps/pid/'+$routeParams.pid).success(function(data) {
		$scope.detail = data;
	});
}