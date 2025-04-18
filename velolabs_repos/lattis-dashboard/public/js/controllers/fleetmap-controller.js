'use strict'

angular.module('skyfleet.controllers')
  .controller('fleetmapController', [
    '$scope',
    '$rootScope',
    '$state',
    function ($scope, $rootScope, $state) {
      $scope.fleetMaps = [{
        title: 'BIKE LOCATOR',
        url: 'bikelocator'
      }, {
        title: 'PARKING',
        url: 'parkinghubs'
      }]
      $scope.currenttab = 'bikelocator'
    }
  ])

/* Type of code fleetTerritory locate in a map section  */
  .controller('fleetTerritoryController', [
    '$scope',
    '$rootScope',
    '$state',
    'mapFactory',
    'myFleetFactory',
    'rootScopeFactory',
    function ($scope, $rootScope, $state, mapFactory, myFleetFactory, rootScopeFactory) {
      // MapInitialization
      var map = mapFactory.initMap([-97.5122717, 40.828412], 3)
      map.addControl(new mapboxgl.NavigationControl({
        position: 'top-right'
      }))
      map.on('load', function () {
        $scope.$on('fleetChange', function (event, id) {
          // getGeofenceData($scope.fleetData, id);
        })

        myFleetFactory.getFleetData().then(function (res) {
          $scope.fleetData = angular.copy(res)
          if (!$scope.currentGeofenceData) {
            // getGeofenceData($scope.fleetData, rootScopeFactory.getData('fleetId'));
          }
        })
      })

      function getGeofenceData (fleetData, fleetId) {
        $scope.currentFleetData = _.first(_.where(fleetData.fleet_data, {fleet_id: fleetId}))
        if (_.where(fleetData.geofence_circle, {fleet_id: fleetId}).length > 0) {
          $scope.currentFleetGeofence = _.first(_.where(fleetData.geofence_circle, {fleet_id: fleetId}))
          $scope.currentGeofenceData = _.first(_.where(fleetData.geofence_circle, {geofence_circle_id: $scope.currentFleetGeofence.geofence_circle_id}))
          $scope.circle = {
            longitude: $scope.currentGeofenceData.longitude,
            latitude: $scope.currentGeofenceData.latitude,
            radius: $scope.currentGeofenceData.radius / 1000
          }
          $scope.polygon = null
          $scope.rectangle = null
        } else {
          $scope.currentFleetGeofence = _.first(_.where(fleetData.geofence_polygon, {fleet_id: fleetId}))
          $scope.currentGeofenceData = _.first(_.where(fleetData.geofence_polygon, {geofence_polygon_id: $scope.currentFleetGeofence.geofence_polygon_id}))
          $scope.geoFence = []
          _.each(JSON.parse($scope.currentGeofenceData.steps), function (element) {
            $scope.geoFence.push([element.longitude, element.latitude])
          })
          if ($scope.geoFence[0][1] == $scope.geoFence[1][1] && $scope.geoFence[1][0] == $scope.geoFence[2][0]) {
            $scope.rectangle = $scope.geoFence
            $scope.polygon = null
            $scope.circle = null
          } else {
            $scope.polygon = $scope.geoFence
            $scope.rectangle = null
            $scope.circle = null
          }
        }
      }

      var geocoder = new MapboxGeocoder({
        accessToken: mapboxgl.accessToken,
        container: 'search'
      })
      map.addControl(geocoder)
      $scope.map = map
    }
  ])
