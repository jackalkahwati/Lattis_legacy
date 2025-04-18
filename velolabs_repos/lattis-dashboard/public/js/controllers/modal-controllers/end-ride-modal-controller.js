'use strict'

angular.module('skyfleet.controllers').controller(
  'endRideModalController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog, notify, $q, $timeout, _, myFleetFactory, tripAnimationFactory) {
    $scope.endingTrip = false
    $scope.endToLive = function () {
      $scope.endingTrip = true
      endCurrentTrip('live')
    }

    $scope.endToService = function () {
      $scope.endingTrip = true
      endCurrentTrip('service')
    }

    $scope.endAndCharge = function() {
      $scope.endingTrip = true
      endCurrentTrip('live', true)
    }

    function endCurrentTrip (endType, charge) {
      $q.when(tripAnimationFactory.getAllTrips({bike_id: rootScopeFactory.getData('selectedBikes')[0]}))
        .then((res) => {
          const trip = _.find(res.payload.trips, trip => trip.date_endtrip === null)
          bikeFleetFactory.endRide({
            trip_id: trip.trip_id,
            to: endType,
            fleet_id: rootScopeFactory.getData('fleetId'),
            charge,
            acl: localStorage.getItem('acl')
          }, function (res) {
            $scope.endingTrip = false
            if (res && res.status === 200) {
              bikeFleetFactory.clearBikeDataCache()
              myFleetFactory.clearTicketsCache()
              $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
              $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
              notify({message: 'Ended the trip successfully', duration: 2000, position: 'right'})
            } else if (res && res.status === 403) {
              notify({
                message: 'Cannot end the trip since it is a payment fleet',
                duration: 2000,
                position: 'right'
              })
            } else {
              notify({
                message: 'Failed to end the trip',
                duration: 2000,
                position: 'right'
              })
            }
            ngDialog.closeAll()
          })
        })
    }
  })
