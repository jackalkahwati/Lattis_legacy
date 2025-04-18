'use strict'

angular.module('skyfleet.controllers')
  .controller('bikeLiveStatusController',
    function (rootScopeFactory, $scope, $timeout, $state, _, mapFactory, utilsFactory) {
      let map = mapFactory.initMap([-97.5122717, 40.828412], 3)
      map.dragRotate.disable()
      map.on('load', function () {
        if ($scope.bikeData.current_status !== 'on_trip') {
          mapFactory.mapFitBounds(map, $scope.geometry, 'low')
          map.addLayer({
            'id': 'parkedBike',
            'type': 'symbol',
            'source': {
              data: $scope.geometry,
              type: 'geojson'
            },
            'layout': {
              'icon-image': 'redpin',
              'icon-size': 1
            }
          })
        } else {
          mapFactory.mapFitBounds(map, $scope.geometry, 'low')
          map.addLayer({
            'id': 'trip',
            'type': 'line',
            'source': {
              'type': 'geojson',
              'data': $scope.geometry
            },
            'interactive': true,
            'layout': {
              'line-join': 'round',
              'line-cap': 'round'
            },
            'paint': {
              'line-color': '#fab',
              'line-width': 4
            }
          })
          map.addLayer({
            'id': 'points',
            'type': 'symbol',
            'source': {
              'type': 'geojson',
              'data': turf.featureCollection([turf.point([$scope.tripData.steps[0][1], $scope.tripData.steps[0][0]], {icon: 'redpin'}),
                turf.point([$scope.tripData.steps[$scope.tripData.steps.length - 1][1], $scope.tripData.steps[$scope.tripData.steps.length - 1][0]], {icon: 'current'})])
            },
            'layout': {
              'icon-image': '{icon}',
              'icon-size': 1,
              'icon-offset': [0, -12]
            }
          })
        }
        $timeout(function () {
          $scope.$apply(function () {
            $scope.showLoader = false
          })
        }, 1000)
      })
      var dom = document.getElementById('map')
      dom.addEventListener('wheel', function (e) {
        e.stopPropagation()
      }, false)
    })
