'use strict'

angular.module('skyfleet').factory('paymentFactory', function ($http) {
  return {
    getFleetBillingAddress: function (data, callback) {
      $http.post('/api/fleet/get-fleet-billing-address', JSON.stringify({fleet_id: data}))
        .then(function (response) {
          callback(response.data)
        }, function (error) {
          console.log(error)
          callback(null)
        })
    },
    getAccountStatus: function (data, callback) {
      $http.post('/api/fleet/get-account-status', JSON.stringify({fleet_id: data}))
        .then(function (response) {
          callback(response.data)
        }, function (error) {
          console.log(error)
          callback(null)
        })
    },
    createFleetBillingAddress: function (data, callback) {
      $http.post('/api/fleet/create-fleet-billing-address', JSON.stringify(data))
        .then(function (response) {
          callback(response.data)
        }, function (error) {
          console.log(error)
          callback(null)
        })
    },
    editFleetBillingAddress: function (data, callback) {
      $http.post('/api/fleet/update-fleet-billing-address', JSON.stringify(data))
        .then(function (response) {
          callback(response.data)
        }, function (error) {
          console.log(error)
          callback(null)
        })
    },
    getFleetCards: function (data, callback) {
      $http.post('/api/fleet/get-cards', JSON.stringify({fleet_id: data}))
        .then(function (response) {
          callback(response.data)
        }, function (error) {
          console.log(error)
          callback(null)
        })
    },
    createFleetPaymentProfile: function (data, callback) {
      $http.post('/api/fleet/create-fleet-payment-profile', JSON.stringify(data))
        .then(function (response) {
          callback(response.data)
        }, function (error) {
          console.log(error)
          callback(null)
        })
    },
    getFleetInvoice: function (data, callback) {
      $http.post('/api/fleet/get-fleet-invoice', JSON.stringify({fleet_id: data}))
        .then(function (response) {
          callback(response.data)
        }, function (error) {
          console.log(error)
          callback(null)
        })
    },
    createFleetBillingCycle: function (data, callback) {
      $http.post('/api/fleet/billing-cycle-fleet', JSON.stringify(data))
        .then(function (response) {
          callback(response.data)
        }, function (error) {
          console.log(error)
          callback(null)
        })
    },
    saveStripeConnectInfo: function (data, callback) {
      $http.post('/api/payments/save-stripe-connect-info', JSON.stringify(data))
        .then(function (response) {
          callback(response.data)
        }, function (error) {
          console.log(error)
          callback(null)
        })
    },
    connectMercadoPago: function({ fleetId, privateKey, publicKey }) {
      return $http
        .post("/api/payments/connect-mercado-pago", {
          fleet_id: fleetId,
          privateKey,
          publicKey,
        })
        .then(function (response) {
          return response.data;
        });
    }
  }
})
