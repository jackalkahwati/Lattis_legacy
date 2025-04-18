'use strict'

angular.module('skyfleet')
  .directive('fleetHeader', function () {
    return {
      restrict: 'E',
      scope: false,
      templateUrl: '../../html/templates/header.html'
    }
  })
