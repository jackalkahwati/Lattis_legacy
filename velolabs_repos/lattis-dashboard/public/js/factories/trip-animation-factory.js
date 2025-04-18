'use strict'

angular.module('skyfleet').factory(
  'tripAnimationFactory',
  [
    '$http',
    '$q',
    '_',
    function ($http, $q, _) {
      let tripsCache, tripsRequestPending, oldQuery
      return {
        getHeatMapTrips: function (data) {
          let deferred = $q.defer()
          $http.post('/api/trips/trip-analytics', JSON.stringify(data))
            .then(function (response) {
              deferred.resolve(response.data)
            }, function (error) {
              deferred.reject(error)
            })
          return deferred.promise
        },
        getAllTrips: function (queryObject) {
          if (!oldQuery) {
            oldQuery = queryObject
            return $q.when(tripsCache || tripsRequestPending || fetchTrips(queryObject))
          } else if (_.isEqual(oldQuery, queryObject)) {
            oldQuery = queryObject
            return $q.when(tripsCache || tripsRequestPending || fetchTrips(queryObject))
          } else if (!_.isEqual(oldQuery, queryObject)) {
            tripsCache = null
            tripsRequestPending = null
            oldQuery = queryObject
            return $q.when(tripsCache || tripsRequestPending || fetchTrips(queryObject))
          }
        },
        clearTripsCache: function () {
          tripsCache = tripsRequestPending = null
        },
        refundTrip: function({ amount, trip_id }) {
          return $http
            .post('/api/trips/refund-trip', JSON.stringify({ amount, trip_id }))
            .then(response => {
              return response.data
            })
        }
      }
      function fetchTrips (queryObject) {
        let deferred = $q.defer()
        tripsRequestPending = deferred.promise
        $http.post('/api/trips/get-trips', JSON.stringify(queryObject))
          .then(function (response) {
            deferred.resolve(response.data)
            tripsCache = response.data
          }, function (error) {
            deferred.reject(error)
          })
        return deferred.promise
      }
    }])
