angular.module('threads', ['threadsServices']).
  config(['$routeProvider', function($routeProvider) {
  $routeProvider.
      when('/', {templateUrl: 'partials/default.html'}).
      when('/jps', {templateUrl: 'partials/jps-pid.html',   controller: JpsListCtrl}).
      when('/jps/:pid', {templateUrl: 'partials/jps-pid-detail.html', controller: JpsDetailCtrl}).
      otherwise({redirectTo: '/'});
}]);
