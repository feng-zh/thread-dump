angular.module('jps', []).
  config(['$routeProvider', function($routeProvider) {
  $routeProvider.
      when('/', {templateUrl: 'partials/jps-pid.html',   controller: JpsListCtrl}).
      when('/:pid', {templateUrl: 'partials/jps-pid-detail.html', controller: JpsDetailCtrl}).
      otherwise({redirectTo: '/'});
}]);