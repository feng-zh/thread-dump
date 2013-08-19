angular.module('threads', ['ngResource']).
  config(['$routeProvider', function($routeProvider) {
  $routeProvider.
      when('/', {templateUrl: 'partials/default.html'}).
      when('/JPS', {templateUrl: 'partials/jps.html',   controller: JpsListCtrl}).
      when('/:remoteType', {templateUrl: 'partials/remote-jvm.html', controller: RemoteListCtrl}).
      when('/detail/:type/:id', {templateUrl: 'partials/detail.html', controller: DetailCtrl}).
      otherwise({redirectTo: '/'});
}]);
