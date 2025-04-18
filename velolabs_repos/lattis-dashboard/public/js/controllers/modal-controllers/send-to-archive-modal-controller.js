'use strict'

angular.module('skyfleet.controllers').controller(
  'archiveDialogController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog, notify, $q, $timeout, lattisErrors) {
    $scope.archiveStatus = 'stolen'

    $scope.closeModal = function () {
      ngDialog.closeAll()
    }

    $scope.sendToArchive = function () {
      if (rootScopeFactory.getData('selectedBikes')) {
        bikeFleetFactory.sendToArchive({
          operator_id: sessionFactory.getCookieId(),
          bikes: rootScopeFactory.getData('selectedBikes'),
          status: $scope.archiveStatus
        }, function (response) {
          if (response && response.status === 200) {
            notify({message: 'Bikes sent to Archive successfully', duration: 2000, position: 'right'})
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
            notify({message: 'Failed to send bikes to Archive', duration: 2000, position: 'right'})
            rootScopeFactory.setData('selectedBikes', null)
            ngDialog.closeAll()
          }
        })
      }
    }
  })
