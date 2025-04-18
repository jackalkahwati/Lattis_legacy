angular.module('skyfleet').factory('MapRectangle', [
  'MapShape',
  function(MapShape) {
    function Rectangle() {
      MapShape.call(this)
    }

    Rectangle.prototype = Object.create(MapShape.prototype)

    Object.defineProperty(Rectangle.prototype, 'constructor', {
      value: Rectangle,
      enumerable: false,
      writable: true
    })

    Rectangle.prototype.addPoint = function(point) {
      if (this.startPoint) {
        this.bbox = turf.bbox(
          turf.featureCollection([
            turf.point(this.startPoint),
            turf.point(point)
          ])
        )
      } else {
        this.startPoint = point
      }
    }

    Rectangle.prototype.getFillGeoJSON = function() {
      if (this.bbox) {
        return turf.bboxPolygon(this.bbox)
      }
      return null
    }

    Rectangle.prototype.getPointsGeoJSON = function() {
      if (this.bbox) {
        return turf.featureCollection(
          turf
            .bboxPolygon(this.bbox)
            .geometry.coordinates[0].slice(0, 4)
            .map(function(point) {
              return turf.point(point)
            })
        )
      } else if (this.startPoint) {
        return turf.featureCollection([turf.point(this.startPoint)])
      }
    }

    Rectangle.prototype.getOutlineGeoJSON = function() {
      return this.getFillGeoJSON()
    }

    Rectangle.prototype.isComplete = function() {
      return !!this.bbox
    }

    Rectangle.prototype.toJSON = function() {
      return {
        shape: 'rectangle',
        bbox: this.bbox
      }
    }

    return Rectangle
  }
])
