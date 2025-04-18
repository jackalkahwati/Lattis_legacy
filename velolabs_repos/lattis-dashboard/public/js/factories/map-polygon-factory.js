angular.module('skyfleet').factory('MapPolygon', [
  'MapShape',
  function(MapShape) {
    function Polygon() {
      MapShape.call(this)
      this.points = []
      this.complete = false
    }

    Polygon.prototype = Object.create(MapShape.prototype)

    Object.defineProperty(Polygon.prototype, 'constructor', {
      value: Polygon,
      enumerable: false,
      writable: true
    })

    Polygon.prototype.addPoint = function(point) {
      let found = -1
      for (var i = 0; i < this.points.length; i++) {
        if (this.points[i][0] === point[0] && this.points[i][1] === point[1]) {
          found = i
          break
        }
      }

      // do not re-add points except the first
      if (found > 0) {
        return
      }

      this.points.push(point)

      // if re-adding the first point, the polygon is complete
      if (found === 0 && this.points.length >= 3) {
        this.complete = true
      }
    }

    Polygon.prototype.getFillGeoJSON = function() {
      if (this.points.length > 2) {
        return turf.polygon([this.points.concat([this.points[0]])])
      }
      return null
    }

    Polygon.prototype.getPointsGeoJSON = function() {
      return turf.featureCollection(
        this.points.map(function(point) {
          return turf.point(point, { lng: point[0], lat: point[1] })
        })
      )
    }

    Polygon.prototype.getOutlineGeoJSON = function() {
      if (this.points.length > 1) {
        return turf.lineString(this.points)
      }
    }

    Polygon.prototype.isComplete = function() {
      return this.complete
    }

    Polygon.prototype.toJSON = function() {
      return {
        shape: 'polygon',
        points: this.points.map(function(point) {
          return { longitude: point[0], latitude: point[1] }
        })
      }
    }

    return Polygon
  }
])
