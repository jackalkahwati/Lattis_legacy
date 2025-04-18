'use strict'

angular.module('skyfleet.controllers').controller(
  'deleteEquipmentModalController',
  function ($scope, profileSettingFactory, sessionFactory, $q, $rootScope, rootScopeFactory, usersFactory, ngDialog) {

    $scope.changesSave = function () {
      $rootScope.$emit('deleteEquipment', $scope.ngDialogData.controller)
      ngDialog.closeAll()
    }
    $scope.changesCancel = function () {
      ngDialog.closeAll()
    }
  })

