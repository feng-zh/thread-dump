angular.module('jps', []).
  config(['$routeProvider', function($routeProvider) {
  $routeProvider.
      when('/jps/pid', {templateUrl: 'partials/jps-pid.html',   controller: JpsListCtrl}).
      when('/jps/pid/:pid', {templateUrl: 'partials/jps-pid-detail.html', controller: JpsDetailCtrl}).
      otherwise({redirectTo: '/error'});
}]);