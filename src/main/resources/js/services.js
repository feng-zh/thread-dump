angular.module('threadsServices', ['ngResource']).
	factory('Jps', function($resource){
		return $resource('jps/pid/:pid', {pid: '@pid'});
	}).
	factory('JpsSampler', function($resource){
		return $resource('jps/pid/:pid/sampler', {pid: '@pid'});
	});
