'use strict'

angular.module('skyfleet.controllers').controller(
  'changeFleetTypeModalController',
  function ($scope, profileSettingFactory, sessionFactory, $q, $rootScope, rootScopeFactory, usersFactory, ngDialog) {

    $scope.changesSave = function () {
      $rootScope.$emit('changeFleetType')
      ngDialog.closeAll()
    }
    $scope.changesCancel = function () {
      ngDialog.closeAll()
    }
  })
