'use strict'

angular.module('skyfleet.controllers').controller(
  'selectExistingBikeDescriptionController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog, notify, $q, $timeout, _, myFleetFactory) {
    $q.all([bikeFleetFactory.getBikeDescriptionAndImage({fleet_id: rootScopeFactory.getData('fleetId')})]).then(function (response) {
      if (response[0].payload) {
        $scope.bikeDescriptionList = angular.copy(response[0].payload.bike_descriptions)
        let descLength = $scope.bikeDescriptionList.length
        for (let i = 0; i < descLength; i++) {
          $scope.bikeDescriptionList[i].date = utilsFactory.dateStringToDate($scope.bikeDescriptionList[i].date)
        }
      }
    })

    /* TODO fetch data from checkbox */
    $scope.selectPhoto = function () {
      $rootScope.$broadcast('photoSelected', $scope.dataInfo.selectedphoto)
      $rootScope.$emit('photoSelected', $scope.dataInfo.selectedphoto)
      ngDialog.closeAll()
    }

    $scope.closeModal = function () {
      ngDialog.closeAll()
    }
  })
