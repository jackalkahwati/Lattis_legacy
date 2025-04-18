'use strict'

angular.module('skyfleet.controllers').controller(
  'damageReportModalController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog, notify, $q, $timeout, _, myFleetFactory) {
    $scope.bikesIdList = []

    let damageData = rootScopeFactory.getData('damageId')
    $scope.damageData = damageData
    $scope.damageData[0].damageNotes = damageData[0].operator_notes ||
                damageData[0].maintenance_notes || damageData[0].rider_notes
    $scope.damageData[0].damagePhoto = damageData[0].operator_photo ||
                damageData[0].user_photo

    $scope.closeModal = function () {
      ngDialog.closeAll()
    }
  })
