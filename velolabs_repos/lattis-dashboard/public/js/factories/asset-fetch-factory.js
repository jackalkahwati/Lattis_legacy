'use strict'

angular.module('skyfleet').factory('assetfetchFactory', [
  '$http',
  'sessionFactory',
  function ($http, sessionFactory) {
    return {
      getAssets: function (done) {
        $http.get('/assets/fetch-assets')
          .then(function (response) {
            done(null, response.data)
          }, function (error) {
            done(error, null)
          })
      },
      getPreferences: function (done) {
        $http.post('/api/operators/get-preferences', JSON.stringify({operator_id: sessionFactory.getCookieId()}))
          .then(function (response) {
            done(response.data)
          }, function (error) {
            console.log('Error: customer response saving new customer:', error)
            done(null)
          })
      },
      setPreferences: function (data, done) {
        console.log('data-fac', data)
        $http.post('/api/operators/set-preferences', JSON.stringify(data))
          .then(function (response) {
            done(response.data)
          }, function (error) {
            console.log('Error: customer response saving new customer:', error)
            done(null)
          })
      }
    }
  }
])
