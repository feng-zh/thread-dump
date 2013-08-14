function JpsListCtrl($scope, $routeParams, JpsList, Sampling) {
	$scope.list = JpsList.query();
	$scope.sampling = {
		duration : 1,
		durationUnit : "minute",
		interval : 50,
		intervalUnit : "millisec"
	};
	$scope.doSampling = function() {
		Sampling.save({
			pid : $scope.sampling.pid
		}, {
			duration : $scope.sampling.duration,
			durationUnit : $scope.sampling.durationUnit,
			interval : $scope.sampling.interval,
			intervalUnit : $scope.sampling.intervalUnit
		})
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