'use strict'

angular.module('skyfleet.controllers')
  .controller('orderNewEllipseController',
    function ($scope, $rootScope, $state, bikeFleetFactory, sessionFactory, rootScopeFactory) {
      $scope.ellipseCount = 0
      $scope.bikeCount = 0

      $scope.placeOrder = function () {
        if ($scope.bikeCount) {
          rootScopeFactory.setData('bikesToAdd', $scope.bikeCount)
          $state.go('add-bikes')
        }
      }
    })
