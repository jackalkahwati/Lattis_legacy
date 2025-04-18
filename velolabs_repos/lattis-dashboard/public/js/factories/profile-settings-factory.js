'use strict'

angular.module('skyfleet.controllers').factory(
  'profileSettingFactory',
  [
    '$http',
    'sessionFactory',
    '$q',
    function ($http, sessionFactory, $q) {
      let cache, requestPending, oldFleetId
      return {
        saveGrowSettings: async (data) => {
          let growDetails = Object.assign({}, data)
          if(!growDetails.growLED) growDetails.growLED = 1
          if(!growDetails.growMaximumSpeed) growDetails.growMaximumSpeed = 20
          if(!growDetails.scooterDriveMode) growDetails.scooterDriveMode = 1
          if(!growDetails.scooterBrakeMode) growDetails.scooterBrakeMode = 1
          for(let key of Object.keys(growDetails)) {
            if(growDetails[key] !== 0 && !growDetails[key]) delete growDetails[key]
          }
          if(Object.keys(growDetails).length === 0) return;
          try {
            const details = await $http.post('api/bike-fleet/grow/settings', { growDetails })
            return details
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred saving Grow settings")
          }
        },
        saveSegwaySettings: async (data) => {
          let segwayDetails = Object.assign({}, data)
          if(!segwayDetails.segwayMaximumSpeedLimit) {segwayDetails.segwayMaximumSpeedLimit = 25}
          try {
            const segwayResponse = await $http.post('api/bike-fleet/segway/settings', { segwayDetails })
            return segwayResponse
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred saving segway settings")
          }
        },
        saveKisiSettings: async (kisiSettings) => {
          try {
            const kisiResponse = await $http.post('api/bike-fleet/settings', { settings: kisiSettings })
            return kisiResponse.data;
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred saving Kisi settings")
          }
        },
        saveEdgeSettings: async (edgeSettings) => {
          try {
            const edgeResponse = await $http.post('api/bike-fleet/settings', { settings: edgeSettings })
            return edgeResponse.data;
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred saving edge settings")
          }
        },
        // Used for ACTON, Omni IoT and Teltonika and Okai
        saveACTONSettings: async (data, vendor) => {
          let ACTONDetails = Object.assign({}, data, {vendor})
          if(!ACTONDetails.maximumSpeedLimit) { ACTONDetails.maximumSpeedLimit = 20 }
          try {
            const actonResponse = await $http.post('api/bike-fleet/acton/settings', { ACTONDetails })
            return actonResponse
          } catch (error) {
            throw new Error((error.data && error.data.error) || `An error occurred saving ${vendor} settings`)
          }
        },
        saveTax: async (data) => {
          try {
            const result = await $http.post('api/tax/addTax', { data })
            return result
          } catch (error) {
            throw new Error((error.data && error.data.error) || `An error occurred saving tax details`)
          }
        },
        getTaxWithFleetId: async (data) => {
          try {
            const result = await $http.post('api/tax/getTax', { data })
            return result
          } catch (error) {
            throw new Error((error.data && error.data.error) || `An error occurred get tax details`)
          }
        },
        deactivateTaxWithTaxId: async (data) => {
          try {
            const result = await $http.post('api/tax/deactivateTax', { data })
            return result
          } catch (error) {
            throw new Error((error.data && error.data.error) || `An error occurred deactivate tax details`)
          }
        },
        updateTax : async (data) => {
          try {
            const result = await $http.post('api/tax/updateTax', data)
            return result
          } catch (error) {
            throw new Error((error.data && error.data.error) || `An error occurred get update details`)
          }
        },
        getFleetIntegrations: async (fleetId) => {
          try {
            const fleetIntegrations = await $http.get(`api/bike-fleet/integrations/${fleetId}`)
            return fleetIntegrations
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred getting fleet integrations")
          }
        },
        getCustomerProfile: function (data, callback) {
          $http.post('api/profile/get-profiles', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        editCustomerProfile: function (profileSettings, callback) {
          $http.post('api/profile/update-profile', JSON.stringify(profileSettings))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        editCustomerAlertSettings: function (alertSettings, callback) {
          $http.post('api/profile/update-alert', JSON.stringify(alertSettings))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        getOperatorProfile: function (callback) {
          $http.post('api/profile/get-profiles', JSON.stringify({operator_id: sessionFactory.getCookieId()}))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        editOperatorProfile: function (data, callback) {
          $http.post('api/profile/update-profile', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },

        loginScoutUser: async (data) => {
          try {
            const integration = await $http.post(`api/profile/login-scout-user`, JSON.stringify(data))
            return integration.data
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error while adding geotab Integration")
          }
        },

        authenticateGeotabUser: async (data) => {
          try {
            const integration = await $http.post(`api/profile/authorize-geotab-user`, JSON.stringify(data))
            return integration.data
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error while adding geotab Integration")
          }
        },

        registerLinka: async (data) => {
          try {
            const integration = await $http.post(`api/profile/register-linka-merchant`, JSON.stringify(data))
            return integration.data
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error while adding lika Integration")
          }
        },
        registerIntegration: async (data) => {
          try {
            const integration = await $http.post(`api/profile/register-integration`, JSON.stringify(data))
            return integration.data
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred while registering integration")
          }
        },

        getTapKeyCredentials: async (path) => {
          try {
            const {data: credentials} = await $http.get(`api/profile/tap-key-credentials?path=${path}`)
            return credentials
          } catch (error) {
            throw new Error((error.data && error.data.error) || "An error occurred while fetching integration credentials")
          }
        },

        getFleetProfile: function (data) {
          let deferred = $q.defer()
          $http.post('api/fleet/fleet-list', JSON.stringify(data))
            .then(function (response) {
              deferred.resolve(response.data)
            }, function (error) {
              deferred.reject(error)
            })
          return deferred.promise
        },
        getOnCall: function (fleetId) {
          if (!oldFleetId) {
            oldFleetId = fleetId
            return $q.when(cache || requestPending || fetchOnCall(fleetId))
          } else if (oldFleetId === fleetId) {
            oldFleetId = fleetId
            return $q.when(cache || requestPending || fetchOnCall(fleetId))
          } else if (oldFleetId !== fleetId) {
            cache = null
            requestPending = null
            oldFleetId = fleetId
            return $q.when(cache || requestPending || fetchOnCall(fleetId))
          }
        },
        clearOnCallCache: function () {
          cache = requestPending = null
        },
        updateOnCall: function (data, callback) {
          $http.post('api/fleet/update-on-call-operator', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(error)
            })
        },
        updateACL: function (data, callback) {
          $http.post('api/fleet/update-acl', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(error)
            })
        },
        updateFleetProfile: function (data, callback) {
          $http.post('api/fleet/edit-fleet-info', data, {headers: {'Content-Type': undefined}})
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(error)
            })
        },
        addOperator: function (data, callback) {
          $http.post('api/operators/create-operator', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        getFleetSchedule: function (data, callback) {
          $http.post('api/bike-fleet/get-bike-group-data', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        getReservationSettings: function (fleetId, callback) {
          $http.get('api/fleet/' + fleetId + '/reservation-settings?parse=true')
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        deleteOperator: function (data, callback) {
          $http.post('api/operators/delete-operator', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        updateFleetSchedule: function (data, callback) {
          $http.post('api/bike-fleet/update-bike-group-data', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        addCustomer: function (data, callback) {
          $http.post('api/operators/create-customer-and-operator', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        updateAppSettings: function (data, callback) {
          $http.post('api/fleet/update-fleet-settings', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        getAppSettings: function (data, callback) {
          $http.post('api/fleet/get-fleet-settings', JSON.stringify(data))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        }
      }

      function fetchOnCall (fleetId) {
        let deferred = $q.defer()
        requestPending = deferred.promise
        $http.post('api/fleet/get-on-call-operator', JSON.stringify({fleet_id: fleetId}))
          .then(function (response) {
            deferred.resolve(response.data)
            cache = response.data
          }, function (error) {
            deferred.reject(error)
          })
        return deferred.promise
      }
    }
  ]
)
