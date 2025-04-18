'use strict'

angular.module('skyfleet')
  .factory('myFleetFactory',
    [
      '$q',
      '$http',
      'sessionFactory',
      function ($q, $http, sessionFactory) {
        var cache, requestPending
        var oldFleetId, ticketCache, ticketRequestPending
        return ({
          /* @function {getFleetData} function to get all the fleets data of a admin */
          getFleetData: function () {
            return $q.when(cache || requestPending || helper())
          },
          getAllTickets: function (fleetId, resolved) {
              return $q.when(ticketList(fleetId, resolved))
          },
          clearFleetCache: function () {
            cache = requestPending = null
          },
          clearTicketsCache: function () {
            ticketCache = ticketRequestPending = null
          },
          getFleetList: function (callback) {
            $http.post('api/fleet/fleet-list', JSON.stringify({operator_id: sessionFactory.getCookieId()}))
              .then(function (response) {
                callback(response.data)
                console.log(response.data)
              }, function (error) {
                callback(null)
              })
          },
          addReservationSettings: function(settings, callback) {
            $http.post('api/fleet/' + settings.fleet_id + '/reservation-settings', settings.reservation_settings)
              .then(function (response) {
                callback(response.data)
              }, function (response) {
                callback(response.data)
              })
          },
          updateReservationSettings: function(settings, reservationSettingsId, callback) {
            $http.patch(
              'api/fleet/' + settings.fleet_id + '/reservation-settings/' + reservationSettingsId, settings.reservation_settings
            )
              .then(function (response) {
                callback(response.data)
              }, function (error) {
                callback(error)
              })
          },
          activateReservationSettings: function(fleetId) {
            return $http.patch(`api/fleet/${fleetId}/reservation-settings/activate`).then(res => res.data)
          },
          deactivateReservationSettings: function(fleetId) {
            return $http.patch(`api/fleet/${fleetId}/reservation-settings/deactivate`).then(res => res.data)
          },
          getReservations: function(bikeId, callback) {
            $http.get('/api/reservations?bike_id=' + bikeId)
              .then(function (response) {
                callback(response)
              }, function (error) {
                callback(null)
              })
          },
          cumulativeState: function (fleetId) {
            let deferred = $q.defer()
            $http.post('api/bike-fleet/cumulative-trip-data', JSON.stringify({fleet_id: fleetId}))
              .then(function (response) {
                deferred.resolve(response.data)
              }, function (error) {
                deferred.reject(error)
              })
            return deferred.promise
          },
          getAlertList: function (callback) {
            $http.get('/api/fleet/get-alert-list')
              .then(function (response) {
                callback(response)
              }, function (error) {
                callback(null)
              })
          },
          resolveTicket: function (ticketId, callback) {
            $http.post('api/tickets/resolve-ticket', JSON.stringify({ticket_id: ticketId}))
              .then(function (response) {
                callback(response.data)
              }, function (error) {
                callback(null)
              })
          }
        })

        function ticketList (fleetId, resolved) {
          var deferred = $q.defer()
          ticketRequestPending = deferred.promise
          let filter = { fleet_id: fleetId }
          if(resolved) {
            filter.resolved = true
          }
          $http.post('/api/tickets/get-ticket', JSON.stringify(filter))
            .then(function (response) {
              deferred.resolve(response.data)
              ticketCache = response.data
            }, function (error) {
              deferred.reject(error)
            })
          return deferred.promise
        }

        function helper () {
          var deferred = $q.defer()
          requestPending = deferred.promise
          $http.post('/api/fleet/get-operator-data', JSON.stringify({'operator_id': sessionFactory.getCookieId()}))
            .then(function (response) {
              deferred.resolve(response.data.payload)
              cache = response.data.payload
            }, function (error) {
              deferred.reject(error)
            })
          return deferred.promise
        }
      }
    ]
  )
