'use strict'

angular.module('skyfleet.controllers').controller(
  'serviceDialogController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog, notify, $q, $timeout, lattisErrors) {
    $scope.serviceStatus = 'ticket'
    $scope.closeModal = function () {
      ngDialog.closeAll()
    }

    $scope.sendToService = function () {
      if (rootScopeFactory.getData('selectedBikes')) {
        if ($scope.serviceStatus === 'transport') {
            bikeFleetFactory.sendToStaging({
              operator_id: sessionFactory.getCookieId(),
              bikes: rootScopeFactory.getData('selectedBikes'),
              current_status: $scope.serviceStatus,
              status: 'suspended'
            }, function (response) {
              if (response && response.status === 200) {
                notify({message: 'Bikes sent to Service successfully', duration: 2000, position: 'right'})
                rootScopeFactory.setData('selectedBikes', null)
                $timeout(function () {
                  $rootScope.$broadcast('clearBikeDataCache')
                  $rootScope.$emit('clearBikeDataCache')
                }, 30)
                ngDialog.closeAll()
              } else if (response && response.code === lattisErrors.Unauthorized) {
                notify({message: 'Bike has active booking', duration: 2000, position: 'right'})
                rootScopeFactory.setData('selectedBikes', null)
                ngDialog.closeAll()
              } else {
                notify({message: 'Failed to send bikes to Service', duration: 2000, position: 'right'})
                rootScopeFactory.setData('selectedBikes', null)
                ngDialog.closeAll()
              }
            })
        }
      }
    }
  })
