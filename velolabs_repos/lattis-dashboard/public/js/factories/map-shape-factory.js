angular.module('skyfleet').factory('MapShape', function () {
  function Shape() {}

  /**
   * Adds a point to the shape
   */
  Shape.prototype.addPoint = function (point) {}

  /**
   * Gets the GeoJSON to fill the shape
   */
  Shape.prototype.getFillGeoJSON = function () {}

  /**
   * Gets the GeoJSON to show the points that highlight the shape
   */
  Shape.prototype.getPointsGeoJSON = function () {}

  /**
   * Gets the GeoJSON to show the outline of the shape
   */
  Shape.prototype.getOutlineGeoJSON = function () {}

  /**
   * Returns where enough points have been added to complete the shape
   */
  Shape.prototype.isComplete = function () {}

  /**
   * Returns JSON representation of the shape
   */
  Shape.prototype.toJSON = function () {}

  /**
   * Clones the shape
   */
  Shape.prototype.clone = function () {
    var copy = new this.constructor()
    for (var attr in this) {
      if (this.hasOwnProperty(attr)) {
        if (Array.isArray(this[attr])) {
          copy[attr] = this[attr].slice()
        } else if (typeof this[attr] === 'object') {
          copy[attr] = this.clone.call(copy[attr])
        } else {
          copy[attr] = this[attr]
        }
      }
    }
    return copy
  }

  Shape.prototype.addToMap = function (map, layerIdPrefix) {
    var geoJson = {
      points: this.getPointsGeoJSON(),
      outline: this.getOutlineGeoJSON(),
      fill: this.getFillGeoJSON()
    }

    var layers = {
      prefix: layerIdPrefix,
      points: layerIdPrefix + '-points',
      outline: layerIdPrefix + '-outline',
      fill: layerIdPrefix + '-fill'
    }

    if (geoJson.fill) {
      if (map.getSource(layers.fill)) {
        map.getSource(layers.fill).setData(geoJson.fill)
      } else {
        map.addSource(layers.fill, {
          type: 'geojson',
          data: geoJson.fill
        })
      }
      if (!map.getLayer(layers.fill)) {
        map.addLayer(
          {
            id: layers.fill,
            type: 'fill',
            source: layers.fill,
            paint: {
              'fill-outline-color': '#ffff6b',
              'fill-color': '#ffff6b',
              'fill-opacity': 0.3
            }
          },
          map.getLayer(layers.points) ? layers.points : null
        )
      }
    } else if (map.getLayer(layers.fill)) {
      map.removeLayer(layers.fill)
    }

    if (geoJson.outline) {
      if (map.getSource(layers.outline)) {
        map.getSource(layers.outline).setData(geoJson.outline)
      } else {
        map.addSource(layers.outline, {
          data: geoJson.outline,
          type: 'geojson'
        })
      }
      if (!map.getLayer(layers.outline)) {
        map.addLayer(
          {
            id: layers.outline,
            type: 'line',
            source: layers.outline,
            layout: {
              'line-join': 'round',
              'line-cap': 'round'
            },
            paint: {
              'line-color': '#c6a700',
              'line-width': 2,
              'line-opacity': 0.5
            }
          },
          map.getLayer(layers.points) ? layers.points : null
        )
      }
    } else if (map.getLayer(layers.outline)) {
      map.removeLayer(layers.outline)
    }

    if (geoJson.points) {
      if (map.getSource(layers.points)) {
        map.getSource(layers.points).setData(geoJson.points)
      } else {
        map.addSource(layers.points, {
          type: 'geojson',
          data: geoJson.points
        })
      }
      if (!map.getLayer(layers.points)) {
        map.addLayer({
          id: layers.points,
          type: 'symbol',
          source: layers.points,
          layout: {
            'icon-image': 'circle',
            'icon-size': 0.85
          }
        })
      }
    } else if (map.getLayer(layers.points)) {
      map.removeLayer(layers.points)
    }

    return layers
  }

  return Shape
})
