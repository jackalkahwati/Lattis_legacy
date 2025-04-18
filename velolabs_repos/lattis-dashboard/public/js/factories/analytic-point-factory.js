'use strict'

angular.module('skyfleet').factory('analyticPointFactory', [
  '$http',
  function ($http) {
    return {
      getPointsInView: function (coordinates, done) {
        var payload = {coordinates: coordinates}
        $http.post('/api/analytics-points', JSON.stringify(payload))
          .then(function (response) {
            done(null, response.data.payload)
          }, function (error) {
            done(error, null)
          })
      }
    }
  }
]
)
