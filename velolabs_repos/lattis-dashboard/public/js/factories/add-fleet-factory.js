'use strict'

angular.module('skyfleet').factory(
  'addFleetFactory',
  [
    '$http',
    'sessionFactory',
    function ($http, sessionFactory) {
      return {
        getCustomerName: function (callback) {
          $http.post('/api/fleet/customer-list', JSON.stringify({operator_id: sessionFactory.getCookieId()}))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              console.log(error)
              callback(null)
            })
        },
        getFleetWithId: function (data, done) {
          $http.post('/api/fleet/get-fleet-by-id', JSON.stringify(data))
            .then(function (response) {
              done(response.data)
            }, function (error) {
              console.log('Error: Fetching fleet with that id:', error)
              done(null)
            })
        },
        addNewCustomer: function (data, done) {
          $http.post('/api/fleet/new-customer', JSON.stringify(data))
            .then(function (response) {
              done(response.data)
            }, function (error) {
              console.log('Error: customer response saving new customer:', error)
              done(null)
            })
        },
        checkFleetName: function (fleetName, callback) {
          $http.post('api/fleet/check-fleet-name', {
            operator_id: sessionFactory.getCookieId(),
            fleet_name: fleetName
          })
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        checkCustomerName: function (customerName, callback) {
          $http.post('api/fleet/check-customer-name', {
            operator_id: sessionFactory.getCookieId(),
            customer_name: customerName
          })
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        addExistingCustomer: function (data, done) {
          $http.post('/api/fleet/existing-customer', JSON.stringify(data))
            .then(function (response) {
              done(response.data)
            }, function (error) {
              console.log(error)
              done(null)
            })
        },
        createGeofence: function (coordinates, callback) {
          $http.post('/api/geofence/geofence-create', JSON.stringify(coordinates))
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        },
        createFleet: function (formData, callback) {
          $http.post('/api/fleet/add-fleet', formData, {headers: {'Content-Type': undefined}})
            .then(function (response) {
              callback(response.data)
            }, function (error) {
              callback(null)
            })
        }
      }
    }
  ]
)
