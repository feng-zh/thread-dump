angular.module('jpsServices', ['ngResource']).
	factory('JpsList', function($resource){
		return $resource('jps/pid');
	}).
	factory('Jps', function($resource){
		return $resource('jps/pid/:pid', {pid: '@pid'});
	});