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
	$scope.remoteSampling = {done:true};
	$scope.sampling = $scope.remoteSampling;
	$scope.doSampling = function() {
		$scope.dump = null;
		$scope.pause = false;
		$resource('samplers/:id/sampling', {id: $scope.agent.agentId}).save({
			duration : $scope.samplingForm.duration,
			durationUnit : $scope.samplingForm.durationUnit,
			interval : $scope.samplingForm.interval,
			intervalUnit : $scope.samplingForm.intervalUnit
		});
		$scope.stopSampling = function() {
			$resource('samplers/:id/sampling', {id: $scope.agent.agentId}).remove();
			$scope.remoteSampling = $resource('samplers/:id/sampling', {id: $scope.agent.agentId}).get();
			$scope.sampling = $scope.remoteSampling;
		}
		var source = new EventSource('samplers/'+$scope.agent.agentId+'/sampling-monitor');
		var handleCallback = function (msg) {
            $scope.$apply(function () {
                $scope.remoteSampling = angular.fromJson(msg.data);
                if (!$scope.pause || $scope.remoteSampling.done) {
                	$scope.sampling = $scope.remoteSampling;
                }
            });
        }
        source.addEventListener('message', handleCallback, false);
	}
}
