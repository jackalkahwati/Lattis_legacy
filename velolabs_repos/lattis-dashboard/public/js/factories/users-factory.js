'use strict'

angular.module('skyfleet').factory(
  'usersFactory',
  [
    '$http',
    '$rootScope',
    'sessionFactory',
    'rootScopeFactory',
    '$q',
    '_',
    function ($http, $rootScope, sessionFactory, rootScopeFactory, $q, _) {
      let bikesCache, bikesRequestPending, oldBikeId
      let csvCache, csvRequestPending, oldFleetId
      let userCache, userRequestPending, oldQuery
      let oldMemberFleetId, memberListCache, memberListRequestPending
      return ({
        getBikesActivity: function (bike_id) {
          if (!oldBikeId) {
            oldBikeId = bike_id
            return $q.when(bikesCache || bikesRequestPending || bikeListActivity(bike_id))
          } else if (oldBikeId === bike_id) {
            oldBikeId = bike_id
            return $q.when(bikesCache || bikesRequestPending || bikeListActivity(bike_id))
          } else if (oldBikeId !== bike_id) {
            bikesCache = null
            bikesRequestPending = null
            oldBikeId = bike_id
            return $q.when(bikesCache || bikesRequestPending || bikeListActivity(bike_id))
          }
        },
        getMemberCsv: function (fleet_id) {
          if (!oldFleetId) {
            oldFleetId = fleet_id
            return $q.when(csvCache || csvRequestPending || fetchMemberCSV(fleet_id))
          } else if (oldFleetId === fleet_id) {
            oldFleetId = fleet_id
            return $q.when(csvCache || csvRequestPending || fetchMemberCSV(fleet_id))
          } else if (oldFleetId !== fleet_id) {
            csvCache = null
            csvRequestPending = null
            oldFleetId = fleet_id
            return $q.when(csvCache || csvRequestPending || fetchMemberCSV(fleet_id))
          }
        },
        getMember: function (queryObject) {
          if (!oldQuery) {
            oldQuery = queryObject
            return $q.when(userCache || userRequestPending || fetchSingleUser(queryObject))
          } else if (_.isEqual(oldQuery, queryObject)) {
            oldQuery = queryObject
            return $q.when(userCache || userRequestPending || fetchSingleUser(queryObject))
          } else if (!_.isEqual(oldQuery, queryObject)) {
            userCache = null
            userRequestPending = null
            oldQuery = queryObject
            return $q.when(userCache || userRequestPending || fetchSingleUser(queryObject))
          }
        },
        getMemberList: function (fleet_id) {
          if (!oldMemberFleetId) {
            oldMemberFleetId = fleet_id
            return $q.when(memberListCache || memberListRequestPending || fetchMemberList(fleet_id))
          } else if (oldMemberFleetId === fleet_id) {
            oldMemberFleetId = fleet_id
            return $q.when(memberListCache || memberListRequestPending || fetchMemberList(fleet_id))
          } else if (oldMemberFleetId !== fleet_id) {
            memberListCache = null
            memberListRequestPending = null
            oldMemberFleetId = fleet_id
            return $q.when(memberListCache || memberListRequestPending || fetchMemberList(fleet_id))
          }
        },
        getAvailableUsers: function(fleet_id, callback) {
          $http.post('/api/members/get-private-fleet-users', JSON.stringify({fleet_id: fleet_id}))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              console.log(error)
              callback(null)
            })
        },
        getUserData: function (fleetId, callback) {
          $http.post('/api/users/get-user-data', JSON.stringify({fleet_id: fleetId}))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              console.log(error)
              callback(null)
            })
        },
        updateUserData: function (data, callback) {
          $http.post('/api/users/update-user-data', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              console.log(error)
              callback(null)
            })
        },
        addMembers: function () {

        },
        updateMemberProfile: function (data, callback) {
          $http.post('/api/members/update-member-profile', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              console.log(error)
              callback(null)
            })
        },
        getDomains: function (data) {
          let deferred = $q.defer()
          $http.post('/api/fleet/get-domain', JSON.stringify(data))
            .then(function (response) {
              deferred.resolve(response.data)
            }, function (error) {
              deferred.reject(error)
            })
          return deferred.promise
        },
        removeDomain: function (domain, callback) {
          $http.post('/api/fleet/delete-domain', JSON.stringify(domain))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        checkOperator: function (email, operatorId, callback) {
          $http.post('/api/operators/check-operator', JSON.stringify({email: email, operator_id: operatorId}))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        uploadCSV: function (formData, callback) {
          $http.post('/api/members/upload-member-csv', formData, {headers: {'Content-Type': undefined}})
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        deleteCSV: function (callback) {
          $http.post('/api/members/revoke-old-member-csv', JSON.stringify({'fleet_id': rootScopeFactory.getData('fleetId')}))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        getBikeActivity: function (bike_id, callback) {
          $http.post('/api/bike-fleet/activity-log', JSON.stringify({bike_id: bike_id}))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        getCsvFile: function (data) {
          let deferred = $q.defer()
          $http.post('/api/members/get-csv-file', JSON.stringify(data))
            .then(function (response) {
              deferred.resolve(response.data)
            }, function (error) {
              deferred.reject(error)
            })
          return deferred.promise
        },
        toggleAccess: function (obj, callback) {
          $http.post('/api/members/grant-or-revoke-member-access', JSON.stringify(obj))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        toggleIndividualAccess: async (userDetails) => {
          try {
            const accessResponse = await $http.post('api/members/toggle-access', userDetails)
            return accessResponse
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred updating access")
          }
        },
        getUser: async (userDetails) => {
          try {
            const user = await $http.post('api/members/get-user', userDetails)
            return user
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred while fetching user")
          }
        },
        revokeAccessForAllFleetMembers: async (fleetId) => {
          try {
            const accessResponse = await $http.post('api/members/revoke-fleet-access', fleetId)
            return accessResponse
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred updating access")
          }
        },
        clearMemberCSVCache: function () {
          csvRequestPending = csvCache = undefined
        },
        promotions: function({ fleet_id, user_id }) {
          return $http
            .get(`/api/members/${user_id}/promotions/${fleet_id}`)
            .then((response) => response.data);
        }
      })

      function bikeListActivity (bike_id) {
        let deferred = $q.defer()
        bikesRequestPending = deferred.promise
        $http.post('/api/bike-fleet/activity-log', JSON.stringify({bike_id: bike_id}))
          .then(function (response) {
            deferred.resolve(response.data)
            bikesCache = response.data
          }, function (error) {
            deferred.reject(error)
          })
        return deferred.promise
      }

      function fetchMemberCSV (fleet_id) {
        let deferred = $q.defer()
        csvRequestPending = deferred.promise
        $http.post('/api/members/get-member-csv', JSON.stringify({fleet_id: fleet_id}))
          .then(function (response) {
            deferred.resolve(response.data)
            csvCache = response.data
          }, function (error) {
            deferred.reject(error)
          })
        return deferred.promise
      }

      function fetchSingleUser (queryObject) {
        let deferred = $q.defer()
        userRequestPending = deferred.promise
        $http.post('/api/members/get-users', JSON.stringify(queryObject))
          .then(function (response) {
            deferred.resolve(response.data)
            userCache = response.data
          }, function (error) {
            deferred.reject(error)
          })
        return deferred.promise
      }

      function fetchMemberList (fleetId) {
        let deferred = $q.defer()
        memberListRequestPending = deferred.promise
        $http.post('/api/members/get-member-list', JSON.stringify({fleet_id: fleetId}))
          .then(function (response) {
            deferred.resolve(response.data)
            memberListCache = response.data
          }, function (error) {
            deferred.reject(error)
          })
        return deferred.promise
      }
    }
  ])
