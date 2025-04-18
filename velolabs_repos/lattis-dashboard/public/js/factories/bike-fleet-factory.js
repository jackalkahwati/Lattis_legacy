'use strict'

angular.module('skyfleet').factory('bikeFleetFactory',
  [
    '$http',
    '$q',
    '_',
    function ($http, $q, _) {
      let maintenanceListCache, requestPending
      let bikesCache, bikesRequestPending, oldBikeQuery
      let imageCache, imageRequestPending, oldQuery
      return ({
        getBikesData: function (queryObject) {
          if (bikesCache && oldBikeQuery === queryObject) {
            oldBikeQuery = queryObject
            return $q.when(bikesCache)
          } else {
            bikesCache = null
            bikesRequestPending = null
            oldBikeQuery = queryObject
            return $q.when(bikesCache || bikesRequestPending || bikeList(queryObject))
          }
        },
        getBikeDetails: async (bikeId) => {
          try {
            const bikeData = await $http.get(`api/bike-fleet/get-bike-details/${bikeId}`)
            return bikeData
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred getting bike details")
          }
        },
        getMaintenanceList: function () {
          return $q.when(maintenanceListCache || requestPending || maintenanceList())
        },
        clearBikeDataCache: function () {
          bikesCache = bikesRequestPending = null
        },
        bikeToWorkshop: function (params, callback) {
          $http.post('api/bike-fleet/send-bikes-to-workshop', JSON.stringify(params))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        toActiveFleet: function (params, callback) {
          $http.post('api/bike-fleet/return-to-active-fleet', JSON.stringify(params))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        createTicket: function (formData, callback) {
          $http.post('api/tickets/create-ticket', formData, {headers: {'Content-Type': undefined}})
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        addBikesToFleet: function (formData, callback) {
          $http.post('api/bike-fleet/add-bike-details', formData, {headers: {'Content-Type': undefined}})
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        deleteBikes: function (params, callback) {
          $http.post('api/bike-fleet/delete-bike', JSON.stringify(params))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        getBikeDescriptionAndImage: function (query) {
          if (!oldQuery) {
            oldQuery = query
            return $q.when(imageCache || imageRequestPending || getImageAndDescription(query))
          } else if (_.isEqual(oldQuery, query)) {
            oldQuery = query
            return $q.when(imageCache || imageRequestPending || getImageAndDescription(query))
          } else if (!_.isEqual(oldQuery, query)) {
            imageCache = null
            imageRequestPending = null
            oldQuery = query
            return $q.when(imageCache || imageRequestPending || getImageAndDescription(query))
          }
        },
        clearDescriptionCache: function () {
          imageCache = imageRequestPending = null
        },
        sendToArchive: function (params, callback) {
          $http.post('api/bike-fleet/send-to-archive', JSON.stringify(params))
            .then(function (response) {
              callback(response.data)
            }, function (response) {
              callback(response.error)
            })
        },
        sendToStaging: function (params, callback) {
          $http.post('api/bike-fleet/send-to-staging', JSON.stringify(params))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        addMemberDomain: function (params, callback) {
          $http.post('api/fleet/add-domain', JSON.stringify(params))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        assignTicket: function (params, callback) {
          $http.post('api/tickets/assign-ticket', JSON.stringify(params))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        sendToService: function (formData, callback) {
          $http.post('api/bike-fleet/send-to-service', formData, {headers: {'Content-Type': undefined}})
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        getLockUsingKey: async (filterEntity, iotKey, fleetId) => {
          try {
            const details = await $http.get(`api/bike-fleet/tapkey-key/lock-id?filter=${iotKey}&fleetId=${fleetId}&filterEntity=${filterEntity}`)
            return details
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred fetching lock with those details")
          }
        },
        deleteFleetDomains: async (fleetId) => {
          try {
            const domainResponse = await $http.post('api/bike-fleet/delete-fleet-domains', fleetId)
            return domainResponse
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred updating access")
          }
        },
        fetchFleetData: async (fleetId) => {
          try {
            const domainResponse = await $http.get(`api/bike-fleet/fleet-data/${fleetId}`)
            return domainResponse
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred fetching fleetData")
          }
        },
        fetchPreDefinedBikes: function (cusId, callback) {
          $http.post('api/bike-fleet/pre-defined-bikes', JSON.stringify({customer_id: cusId}))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        endRide: function (data, callback) {
          $http.post('api/trips/end-trip', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },

        endTripPort: function (data, callback) {
          $http.post('api/trips/end-trip-port', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },

        getScoutApiKey: function (fleetId, callback) {
          $http.post('api/bike-fleet/get-scout-api-key', JSON.stringify(fleetId))
            .then(function(response) {
              callback(response.data)
            }, function (error){
              callback(error)
            })
        },

        getGeotabApiKey: function (fleetId, callback) {
          $http.post('api/bike-fleet/get-geotab-api-key', JSON.stringify(fleetId))
            .then(function(response) {
              callback(response.data)
            }, function (error){
              callback(error)
            })
        }
      })

      function maintenanceList () {
        let deferred = $q.defer()
        requestPending = deferred.promise
        $http.get('api/bike-fleet/maintenance-categories')
          .then(function (response) {
            deferred.resolve(response.data)
            maintenanceListCache = response.data
          }, function (error) {
            deferred.reject(error)
          })
        return deferred.promise
      }

      function bikeList (queryObject) {
        let deferred = $q.defer()
        bikesRequestPending = deferred.promise
        $http.post('api/bike-fleet/get-bike-fleet-data', JSON.stringify(queryObject))
          .then(function (response) {
            deferred.resolve(response.data)
            bikesCache = response.data
          }, function (error) {
            deferred.reject(error)
          })
        return deferred.promise
      }

      function getImageAndDescription (query) {
        let deferred = $q.defer()
        imageRequestPending = deferred.promise
        $http.post('api/bike-fleet/get-bike-image-and-description', JSON.stringify(query))
          .then(function (response) {
            deferred.resolve(response.data)
            imageCache = response.data
          }, function (error) {
            deferred.reject(error)
          })
        return deferred.promise
      }
    }])
