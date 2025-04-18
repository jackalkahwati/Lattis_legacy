'use strict'

angular.module('skyfleet').factory('mapFactory', function (lattisConstants) {
  mapboxgl.accessToken = lattisConstants.mapboxAccessToken
  return ({
    /* @function {initMap} initiate a map object with the given center point and style
             * @param {center} Center latlng of the map
             * @param {zoom} Zoom level of the map
             * @todo Try to calculate the center of the map and zoom level based on the GeoJson
             */
    initMap: function (center, zoom) {
      var map = new mapboxgl.Map({
        container: 'map',
        style: 'https://s3-us-west-2.amazonaws.com/mapbox.sprite/light-v9.json',
        center: center,
        zoom: zoom,
        attributionControl: false
      })
      .addControl(new mapboxgl.AttributionControl({
        compact: true
      }));

      $('#map').mouseover(function () {
        $('body').getNiceScroll().hide()
      })
      $('#map').mouseout(function () {
        $('body').getNiceScroll().show()
      })

      return map
    },

    addLineString: function (mapObject, layerName, sourceData, style) {
      mapObject.addLayer({
        'id': layerName,
        'type': 'line',
        'source': {
          'type': 'geojson',
          'data': sourceData
        },
        'interactive': true,
        'layout': {
          'line-join': 'round',
          'line-cap': 'round'
        },
        'paint': style
      })
    },

    addMarker: function (mapObject, layerName, sourceData, style) {
      mapObject.addLayer({
        'id': layerName,
        'type': 'symbol',
        'source': {
          'type': 'geojson',
          'data': sourceData
        },
        'interactive': true,
        'layout': style
      })
    },

    addFillLayer: function (mapObject, layerName, sourceData, style) {
      mapObject.addLayer({
        'id': layerName,
        'type': 'fill',
        'source': {
          'type': 'geojson',
          'data': sourceData
        },
        'layout': {},
        'paint': style
      })
    },

    mapFitBounds: function (mapObject, geoJSON, padding) {
      var boundingBox = turf.bbox(geoJSON)
      switch (padding) {
        case 'low':
          mapObject.fitBounds([[boundingBox[0], boundingBox[1] - 0.010], [boundingBox[2], boundingBox[3] + 0.005]], {
            linear: true
          })
          break
        case 'medium':
          mapObject.fitBounds([[boundingBox[0], boundingBox[1] - 0.15], [boundingBox[2], boundingBox[3] + 0.25]], {
            linear: true
          })
          break
        case 'parking':
          mapObject.fitBounds([[boundingBox[0], boundingBox[1]], [boundingBox[2], boundingBox[3]]], {
            linear: true, padding: {top: 100, bottom: 80, left: 80, right: 180}
          })
          break
      }
    },
    setIconSize: function (mapObject, zoomLevel, iconLayer) {
      if (zoomLevel < 8) {
        mapObject.setLayoutProperty(iconLayer, 'icon-size', 0.3)
      } else if (zoomLevel < 12) {
        mapObject.setLayoutProperty(iconLayer, 'icon-size', 0.5)
      } else if (zoomLevel < 16) {
        mapObject.setLayoutProperty(iconLayer, 'icon-size', 0.6)
      } else if (zoomLevel > 16) {
        mapObject.setLayoutProperty(iconLayer, 'icon-size', 0.8)
      }
    }
  })
}
)
