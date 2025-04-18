'use strict'

angular.module('skyfleet')
  .factory('parkingFactory',
    [
      '$q',
      '$http',
      'sessionFactory',
      '_',
      function ($q, $http, sessionFactory, _) {
        var oldZoneQuery, zoneCache, zoneRequestPending
        var oldSpotQuery, spotCache, spotRequestPending
        let oldPublicSpotQuery, publicSpotCache, publicSpotRequestPending

        return ({
          /* @function {getParkingZonesData} function to get all the parking zones of a admin */
          getParkingZonesData: function (zoneQuery) {
            if (!oldZoneQuery) {
              oldZoneQuery = zoneQuery
              return $q.when(zoneCache || zoneRequestPending || fetchParkingZone(zoneQuery))
            } else if (_.isEqual(oldZoneQuery, zoneQuery)) {
              oldZoneQuery = zoneQuery
              return $q.when(zoneCache || zoneRequestPending || fetchParkingZone(zoneQuery))
            } else if (!_.isEqual(oldZoneQuery, zoneQuery)) {
              zoneCache = null
              zoneRequestPending = null
              oldZoneQuery = zoneQuery
              return $q.when(zoneCache || zoneRequestPending || fetchParkingZone(zoneQuery))
            }
          },
          clearParkingZonesCache: function () {
            zoneCache = zoneRequestPending = null
          },
          /* @function {getParkingSpotsData} function to get all the parking spots of a admin */
          getParkingSpotsData: function (spotQuery) {
            if (!oldSpotQuery) {
              oldSpotQuery = spotQuery
              return $q.when(spotCache || spotRequestPending || fetchParkingSpot(spotQuery))
            } else if (_.isEqual(oldSpotQuery, spotQuery)) {
              oldSpotQuery = spotQuery
              return $q.when(spotCache || spotRequestPending || fetchParkingSpot(spotQuery))
            } else if (!_.isEqual(oldSpotQuery, spotQuery)) {
              spotCache = null
              spotRequestPending = null
              oldSpotQuery = spotQuery
              return $q.when(spotCache || spotRequestPending || fetchParkingSpot(spotQuery))
            }
          },
          clearParkingSpotCache: function () {
            spotCache = spotRequestPending = null
          },
          /* @function {getParkingSpotsData} function to get all the parking spots of a admin */
          getPublicParkingSpotsData: function (spotQuery) {
            if (!oldPublicSpotQuery) {
              oldPublicSpotQuery = spotQuery
              return $q.when(publicSpotCache || publicSpotRequestPending || fetchPublicParkingSpot(spotQuery))
            } else if (_.isEqual(oldSpotQuery, spotQuery)) {
              oldPublicSpotQuery = spotQuery
              return $q.when(publicSpotCache || publicSpotRequestPending || fetchPublicParkingSpot(spotQuery))
            } else if (!_.isEqual(oldSpotQuery, spotQuery)) {
              publicSpotCache = null
              publicSpotRequestPending = null
              oldPublicSpotQuery = spotQuery
              return $q.when(publicSpotCache || publicSpotRequestPending || fetchPublicParkingSpot(spotQuery))
            }
          },
          clearPublicParkingSpotCache: function () {
            publicSpotCache = publicSpotRequestPending = null
          },
          addParkingZone: function (zoneData, callback) {
            $http.post('api/parking/create-parking-zones', JSON.stringify(zoneData))
              .then(function (response) {
                callback(response.data)
              }, function (error) {
                callback(null)
              })
          },
          editParkingZone: function (zoneData, callback) {
            $http.post('api/parking/edit-parking-zones', JSON.stringify(zoneData))
              .then(function (response) {
                callback(response.data)
              }, function (error) {
                callback(null)
              })
          },
          editParkingSpot: function (spotData, callback) {
            $http.post('api/parking/edit-parking-spots', spotData, {headers: {'Content-Type': undefined}})
              .then(function (response) {
                callback(response.data)
              }, function (error) {
                callback(null)
              })
          },
          createParkingSpot: function (spotData, callback) {
            $http.post('api/parking/create-parking-spots', spotData, {headers: {'Content-Type': undefined}})
              .then(function (response) {
                callback(response.data)
              }, function (error) {
                callback(error)
              })
          },
          deleteParkingZone: function (zoneId, callback) {
            $http.post('api/parking/delete-parking-zones', JSON.stringify(zoneId))
              .then(function (response) {
                callback(response.data)
              }, function (error) {
                callback(null)
              })
          },
          deleteParkingSpot: function (spotId, callback) {
            $http.post('api/parking/delete-parking-spots', JSON.stringify(spotId))
              .then(function (response) {
                callback(response.data)
              }, function (error) {
                callback(null)
              })
          },
          uploadCSV: function (csvfile, callback) {
            $http.post('api/parking/upload-parking-spots', csvfile, {headers: {'Content-Type': undefined}})
              .then(function (response) {
                callback(response.data)
              }, function (error) {
                callback(null)
              })
          },
          saveParkingSettings: function (settings, callback) {
            $http.post('api/parking/set-parking-settings', JSON.stringify(settings))
              .then(function (response) {
                callback(response.data)
              }, function (error) {
                callback(null)
              })
          }
        })

        function fetchParkingZone (zoneQuery) {
          var deferred = $q.defer()
          zoneRequestPending = deferred.promise
          $http.post('/api/parking/get-parking-zones', JSON.stringify(zoneQuery))
            .then(function (response) {
              deferred.resolve(response.data.payload)
              zoneCache = response.data.payload
            }, function (error) {
              deferred.reject(error)
            })
          return deferred.promise
        }

        function fetchParkingSpot (spotQuery) {
          var deferred = $q.defer()
          spotRequestPending = deferred.promise
          $http.post('/api/parking/get-parking-spots', JSON.stringify(spotQuery))
            .then(function (response) {
              deferred.resolve(response.data.payload)
              spotCache = response.data.payload
            }, function (error) {
              deferred.reject(error)
            })
          return deferred.promise
        }

        function fetchPublicParkingSpot (spotQuery) {
          var deferred = $q.defer()
          publicSpotRequestPending = deferred.promise
          $http.post('/api/parking/get-public-parking-spots', JSON.stringify(spotQuery))
            .then(function (response) {
              deferred.resolve(response.data.payload)
              publicSpotCache = response.data.payload
            }, function (error) {
              deferred.reject(error)
            })
          return deferred.promise
        }
      }
    ]
  )
