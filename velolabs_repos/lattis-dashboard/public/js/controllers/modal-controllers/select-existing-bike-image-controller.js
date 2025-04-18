'use strict'

angular.module('skyfleet.controllers').controller(
  'selectExistingBikeImageController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog, notify, $q, $timeout, _, myFleetFactory) {
    $q.all([bikeFleetFactory.getBikeDescriptionAndImage({fleet_id: rootScopeFactory.getData('fleetId')})]).then(function (response) {
      if (response[0].payload) {
        $scope.bikePhotoList = angular.copy(response[0].payload.bike_images)
        let photoLength = $scope.bikePhotoList.length
        for (let i = 0; i < photoLength; i++) {
          $scope.bikePhotoList[i].date = utilsFactory.dateStringToDate($scope.bikePhotoList[i].date)
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
