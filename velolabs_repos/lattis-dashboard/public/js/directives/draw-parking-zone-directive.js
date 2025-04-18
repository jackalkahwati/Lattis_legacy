'use strict'

angular.module('skyfleet')
  .directive('drawParkingZone',
    ['editParkingZoneFactory', '$rootScope',
      function (editParkingZoneFactory, $rootScope) {
        return {
          restrict: 'E',
          scope: {
            map: '='
          },
          template: '<div>' +
                    "<img ng-src='images/Line.png'' class='map-zones map-line inline-block' ng-class='{\"maptool_select\": toolselect.poly == true}' ng-click='polygonClick(); tool'>" +
                    "<img ng-src='images/square.png'' class='map-zones inline-block' ng-class='{\"maptool_select\": toolselect.rect == true}' ng-click='rectangleClick()'>" +
                    "<img ng-src='images/circle.png'' class='map-zones inline-block' ng-class='{\"maptool_select\": toolselect.circle == true}' ng-click='circleClick()'> </div>",
          link: function (scope, element, attr) {
            /* todo use proper help texts in popup while starting to draw a parking zone */
            /* Variables initialization */
            var isFirstClick = true
            var dataHandler = []
            var drawCompleted = false
            var id
            var polyGeo = null
            var defaultCursor = scope.map.getCanvas().style.cursor;
            scope.toolselect = {'rect': false, 'circle': false, 'poly': false}

            scope.$on('createSpot', function () {
              scope.map.off('click', rectangle_click)
              scope.map.off('click', circle_click)
              scope.map.off('click', polygon_click)
              scope.map.off('mousemove', rectangle_hover)
              scope.map.off('mousemove', circle_hover)
              scope.map.off('mousemove', polygon_hover)
            })

            /* Draw circle initialization function */
            scope.circleClick = function () {
              $rootScope.$broadcast('createZone')
              $rootScope.$emit('createZone')
              scope.toolselect = {'rect': false, 'circle': true, 'poly': false}
              scope.reinitializeVariables()
              id = Math.floor((Math.random() * 1000) + 1)
              scope.map.addSource('circle' + id.toString(), {
                data: turf.point([0, 0]),
                type: 'geojson'
              })
              scope.circleSelected = !scope.circleSelected
              scope.map.off('click', rectangle_click)
              scope.map.on('click', circle_click)
              scope.map.off('click', polygon_click)
              scope.map.off('mousemove', rectangle_hover)
              scope.map.on('mousemove', circle_hover)
              scope.map.off('mousemove', polygon_hover)
            }

            scope.rectangleClick = function () {
              $rootScope.$broadcast('createZone')
              $rootScope.$emit('createZone')
              scope.toolselect = {'rect': true, 'circle': false, 'poly': false}
              scope.reinitializeVariables()
              id = Math.floor((Math.random() * 1000) + 1)
              scope.map.on('click', rectangle_click)
              scope.map.off('click', circle_click)
              scope.map.off('click', polygon_click)
              scope.map.on('mousemove', rectangle_hover)
              scope.map.off('mousemove', circle_hover)
              scope.map.off('mousemove', polygon_hover)
            }

            scope.polygonClick = function () {
              $rootScope.$broadcast('createZone')
              $rootScope.$emit('createZone')
              scope.toolselect = {'rect': false, 'circle': false, 'poly': true}
              scope.reinitializeVariables()
              id = Math.floor((Math.random() * 1000) + 1)
              /* Add the Start Point Geojson Source and Layer to the map */
              scope.map.addSource('polystart' + id.toString(), {
                data: turf.point([0, 0]),
                type: 'geojson'
              })
              scope.map.addLayer({
                'id': 'start' + id.toString(),
                'type': 'symbol',
                'source': 'polystart' + id.toString(),
                'layout': {
                  'icon-image': 'circle',
                  'icon-size': 0.85
                }
              })
              polyGeo = {
                'type': 'Feature',
                'properties': {},
                'geometry': {
                  'type': 'Polygon',
                  'coordinates': [dataHandler]
                }
              }
              scope.map.off('click', rectangle_click)
              scope.map.off('click', circle_click)
              scope.map.on('click', polygon_click)
              scope.map.off('mousemove', rectangle_hover)
              scope.map.off('mousemove', circle_hover)
              scope.map.on('mousemove', polygon_hover)
            }

            scope.addOutline = function () {
              scope.map.addSource('outline' + id.toString(), {
                data: turf.lineString([[0, 0], [0, 0]]),
                type: 'geojson'
              })
              scope.map.addLayer({
                'id': 'outline' + id.toString(),
                'type': 'line',
                'source': 'outline' + id.toString(),
                'layout': {
                  'line-join': 'round',
                  'line-cap': 'round'
                },
                'paint': {
                  'line-color': '#F65295',
                  'line-width': 2,
                  'line-opacity': 0.5
                }
              }, 'start' + id.toString())
            }

            scope.addPoints = function (source, before) {
              if (!scope.map.getLayer('points' + id.toString())) {
                if (before) {
                  scope.map.addLayer({
                    'id': 'points' + id.toString(),
                    'type': 'symbol',
                    'source': source,
                    'layout': {
                      'icon-image': 'circle',
                      'icon-size': 0.85
                    }
                  }, before)
                } else {
                  scope.map.addSource(source, {
                    data: turf.featureCollection([turf.point([0, 0])]),
                    type: 'geojson'
                  })
                  scope.map.addLayer({
                    'id': 'points' + id.toString(),
                    'type': 'symbol',
                    'source': source,
                    'layout': {
                      'icon-image': 'circle',
                      'icon-size': 0.85
                    }
                  })
                }
              }
            }

            scope.reinitializeVariables = function () {
              scope.map.getCanvas().style.cursor = defaultCursor
              isFirstClick = true
              dataHandler = []
              drawCompleted = false
              id = Math.floor((Math.random() * 1000) + 1)
              scope.map.off('click', rectangle_click)
              scope.map.off('click', circle_click)
              scope.map.off('click', polygon_click)
              scope.map.off('mousemove', rectangle_hover)
              scope.map.off('mousemove', circle_hover)
              scope.map.off('mousemove', polygon_hover)
            }

            function polygon_click (e) {
              /* Check for clicks on polygon start point */
              if (scope.map.getLayer('start' + id.toString())) {
                var features = scope.map.queryRenderedFeatures(e.point, {
                  layers: ['start' + id.toString()]
                })
              }
              if (isFirstClick && !drawCompleted) {
                isFirstClick = false
                scope.map.dragPan.disable()
                /* Push the 1st and last points of the polygon */
                dataHandler.push([e.lngLat.lng, e.lngLat.lat], [e.lngLat.lng, e.lngLat.lat])
                scope.map.getSource('polystart' + id.toString()).setData(turf.point([e.lngLat.lng, e.lngLat.lat]))
                scope.addOutline()
              } else if (features && features.length) {
                dataHandler.splice(dataHandler.length - 1, 1, dataHandler[0])
                scope.map.getSource('poly' + id.toString()).setData(turf.featureCollection([polyGeo]))
                drawCompleted = true
                scope.map.removeLayer('points' + id.toString())
                scope.map.removeLayer('start' + id.toString())
                scope.map.removeLayer('outline' + id.toString())
                editParkingZoneFactory.editHandler(scope.map, 'poly' + id.toString(), {
                  'index': 0,
                  'type': 'polygon'
                }, 'save')
                scope.reinitializeVariables()
                scope.map.dragPan.enable()
              } else if (!drawCompleted) {
                /* push the datas into the Point array */
                /* Add the Points Geojson Source and Layer to the map */
                scope.addPoints('polystart' + id.toString(), 'points' + id.toString(), )
                /* Set the new data from the mouse event to the source of the points layer */
                scope.map.getSource('polystart' + id.toString()).setData(turf.explode(polyGeo))
                scope.map.getSource('outline' + id.toString()).setData(turf.lineString(dataHandler))
                /* Set the new data from the mouse event to the source of the polygon layer */
                dataHandler.splice(dataHandler.length - 1, 0, [e.lngLat.lng, e.lngLat.lat])
                scope.map.getSource('poly' + id.toString()).setData(polyGeo)
              }
            }

            function polygon_hover (e) {
              scope.map.getCanvas().style.cursor = 'crosshair'
              if (scope.map.getLayer('start' + id.toString())) {
                var features = scope.map.queryRenderedFeatures(e.point, {
                  layers: ['start' + id.toString()]
                })
                scope.map.getCanvas().style.cursor = features.length ? 'pointer' : 'crosshair'
              }
              if (!isFirstClick && !drawCompleted) {
                dataHandler.splice(dataHandler.length - 1, 1, [e.lngLat.lng, e.lngLat.lat])
                /* Add the Polygon Geojson Source and Layer to the map */
                if (!scope.map.getLayer('poly' + id.toString())) {
                  scope.map.addSource('poly' + id.toString(), {
                    data: polyGeo,
                    type: 'geojson'
                  })
                  scope.map.addLayer({
                    'id': 'poly' + id.toString(),
                    'type': 'fill',
                    'source': 'poly' + id.toString(),
                    'layout': {},
                    'paint': {
                      'fill-outline-color': '#F65295',
                      'fill-color': '#F65295',
                      'fill-opacity': 0.3
                    }
                  }, 'start' + id.toString())
                }
                /* Set the new data from the mouse event to the source of the polygon layer */
                scope.map.getSource('poly' + id.toString()).setData(polyGeo)
                scope.map.getSource('outline' + id.toString()).setData(turf.lineString(dataHandler))
              }
            }

            function rectangle_click (e) {
              var features = scope.map.queryRenderedFeatures(e.point, {
                layers: ['points' + id.toString()]
              })
              if (isFirstClick && !drawCompleted) {
                isFirstClick = false
                scope.map.dragPan.disable()
                dataHandler.push([e.lngLat.lng, e.lngLat.lat], [e.lngLat.lng, e.lngLat.lat])
              } else if (!drawCompleted) {
                drawCompleted = true
                scope.map.dragPan.enable()
                dataHandler.splice(dataHandler.length - 3, 1, [e.lngLat.lng, e.lngLat.lat])
                dataHandler.splice(dataHandler.length - 4, 1, [dataHandler[2][0], dataHandler[0][1]])
                dataHandler.splice(dataHandler.length - 2, 1, [dataHandler[0][0], dataHandler[2][1]])
                /* Set the new data from the mouse event to the source of the rectangle layer */
                scope.map.getSource('rect' + id.toString()).setData(turf.featureCollection([turf.polygon([dataHandler])]))
                scope.map.getSource('outline' + id.toString()).setData(turf.lineString(dataHandler))
                scope.map.getSource('point' + id.toString()).setData(turf.explode(turf.polygon([dataHandler])))
                scope.map.dragPan.enable()
                scope.map.removeLayer('points' + id.toString())
                scope.map.removeLayer('outline' + id.toString())
                editParkingZoneFactory.editHandler(scope.map, 'rect' + id.toString(), {
                  'index': 0,
                  'type': 'rectangle',
                  'center': JSON.stringify(dataHandler[0])
                }, 'save')
                scope.reinitializeVariables()
              }
            }

            function rectangle_hover (e) {
              scope.map.getCanvas().style.cursor = 'crosshair'
              if (!isFirstClick && !drawCompleted && dataHandler.length == 2) {
                dataHandler.splice(dataHandler.length - 1, 0, [e.lngLat.lng, e.lngLat.lat])
                /* Calculate the 2nd and 4th point from the 1st and 2nd point */
                dataHandler.splice(dataHandler.length - 2, 0, [dataHandler[1][0], dataHandler[0][1]])
                dataHandler.splice(dataHandler.length - 1, 0, [dataHandler[0][0], dataHandler[2][1]])
                if (!scope.map.getLayer('rect' + id.toString())) {
                  scope.addOutline()
                  scope.map.addSource('rect' + id.toString(), {
                    data: turf.polygon([
                      [
                        [0, 0],
                        [0, 0],
                        [0, 0],
                        [0, 0],
                        [0, 0]
                      ]
                    ]),
                    type: 'geojson'
                  })
                  scope.map.addLayer({
                    'id': 'rect' + id.toString(),
                    'type': 'fill',
                    'source': 'rect' + id.toString(),
                    'layout': {},
                    'paint': {
                      'fill-antialias': true,
                      'fill-outline-color': '#F65295',
                      'fill-color': '#F65295',
                      'fill-opacity': 0.3
                    }
                  })
                  scope.addPoints('point' + id.toString())
                }
                /* Set the new data from the mouse event to the source of the rectangle layer */
                scope.map.getSource('rect' + id.toString()).setData(turf.polygon([dataHandler]))
                scope.map.getSource('outline' + id.toString()).setData(turf.lineString(dataHandler))
                scope.map.getSource('point' + id.toString()).setData(turf.explode(turf.polygon([dataHandler])))
              } else if (!isFirstClick && !drawCompleted) {
                dataHandler.splice(dataHandler.length - 3, 1, [e.lngLat.lng, e.lngLat.lat])
                /* Calculate the 2nd and 4th point from the 1st and 3rd point */
                dataHandler.splice(dataHandler.length - 4, 1, [dataHandler[2][0], dataHandler[0][1]])
                dataHandler.splice(dataHandler.length - 2, 1, [dataHandler[0][0], dataHandler[2][1]])
                /* Set the new data from the mouse event to the source of the rectangle layer */
                scope.map.getSource('rect' + id.toString()).setData(turf.polygon([dataHandler]))
                scope.map.getSource('outline' + id.toString()).setData(turf.lineString(dataHandler))
                scope.map.getSource('point' + id.toString()).setData(turf.explode(turf.polygon([dataHandler])))
              }
            }

            function circle_click (e) {
              if (isFirstClick && !drawCompleted) {
                isFirstClick = false
                scope.map.dragPan.disable()
                dataHandler.push([e.lngLat.lng, e.lngLat.lat])
                scope.addOutline()
                scope.map.getSource('circle' + id.toString()).setData(turf.point([e.lngLat.lng, e.lngLat.lat]))
                scope.map.addLayer({
                  'id': 'circle' + id.toString(),
                  'type': 'fill',
                  'source': 'circle' + id.toString(),
                  'layout': {},
                  'paint': {
                    'fill-outline-color': '#F65295',
                    'fill-color': '#F65295',
                    'fill-opacity': 0.3
                  }
                })
              } else {
                scope.map.dragPan.enable()
                drawCompleted = true
                scope.map.removeLayer('points' + id.toString())
                scope.map.removeLayer('outline' + id.toString())
                editParkingZoneFactory.editHandler(scope.map, 'circle' + id.toString(), {
                  'index': 0,
                  'radius': turf.distance(turf.point(dataHandler[0]), turf.point(dataHandler[1]), 'miles'),
                  'type': 'circle',
                  'center': JSON.stringify(dataHandler[0])
                }, 'save')
                scope.reinitializeVariables()
              }
            }

            function circle_hover (e) {
              scope.map.getCanvas().style.cursor = 'crosshair'
              if (!isFirstClick && !drawCompleted) {
                scope.map.dragPan.disable()
                dataHandler[1] = [e.lngLat.lng, e.lngLat.lat]
                scope.map.getSource('circle' + id.toString()).setData(turf.featureCollection([turf.circle(turf.point(dataHandler[0]), turf.distance(turf.point(dataHandler[0]), turf.point(dataHandler[1]), 'miles'), 1000, 'miles')]))
                scope.addPoints('points' + id.toString())
                var destination = turf.featureCollection([turf.destination(turf.point(dataHandler[0]), turf.distance(turf.point(dataHandler[0]), turf.point(dataHandler[1]), 'miles'), 0, 'miles'),
                  turf.destination(turf.point(dataHandler[0]), turf.distance(turf.point(dataHandler[0]), turf.point(dataHandler[1]), 'miles'), 90, 'miles'),
                  turf.destination(turf.point(dataHandler[0]), turf.distance(turf.point(dataHandler[0]), turf.point(dataHandler[1]), 'miles'), 180, 'miles'),
                  turf.destination(turf.point(dataHandler[0]), turf.distance(turf.point(dataHandler[0]), turf.point(dataHandler[1]), 'miles'), 270, 'miles')
                ])
                scope.map.getSource('points' + id.toString()).setData(destination)
                scope.map.getSource('outline' + id.toString()).setData(turf.lineString(turf.circle(turf.point(dataHandler[0]), turf.distance(turf.point(dataHandler[0]), turf.point(dataHandler[1]), 'miles'), 1000, 'miles').geometry.coordinates[0]))
              }
            }

            scope.$on('cancelClicked', function (event, layerData) {
              scope.map.setLayoutProperty('explode', 'visibility', 'none')
              scope.map.setLayoutProperty('outlineEdit', 'visibility', 'none')
              scope.map.setLayoutProperty(layerData, 'visibility', 'none')
            })
          }
        }
      }
    ])
