'use strict'

angular.module('skyfleet')
  .directive('fleetFooter', function () {
    return {
      restrict: 'E',
      scope: {
        date: '='
      },
      templateUrl: '../../html/templates/footer.html',
      link: function(scope) {
        scope.getDate = function () {
          var date = new Date ();
          return date.getFullYear();
        };
      }
    };
  });
