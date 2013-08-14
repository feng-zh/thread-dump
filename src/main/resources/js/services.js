angular.module('jpsServices', ['ngResource']).
	factory('JpsList', function($resource){
		return $resource('jps/pid');
	}).
	factory('Jps', function($resource){
		return $resource('jps/pid/:pid', {pid: '@pid'});
	}).
	factory('Sampling', function($resource){
		return $resource('jps/pid/:pid/sampling', {pid: '@pid'}, {
			save: {method:'POST'}
		});
	});