angular.module('skyfleet').factory('MapCircle', [
  'MapShape',
  function(MapShape) {
    function Circle() {
      MapShape.call(this)
    }

    Circle.prototype = Object.create(MapShape.prototype)

    Object.defineProperty(Circle.prototype, 'constructor', {
      value: Circle,
      enumerable: false,
      writable: true
    })

    Circle.prototype.addPoint = function(point) {
      if (this.center) {
        this.radius = turf.distance(turf.point(this.center), turf.point(point))
      } else {
        this.center = point
      }
    }

    Circle.prototype.getFillGeoJSON = function() {
      if (this.isComplete()) {
        return turf.circle(this.center, this.radius)
      }
      return null
    }

    Circle.prototype.getPointsGeoJSON = function() {
      var points = []
      if (this.center) {
        points.push(turf.point(this.center))
        if (this.radius) {
          points.push(
            turf.destination(turf.point(this.center), this.radius, 0),
            turf.destination(turf.point(this.center), this.radius, 90),
            turf.destination(turf.point(this.center), this.radius, 180),
            turf.destination(turf.point(this.center), this.radius, 270)
          )
        }
      }
      return turf.featureCollection(points)
    }

    Circle.prototype.getOutlineGeoJSON = function() {
      return this.getFillGeoJSON()
    }

    Circle.prototype.isComplete = function() {
      return this.center && this.radius
    }

    Circle.prototype.toJSON = function() {
      return {
        shape: 'circle',
        center: { longitude: this.center[0], latitude: this.center[1] },
        radius: { value: this.radius, units: 'kilometers' }
      }
    }

    return Circle
  }
])
