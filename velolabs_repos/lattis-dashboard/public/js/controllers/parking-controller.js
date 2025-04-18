'use strict'

angular.module('skyfleet.controllers')
  .controller('parkingHubsController',
    function ($scope, rootScopeFactory, $state, mapFactory, myFleetFactory, parkingFactory, _, $filter, utilsFactory, editParkingZoneFactory, $timeout, $rootScope, sessionFactory, notify, lattisConstants, $q, $window, hubsFactory) {
      // MapInitialization
      $rootScope.showLoader = true
      var map = mapFactory.initMap([-97.5122717, 40.828412], 3)
      map.dragRotate.disable()
      var nav = new mapboxgl.NavigationControl()
      map.addControl(nav, 'bottom-right')
      $scope.map = map
      $scope.zone_empty = false
      $scope.spot_empty = false
      $scope.public_spotempty = false
      var editingParkingSpot = false
      var editingParkingZone = false
      var viewingHub = false
      var tempData = {}
      var popupzone
      let geoRecovered
      var zone_icon = angular.element(document.querySelector('#zone')),
        spot_icon = angular.element(document.querySelector('#spot')),
        public_spot = angular.element(document.querySelector('#public'))

      var defaultCursor = map.getCanvas().style.cursor

      map.on('mousemove', changeCursor)

      function changeCursor(e) {
        var features = map.queryRenderedFeatures(e.point, {
          layers: ['publicParkingSpots', 'parkingSpots', 'zoneLayer', 'hubs']
        })
        if (!editingParkingZone) {
          if (features.length) {
            map.getCanvas().style.cursor = 'pointer'
          } else {
            map.getCanvas().style.cursor = defaultCursor
          }
        }
      }

      function addTempZone (data) {
        if (!map.getSource('tempZone')) {
          map.addSource('tempZone', {
            data: turf.featureCollection([data]),
            type: 'geojson'
          })
          map.addLayer({
            'id': 'tempZone',
            'type': 'fill',
            'source': 'tempZone',
            'layout': {},
            'paint': {
              'fill-outline-color': '#F65295',
              'fill-color': '#F65295',
              'fill-opacity': 0.18
            }
          })
        } else {
          map.getSource('tempZone').setData(turf.featureCollection([data]))
        }
      }

      function drawPublic () {
        $scope.PublicSpotsCollection = []
        _.each($scope.PublicParkingSpotsData, function (element) {
          if (element.type === 'public') {
            element.type = 'parking_racks'
            element['spot_type'] = 'public'
          } else {
            element['spot_type'] = 'normal'
          }
          $scope.PublicSpotsCollection.push(turf.point([element.longitude, element.latitude], {
            'type': utilsFactory.underscoreToHyphen(element.type),
            'capacity': element.capacity,
            'parking_spot_id': element.parking_spot_id,
            'inside_zone': 1,
            'spot_type': element.spot_type
          }))
        })
        $scope.publicSpotsFeatureCollection = turf.featureCollection($scope.PublicSpotsCollection)
        if (map.getSource('publicSpots')) {
          map.getSource('publicSpots').setData($scope.publicSpotsFeatureCollection)
        } else {
          map.addSource('publicSpots', {
            data: $scope.publicSpotsFeatureCollection,
            type: 'geojson',
            cluster: true,
            clusterMaxZoom: 14,
            clusterRadius: 50
          })
          map.addLayer({
            'id': 'publicParkingSpots',
            'type': 'symbol',
            'source': 'publicSpots',
            'layout': {
              'icon-image': '{type}',
              'icon-size': 0
            },
            'paint': {
              'icon-translate': [-5, -5],
              'icon-translate-anchor': 'map',
              'icon-opacity': {
                'type': 'identity',
                'property': 'inside_zone'
              }
            },
            'filter': ['!has', 'parking_spot_id']
          })

          map.addLayer({
            id: 'publicClusters',
            type: 'circle',
            source: 'publicSpots',
            filter: ['has', 'point_count'],
            paint: {
              'circle-color': {
                property: 'point_count',
                type: 'interval',
                stops: [
                  [5, '#51bbd6'],
                  [10, '#f1f075'],
                  [15, '#f28cb1']
                ]
              },
              'circle-radius': 15
            }
          })
          map.setLayoutProperty('publicClusters', 'visibility', 'none')
          map.addLayer({
            id: 'publicCluster-count',
            type: 'symbol',
            source: 'publicSpots',
            filter: ['!has', 'point_count'],
            layout: {
              'text-field': '{point_count_abbreviated}',
              'text-font': ['DIN Offc Pro Medium', 'Arial Unicode MS Bold'],
              'text-size': 12
            }
          })
          mapFactory.setIconSize(map, map.getZoom(), 'publicParkingSpots')
        }
      }

      function drawHubs() {
        const hubsCollection = $scope.liveHubs.map((hub) =>
          turf.point([hub.longitude, hub.latitude], {
            type: 'hubs',
            label: hub.hubName.toUpperCase(),
            hubUUID: hub.hubUUID
          })
        )

        const hubsFeatureCollection = turf.featureCollection(hubsCollection)

        if (map.getSource('hubs')) {
          map.getSource('hubs').setData(hubsFeatureCollection)
          mapFactory.setIconSize(map, map.getZoom(), 'hubs')
        } else {
          map.loadImage('../../images/hubs-parking.png', (err, image) => {
            if (!err && !map.hasImage('parking_hubs')) {
              map.addImage('parking_hubs', image, { pixelRatio: 3 })
            }

            map.addSource('hubs', {
              data: hubsFeatureCollection,
              type: 'geojson'
            })

            map.addLayer({
              id: 'hubs',
              type: 'symbol',
              source: 'hubs',
              layout:  err ? {
                'text-field': '{label}',
                'text-font': ['DIN Offc Pro Medium', 'Arial Unicode MS Bold'],
                'text-size': 10,
                'text-anchor': 'top',
              } : {
                'icon-image': 'parking_hubs',
                'icon-size': 0
              }
            })
            mapFactory.setIconSize(map, map.getZoom(), 'hubs')
          })
        }
      }

      function drawData () {
        $scope.fleetParkingZones = _.filter($scope.parkingSpotsData, function (element) {
          return element.fleet_id === rootScopeFactory.getData('fleetId')
        })
        $scope.spotsCollection = []
        _.each($scope.fleetParkingZones, function (element) {
          if (element.type === 'public') {
            element.type = 'parking_racks'
            element['spot_type'] = 'public'
          } else {
            element['spot_type'] = 'normal'
          }
          $scope.spotsCollection.push(turf.point([element.longitude, element.latitude], {
            'type': utilsFactory.underscoreToHyphen(element.type),
            'capacity': element.capacity,
            'parking_spot_id': element.parking_spot_id,
            'inside_zone': 1,
            'spot_type': element.spot_type
          }))
        })
        $scope.spotsFeatureCollection = turf.featureCollection($scope.spotsCollection)
        if (map.getSource('spots')) {
          map.getSource('spots').setData($scope.spotsFeatureCollection)
        } else {
          map.addSource('spots', {
            data: $scope.spotsFeatureCollection,
            type: 'geojson',
            cluster: true,
            clusterMaxZoom: 14,
            clusterRadius: 50
          })
          map.addLayer({
            'id': 'parkingSpots',
            'type': 'symbol',
            'source': 'spots',
            'layout': {
              'icon-image': '{type}',
              'icon-size': 0
            },
            'paint': {
              'icon-translate': [-5, -5],
              'icon-translate-anchor': 'map',
              'icon-opacity': {
                'type': 'identity',
                'property': 'inside_zone'
              }
            },
            'filter': ['!has', 'parking_spot_id']
          })

          map.addLayer({
            id: 'clusters',
            type: 'circle',
            source: 'spots',
            filter: ['has', 'point_count'],
            paint: {
              'circle-color': {
                property: 'point_count',
                type: 'interval',
                stops: [
                  [5, '#51bbd6'],
                  [10, '#f1f075'],
                  [15, '#f28cb1']
                ]
              },
              'circle-radius': 15
            }
          })
          map.setLayoutProperty('clusters', 'visibility', 'none')
          map.addLayer({
            id: 'cluster-count',
            type: 'symbol',
            source: 'spots',
            filter: ['!has', 'point_count'],
            layout: {
              'text-field': '{point_count_abbreviated}',
              'text-font': ['DIN Offc Pro Medium', 'Arial Unicode MS Bold'],
              'text-size': 12
            }
          })
          mapFactory.setIconSize(map, map.getZoom(), 'parkingSpots')
        }
      }

      function drawZones () {
        $scope.fleetZones = _.filter($scope.zoneData, function (element) {
          return element.fleet_id === rootScopeFactory.getData('fleetId')
        })
        $scope.zoneCollection = []
        $scope.outlineCollection = []
        _.each($scope.fleetZones, function (element, index) {
          if (element.type === 'rectangle' || element.type === 'polygon') {
            var polygonFeature = utilsFactory.jsonToPolygonFeature(JSON.parse(element.geometry), {
              'type': element.type,
              'index': index,
              'zone_id': element.parking_area_id,
              'name': element.name
            })
            $scope.outlineCollection.push(turf.lineString(polygonFeature.geometry.coordinates[0], {zone_id: element.parking_area_id}))
            $scope.zoneCollection.push(polygonFeature)
          }
          else if (element.type === 'circle') {
            const circle = JSON.parse(element.geometry)[0]
            let { longitude, latitude, radius } = circle
            radius = utilsFactory.getMiles(radius)
            radius = radius? radius: 0.001;
            if (radius > 0) {
              const circleFeature = turf.circle(turf.point([longitude, latitude]), radius, 10000, 'miles')
              circleFeature['properties'] = {
                'type': element.type,
                'radius': utilsFactory.getMiles(JSON.parse(element.geometry)[0].radius),
                'index': index,
                'center': [JSON.parse(element.geometry)[0].longitude, JSON.parse(element.geometry)[0].latitude],
                'zone_id': element.parking_area_id,
                'name': element.name
              }
              $scope.outlineCollection.push(turf.lineString(circleFeature.geometry.coordinates[0], {zone_id: element.parking_area_id}))
              $scope.zoneCollection.push(circleFeature)
            }
          }
        })
        $scope.zonesCollection = turf.featureCollection($scope.zoneCollection)
        $scope.outlinesCollection = turf.featureCollection($scope.outlineCollection)

        if (map.getSource('zones')) {
          map.getSource('zones').setData($scope.zonesCollection)
          map.getSource('outline').setData($scope.outlinesCollection)
        } else {
          map.addSource('zones', {
            data: $scope.zonesCollection,
            type: 'geojson'
          })
          map.addLayer({
            'id': 'zoneLayer',
            'type': 'fill',
            'source': 'zones',
            'layout': {},
            'filter': ['!has', 'zone_id'],
            'paint': {
              'fill-outline-color': '#F65295',
              'fill-color': '#F65295',
              'fill-opacity': 0.18
            }
          }, 'country-label-lg')
          map.addSource('outline', {
            data: $scope.outlinesCollection,
            type: 'geojson'
          })
          map.addLayer({
            'id': 'outline',
            'type': 'line',
            'source': 'outline',
            'layout': {
              'line-join': 'round',
              'line-cap': 'round'
            },
            'filter': ['!has', 'zone_id'],
            'paint': {
              'line-color': '#F65295',
              'line-width': 2,
              'line-opacity': 0.5
            }
          }, 'country-label-lg')
        }
      }

      if (rootScopeFactory.getData('fleetId')) {
        $q.all([
          parkingFactory.getParkingZonesData({
            fleet_id: rootScopeFactory.getData('fleetId'),
          }),
          parkingFactory.getParkingSpotsData({
            fleet_id: rootScopeFactory.getData('fleetId'),
          }),
          parkingFactory.getPublicParkingSpotsData({
            customer_id: rootScopeFactory.getData('customerId'),
          }),
          myFleetFactory.getFleetData(),
          hubsFactory.list({ status: 'live' })
        ]).then(function (response) {
          $scope.zoneData = angular.copy(response[0])
          $scope.parkingSpotsData = angular.copy(response[1])
          $scope.PublicParkingSpotsData = angular.copy(response[2])
          $scope.fleetMetaData = _.findWhere(response[3].fleet_data, {
            fleet_id: rootScopeFactory.getData('fleetId'),
          })

          $scope.liveHubs = response[4]

          initParkingList()
          $rootScope.showLoader = false
          if (map.isStyleLoaded()) {
            drawData()
            drawPublic()
            drawZones()
            drawHubs()
            initOpacity()
            loadPreference()
          } else {
            map.on('load', function () {
              drawData()
              drawPublic()
              drawZones()
              drawHubs()
              initOpacity()
              loadPreference()
            })
          }
        })
      }


      $scope.$on('fleetChange', function (event, fleetId) {
        $scope.zoneData = null
        $scope.parkingSpotsData = null
        $q.all([
          parkingFactory.getParkingZonesData({ fleet_id: fleetId }),
          parkingFactory.getParkingSpotsData({ fleet_id: fleetId }),
          parkingFactory.getPublicParkingSpotsData({
            customer_id: rootScopeFactory.getData('customerId'),
          }),
          myFleetFactory.getFleetData(),
          hubsFactory.list({ status: 'live', fleet_id: fleetId }),
        ]).then(function (response) {
          $scope.zoneData = angular.copy(response[0])
          $scope.parkingSpotsData = angular.copy(response[1])
          $scope.PublicParkingSpotsData = angular.copy(response[2])
          $scope.fleetMetaData = _.findWhere(response[3].fleet_data, {
            fleet_id: fleetId,
          })

          $scope.liveHubs = response[4]

          initParkingList()
          $rootScope.showLoader = false
          if (map.isStyleLoaded()) {
            drawData()
            drawPublic()
            drawZones()
            drawHubs()
            initOpacity()
            loadPreference()
          } else {
            map.on('load', function () {
              drawData()
              drawPublic()
              drawZones()
              drawHubs()
              initOpacity()
              loadPreference()
            })
          }
        })
      })


      $scope.getParkingZones = function (type) {
        if ($scope.buttonZones) {
          map.setFilter('zoneLayer', ['all', ['has', 'zone_id']])
          map.setFilter('outline', ['all', ['has', 'zone_id']])
          if ($scope.publicButton && $scope.buttonSpots) {
            let dummyPublic = angular.copy($scope.publicSpotsFeatureCollection)
            _.each($scope.spotsFeatureCollection.features, function (element) {
              dummyPublic.features.push(element)
            })
            _.each($scope.zonesCollection.features, function (element) {
              dummyPublic.features.push(element)
            })
            if (dummyPublic.features.length) {
              mapFactory.mapFitBounds(map, dummyPublic, 'low')
            }
          } else if ($scope.buttonSpots) {
            let dummyPublic = angular.copy($scope.spotsFeatureCollection)
            _.each($scope.zonesCollection.features, function (element) {
              dummyPublic.features.push(element)
            })
            if (dummyPublic.features.length) {
              mapFactory.mapFitBounds(map, dummyPublic, 'low')
            }
          } else if ($scope.publicButton) {
            let dummyPublic = angular.copy($scope.publicSpotsFeatureCollection)
            _.each($scope.zonesCollection.features, function (element) {
              dummyPublic.features.push(element)
            })
            if (dummyPublic.features.length) {
              mapFactory.mapFitBounds(map, dummyPublic, 'low')
            }
          } else if ($scope.zonesCollection.features.length) {
            mapFactory.mapFitBounds(map, $scope.zonesCollection, 'low')
          }
          if (!$scope.zonesCollection.features.length) {
            if ($scope.zone_notification) {
              $scope.zone_notification = !$scope.zone_notification
              $scope.zone_empty = !$scope.zone_empty
              if (type === 'init') {
                $scope.$apply(function () {
                  $scope.buttonZones = false
                })
              } else {
                $scope.buttonZones = false
              }
              zone_icon.removeClass('fa-question-circle-o')
              zone_icon.addClass('fa-question-circle')
            } else {
              $scope.zone_empty = !$scope.zone_empty
              $scope.shape_showzone = false
              if (type === 'init') {
                $scope.$apply(function () {
                  $scope.buttonZones = false
                })
              } else {
                $scope.buttonZones = false
              }
            }
          }
          map.getSource('spots').setData($scope.spotOpacity)
          map.getSource('publicSpots').setData($scope.publicSpotOpacity)
          if ($scope.fleetMetaData && type !== 'init') {
            let latestParkingState = utilsFactory.trueTOBinary({show_parking_spots: $scope.buttonSpots,
              show_parking_zones: $scope.buttonZones})

            if (!_.isEqual(latestParkingState, {show_parking_spots: $scope.fleetMetaData.show_parking_spots,
              show_parking_zones: $scope.fleetMetaData.show_parking_zones})) {
              latestParkingState.fleet_id = rootScopeFactory.getData('fleetId')
              parkingFactory.saveParkingSettings(latestParkingState, function (response) {
                if (response && response.status === 200) {
                  myFleetFactory.clearFleetCache()
                  $scope.fleetMetaData.show_parking_spots = latestParkingState.show_parking_spots
                  $scope.fleetMetaData.show_parking_zones = latestParkingState.show_parking_zones
                }
              })
            }
          }

          sessionStorage.setItem('isParkingZoneEnabled', true)
        } else {
          map.setFilter('zoneLayer', ['all', ['!has', 'zone_id']])
          map.setFilter('outline', ['all', ['!has', 'zone_id']])
          _.each($scope.spotsFeatureCollection.features, function (ele, index) {
            $scope.spotsFeatureCollection.features[index].properties.inside_zone = 1
          })
          map.getSource('spots').setData($scope.spotsFeatureCollection)

          _.each($scope.publicSpotsFeatureCollection.features, function (ele, index) {
            $scope.publicSpotsFeatureCollection.features[index].properties.inside_zone = 1
          })
          map.getSource('publicSpots').setData($scope.publicSpotsFeatureCollection)
          if ($scope.fleetMetaData && type !== 'init') {
            let latestParkingState = utilsFactory.trueTOBinary({show_parking_spots: $scope.buttonSpots,
              show_parking_zones: $scope.buttonZones})

            if (!_.isEqual(latestParkingState, {show_parking_spots: $scope.fleetMetaData.show_parking_spots,
              show_parking_zones: $scope.fleetMetaData.show_parking_zones})) {
              latestParkingState.fleet_id = rootScopeFactory.getData('fleetId')
              parkingFactory.saveParkingSettings(latestParkingState, function (response) {
                if (response && response.status === 200) {
                  myFleetFactory.clearFleetCache()
                  $scope.fleetMetaData.show_parking_spots = latestParkingState.show_parking_spots
                  $scope.fleetMetaData.show_parking_zones = latestParkingState.show_parking_zones
                }
              })
            }
          }
        }
      }

      function initParkingList() {
        $scope.parkingList = [].concat(
          $scope.zoneData.map(function(zone) {
            return Object.assign({}, zone, {
              type: 'Parking Zone'
            })
          }),
          $scope.parkingSpotsData.map(function(spot) {
            return Object.assign({}, spot, {
              type: 'Parking Spot'
            })
          }),
          $scope.PublicParkingSpotsData.map(function(spot) {
            return Object.assign({}, spot, {
              type: 'Parking Spot',
              public: true
            })
          }),
          $scope.liveHubs.map(function (hub) {
            return Object.assign({}, hub, {
              name: hub.hubName,
              type: hub.type === 'parking_station' ? 'Parking Station' : 'Docking Station',
            })
          })
        )
      }

      function initOpacity () {
        let inside = turf.within($scope.spotsFeatureCollection, $scope.zonesCollection)
        $scope.spotOpacity = angular.copy($scope.spotsFeatureCollection)
        $scope.publicSpotOpacity = angular.copy($scope.publicSpotsFeatureCollection)
        _.each($scope.spotOpacity.features, function (ele, index) {
          $scope.spotOpacity.features[index].properties.inside_zone = 0.4
        })

        $scope.spotOpacity.features.forEach(function (spot, spotIndex) {
          inside.features.forEach(function (inside) {
            if (spot.properties.parking_spot_id === inside.properties.parking_spot_id) {
              $scope.spotOpacity.features[spotIndex].properties.inside_zone = 1
            }
          })
        })

        let publicInside = turf.within($scope.publicSpotsFeatureCollection, $scope.zonesCollection)
        _.each($scope.publicSpotOpacity.features, function (ele, index) {
          $scope.publicSpotOpacity.features[index].properties.inside_zone = 0.4
        })

        $scope.publicSpotOpacity.features.forEach(function (spot, spotIndex) {
          publicInside.features.forEach(function (inside) {
            if (spot.properties.parking_spot_id === inside.properties.parking_spot_id) {
              $scope.publicSpotOpacity.features[spotIndex].properties.inside_zone = 1
            }
          })
        })
      }

      $scope.getParkingSpots = function (type) {
        if ($scope.buttonSpots) {
          map.setFilter('parkingSpots', ['all', ['has', 'parking_spot_id']])
          map.setLayoutProperty('clusters', 'visibility', 'visible')
          map.setFilter('cluster-count', ['has', 'point_count'])
          if ($scope.publicButton) {
            let dummyPublic = angular.copy($scope.publicSpotsFeatureCollection)
            _.each($scope.spotsFeatureCollection.features, function (element) {
              dummyPublic.features.push(element)
            })
            mapFactory.mapFitBounds(map, dummyPublic, 'low')
          } else if ($scope.spotsFeatureCollection && $scope.spotsFeatureCollection.features.length) {
            mapFactory.mapFitBounds(map, $scope.spotsFeatureCollection, 'low')
          }
          sessionStorage.setItem('isParkingSpotEnabled', true)
          if (!$scope.spotsFeatureCollection.features.length) {
            if ($scope.spot_notification) {
              $scope.spot_notification = !$scope.spot_notification
              $scope.spot_empty = !$scope.spot_empty
              if (type === 'init') {
                $scope.$apply(function () {
                  $scope.buttonSpots = false
                })
              } else {
                $scope.buttonSpots = false
              }
              if ($scope.dropdown_1 === 'box-drop') {
                $scope.dropdown_1 = 'box-rise'
              } else {
                $scope.dropdown_1 = 'box-drop'
              }
              spot_icon.removeClass('fa-question-circle-o')
              spot_icon.addClass('fa-question-circle')
            } else {
              $scope.spot_empty = !$scope.spot_empty
              $scope.shape_showspot = false
              if (type === 'init') {
                $scope.$safeApply(function () {
                  $scope.buttonSpots = false
                })
              } else {
                $scope.buttonSpots = false
              }
            }
          }
          if ($scope.fleetMetaData && type !== 'init') {
            let latestParkingState = utilsFactory.trueTOBinary({show_parking_spots: $scope.buttonSpots,
              show_parking_zones: $scope.buttonZones})

            if (!_.isEqual(latestParkingState, {show_parking_spots: $scope.fleetMetaData.show_parking_spots,
              show_parking_zones: $scope.fleetMetaData.show_parking_zones})) {
              latestParkingState.fleet_id = rootScopeFactory.getData('fleetId')
              parkingFactory.saveParkingSettings(latestParkingState, function (response) {
                if (response && response.status === 200) {
                  myFleetFactory.clearFleetCache()
                  $scope.fleetMetaData.show_parking_spots = latestParkingState.show_parking_spots
                  $scope.fleetMetaData.show_parking_zones = latestParkingState.show_parking_zones
                }
              })
            }
          }
        } else {
          map.setFilter('parkingSpots', ['all', ['!has', 'parking_spot_id']])
          map.setLayoutProperty('clusters', 'visibility', 'none')
          map.setFilter('cluster-count', ['!has', 'point_count'])
          if ($scope.publicButton) {
            mapFactory.mapFitBounds(map, $scope.publicSpotsFeatureCollection, 'low')
          }
          sessionStorage.setItem('isParkingSpotEnabled', false)
          if ($scope.fleetMetaData && type !== 'init') {
            let latestParkingState = utilsFactory.trueTOBinary({show_parking_spots: $scope.buttonSpots,
              show_parking_zones: $scope.buttonZones})

            if (!_.isEqual(latestParkingState, {show_parking_spots: $scope.fleetMetaData.show_parking_spots,
              show_parking_zones: $scope.fleetMetaData.show_parking_zones})) {
              latestParkingState.fleet_id = rootScopeFactory.getData('fleetId')
              parkingFactory.saveParkingSettings(latestParkingState, function (response) {
                if (response && response.status === 200) {
                  myFleetFactory.clearFleetCache()
                  $scope.fleetMetaData.show_parking_spots = latestParkingState.show_parking_spots
                  $scope.fleetMetaData.show_parking_zones = latestParkingState.show_parking_zones
                }
              })
            }
          }
        }
      }

      $scope.showPublicSpots = function () {
        if ($scope.publicButton) {
          map.setFilter('publicParkingSpots', ['all', ['has', 'parking_spot_id']])
          map.setLayoutProperty('publicClusters', 'visibility', 'visible')
          map.setFilter('publicCluster-count', ['has', 'point_count'])
          if ($scope.publicSpotsFeatureCollection.features.length) {
            let dummyPublic = angular.copy($scope.publicSpotsFeatureCollection)
            _.each($scope.spotsFeatureCollection.features, function (element) {
              dummyPublic.features.push(element)
            })
            mapFactory.mapFitBounds(map, dummyPublic, 'low')
          } else {
            if ($scope.public_notification) {
              $scope.public_notification = !$scope.public_notification
              $scope.public_spotempty = !$scope.public_spotempty
              $scope.publicButton = false
            } else {
              $scope.public_spotempty = !$scope.public_spotempty
              $scope.publicButton = false
            }
          }
          sessionStorage.setItem('isPublicParkingSpotEnabled', true)
        } else {
          map.setFilter('publicParkingSpots', ['all', ['!has', 'parking_spot_id']])
          map.setLayoutProperty('publicClusters', 'visibility', 'none')
          map.setFilter('publicCluster-count', ['!has', 'point_count'])
          if ($scope.buttonSpots && $scope.spotsFeatureCollection.features.length) {
            mapFactory.mapFitBounds(map, $scope.spotsFeatureCollection, 'low')
          }
          sessionStorage.setItem('isPublicParkingSpotEnabled', false)
        }
      }

      $scope.saveParking = function () {
        if ($scope.publicCsv) {
          let csvData = new FormData()
          csvData.append('spot_csv', $scope.publicCsv)
          csvData.append('fleet_id', rootScopeFactory.getData('fleetId'))
          csvData.append('operator_id', sessionFactory.getCookieId())
          csvData.append('customer_id', rootScopeFactory.getData('customerId'))
          parkingFactory.uploadCSV(csvData, function (response) {
            if (response && response.status === 200) {
              notify({
                message: 'Added the public parking spots successfully',
                duration: 2000,
                position: 'right'
              })
              $scope.publicCsv = null
              parkingFactory.clearParkingSpotCache()
              $timeout(function () {
                $q.all([parkingFactory.getParkingSpotsData({fleet_id: rootScopeFactory.getData('fleetId')}),
                  parkingFactory.getPublicParkingSpotsData({customer_id: rootScopeFactory.getData('customerId')})]).then(function (response) {
                  $scope.parkingSpotsData = angular.copy(response[0])
                  $scope.PublicParkingSpotsData = angular.copy(response[1])
                  drawData()
                  drawPublic()
                  initOpacity()
                })
              }, 50)
            } else {
              notify({
                message: 'Failed to create the public parking spots',
                duration: 2000,
                position: 'right'
              })
            }
          })
        }
      }

      $scope.shape_showspot, $scope.toggleshow, $scope.public_showspot, $scope.shape_showzone = false
      $scope.shape = function (s) {
        if (s === 'zone') {
          zone_icon.removeClass('fa-question-circle-o')
          zone_icon.addClass('fa-question-circle')
          $scope.zone_empty = false
          $scope.shape_showzone = !$scope.shape_showzone
          $scope.zone_notification = false
        } else if (s === 'spot') {
          spot_icon.removeClass('fa-question-circle-o')
          spot_icon.addClass('fa-question-circle')
          $scope.spot_empty = false
          $scope.spot_notification = false
          $scope.shape_showspot = !$scope.shape_showspot
        } else if (s === 'publicParking') {
          public_spot.removeClass('fa-question-circle-o')
          public_spot.addClass('fa-question-circle')
          $scope.public_notification = false
          $scope.public_showspot = !$scope.public_showspot
        } else if (s === 'toggle') {
          $scope.toggleshow = !$scope.toggleshow
        }
      }

      $scope.zone_notification, $scope.spot_notification, $scope.public_notification = false

      $scope.inquiry = function (s) {
        // inquiry pop-up function
        if (s === 'zone') {
          if (!$scope.zone_notification) {
            zone_icon.removeClass('fa-question-circle')
            zone_icon.addClass('fa-question-circle-o')
            $scope.zone_notification = !$scope.zone_notification
            $scope.zone_empty = false
            $scope.shape_showzone = false
          } else {
            zone_icon.removeClass('fa-question-circle-o')
            zone_icon.addClass('fa-question-circle')
            $scope.zone_notification = !$scope.zone_notification
            $scope.zone_empty = false
            $scope.shape_showzone = false
          }
        } else if (s === 'spot') {
          if (!$scope.spot_notification) {
            spot_icon.removeClass('fa-question-circle')
            spot_icon.addClass('fa-question-circle-o')
            $scope.spot_notification = !$scope.spot_notification
            $scope.shape_showspot = false
            $scope.spot_empty = false
          } else {
            spot_icon.removeClass('fa-question-circle-o')
            spot_icon.addClass('fa-question-circle')
            $scope.spot_notification = !$scope.spot_notification
            $scope.shape_showspot = false
            $scope.spot_empty = false
          }
        } else {
          if (!$scope.public_notification) {
            public_spot.removeClass('fa-question-circle')
            public_spot.addClass('fa-question-circle-o')
            $scope.public_notification = !$scope.public_notification
            $scope.public_showspot = false
            $scope.public_spotempty = false
          } else {
            public_spot.removeClass('fa-question-circle-o')
            public_spot.addClass('fa-question-circle')
            $scope.public_notification = !$scope.public_notification
            $scope.public_showspot = false
            $scope.public_spotempty = false
          }
        }
      }

      $scope.$on('createZone', function () {
        $scope.map.off('click', spot_click)
      })

      // Declare variables
      var isFirstClick = false
      var icon_type
      $scope.spotselect = {'spot1': false, 'spot2': false, 'spot3': false, 'spot4': false}
      $scope.parkingMeter = function () {
        $rootScope.$broadcast('createSpot')
        $rootScope.$emit('createSpot')
        $scope.map.off('click', spot_click)
        $scope.spotselect = {'spot1': true, 'spot2': false, 'spot3': false, 'spot4': false}
        isFirstClick = true
        editingParkingZone = true
        icon_type = 'parking_meter'
        $scope.map.on('click', spot_click)
      }

      $scope.sheffieldStand = function () {
        $rootScope.$broadcast('createSpot')
        $rootScope.$emit('createSpot')
        $scope.map.off('click', spot_click)
        $scope.spotselect = {'spot1': false, 'spot2': true, 'spot3': false, 'spot4': false}
        isFirstClick = true
        editingParkingZone = true
        icon_type = 'parking_racks'
        $scope.map.on('click', spot_click)
      }

      $scope.lightningRack = function () {
        $rootScope.$broadcast('createSpot')
        $rootScope.$emit('createSpot')
        $scope.map.off('click', spot_click)
        $scope.spotselect = {'spot1': false, 'spot2': false, 'spot3': true, 'spot4': false}
        isFirstClick = true
        editingParkingZone = true
        icon_type = 'charging_spot'
        $scope.map.on('click', spot_click)
      }

      $scope.parkingSpot = function () {
        $scope.map.off('click', spot_click)
        $scope.spotselect = {'spot1': false, 'spot2': false, 'spot3': false, 'spot4': true}
        isFirstClick = true
        editingParkingZone = true
        icon_type = 'generic_parking'
        $scope.map.on('click', spot_click)
      }

      $scope.handleViewClick = function(parking) {
        var maxZoom = 14
        if (parking.type === 'Parking Zone') {
          var collection = JSON.parse(parking.geometry).map(function(point) {
            return turf.point([point.longitude, point.latitude])
          })
          $scope.map.fitBounds(turf.bbox(turf.featureCollection(collection)), {
            linear: true,
            maxZoom: maxZoom,
          })
        } else if (parking.type === 'Parking Spot') {
          $scope.map.fitBounds(turf.bbox(turf.point([parking.longitude, parking.latitude])), {
            linear: true,
            maxZoom: maxZoom,
          })
        } else if (parking.hubName) {
          $scope.map.fitBounds(turf.bbox(turf.point([parking.longitude, parking.latitude])), {
            linear: true,
            maxZoom: maxZoom,
          })
        }

        $window.scrollTo(0, 0)
      }

      $scope.handleEditClick = function(parking) {
        if (parking.hubName) {
          $state.go("hub-edit", { uuid: parking.hubUUID });
          return;
        }

        if (parking.type === 'Parking Zone') {
          var feature = _.find($scope.zoneCollection, function(f) {
            return f.properties.zone_id == parking.parking_area_id
          })
          if (feature.properties.center) {
            feature.properties.center = JSON.stringify(feature.properties.center)
          }
          editParkingZone(feature)
        } else if (parking.type === 'Parking Spot') {
          var feature = _.find($scope.spotsFeatureCollection.features, function(f) {
            return f.properties.parking_spot_id == parking.parking_spot_id
          }) || _.find($scope.publicSpotsFeatureCollection.features, function(f) {
            return f.properties.parking_spot_id == parking.parking_spot_id
          })
          feature.layer = {
            id: parking.public ? 'publicParkingSpots' : 'parkingSpots'
          }
          editParkingSpot(feature)
        }
        $window.scrollTo(0, 0)
      }

      $scope.handleDeleteClick = function(parking) {
        parkingFactory.deleteParkingZone({parking_area_id: parking.parking_area_id}, function (response) {
          if (response && response.status === 200) {
            parkingFactory.clearParkingZonesCache()
            $rootScope.$broadcast('saveCompleted')
            // removeAllListeners()
            initParkingList();
            notify({
              message: 'Deleted the parking zone successfully',
              duration: 2000,
              position: 'right'
            })
          } else {
            notify({
              message: 'Failed to deleted the parking zone',
              duration: 2000,
              position: 'right'
            })
          }
        })
      }

      $scope.isEditing = function() {
        return editingParkingSpot || editingParkingZone
      }

      function editParkingZone(feature) {
        tempData['zone'] = $scope.zonesCollection.features[feature.properties.index]
        tempData['outline'] = $scope.outlinesCollection.features[feature.properties.index]
        tempData['index'] = feature.properties.index
        $scope.tempData = angular.copy(tempData)
        $scope.zonesCollection.features.splice(feature.properties.index, 1)
        $scope.outlinesCollection.features.splice(feature.properties.index, 1)
        map.getSource('zones').setData($scope.zonesCollection)
        map.getSource('outline').setData($scope.outlinesCollection)
        editingParkingZone = true
        addTempZone(tempData.zone)
        $scope.map.setLayoutProperty('tempZone', 'visibility', 'visible')
        feature.properties['index'] = 0
        map.getCanvas().style.cursor = defaultCursor;
        editParkingZoneFactory.editHandler(map, 'tempZone', feature.properties, 'edit')
      }

      function editParkingSpot(feature) {
        if (editingParkingSpot) {
          return
        }
        map.setCenter([feature.geometry.coordinates[0] - 0.00005, feature.geometry.coordinates[1]])
        var spot_details
        if (feature.layer.id === 'parkingSpots') {
          spot_details = _.where($scope.parkingSpotsData, {parking_spot_id: feature.properties.parking_spot_id})
        } else if (feature.layer.id === 'publicParkingSpots') {
          spot_details = _.where($scope.PublicParkingSpotsData, {parking_spot_id: feature.properties.parking_spot_id})
        }
        // $scope.map.setLayoutProperty('outline', 'visibility', 'none');
        geoRecovered = feature.properties
        popupzone = new mapboxgl.Popup({offset: [-15, -8], anchor: 'right', closeOnClick: false})
          .setLngLat(feature.geometry.coordinates)
          .setHTML('<div class="modal-content"><div class="modal-header bg-HeaderToolbar" style="padding: 15px;">' +
                            '<button type="button" class="close" style="font-size:20px;font-weight:100;">&times;</button>' +
                            '<h4 class="modal-title text-white"></h4>' +
                            '</div>' +
                            '<div class="modal-body cl_grey wid-">' +
                            '<div class="row"><div class="col-sm-7">' +
                            '<div class="form-group"><p class="ft-12">NAME</p>' +
                            '<input type="text" class="form-control nobor-rad" id="selectspotName" placeholder="Eg. Lower Campus East" /></div>' +
                            '<div class="form-group"><p class="ft-12">DESCRIPTION</p>' +
                            '<textarea id="selectspotDec" class="fullwid noborder selectspotDec" style="resize: none;" rows="6"></textarea></div>' +
                            '</div>' +
                            '<div class="col-sm-5"><div class="ft-12" style="height: 230px;" >' +
                            '<p class="ft-12">PHOTO</p>' +
                            '<div id="parking_image__" style="width:100%;height:200px;"></div></div> ' +
                            '<div><label class="fullwid menuactive pull-left text-center nomarg pointer ft-11 m-t-7"><span><i class="fa fa-upload"></i> Upload new picture</span><input accept="{{accept}}" type="file" class="hidden" ng-model="filename" id="popupimg"></label></div></div></div></div>' +
                            '<div class="modal-footer noborder">' +
                            '<div class="text-right fullwid m-tb-10 p-lr-5">' +
                            '<button class="btn custom-btn ft-12 p-tb-10 bg-HeaderToolbar wid-75 pull-left deletePopup">DELETE </button>' +
                            '<button class="btn custom-btn ft-12 p-tb-10 wid-75 savePopupUpdate" >SAVE </button>' +
                            '</div></div></div>' +
                            '')
          .addTo($scope.map)
        editingParkingSpot = true
        jQuery('.close').on('click', function () {
          $scope.$broadcast('cancelParkingSpotEdit')
        })
        var pic_link, pic_linkObj = ''
        var parkingValid = false
        jQuery('#selectspotName, #selectspotDec').change(function (e) {
          if (e.target.id == 'selectspotName') {
            e.target.value != spot_details[0].name ? parkingValid = true : parkingValid = false
          } else if (e.target.id == 'selectspotDec') {
            e.target.value != spot_details[0].description ? parkingValid = true : parkingValid = false
          }
        })
        $('#popupimg').change(function (e) {
          var input = e.target
          if (input.files && input.files[0]) {
            var reader = new FileReader()
            reader.onload = function (e) {
              jQuery('#parking_image__').css('background-image', 'url(' + e.target.result + ')')
              pic_link = e.target.result
            }
            reader.readAsDataURL(input.files[0])
            pic_linkObj = input.files[0]
            // let's validate the image size and disable upload for large & non image files:
            var fileFormat = ['image/png', 'image/jpg', 'image/jpeg']
            var supportedFileFormats = fileFormat.includes(pic_linkObj.type)
            if (pic_linkObj && (pic_linkObj.size > 500000 || supportedFileFormats === false)){
              $('.savePopupUpdate').prop('disabled', true)
              $('.fa-upload').after(
                '<span id="error-file-change" style="color: red; width:100%; position: absolute; margin-left: -129px; padding-top: 45px">Incorrect size or format. Please select a different file.</span>'
              )
            } else {
              $('.savePopupUpdate').prop('disabled', false)
              $('#error-file-change').remove();
            }
          }
        })

        jQuery('.modal-title').text(spot_details[0].name)
        jQuery('#selectspotName').val(spot_details[0].name)
        jQuery('#selectspotDec').text(spot_details[0].description)
        jQuery('#parking_image__').css('background-image', 'url(' + spot_details[0].pic + ')')

        jQuery('.savePopupUpdate').on('click', function (e) {
          if (parkingValid || pic_link) {
            let editParkingData = new FormData()
            editParkingData.append('name', jQuery('#selectspotName').val())
            editParkingData.append('description', jQuery('#selectspotDec').val())
            editParkingData.append('parking_spot_id', spot_details[0].parking_spot_id)
            editParkingData.append('type', spot_details[0].type)
            editParkingData.append('latitude', spot_details[0].latitude)
            editParkingData.append('longitude', spot_details[0].longitude)
            editParkingData.append('fleet_id', rootScopeFactory.getData('fleetId'))

            if (pic_link) {
              editParkingData.append('pic', pic_linkObj)
            } else {
              editParkingData.append('pic_link', spot_details[0].pic)
            }
            parkingFactory.editParkingSpot(editParkingData, function (response) {
              if (response && response.status === 200) {
                notify({
                  message: 'Saved the parking spot details successfully',
                  duration: 2000,
                  position: 'right'
                })
                parkingFactory.clearParkingSpotCache()
                popupzone.remove()
                editingParkingSpot = false
                $timeout(function () {
                  $q.all([parkingFactory.getParkingSpotsData({fleet_id: rootScopeFactory.getData('fleetId')}),
                    parkingFactory.getPublicParkingSpotsData({customer_id: rootScopeFactory.getData('customerId')})]).then(function (response) {
                    $scope.parkingSpotsData = angular.copy(response[0])
                    $scope.PublicParkingSpotsData = angular.copy(response[1])
                    drawData()
                    drawPublic()
                    initOpacity()
                  })
                }, 50)
              } else {
                notify({
                  message: 'Failed to update the parking spots',
                  duration: 2000,
                  position: 'right'
                })
              }
            })
          } else {
            notify({
              message: 'Please edit field parking spots',
              duration: 2000,
              position: 'right'
            })
          }
        })
        jQuery('.deletePopup').on('click', function (e) {
          if (geoRecovered && geoRecovered.parking_spot_id) {
            parkingFactory.deleteParkingSpot({parking_spot_id: geoRecovered.parking_spot_id}, function (response) {
              if (response && response.status === 200) {
                if ($scope.spotsFeatureCollection.features.length === 1) {
                  let latestParkingState = {}
                  latestParkingState.fleet_id = rootScopeFactory.getData('fleetId')
                  latestParkingState.show_parking_spots = 0
                  latestParkingState.show_parking_zones = $scope.fleetMetaData.show_parking_zones
                  parkingFactory.saveParkingSettings(latestParkingState, function (response) {
                    if (response && response.status === 200) {
                      myFleetFactory.clearFleetCache()
                      $timeout(function () {
                        $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
                        $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
                      }, 250)
                    }
                  })
                }
                parkingFactory.clearParkingSpotCache()
                editingParkingSpot = false
                $timeout(function () {
                  $q.all([parkingFactory.getParkingSpotsData({fleet_id: rootScopeFactory.getData('fleetId')}),
                    parkingFactory.getPublicParkingSpotsData({customer_id: rootScopeFactory.getData('customerId')})]).then(function (response) {
                    $scope.parkingSpotsData = angular.copy(response[0])
                    $scope.PublicParkingSpotsData = angular.copy(response[1])
                    if(!$scope.parkingSpotsData.length && !$scope.PublicParkingSpotsData.length) {
                      $scope.buttonSpots = false
                    }
                    drawData()
                    drawPublic()
                    initOpacity()
                  })
                }, 50)
                notify({
                  message: 'Deleted the parking spot successfully',
                  duration: 2000,
                  position: 'right'
                })
                popupzone.remove()
              } else {
                notify({
                  message: 'Failed to delete the parking spot',
                  duration: 2000,
                  position: 'right'
                })
              }
            })
          }
        })
      }

      map.on('zoom', function () {
        if (map.getLayer('parkingSpots')) {
          mapFactory.setIconSize(map, map.getZoom(), 'parkingSpots')
        }
        if (map.getLayer('publicParkingSpots')) {
          mapFactory.setIconSize(map, map.getZoom(), 'publicParkingSpots')
        }

        if(map.getLayer('hubs')) {
          mapFactory.setIconSize(map, map.getZoom(), 'hubs')
        }
      })

      /* Anonymous map onClick listener function listening for click on parking zone's to start editing them */
      map.on('click', function (e) {
        var zones = $scope.map.queryRenderedFeatures(e.point, {layers: ['zoneLayer']})
        var spots = $scope.map.queryRenderedFeatures(e.point, {layers: ['parkingSpots', 'publicParkingSpots']})
        var hubs = $scope.map.queryRenderedFeatures(e.point, {layers: ['hubs']})

        if (zones.length && !editingParkingZone) {
          editParkingZone(zones[0])
        }
        if (!zones.length && editingParkingZone) {
          $scope.$emit('cancelParkingZoneEdit')
        }

        if (spots.length && !editingParkingSpot) {
          editParkingSpot(spots[0])
        }
        if (!spots.length && editingParkingSpot) {
          $scope.$broadcast('cancelParkingSpotEdit')
        }

        if (hubs.length && !viewingHub) {
          viewHub(hubs[0])
        }

        if (!hubs.length && viewingHub) {
          $scope.$broadcast('cancelViewHub')
        }

        $scope.$apply()
      })

      function viewHub(feature) {
        if (viewingHub) {
          return
        }

        viewingHub = true
        map.setCenter([feature.geometry.coordinates[0] - 0.00005, feature.geometry.coordinates[1]])

        var hub_details = _.find($scope.liveHubs, {hubUUID: feature.properties.hubUUID})
        popupzone = new mapboxgl.Popup({offset: [-15, -8], anchor: 'right', closeOnClick: false})
          .setLngLat(feature.geometry.coordinates)
          .setHTML(`
            <div class="modal-content">
              <div class="modal-header bg-HeaderToolbar" style="padding: 15px">
                  <button type="button" class="close" style="font-size: 20px; font-weight: 100">
                      &times;
                  </button>
                  <h4 class="modal-title text-white hub__title">Title!!</h4>
              </div>
              <div class="modal-body cl_grey wid- hub__details">
                  <div class="row">
                      <div class="col-xs-12 col-md-7">
                          <div class="row">
                              <p class="p-b-10 hub__name">
                                <span class="ft-14 ft-bold text-uppercase">
                                  Name:
                                </span>
                                <a target="_blank">
                                  <span class="value"></span>
                                </a>
                              </p>
                              <p class="p-b-10 hub__description">
                                <span class="ft-14 ft-bold text-uppercase">
                                  Description:
                                </span>
                                <span class="value"></span>
                              </p>
                              <p class="p-b-10 hub__ports">
                                <span class="ft-14 ft-bold text-uppercase">
                                  Ports:
                                </span>
                                <a target="_blank">
                                  <span class="value"></span>
                                </a>
                              </p>
                              <p class="p-b-10 hub__status">
                                <span class="ft-14 ft-bold text-uppercase">
                                  Status:
                                </span>
                                <span class="value"></span>
                              </p>
                          </div>
                      </div>
                      <div class="col-xs-12 col-md-5" style="height: 230px;">
                          <div class="hub__image" style="background-size:contain;"></div>
                      </div>
                  </div>
              </div>
            </div>
          `)
          .addTo($scope.map)

        jQuery('.modal-title').text(hub_details.hubName)
        jQuery('.hub__name span.value').text(hub_details.hubName)
        jQuery('.hub__name a')
          .attr('href', `/#/hubs/${hub_details.hubUUID}/details`)
        jQuery('.hub__description span.value').text(hub_details.description)
        jQuery('.hub__ports span.value').text(`${hub_details.ports.length} ports`)
        jQuery('.hub__ports a')
          .attr('href', `/#/hubs/${hub_details.hubUUID}/ports`)
        jQuery('.hub__status span.value').text(`${hub_details.localHubStatus} (${hub_details.remoteHubStatus})`)
        jQuery('.hub__image').parent()
          .css('background-image', 'url(' + hub_details.image + ')')
          .css('background-size', 'contain')
          .css('background-repeat', 'no-repeat')

        jQuery('.popupclose, .close').on('click', function () {
          exitViewHub()
        })

      }

      function exitViewHub() {
        isFirstClick = false
        viewingHub = false
        $scope.$apply()
        popupzone.remove()
      }

      $scope.$on('cancelViewHub', function() {
        exitViewHub()
      })

      $scope.$on('cancelParkingSpotEdit', function() {
        popupzone.remove()
        $timeout(function() {
          editingParkingSpot = false
        })
      })

      /* Listens to the event emitted while the cancel button is clicked while creating or editing a zone */
      $scope.$on('cancelClicked', function () {
        if (!_.isEmpty($scope.tempData)) {
          $scope.zonesCollection.features.splice($scope.tempData.index, 0, $scope.tempData.zone)
          $scope.outlinesCollection.features.splice($scope.tempData.index, 0, $scope.tempData.outline)
          map.getSource('zones').setData($scope.zonesCollection)
          map.getSource('outline').setData($scope.outlinesCollection)
          $rootScope.$emit('zoneDataRefreshed')
        }
        editingParkingZone = false
        tempData = $scope.tempData = {}
      })
      // $scope.viewIcon = false;
      /* Listens to the event emitted while the save button is clicked while creating or editing a zone */
      $scope.$on('saveCompleted', function () {
        parkingFactory.getParkingZonesData({fleet_id: rootScopeFactory.getData('fleetId')}).then(function (response) {
          editingParkingZone = false
          $scope.zoneData = angular.copy(response)
          drawZones()
          initParkingList()
          $rootScope.$emit('zoneDataRefreshed')
          editingParkingZone = false
          if (!$scope.zonesCollection.features.length) {
            $scope.buttonZones = false
          }
          tempData = $scope.tempData = {}
        })

        $timeout(function () {
          let latestParkingState = {}
          latestParkingState.fleet_id = rootScopeFactory.getData('fleetId')
          if ($scope.fleetMetaData && _.isNumber($scope.fleetMetaData.show_parking_spots)) {
            latestParkingState.show_parking_spots = $scope.fleetMetaData.show_parking_spots
          } else {
            latestParkingState.show_parking_spots = null
          }
          $scope.zonesCollection.features.length ? latestParkingState.show_parking_zones = 1
            : latestParkingState.show_parking_zones = 0
          if (!_.isEqual(latestParkingState, {show_parking_spots: $scope.fleetMetaData.show_parking_spots,
            show_parking_zones: $scope.fleetMetaData.show_parking_zones})) {
            parkingFactory.saveParkingSettings(latestParkingState, function (response) {
              if (response && response.status === 200) {
                myFleetFactory.clearFleetCache()
                $timeout(function () {
                  $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
                  $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
                }, 250)
              }
            })
          }
        }, 1000)
      })

      let geocoder = new MapboxGeocoder({
        accessToken: lattisConstants.mapboxAccessToken
      })

      map.addControl(geocoder, 'top-left')

      function hubClick (e) {
        if (!isFirstClick) {
          return
        }

      }
      /* Map on click listener to open a popup to enter the details of the new parking spot */
      function spot_click (e) {
        if (isFirstClick === true) {
          var clickPoint = turf.point([e.lngLat.lng, e.lngLat.lat])
          if (map.getSource('createSpots')) {
            map.getSource('createSpots').setData(clickPoint)
          } else {
            map.addSource('createSpots', {
              data: clickPoint,
              type: 'geojson'
            })
            map.addLayer({
              'id': 'createparkingSpots',
              'type': 'symbol',
              'source': 'createSpots',
              'layout': {
                'icon-size': 0.6
              }
            })
          }
          map.setCenter([e.lngLat.lng, e.lngLat.lat])
          map.setLayoutProperty('createparkingSpots', 'icon-image', utilsFactory.underscoreToHyphen(icon_type))
          map.getSource('createSpots').setData(clickPoint)
          map.setLayoutProperty('createparkingSpots', 'visibility', 'visible')
          // Show a parking spot Popup
          popupzone = new mapboxgl.Popup({offset: [0, 5], anchor: 'right', closeOnClick: false})
            .setLngLat(clickPoint.geometry.coordinates)
            .setHTML('<div class="modal-content"><div class="modal-header bg-HeaderToolbar" style="padding: 15px;">' +
                            '<button type="button" class="close" style="font-size:20px;font-weight:100;">&times;</button>' +
                            '<h4 class="modal-title text-white">NEW PARKING SPOT</h4>' +
                            '</div>' +
                            '<div class="modal-body cl_grey wid-">' +
                            '<div class="row"><div class="col-sm-7">' +
                            '<div class="form-group"><p class="ft-12">NAME</p>' +
                            ' <input type="text" class="form-control nobor-rad" id="spot_name" ng-modal="spot_name" placeholder="Eg. Lower Campus East" /></div></div>' +
                            '<div class="col-sm-5"><div class="ft-12 m-t-20">Choose a short but descriptive name.' +
                            'This is the name that will appear in the app when riders search for places to ' +
                            'park.</div></div></div>' +
                            '<div class="row"><div class="col-sm-7"><div class="form-group"><p class="ft-12">DESCRIPTION</p>' +
                            '<textarea class="form-control nobor-rad" rows="3" id="spot_dec" ng-modal="spot_dec" placeholder="Eg. Lower Campus East"></textarea></div>' +
                            '</div>' +
                            '<div class="col-sm-5"><div class="ft-12 m-t-20">You can override the generic description in your profile and settings.' +
                            'Alternately, you can create a custom description for this particular bike rack.</div></div></div>' +
                            '<div class="row"><div class="col-sm-7"><div class="form-group fileupload"><p class="ft-12">PHOTO</p>' +
                            '<label class="btn-file new-txt-box wid-120 pull-left nomarg bg-lighrgery bor-rad3" ><span>Browse</span>'+
                            '<input type="file" class="hidden" ng-model="filename" id="upload-file"></label><span id="upload-file-info" class="pull-left p-tb5-lr20"></span></div>' +
                            '</div></div></div>' +
                            '<div class="modal-footer noborder">' +
                            '<div class="text-right fullwid m-tb-10">' +
                            '<button disabled = "disabled" class="btn custom-btn ft-12 bg-HeaderToolbar saveParkingSpot wid-75">SAVE ' +
                            '</button>' +
                            '</div></div></div>' +
                            '')
            .addTo($scope.map)

          var spotPopupdata = {}

          jQuery('.popupclose, .close').on('click', function () {
            spotPopupdata = {}
            exitSpot()
          })

          // FUNCTION TO CHECK FOR VALIDATION
          jQuery('#spot_name, #spot_dec, #upload-file').on('change', function () {
            spotPopupdata = {
              'name': utilsFactory.getValueof('spot_name').value,
              'description': utilsFactory.getValueof('spot_dec').value,
              'spot_img': utilsFactory.getValueof('upload-file').files[0]
            }

            var fileFormat = ['image/png', 'image/jpg', 'image/jpeg']
            var supportedFileFormats = fileFormat.includes(spotPopupdata.spot_img.type)
            if (spotPopupdata.spot_img && (spotPopupdata.spot_img.size > 500000 || supportedFileFormats === false)){
              if ($('#error-file-upload').length > 0) {
                return
              } else {
                $('.btn-file').after(
                  '<span id="error-file-upload" style="color: red; width:100%; position: absolute; margin-left: 7px;">Incorrect size or format. Please select a different file.</span>'
                );
              }
            } else {
              $('#error-file-upload').remove();
            }

            if (spotPopupdata.name === '' || spotPopupdata.description === '' || spotPopupdata.spot_img === undefined) {
              jQuery('.saveParkingSpot').prop('disabled', true)
            }
            else if (spotPopupdata.spot_img && (spotPopupdata.spot_img.size > 500000 || supportedFileFormats === false)){
                $('.saveParkingSpot').prop('disabled', true)
            } else {
              jQuery('.saveParkingSpot').prop('disabled', false)
            }
          })

          /* @function {saveParkingSpot} - onClick for the save parking spot and store a upload file in db */
          jQuery('.saveParkingSpot').on('click', function () {
            spotPopupdata = {
              'name': utilsFactory.getValueof('spot_name').value,
              'description': utilsFactory.getValueof('spot_dec').value,
              'spot_img': utilsFactory.getValueof('upload-file').files[0]
            }
            if (spotPopupdata.name === '' && spotPopupdata.description === '' && spotPopupdata.spot_img === undefined) {
              alert('Please fill out fields')
              jQuery('.saveParkingSpot').prop('disabled', true)
            } else {
              var formData = new FormData()
              formData.append('pic', spotPopupdata.spot_img)
              formData.append('name', spotPopupdata.name)
              formData.append('description', spotPopupdata.description)
              formData.append('operator_id', sessionFactory.getCookieId())
              formData.append('fleet_id', rootScopeFactory.getData('fleetId'))
              formData.append('customer_id', rootScopeFactory.getData('customerId'))
              formData.append('latitude', e.lngLat.lat)
              formData.append('longitude', e.lngLat.lng)
              formData.append('type', icon_type)
              let zoneData = $scope.map.queryRenderedFeatures(e.point, {layers: ['zoneLayer']})
              if (zoneData.length) {
                formData.append('parking_area_id', zoneData[0].properties.zone_id)
              }

              parkingFactory.createParkingSpot(formData, function (res) {
                if (res && res.status === 200) {
                  parkingFactory.clearParkingSpotCache()
                  parkingFactory.getParkingSpotsData({fleet_id: rootScopeFactory.getData('fleetId')}).then(function (response) {
                    $scope.parkingSpotsData = angular.copy(response)
                    drawData()
                    initOpacity()
                    initParkingList()
                    let latestParkingState = {}
                    latestParkingState.fleet_id = rootScopeFactory.getData('fleetId')
                    latestParkingState.show_parking_spots = 1
                    latestParkingState.show_parking_zones = $scope.fleetMetaData.show_parking_zones
                    if (!_.isEqual(latestParkingState, {show_parking_spots: $scope.fleetMetaData.show_parking_spots,
                      show_parking_zones: $scope.fleetMetaData.show_parking_zones})) {
                      parkingFactory.saveParkingSettings(latestParkingState, function (response) {
                        if (response && response.status === 200) {
                          myFleetFactory.clearFleetCache()
                          $timeout(function () {
                            $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
                            $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
                          }, 250)
                        }
                      })
                    }
                  })
                  notify({
                    message: 'Created a parking spot successfully',
                    duration: 2000,
                    position: 'right'
                  })
                } else if (res && res.status === 500) {
                  notify(
                    {
                      message: 'Can\'t create extra parking spot due to reservation settings',
                      duration: 2000,
                      position: 'right'
                    }
                  )
                } else {
                  notify({
                    message: 'Failed to create a parking spot',
                    duration: 2000,
                    position: 'right'
                  })
                }
              })
              editingParkingZone = false
              exitSpot()
            }
          })
          isFirstClick = false
        }
      }

      function loadPreference () {
        if ($scope.fleetMetaData && $scope.fleetMetaData.show_parking_zones) {
          $scope.$safeApply(function () {
            $scope.buttonZones = true
          })
          $scope.getParkingZones('init')
        } else {
          $scope.$safeApply(function () {
            $scope.buttonZones = false
          })
        }

        if ($scope.fleetMetaData && $scope.fleetMetaData.show_parking_spots) {
          $scope.$safeApply(function () {
            $scope.buttonSpots = true
          })
          $scope.getParkingSpots('init')
        }

        if (JSON.parse(sessionStorage.getItem('isPublicParkingSpotEnabled'))) {
          $scope.$safeApply(function () {
            $scope.publicButton = true
          })
          $scope.showPublicSpots()
        }
      }

      function exitSpot () {
        isFirstClick = false
        editingParkingSpot = false
        editingParkingZone = false
        $scope.map.setLayoutProperty('createparkingSpots', 'visibility', 'none')
        $scope.spotselect = {'spot1': false, 'spot2': false, 'rack': false}
        $scope.$apply()
        popupzone.remove()
      }
      var dom = document.getElementById('map')
      dom.addEventListener('wheel', function (e) {
        e.stopPropagation()
      }, false)
    })
