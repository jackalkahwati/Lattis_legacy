'use strict'

angular.module('skyfleet.controllers').controller(
  'liveDialogController',
  function ($scope, bikeFleetFactory, sessionFactory, myFleetFactory, $rootScope, rootScopeFactory, utilsFactory, mapFactory, tripAnimationFactory, ngDialog, notify, $q) {
    $scope.bikeStatus = 'parked'

    $scope.closeModal = function () {
      ngDialog.closeAll()
    }

    $rootScope.$on('ngDialog.opened', function (e, $dialog) {
      $q.when(tripAnimationFactory.getAllTrips({bike_id: rootScopeFactory.getData('selectedBikes')[0]}))
        .then((res) => {
          const trips = res.payload.trips
          const trip = _.sortBy(trips, 'trip_id')[trips.length - 1]
          let endridemap = new mapboxgl.Map({
            container: 'endridemap',
            style: 'https://s3-us-west-2.amazonaws.com/mapbox.sprite/light-v9.json',
            center: [-97.5122717, 40.828412],
            zoom: 3
          })
          endridemap.on('load', function () {
            endridemap.addLayer({
              'id': 'points',
              'type': 'symbol',
              'source': {
                'type': 'geojson',
                'data': turf.point(utilsFactory.lastPointInStepsArray(trip.steps))
              },
              'layout': {
                'icon-image': 'redpin'
              }
            })
          })
          mapFactory.mapFitBounds(endridemap, turf.point(utilsFactory.lastPointInStepsArray(trip.steps)), 'low')
        })
    });

    if (rootScopeFactory.getData('selectedBikes') &&
      rootScopeFactory.getData('selectedBikes').length === 1) {
      myFleetFactory.getAllTickets({ fleet_id: rootScopeFactory.getData('fleetId') }).then(function (response) {
        var filteredTickets = _.where(response.payload, { bike_id: parseInt(rootScopeFactory.getData('selectedBikes')[0]) })
        $scope.activeTicket = filteredTickets[0]
      });
    }

    $scope.sendToLive = function () {
      if (rootScopeFactory.getData('selectedBikes')) {
        bikeFleetFactory.toActiveFleet({
          operator_id: sessionFactory.getCookieId(),
          bikes: rootScopeFactory.getData('selectedBikes'),
          current_status: $scope.bikeStatus
        }, function (response) {
          if (response && response.status === 200) {
            notify({ message: 'Bikes returned to Live successfully', duration: 2000, position: 'right' })
            $rootScope.$broadcast('clearBikeDataCache')
            $rootScope.$emit('clearBikeDataCache')
            $rootScope.$emit('disableBikeOptions')
            $rootScope.$broadcast('disableBikeOptions')
            rootScopeFactory.setData('selectedBikes', null)
            ngDialog.closeAll()
          } else {
            notify({ message: 'Failed to send bikes to Live', duration: 2000, position: 'right' })
            rootScopeFactory.setData('selectedBikes', null)
            ngDialog.closeAll()
          }
        })
      }
    }
  })
