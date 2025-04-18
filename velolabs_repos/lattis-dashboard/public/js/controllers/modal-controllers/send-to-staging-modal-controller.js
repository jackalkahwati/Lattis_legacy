'use strict'

angular.module('skyfleet.controllers').controller(
  'stagingDialogController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog, notify, $q, $timeout, lattisErrors) {
    $scope.stagingStatus = 'lock_assigned'
    $scope.closeModal = function () {
      ngDialog.closeAll()
    }

    $scope.sendToStaging = function () {
      if (rootScopeFactory.getData('selectedBikes')) {
        bikeFleetFactory.sendToStaging({
          operator_id: sessionFactory.getCookieId(),
          bikes: rootScopeFactory.getData('selectedBikes'),
          current_status: $scope.stagingStatus
        }, function (response) {
          if (response && response.status === 200) {
            notify({message: 'Bikes sent to Staging successfully', duration: 2000, position: 'right'})
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
            notify({message: 'Failed to send bikes to Staging', duration: 2000, position: 'right'})
            rootScopeFactory.setData('selectedBikes', null)
            ngDialog.closeAll()
          }
        })
      }
    }
  })
