'use strict'

angular.module('skyfleet.controllers').controller('geofencesController', [
  '$rootScope',
  '$scope',
  '$timeout',
  '$http',
  'rootScopeFactory',
  'mapFactory',
  'notify',
  'lattisConstants',
  'MapCircle',
  'MapPolygon',
  'MapRectangle',
  function (
    $rootScope,
    $scope,
    $timeout,
    $http,
    rootScopeFactory,
    mapFactory,
    notify,
    lattisConstants,
    MapCircle,
    MapPolygon,
    MapRectangle
  ) {
    var map = mapFactory.initMap([-97.5122717, 40.828412], 3)
    var showingPopup = false
    var geofenceList = []
    var newGeofenceShape
    var popup
    $rootScope.showLoader = true
    $scope.showGeofences = false
    $scope.showShapeSelector = false
    $scope.showHint = false
    $scope.selectedShape = ''
    $scope.showMapLegend = false
    $scope.showEmptyMessage = false

    map.dragRotate.disable()
    map.addControl(new mapboxgl.NavigationControl(), 'bottom-right')
    map.addControl(
      new MapboxGeocoder({
        accessToken: lattisConstants.mapboxAccessToken
      }),
      'top-left'
    )

    $scope.toggleGeofences = function (e) {
      if (!geofenceList.length) {
        $scope.showEmptyMessage = !$scope.showEmptyMessage
        e.preventDefault()
        return
      }
      $http
        .patch(`api/fleet/geofences/status/${ rootScopeFactory.getData('fleetId') }`, {
          status: !$scope.showGeofences
        })
        .then(
          function (response) {
            $scope.showGeofences = !!response.data.payload.geofence_enabled;
            notify({
              message: 'Geofence status updated successfully',
              duration: 2000,
              position: 'right'
            })
            $rootScope.showLoader = false
            geofenceList.forEach(function (geofence) {
              map.setLayoutProperty(geofence.layers.fill, 'visibility', $scope.showGeofences ? 'visible' : 'none')
              map.setLayoutProperty(geofence.layers.outline, 'visibility', $scope.showGeofences ? 'visible' : 'none')
              map.setLayoutProperty(
                geofence.layers.points,
                'visibility',
                $scope.showGeofences ? map.getLayoutProperty(geofence.layers.points, 'visibility') : 'none'
              )
            })
          },
          function () {
            notify({
              message: 'Failed to update geofence status',
              duration: 2000,
              position: 'right'
            })
            $rootScope.showLoader = false
          }
        )
      if (!$scope.showGeofences && showingPopup) {
        popup.remove()
      }
      if ($scope.showGeofences) {
        focusGeofences()
      }
    }

    $scope.toggleShapeSelector = function () {
      $scope.showShapeSelector = !$scope.showShapeSelector
      if ($scope.showEmptyMessage && $scope.showShapeSelector) {
        $scope.showEmptyMessage = false
      }
      if ($scope.showHint && $scope.showShapeSelector) {
        $scope.showHint = false
      }
      updateMapCursor()
      if (!$scope.showShapeSelector && newGeofenceShape) {
        clearNewGeofence()
      }
    }

    $scope.toggleHint = function () {
      $scope.showHint = !$scope.showHint
    }

    $scope.setSelectedShape = function (shape) {
      if (newGeofenceShape && $scope.selectedShape !== shape) {
        clearNewGeofence()
      }
      $scope.selectedShape = shape
      updateMapCursor()
    }

    $scope.toggleMapLegend = function () {
      $scope.showMapLegend = !$scope.showMapLegend
    }

    map.on("load", function(){
      if (rootScopeFactory.getData('fleetId')) {
        fetchGeofences(rootScopeFactory.getData('fleetId'))
      }
      $scope.$on('fleetChange', function (event, fleetId) {
          fetchGeofences(fleetId)
      })
    })

    map.on('click', onMapClick)
    map.on('mousemove', onMapMouseMove)

    function isCreating() {
      return $scope.showShapeSelector && $scope.selectedShape
    }

    function clearNewGeofence() {
      newGeofenceShape = null
      map.removeLayer('new-geofence-points')
      map.removeLayer('new-geofence-outline')
      map.removeLayer('new-geofence-fill')
    }

    function updateMapCursor() {
      if (isCreating()) {
        map.getCanvas().style.cursor = 'crosshair'
      } else {
        map.getCanvas().style.cursor = ''
      }
    }

    function fetchGeofences(fleetId) {
      $http
        .get('api/fleet/geofences', {
          params: {
            fleet_id: fleetId
          }
        })
        .then(
          function (response) {
            const { geofences, enabled } = response.data.payload;
            geofenceList = geofences.map(renderGeofence)
            if(enabled && !!enabled.geofence_enabled) {
              $scope.showGeofences = !!geofenceList.length
            } else $scope.showGeofences = false
            geofenceList.forEach(function (geofence) {
              map.setLayoutProperty(geofence.layers.fill, 'visibility', $scope.showGeofences ? 'visible' : 'none')
              map.setLayoutProperty(geofence.layers.outline, 'visibility', $scope.showGeofences ? 'visible' : 'none')
              map.setLayoutProperty(
                geofence.layers.points,
                'visibility',
                $scope.showGeofences ? map.getLayoutProperty(geofence.layers.points, 'visibility') : 'none'
              )
            })
            if($scope.showGeofences) focusGeofences()
            $rootScope.showLoader = false
          },
          function () {
            notify({
              message: 'Failed to fetch geofences',
              duration: 2000,
              position: 'right'
            })
            $rootScope.showLoader = false
          }
        )
    }

    function focusGeofences() {
      map.fitBounds(
        turf.bbox(
          turf.featureCollection(
            geofenceList.map(function (geofence) {
              return geofence.shape.getFillGeoJSON()
            })
          )
        ),
        {
          linear: true,
          padding: { top: 50, bottom: 50, left: 50, right: 50 }
        }
      )
    }

    function renderGeofence(geofence, index) {
      geofence.shape = parseGeometry(geofence.geometry)
      geofence.layers = geofence.shape.addToMap(map, 'geofence-' + geofence.geofence_id)
      map.setLayoutProperty(geofence.layers.points, 'visibility', 'none')
      addEventListeners(geofence, index)
      return geofence
    }

    function parseGeometry(geometry) {
      var shape

      switch (geometry.shape) {
        case 'circle':
          shape = new MapCircle()
          shape.center = [geometry.center.longitude, geometry.center.latitude]
          shape.radius = geometry.radius.value
          break
        case 'rectangle':
          shape = new MapRectangle()
          shape.bbox = geometry.bbox
          break
        case 'polygon':
          shape = new MapPolygon()
          shape.points = geometry.points.map(function (point) {
            return [point.longitude, point.latitude]
          })
          break
        default:
          return null
      }
      return shape
    }

    function addEventListeners(geofence, index) {
      map.on('mouseover', geofence.layers.fill, onGeofenceHover)
      map.on('mouseout', geofence.layers.fill, onGeofenceMouseOut)
      map.on('click', geofence.layers.fill, onGeofenceClick)

      function onGeofenceHover() {
        if (!isCreating()) {
          map.getCanvas().style.cursor = 'pointer'
        }
      }

      function onGeofenceMouseOut() {
        if (!isCreating()) {
          map.getCanvas().style.cursor = ''
        }
      }

      function onGeofenceClick() {
        if (showingPopup || isCreating()) {
          return
        }
        map.setLayoutProperty(geofence.layers.points, 'visibility', 'visible')
        showingPopup = true
        showPopup({
          shape: geofence.shape,
          name: geofence.name,
          id: geofence.layers.prefix,
          onClose: function () {
            map.setLayoutProperty(geofence.layers.points, 'visibility', 'none')
            showingPopup = false
          },
          onDelete: function (popup) {
            $rootScope.showLoader = true
            $http.delete('api/fleet/geofences/' + geofence.geofence_id).then(
              function () {
                popup.remove()
                map.removeLayer(geofence.layers.points)
                map.removeLayer(geofence.layers.outline)
                map.removeLayer(geofence.layers.fill)
                geofenceList.splice(index, 1) // remove from geofenceList
                if (!geofenceList.length) {
                  $scope.showGeofences = false
                }
                notify({
                  message: 'Geofence deleted successfully',
                  duration: 2000,
                  position: 'right'
                })
                $rootScope.showLoader = false
              },
              function () {
                notify({
                  message: 'Failed to delete geofence',
                  duration: 2000,
                  position: 'right'
                })
                $rootScope.showLoader = false
              }
            )
          },
          onSave: function (details, popup) {
            $rootScope.showLoader = true
            $http
              .patch('api/fleet/geofences/' + geofence.geofence_id, {
                name: details.name
              })
              .then(
                function (response) {
                  popup.remove()
                  map.off('mouseover', geofence.layers.fill, onGeofenceHover)
                  map.off('mouseout', geofence.layers.fill, onGeofenceMouseOut)
                  map.off('click', geofence.layers.fill, onGeofenceClick)
                  geofenceList[index] = renderGeofence(response.data.payload)
                  notify({
                    message: 'Geofence updated successfully',
                    duration: 2000,
                    position: 'right'
                  })
                  $rootScope.showLoader = false
                },
                function () {
                  notify({
                    message: 'Failed to update geofence',
                    duration: 2000,
                    position: 'right'
                  })
                  $rootScope.showLoader = false
                }
              )
          }
        })
      }
    }

    function onMapClick(e) {
      if (isCreating()) {
        if (!newGeofenceShape) {
          switch ($scope.selectedShape) {
            case 'circle':
              newGeofenceShape = new MapCircle()
              break
            case 'rectangle':
              newGeofenceShape = new MapRectangle()
              break
            case 'polygon':
              newGeofenceShape = new MapPolygon()
              break
          }
        }
        if (newGeofenceShape instanceof MapPolygon) {
          var features = map.getLayer('new-geofence-points')
            ? map.queryRenderedFeatures(e.point, { layers: ['new-geofence-points'] })
            : []
          if (features.length) {
            newGeofenceShape.addPoint([features[0].properties.lng, features[0].properties.lat])
          } else {
            newGeofenceShape.addPoint([e.lngLat.lng, e.lngLat.lat])
          }
        } else {
          newGeofenceShape.addPoint([e.lngLat.lng, e.lngLat.lat])
        }
        newGeofenceShape.addToMap(map, 'new-geofence')
        if (newGeofenceShape.isComplete()) {
          finishCreating()
        }
      }
    }

    function onMapMouseMove(e) {
      if (isCreating() && newGeofenceShape && !newGeofenceShape.isComplete()) {
        var tempGeofence = newGeofenceShape.clone()
        tempGeofence.addPoint([e.lngLat.lng, e.lngLat.lat])
        tempGeofence.addToMap(map, 'new-geofence')
      }
    }

    function finishCreating() {
      $timeout(function () {
        $scope.showShapeSelector = false
        $scope.selectedShape = ''
      })
      map.getCanvas().style.cursor = ''

      showPopup({
        id: 'new-geofence',
        shape: newGeofenceShape,
        onClose: function () {
          clearNewGeofence()
        },
        onDelete: function (popup) {
          popup.remove()
        },
        onSave: function (details, popup) {
          $rootScope.showLoader = true

          $http
            .post('api/fleet/create-geofence', {
              fleet_id: rootScopeFactory.getData('fleetId'),
              name: details.name,
              geometry: JSON.stringify(newGeofenceShape)
            })
            .then(
              function (response) {
                popup.remove()
                geofenceList.push(renderGeofence(response.data.payload))
                notify({
                  message: 'Geofence created successfully',
                  duration: 2000,
                  position: 'right'
                })
                $scope.showGeofences = true
                $rootScope.showLoader = false
              },
              function () {
                notify({
                  message: 'Failed to create geofence',
                  duration: 2000,
                  position: 'right'
                })
                $rootScope.showLoader = false
              }
            )
        }
      })
    }

    function showPopup(options) {
      var bbox = turf.bbox(options.shape.getFillGeoJSON())
      var lngLat = [bbox[2], bbox[3]]

      if (!options.name) {
        options.name = ''
      }

      popup = new mapboxgl.Popup({
        offset: [5, 55],
        anchor: 'left',
        closeOnClick: true
      })
        .setLngLat(lngLat)
        .setHTML(
          '<div class="modal-content ' +
          options.id +
          '">' +
          '<div class="modal-header bg-HeaderToolbar">' +
          '  <button type="button" class="close ft-16" style="margin-right: -14px;margin-top:-6px;font-size:18px:font-weight:100;">&times;</button>' +
          '  <h4 class="modal-title text-white titleview">' +
          options.name +
          '</h4>' +
          '</div>' +
          '<div class="modal-body cl_grey">' +
          '  <div class="form-group">' +
          '    <p class="changeEdit"><i class="fa fa-pencil"></i> EDIT NAME</p>' +
          '    <input type="text" class="form-control nobor-rad" id="geofence_name" placeholder="Eg. Lower Campus East" value="' +
          options.name +
          '"/>' +
          '  </div>' +
          '</div>' +
          '<div class="modal-footer noborder">' +
          '  <div class="text-left fullwid">' +
          '    <button class="btn custombtn-default pbtn-tb11-lr20 ft-12 delete-geofence">DELETE</button>' +
          '    <button class="btn pull-right custom-btn pbtn-tb11-lr20 ft-12 save-geofence">SAVE</button>' +
          '  </div>' +
          '</div>' +
          '</div>'
        )
        .addTo(map)

      map.fitBounds(bbox, {
        linear: true,
        padding: { top: 80, bottom: 10, left: 250, right: 450 }
      })

      var popupEl = jQuery('.' + options.id)

      popupEl.find('.close').click(function () {
        popup.remove()
      })

      popupEl.find('.save-geofence').click(function () {
        var name = jQuery('.' + options.id + ' input#geofence_name').val()
        if (name) {
          options.onSave({ name: name }, popup)
        } else {
          notify({
            message: 'Name is required',
            duration: 2000,
            position: 'right'
          })
        }
      })

      popupEl.find('.delete-geofence').click(function () {
        options.onDelete(popup)
      })

      popup.on('close', function () {
        options.onClose(popup)
        popup = null
      })
    }
  }
])
