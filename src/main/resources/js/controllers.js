function JpsListCtrl($scope, $routeParams, $resource) {
	$scope.list = $resource('jps/pid').query();
}

function RemoteListCtrl($scope, $routeParams, $resource) {
	$scope.remoteType = $routeParams.remoteType;
	$scope.refresh =function() {
		$scope.list = $resource("remote/url").query();
	};
	$scope.refresh();
	$scope.addForm = {
		remoteType: $scope.remoteType,
		url: "",
		user: "",
		password: ""
	};
	$scope.add = function(){
		$resource("remote/url").save($scope.addForm);
		$scope.list = $resource("remote/url").query();
	};
	$scope.remove = function(agentId){
		$resource("remote/url/:id", {id: agentId}).remove();
		$scope.list = $resource("remote/url").query();
	};
}

function DetailCtrl($scope, $routeParams, $resource) {
	$scope.type = $routeParams.type;
	$scope.id = $routeParams.id;
	if ($scope.type=='JPS') {
		$scope.jpsDetail = $resource('jps/pid/:id', {id: $scope.id}).get(function(data){
			$scope.name = "PID "+data.pid;
		});
		$scope.connect = function() {
			$scope.agent = $resource('jps/pid/:id/sampler', {id: $scope.id}).save();
		};
	} else {
		$scope.remoteDetail = $resource('remote/url/:id', {id: $scope.id}).get(function(data){
			$scope.name = data.url;
		});
		$scope.connect = function() {
			$scope.agent = $resource('remote/url/:id/sampler', {id: $scope.id}).save();
		};
	}
	$scope.status = 1; // ready
	$scope.dumpThread = function() {
		$scope.status = 1; // ready
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
	$scope.remoteSampling = {done:true, estimatedProgress:0};
	$scope.sampling = $scope.remoteSampling;
	$scope.doSampling = function() {
		$scope.status = 2; // before sampling
		$scope.dump = null;
		$scope.pause = false;
		$resource('samplers/:id/sampling', {id: $scope.agent.agentId}).save({
			duration : $scope.samplingForm.duration,
			durationUnit : $scope.samplingForm.durationUnit,
			interval : $scope.samplingForm.interval,
			intervalUnit : $scope.samplingForm.intervalUnit
		});
		$scope.stopSampling = function() {
			$scope.status = 3; // end sampling
			$resource('samplers/:id/sampling', {id: $scope.agent.agentId}).remove();
			$scope.remoteSampling = $resource('samplers/:id/sampling', {id: $scope.agent.agentId}).get();
			$scope.sampling = $scope.remoteSampling;
		}
		var source = new EventSource('samplers/'+$scope.agent.agentId+'/sampling-monitor');
		var handleCallback = function (msg) {
            $scope.$apply(function () {
            	if ($scope.status == 2) {
            		$scope.status = 4; // get sampling
            	}
                $scope.remoteSampling = angular.fromJson(msg.data);
                if (!$scope.pause){
                	$scope.sampling = $scope.remoteSampling;
                }
                if ($scope.remoteSampling.done) {
                	$scope.status = 3; // end sampling
                	$scope.sampling = $scope.remoteSampling;
                }
            });
        }
        source.addEventListener('message', handleCallback, false);
	}
}
