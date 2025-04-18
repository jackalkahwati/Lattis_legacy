'use strict'

angular.module('skyfleet.controllers').controller(
  'selectImageController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog) {
    $scope.uploadLogo = function () {
      $rootScope.$broadcast('logoSaved')
      $rootScope.$emit('logoSaved')
      ngDialog.closeAll()
    }

    $scope.saveFleetLogo = function () {
      $rootScope.$broadcast('saveFleetLogo')
      ngDialog.closeAll()
    }

    $scope.saveBikeImage = () => {
      $rootScope.$broadcast('saveBikeImage')
      ngDialog.closeAll()
    }

    $scope.cancelLogo = function () {
      $rootScope.$broadcast('clearUpload')
      $rootScope.$emit('clearUpload')
      ngDialog.closeAll()
    }
  })
