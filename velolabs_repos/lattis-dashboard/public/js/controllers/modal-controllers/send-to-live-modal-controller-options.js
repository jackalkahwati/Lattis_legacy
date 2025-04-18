'use strict'

angular.module('skyfleet.controllers').controller(
  'liveOptionsDialogController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog, notify, $q, $timeout, lattisErrors) {
    $scope.bikeStatus = 'parked'
    $scope.closeModal = function () {
      ngDialog.closeAll()
    }

    $scope.sendToLive = function () {
      if (rootScopeFactory.getData('selectedBikes')) {
        if ($scope.bikeStatus === 'collect') {
            bikeFleetFactory.toActiveFleet({
              operator_id: sessionFactory.getCookieId(),
              bikes: rootScopeFactory.getData('selectedBikes'),
              current_status: $scope.bikeStatus,
            }, function (response) {
              if (response && response.status === 200) {
                notify({message: 'Bikes sent to Live successfully', duration: 2000, position: 'right'})
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
                notify({message: 'Failed to send bikes to Live', duration: 2000, position: 'right'})
                rootScopeFactory.setData('selectedBikes', null)
                ngDialog.closeAll()
              }
            })
        }
      }
    }
  })
