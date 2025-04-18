'use strict'

angular.module('skyfleet').factory('editParkingZoneFactory',
  [
    '$rootScope',
    'utilsFactory',
    'parkingFactory',
    'rootScopeFactory',
    'sessionFactory',
    '$timeout',
    'notify',
    'mapFactory', function ($rootScope, utilsFactory, parkingFactory, rootScopeFactory, sessionFactory, $timeout, notify, mapFactory) {
      let geoRecovered
      return {
        editHandler: function (mapObject, layerData, zoneProps, editType) {
          /* Variables initializations */
          var startEdit = false
          var clickedPoint = null
          var popupZone
          var popup
          var dataPoints = zoneProps.type == 'circle' ? explodeCircle(JSON.parse(zoneProps.center), zoneProps.radius)
            : explodePolygon(mapObject.getSource(layerData)._data.features[zoneProps.index])
          var finalData = mapObject.getSource(layerData)._data
          var defaultCursor = mapObject.getCanvas().style.cursor;

          if (!mapObject.getSource('exploded')) {
            mapObject.addSource('outlineEdit', {
              data: turf.lineString([[0, 0], [0, 0]]),
              type: 'geojson'
            })
            mapObject.addLayer({
              'id': 'outlineEdit',
              'type': 'line',
              'source': 'outlineEdit',
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
            mapObject.addSource('exploded', {
              data: turf.point([0, 0]),
              type: 'geojson'
            })
            mapObject.addLayer({
              'id': 'explode',
              'type': 'symbol',
              'source': 'exploded',
              'layout': {
                'icon-image': 'circle',
                'icon-size': 0.85
              }
            })
          }

          mapObject.getSource('outlineEdit').setData(drawOutline(mapObject.getSource(layerData)._data.features[zoneProps.index]))
          mapObject.getSource('exploded').setData(dataPoints)
          mapObject.setLayoutProperty('explode', 'visibility', 'visible')
          mapObject.setLayoutProperty('outlineEdit', 'visibility', 'visible')
          popup ? movePopupLocation(finalData) : show_popup(finalData)
          if (zoneProps.center) {
            finalData.features[zoneProps.index].properties.center = JSON.parse(zoneProps.center)
            finalData.features[zoneProps.index].properties.radius = zoneProps.radius
          }

          switch (zoneProps.type) {
            case 'rectangle':
              mapObject.on('click', rectangleClickListener)
              mapObject.on('mousemove', rectangleHoverListener)
              break
            case 'polygon':
              mapObject.on('click', polygonClickListener)
              mapObject.on('mousemove', polygonHoverListener)
              break
            case 'circle':
              mapObject.on('click', circleClickListener)
              mapObject.on('mousemove', circleHoverListener)
              break
          }

          mapObject.on('mousemove', changeCursor)

          function changeCursor (e) {
            var features = mapObject.queryRenderedFeatures(e.point, {
              layers: ['explode']
            })
            if (features.length || startEdit) {
              mapObject.getCanvas().style.cursor = 'crosshair'
            } else {
              mapObject.getCanvas().style.cursor = defaultCursor
            }
          }

          function rectangleClickListener (e) {
            var pointToEdit = mapObject.queryRenderedFeatures(e.point, {
              layers: ['explode']
            })
            if (pointToEdit.length && !startEdit) {
              startEdit = true
              clickedPoint = pointToEdit[0].properties.index
              popup.remove()
              mapObject.dragPan.disable()
            } else if (startEdit) {
              mapObject.getSource(layerData).setData(finalData)
              startEdit = false
              movePopupLocation(explodePolygon(finalData.features[zoneProps.index]))
              mapObject.dragPan.enable()
            }
          }

          function rectangleHoverListener (e) {
            if (startEdit) {
              switch (clickedPoint) {
                case 0:
                  finalData.features[zoneProps.index].geometry.coordinates[0][0] = finalData.features[zoneProps.index].geometry.coordinates[0][4] = [e.lngLat.lng, e.lngLat.lat]
                  finalData.features[zoneProps.index].geometry.coordinates[0][1][1] = e.lngLat.lat
                  finalData.features[zoneProps.index].geometry.coordinates[0][3][0] = e.lngLat.lng
                  mapObject.getSource(layerData).setData(finalData)
                  mapObject.getSource('exploded').setData(explodePolygon(finalData.features[zoneProps.index]))
                  mapObject.getSource('outlineEdit').setData(drawOutline(finalData.features[zoneProps.index]))
                  break
                case 1:
                  finalData.features[zoneProps.index].geometry.coordinates[0][1] = [e.lngLat.lng, e.lngLat.lat]
                  finalData.features[zoneProps.index].geometry.coordinates[0][0][1] = finalData.features[zoneProps.index].geometry.coordinates[0][4][1] = e.lngLat.lat
                  finalData.features[zoneProps.index].geometry.coordinates[0][2][0] = e.lngLat.lng
                  mapObject.getSource(layerData).setData(finalData)
                  mapObject.getSource('exploded').setData(explodePolygon(finalData.features[zoneProps.index]))
                  mapObject.getSource('outlineEdit').setData(drawOutline(finalData.features[zoneProps.index]))
                  break
                case 2:
                  finalData.features[zoneProps.index].geometry.coordinates[0].splice(finalData.features[zoneProps.index].geometry.coordinates[0].length - 3, 1, [e.lngLat.lng, e.lngLat.lat])
                  finalData.features[zoneProps.index].geometry.coordinates[0].splice(finalData.features[zoneProps.index].geometry.coordinates[0].length - 4, 1, [finalData.features[zoneProps.index].geometry.coordinates[0][2][0], finalData.features[zoneProps.index].geometry.coordinates[0][0][1]])
                  finalData.features[zoneProps.index].geometry.coordinates[0].splice(finalData.features[zoneProps.index].geometry.coordinates[0].length - 2, 1, [finalData.features[zoneProps.index].geometry.coordinates[0][0][0], finalData.features[zoneProps.index].geometry.coordinates[0][2][1]])
                  mapObject.getSource(layerData).setData(finalData)
                  mapObject.getSource('exploded').setData(explodePolygon(finalData.features[zoneProps.index]))
                  mapObject.getSource('outlineEdit').setData(drawOutline(finalData.features[zoneProps.index]))
                  break
                case 3:
                  finalData.features[zoneProps.index].geometry.coordinates[0][3] = [e.lngLat.lng, e.lngLat.lat]
                  finalData.features[zoneProps.index].geometry.coordinates[0][0][0] = finalData.features[zoneProps.index].geometry.coordinates[0][4][0] = e.lngLat.lng
                  finalData.features[zoneProps.index].geometry.coordinates[0][2][1] = e.lngLat.lat
                  mapObject.getSource(layerData).setData(finalData)
                  mapObject.getSource('exploded').setData(explodePolygon(finalData.features[zoneProps.index]))
                  mapObject.getSource('outlineEdit').setData(drawOutline(finalData.features[zoneProps.index]))
                  break
              }
            }
          }

          function polygonClickListener (e) {
            var pointToEdit = mapObject.queryRenderedFeatures(e.point, {
              layers: ['explode']
            })
            if (pointToEdit.length && !startEdit) {
              startEdit = true
              clickedPoint = pointToEdit[0].properties.index
              popup.remove()
              mapObject.dragPan.disable()
            } else if (startEdit) {
              mapObject.getSource(layerData).setData(finalData)
              startEdit = false
              movePopupLocation(explodePolygon(finalData.features[zoneProps.index]))
              mapObject.dragPan.enable()
            }
          }

          function polygonHoverListener (e) {
            if (startEdit && clickedPoint == 0 || clickedPoint == finalData.features[zoneProps.index].geometry.coordinates[0].length - 1) {
              finalData.features[zoneProps.index].geometry.coordinates[0][finalData.features[zoneProps.index].geometry.coordinates[0].length - 1] = [e.lngLat.lng, e.lngLat.lat]
              finalData.features[zoneProps.index].geometry.coordinates[0][0] = [e.lngLat.lng, e.lngLat.lat]
              finalData.features[zoneProps.index].geometry.coordinates = [finalData.features[zoneProps.index].geometry.coordinates[0]]
              mapObject.getSource(layerData).setData(finalData)
              mapObject.getSource('exploded').setData(explodePolygon(finalData.features[zoneProps.index]))
              mapObject.getSource('outlineEdit').setData(drawOutline(finalData.features[zoneProps.index]))
            } else if (startEdit) {
              finalData.features[zoneProps.index].geometry.coordinates[0][clickedPoint] = [e.lngLat.lng, e.lngLat.lat]
              mapObject.getSource(layerData).setData(finalData)
              mapObject.getSource('exploded').setData(explodePolygon(finalData.features[zoneProps.index]))
              mapObject.getSource('outlineEdit').setData(drawOutline(finalData.features[zoneProps.index]))
            }
          }

          function circleClickListener (e) {
            var pointToEdit = mapObject.queryRenderedFeatures(e.point, {
              layers: ['explode']
            })
            if (pointToEdit.length && !startEdit && pointToEdit[0].properties.type == 'center') {
              startEdit = true
              clickedPoint = true
              popup.remove()
              mapObject.dragPan.disable()
            } else if (pointToEdit.length && !startEdit) {
              startEdit = true
              clickedPoint = false
              popup.remove()
              mapObject.dragPan.disable()
            } else if (startEdit) {
              startEdit = false
              clickedPoint = null
              movePopupLocation(finalData)
              mapObject.dragPan.enable()
            }
          }

          function circleHoverListener (e) {
            if (startEdit && clickedPoint) {
              var tempObj = finalData.features[zoneProps.index].properties
              finalData.features[zoneProps.index] = turf.circle(turf.point([e.lngLat.lng, e.lngLat.lat]), tempObj.radius, 1000, 'miles')
              finalData.features[zoneProps.index]['properties'] = {
                'center': [e.lngLat.lng, e.lngLat.lat],
                'index': zoneProps.index,
                'radius': tempObj.radius,
                'type': zoneProps.type
              }
              mapObject.getSource(layerData).setData(finalData)
              mapObject.getSource('exploded').setData(explodeCircle([e.lngLat.lng, e.lngLat.lat], tempObj.radius))
              mapObject.getSource('outlineEdit').setData(drawOutline(mapObject.getSource(layerData)._data.features[zoneProps.index]))
            } else if (startEdit) {
              var tempObj = finalData.features[zoneProps.index].properties
              finalData.features[zoneProps.index] = turf.circle(turf.point(finalData.features[zoneProps.index].properties.center), turf.distance(turf.point(finalData.features[zoneProps.index].properties.center), turf.point([e.lngLat.lng, e.lngLat.lat]), 'miles'), 1000, 'miles')
              finalData.features[zoneProps.index]['properties'] = {
                'center': tempObj.center,
                'index': zoneProps.index,
                'radius': turf.distance(turf.point(tempObj.center), turf.point([e.lngLat.lng, e.lngLat.lat]), 'miles'),
                'type': zoneProps.type
              }
              mapObject.getSource(layerData).setData(finalData)
              mapObject.getSource('exploded').setData(explodeCircle(finalData.features[zoneProps.index].properties.center, turf.distance(turf.point(finalData.features[zoneProps.index].properties.center), turf.point([e.lngLat.lng, e.lngLat.lat]), 'miles')))
              mapObject.getSource('outlineEdit').setData(drawOutline(mapObject.getSource(layerData)._data.features[zoneProps.index]))
            }
          }

          function findPopupLocation (featureLayer) {
            var bbox = turf.bbox(featureLayer)
            return [bbox[2], bbox[3]]
          }

          function explodePolygon (geoJSONFeature) {
            var data = turf.explode(geoJSONFeature)
            _.each(data.features, function (element, index) {
              element.properties = {'index': index}
            })
            return data
          }

          function drawOutline (geoJSONFeature) {
            return turf.lineString(geoJSONFeature.geometry.coordinates[0])
          }

          function explodeCircle (center, radius) {
            var destination = turf.featureCollection([turf.destination(center, radius, 0, 'miles'),
              turf.destination(turf.point(center), radius, 90, 'miles'),
              turf.destination(turf.point(center), radius, 180, 'miles'),
              turf.destination(turf.point(center), radius, 270, 'miles'),
              turf.point(center, {'type': 'center'})
            ])
            return destination
          }

          function movePopupLocation (featureLayer) {
            popup.setLngLat(findPopupLocation(featureLayer))
              .addTo(mapObject)
          }

          function show_popup (geo) {
            popup = new mapboxgl.Popup({offset: [5, 55], anchor: 'left',closeOnClick: true})
              .setLngLat(findPopupLocation(geo))
              .setHTML('<div class="modal-content"><div class="modal-header bg-HeaderToolbar">' +
                            '<button type="button" class="close ft-16" style="margin-right: -14px;margin-top:-6px;font-size:18px:font-weight:100;">&times;</button>' +
                            '<h4 class="modal-title text-white titleview"></h4>' +
                            '</div>' +
                            '<div class="modal-body cl_grey">' +
                            '<div class="form-group"><p class="changeEdit">NAME</p>' +
                            ' <input type="text" class="form-control nobor-rad zoneW" id="parkingZone" ng-modal="zone_name" placeholder="Eg. Lower Campus East" /></div></div>' +
                            '<div class="modal-footer noborder">' +
                            '<div class="text-left fullwid">' +
                            '<button class="btn custombtn-default pbtn-tb11-lr20 ft-12 popupclose">CANCEL</button>' +
                            '<button class="btn custombtn-default pbtn-tb11-lr20 ft-12 zonedelete">DELETE</button>' +
                            '<button class="btn pull-right custom-btn pbtn-tb11-lr20 ft-12 saveZone">SAVE ' +
                            '</button>' +
                            '</div></div></div>' + '')
              .addTo(mapObject)
            mapObject.easeTo({
              center: popup.getLngLat(),
              zoom: 14
            })
            if (geo.features[0].properties) {
              jQuery('.titleview').text(geo.features[0].properties.name)
              jQuery('.changeEdit').html("<i class='fa fa-pencil'></i> EDIT NAME")
              jQuery('.popupclose').css('display', 'none')
              jQuery('.zonedelete').css('display :inline-block', 'background : #394d65')
              jQuery('.saveZone').removeClass('bg-HeaderToolbar')
              geoRecovered = geo.features[0]
            } else {
              jQuery('.titleview').text('NEW PARKING ZONE')
              jQuery('.zonedelete').css('display', 'none')
              jQuery('.popupclose').css('display', 'inline-block')
              jQuery('.saveZone').addClass('bg-HeaderToolbar')
            }
            popupZone = angular.element(document.getElementById('parkingZone'))

            if (editType === 'edit') {
              popupZone[0].value = zoneProps.name
            }
          }

          function removeAllListeners () {
            mapObject.getCanvas().style.cursor = defaultCursor
            popup.remove()
            startEdit = false
            clickedPoint = null
            dataPoints = null
            finalData = null
            mapObject.off('click', rectangleClickListener)
            mapObject.off('mousemove', rectangleHoverListener)
            mapObject.off('click', polygonClickListener)
            mapObject.off('mousemove', polygonHoverListener)
            mapObject.off('click', circleClickListener)
            mapObject.off('mousemove', circleHoverListener)
            mapObject.off('mousemove', changeCursor)
          }

          var removeEditCancelListener = $rootScope.$on('cancelParkingZoneEdit', function () {
            $rootScope.$broadcast('cancelClicked', layerData)
            removeAllListeners()
            removeEditCancelListener()
          })

          jQuery('.popupclose, .close').on('click', function () {
            $rootScope.$broadcast('cancelClicked', layerData)
            removeAllListeners()
          })

          jQuery('.zonedelete').on('click', function (e) {
            if (geoRecovered && geoRecovered.properties.zone_id) {
              parkingFactory.deleteParkingZone({parking_area_id: geoRecovered.properties.zone_id}, function (response) {
                if (response && response.status === 200) {
                  parkingFactory.clearParkingZonesCache()
                  $rootScope.$broadcast('saveCompleted')
                  removeAllListeners()
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
            } else {
              $rootScope.$broadcast('cancelClicked', layerData)
              removeAllListeners()
            }
          })

          jQuery('.saveZone').on('click', function (e) {
            if (popupZone[0].value != '') {
              zoneHandler(zoneProps.type, editType)
              removeAllListeners()
            } else {
              alert('Please enter a name for the created parking zone')
            }
          })

          function completeSave () {
            parkingFactory.clearParkingZonesCache()
            $rootScope.$broadcast('saveCompleted')
            notify({
              message: 'Saved the parking zone successfully',
              duration: 2000,
              position: 'right'
            })
          }

          $rootScope.$on('zoneDataRefreshed', function (event, id) {
            $timeout(function () {
              mapObject.setLayoutProperty('explode', 'visibility', 'none')
              mapObject.setLayoutProperty('outlineEdit', 'visibility', 'none')
              mapObject.setLayoutProperty(layerData, 'visibility', 'none')
            }, 350)
          })

          function zoneHandler (zoneType, editType) {
            var editGeometry
            if (editType === 'save') {
              zoneType === 'circle' ? editGeometry = [{
                'latitude': JSON.parse(zoneProps.center)[1],
                'longitude': JSON.parse(zoneProps.center)[0],
                'radius': utilsFactory.getMeters(zoneProps.radius)
              }] : editGeometry = utilsFactory.featureToJsonObject(finalData)
              parkingFactory.addParkingZone({
                'fleet_id': rootScopeFactory.getData('fleetId'),
                'operator_id': sessionFactory.getCookieId(),
                'geometry': editGeometry,
                'type': zoneProps.type,
                'name': popupZone[0].value
              }, function (res) {
                if (res && res.status === 200) {
                  completeSave()
                } else {
                  notify({
                    message: 'Failed to saved the parking zone',
                    duration: 2000,
                    position: 'right'
                  })
                }
              })
            } else if (editType === 'edit') {
              zoneType === 'circle' ? editGeometry = [{
                'latitude': finalData.features[zoneProps.index].properties.center[1],
                'longitude': finalData.features[zoneProps.index].properties.center[0],
                'radius': utilsFactory.getMeters(finalData.features[zoneProps.index].properties.radius)
              }] : editGeometry = utilsFactory.featureToJsonObject(finalData)
              parkingFactory.editParkingZone({
                'fleet_id': rootScopeFactory.getData('fleetId'),
                'operator_id': sessionFactory.getCookieId(),
                'geometry': editGeometry,
                'type': zoneProps.type,
                'parking_area_id': zoneProps.zone_id,
                'name': popupZone[0].value
              }, function (res) {
                if (res && res.status === 200) {
                  completeSave()
                } else {
                  notify({
                    message: 'Failed to saved the parking zone',
                    duration: 2000,
                    position: 'right'
                  })
                }
              })
            }
          }
        }
      }
    }])
