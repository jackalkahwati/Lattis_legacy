'use strict'

angular.module('skyfleet.controllers')
  .controller('bikeLocatorController',
    function ($scope, $rootScope, $state, mapFactory, myFleetFactory, rootScopeFactory, _, parkingFactory, utilsFactory, bikeFleetFactory, tripAnimationFactory, $interval, $timeout, $q) {
      $scope.currenttab = 'parkinghubs'
      $scope.bikeNames = []
      let popup
      var bikeSetpopup
      let isPopUpOpen = false
      $scope.isDamage = false
      $scope.filter = {}
      var clickCount = 0
      // MapInitialization
      let promise
      var map = mapFactory.initMap([-97.5122717, 40.828412], 3)
      map.dragRotate.disable()
      map.addControl(new mapboxgl.NavigationControl({
        position: 'top-right'
      }))

      map.on('zoom', function () {
        if (map.getLayer('parkingSpots')) {
          mapFactory.setIconSize(map, map.getZoom(), 'parkingSpots')
        }
      })

      if (rootScopeFactory.getData('fleetId')) {
        getBikesData(rootScopeFactory.getData('fleetId'))
        startTimer()
        parkingFactory.getParkingZonesData({fleet_id: rootScopeFactory.getData('fleetId')}).then(function (res) {
          $scope.zoneData = angular.copy(res)
        })
      }

      function startTimer () {
        stopTimer()
        promise = $interval(fetchBikes, 100000)
      }

      function stopTimer () {
        $interval.cancel(promise)
      }

      function fetchBikes () {
        bikeFleetFactory.clearBikeDataCache()
        $timeout(function () {
          getBikesData(rootScopeFactory.getData('fleetId'))
        }, 20)
      }

      $scope.$on('$destroy', function () {
        stopTimer()
      })

      $scope.$on('fleetChange', function (event, id) {
        getBikesData(id)
        startTimer()
        parkingFactory.getParkingZonesData({fleet_id: id}).then(function (res) {
          $scope.zoneData = angular.copy(res)
          loadPreference()
        })
      })

      function getBikesData (fleetId) {
        $q.all([bikeFleetFactory.getBikesData({fleet_id: fleetId}), tripAnimationFactory.getAllTrips({fleet_id: fleetId})]).then(function (response) {
          if (response[0].payload) {
            $scope.featureArray = []
            $scope.bikeNames = []
            $scope.bikesData = angular.copy(response[0].payload)
            _.each($scope.bikesData.bike_data, function (element, index) {
              if (element.latitude && element.status !== 'inactive' && element.status !== 'deleted') {
                if (element.current_status === 'reported_stolen') element.current_status = 'stolen'
                if (element.current_status === 'on_trip') {
                  let trip = fetchLatestTrips(response[1].payload.trips, element.bike_id)
                  if (_.isObject(trip)) {
                    $scope.featureArray.push(turf.point(utilsFactory.lastPointInStepsArray(trip.steps), {
                      bikeId: element.bike_id,
                      bikeName: element.bike_name,
                      bikeStatus: element.current_status,
                      iconType: setStatus(element.current_status, element.battery_level),
                      description: element.description
                    }))
                    $scope.bikeNames.push({ value: element.bike_name })
                  }
                } else {
                  $scope.featureArray.push(turf.point([element.longitude, element.latitude], {
                    bikeId: element.bike_id,
                    bikeName: element.bike_name,
                    bikeStatus: element.current_status,
                    iconType: setStatus(element.current_status, element.battery_level),
                    description: element.description
                  }))
                  $scope.bikeNames.push({ value: element.bike_name })
                }
              }
            })
            if (isPopUpOpen) {
              _.each($scope.featureArray, function (element) {
                if (element.properties.bikeId == bikeSetpopup) {
                  popup.setLngLat(element.geometry.coordinates).addTo(map)
                }
              })
            }
            $scope.dataCollection = turf.featureCollection($scope.featureArray)
            if ($scope.selectedBike) {
              findBike()
            } else {
              filterDisabled()
            }
            $timeout(function () {
              if ($scope.dataCollection.features.length && map.getZoom() < 5) {
                mapFactory.mapFitBounds(map, $scope.dataCollection, 'low')
              }
            }, 3000)
          }
        })
      }

      function filterDisabled () {
        if ($scope.filter) {
          var copyData = angular.copy($scope.dataCollection)
          _.each($scope.filter, function (element, key) {
            if (!element) {
              copyData.features = _.reject(copyData.features, function (x) {
                return x.properties.iconType === utilsFactory.underscoreToHyphen(key)
              })
            }
          })
          if (map.getSource('newPoints')) {
            map.getSource('newPoints').setData(copyData)
          }
        }
      }

      $scope.updateData = function () {
        var copyData = angular.copy($scope.dataCollection)
        if (!$scope.filter.ellipse_battery_low) {
          _.each(copyData.features, function (element, index) {
            if (element.properties.iconType === 'ellipse-battery-low') {
              copyData.features[index].properties.iconType = setStatus(element.properties.bikeStatus, null)
            }
          })
          _.each($scope.filter, function (element, key) {
            if (key !== 'ellipse-battery-low' && !element) {
              copyData.features = _.reject(copyData.features, function (x) {
                return x.properties.iconType === utilsFactory.underscoreToHyphen(key)
              })
            }
            if (clickCount == 1 && isPopUpOpen) {
              jQuery('.mapboxgl-popup').css('visibility', 'hidden')
              clickCount = 0
            } else {
              jQuery('.mapboxgl-popup').css('visibility', 'visible')
              clickCount = 1
            }
          })
        } else {
          _.each($scope.filter, function (element, key) {
            if (!element) {
              copyData.features = _.reject(copyData.features, function (x) {
                return x.properties.iconType === utilsFactory.underscoreToHyphen(key)
              })
            }
          })
        }
        map.getSource('newPoints').setData(copyData)
        startTimer()
      }

      $scope.findBike = findBike

      function findBike () {
        var collectionCopy = angular.copy($scope.dataCollection)
        if ($scope.selectedBike) {
          var filteredFeatures =  _.filter(collectionCopy.features, function (feature) {
            return feature.properties.bikeName === $scope.selectedBike
          })
          if (filteredFeatures.length) {
            collectionCopy.features = filteredFeatures;
            map.getSource('newPoints').setData(collectionCopy)
            mapFactory.mapFitBounds(map, collectionCopy, 'low')
          }
        }
      }

      function setStatus (status, battery) {
        if (battery && battery <= 15) {
          return 'ellipse-battery-low'
        } else if (status === 'on_trip') {
          return 'in-ride'
        } else if (status === 'parked') {
          return 'parked'
        } else if (status === 'damaged' || status === 'under_maintenance') {
          return 'maintenance'
        } else if (status === 'stolen') {
          return status
        }
      }

      function fetchLatestTrips (tripdData, bikeId) {
        let filterd = _.where(tripdData, {bike_id: bikeId})
        if (_.isObject(_.max(filterd, function (data) {
          return data.trip_id
        }))) {
          return _.max(filterd, function (data) {
            return data.trip_id
          })
        }
      }

      map.on('load', function () {
        map.addSource('newPoints', {
          data: $scope.dataCollection || turf.featureCollection([]),
          type: 'geojson',
          cluster: true,
          clusterMaxZoom: 20,
          clusterRadius: 120
        })

        map.addLayer({
          'id': 'parkingSpots',
          'type': 'symbol',
          'source': 'newPoints',
          'layout': {
            'icon-image': '{iconType}',
            'icon-size': 0.6,
            'icon-allow-overlap': true,
            'text-allow-overlap': true
          }
        })

        map.addLayer({
          id: 'clusters',
          type: 'circle',
          source: 'newPoints',
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

        map.addLayer({
          id: 'cluster-count',
          type: 'symbol',
          source: 'newPoints',
          filter: ['has', 'point_count'],
          layout: {
            'text-field': '{point_count_abbreviated}',
            'text-font': ['DIN Offc Pro Medium', 'Arial Unicode MS Bold'],
            'text-size': 12
          }
        })
        mapFactory.setIconSize(map, map.getZoom(), 'parkingSpots')

        map.addSource('geofence', {
          data: turf.featureCollection([turf.point([0, 0])]),
          type: 'geojson'
        })
        map.addLayer({
          'id': 'polygon',
          'type': 'fill',
          'source': 'geofence',
          'layout': {},
          'paint': {
            'fill-outline-color': '#7F8087',
            'fill-color': '#7F8087',
            'fill-opacity': 0.55
          }
        })
      })

      /* parking zone related code */

      /* @function {zoneRawDataToGeoJSON} - converts raw data from api response to GeoJSON */
      function zoneRawDataToGeoJSON () {
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
            $scope.outlineCollection.push(turf.lineString(polygonFeature.geometry.coordinates[0]))
            $scope.zoneCollection.push(polygonFeature)
          } else if (element.type === 'circle') {
            var circleFeature = turf.circle(turf.point([JSON.parse(element.geometry)[0].longitude, JSON.parse(element.geometry)[0].latitude]), utilsFactory.getMiles(JSON.parse(element.geometry)[0].radius), 10000, 'miles')
            circleFeature['properties'] = {
              'type': element.type,
              'radius': utilsFactory.getMiles(JSON.parse(element.geometry)[0].radius),
              'index': index,
              'center': [JSON.parse(element.geometry)[0].longitude, JSON.parse(element.geometry)[0].latitude],
              'zone_id': element.parking_area_id,
              'name': element.name
            }
            $scope.outlineCollection.push(turf.lineString(circleFeature.geometry.coordinates[0]))
            $scope.zoneCollection.push(circleFeature)
          }
        })
        $scope.zonesCollection = turf.featureCollection($scope.zoneCollection)
        $scope.outlinesCollection = turf.featureCollection($scope.outlineCollection)
      }

      function addZoneToMap (data) {
        if (map.getSource('zones')) {
          map.getSource('zones').setData(data)
          map.getSource('outline').setData($scope.outlinesCollection)
        } else {
          map.addSource('zones', {
            data: data,
            type: 'geojson'
          })
          map.addLayer({
            'id': 'zoneLayer',
            'type': 'fill',
            'source': 'zones',
            'layout': {},
            'paint': {
              'fill-outline-color': '#F65295',
              'fill-color': '#F65295',
              'fill-opacity': 0.18
            }
          })
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
            'paint': {
              'line-color': '#F65295',
              'line-width': 2,
              'line-opacity': 0.5
            }
          })
        }
        if ($scope.parkingToggle) {
          $scope.map.setLayoutProperty('zoneLayer', 'visibility', 'none')
          $scope.map.setLayoutProperty('outline', 'visibility', 'none')
        }
      }

      /* Start of zone part */
      /* @function {$scope.getParkingZones} - onClick for the show all parking zones button */
      $scope.getParkingZones = function () {
        if (!map.getSource('zones')) {
          zoneRawDataToGeoJSON()
          addZoneToMap($scope.zonesCollection)
        }
        if (!$scope.parkingToggle) {
          if ($scope.zoneCollection.length) {
            mapFactory.mapFitBounds(map, $scope.zonesCollection, 'low')
          }
          map.setLayoutProperty('zoneLayer', 'visibility', 'visible')
          map.setLayoutProperty('outline', 'visibility', 'visible')
        } else {
          map.setLayoutProperty('zoneLayer', 'visibility', 'none')
          map.setLayoutProperty('outline', 'visibility', 'none')
        }
      }

      map.on('click', 'parkingSpots', function (e) {
        bikeSetpopup = e.features[0].properties.bikeId
        if (!isPopUpOpen) {
          popup = new mapboxgl.Popup({offset: [8, -5], anchor: 'left', closeOnClick: false})
            .setLngLat(e.features[0].geometry.coordinates)
          if (e.features[0].properties.iconType !== 'maintenance') {
            popup.setHTML(rideContent(e.features[0].properties))
              .addTo(map)
          } else {
            $scope.$apply(function () {
              $scope.isDamage = true
            })
            popup.setHTML(maintenanceContent(e.features[0].properties))
              .addTo(map)
          }
          isPopUpOpen = true
        }
        jQuery('.popupclose').on('click', function () {
          popup.remove()
          isPopUpOpen = false
          $scope.$apply(function () {
            $scope.isDamage = false
          })
        })
        jQuery('.viewRide').click(function () {
          window.location = '/#/bike-details/' + $scope.currentBikeId
        })
      })

      function loadPreference () {
        $scope.$safeApply(function () {
          $scope.parkingToggle = false
        })
        if (!map.getSource('zones')) {
          zoneRawDataToGeoJSON()
          addZoneToMap($scope.zonesCollection)
        }
      }

      function maintenanceContent (feature) {
        $scope.currentBikeId = feature.bikeId
        var innerContent = ''
        innerContent += '<div class="modal-content"><div class="modal-header bg-HeaderToolbar">'
        innerContent += '<h4 class="modal-title text-white inline-block">' + feature.iconType + '</h4>'
        innerContent += '<span class="pull-right text-white pointer popupclose ft-20">x</span></div>'
        innerContent += '<div class="modal-body cl_grey"><div class="row p-b-20"><div class="col-sm-12 popup-box">'
        innerContent += '<ul class="list-unstyled list-inline"><li><span>Ride name:</span></li><li class="wid-230"><span class="fw-300">' + feature.bikeName + '</span></li></ul>'
        innerContent += '<ul class="list-unstyled list-inline"><li><span>Trip ID:</span></li><li class="wid-230"><span class="fw-300">#224</span></li></ul>'
        innerContent += '<ul class="list-unstyled list-inline"><li><span>User:</span></li><li class="wid-230"><span class="fw-300">John Smith</span></li></ul>'
        innerContent += '<ul class="list-unstyled list-inline"><li><span>Trip date:</span></li><li class="wid-230"><span class="fw-300">April 29th 2017</span></li></ul>'
        innerContent += '<ul class="list-unstyled list-inline"><li><span>Pick up:</span></li><li class="wid-230"><span class="fw-300">Marsh Street Hub (#12) 11:15am</span></li></ul>'
        innerContent += '<ul class="list-unstyled list-inline"><li><span>Drop off:</span></li><li class="wid-230"><span class="fw-300">Marsh Street Hub (#12) 11:15am</span></li></ul>'
        innerContent += '<ul class="list-unstyled list-inline"><li><span>Description:</span></li><li class="wid-230"><span class="fw-300">There is a problem with the gears, they keep slipping. Was not possible to continue Ride.</span></li></ul>'
        innerContent += '<div class="text-center fullwid m-t-20"><button class="btn custom-btn notransition bg-HeaderToolbar viewRide" ng-click="goToBike()">VIEW RIDE</button></div>'
        innerContent += '</div></div></div>'
        return innerContent
      }

      function rideContent (feature) {
        $scope.currentBikeId = feature.bikeId
        var html = ''
        html += '<div class="modal-content"><div class="modal-header bg-HeaderToolbar">'
        html += '<h4 class="modal-title text-white inline-block text-uppercase">' + feature.iconType + '</h4>'
        html += '<span class="pull-right text-white pointer popupclose ft-20">x</span></div>'
        html += '<div class="modal-body cl_grey">'
        html += '<div class="row p-b-20"><div class="col-sm-12 popup-box">'
        html += '<ul class="list-unstyled list-inline"><li><span>Description</span></li><li><span class="fw-300" style="display: inline-block;width: 250px;">' + feature.description + ' </span></li></ul>'
        html += '<ul class="list-unstyled list-inline"><li><span>Ride name</span></li><li><span class="fw-300" style="display: inline-block;width: 250px;">' + feature.bikeName + '</span></li></ul>'
        html += '<div class="text-center fullwid m-t-20"><button class="btn custom-btn notransition bg-HeaderToolbar viewRide" ng-click="goToBike()">VIEW RIDE</button></div>'
        html += '</div></div></div>'
        return html
      }
      var dom = document.getElementById('map')
      dom.addEventListener('wheel', function (e) {
        e.stopPropagation()
      }, false)
    })
