function JpsListCtrl($scope, $routeParams, Jps) {
	$scope.list = Jps.query();
}
var ggg;
function JpsDetailCtrl($scope, $routeParams, Jps, JpsSampler, $resource) {
	$scope.detail = Jps.get({
		pid : $routeParams.pid
	});
	$scope.connect = function() {
		$scope.agent = JpsSampler.save({
			pid : $routeParams.pid
		});
	};
	$scope.dumpThread = function() {
		$scope.dump = $resource("samplers/:id/dumpStack", {
			id : $scope.agent.agentId
		}).get();
	};
	$scope.samplingForm = {
		duration : 5,
		durationUnit : "second",
		interval : 50,
		intervalUnit : "millisec"
	};
	$scope.doSampling = function() {
		$scope.dump = null;
		$resource('samplers/:id/sampling', {id: $scope.agent.agentId}).save({
			duration : $scope.samplingForm.duration,
			durationUnit : $scope.samplingForm.durationUnit,
			interval : $scope.samplingForm.interval,
			intervalUnit : $scope.samplingForm.intervalUnit
		});
		var handleCallback = function (msg) {
            $scope.$apply(function () {
                $scope.sampling = angular.fromJson(msg.data);
            });
        }
		var source = new EventSource('samplers/'+$scope.agent.agentId+'/sampling-monitor');
        source.addEventListener('message', handleCallback, false);
	}
}
