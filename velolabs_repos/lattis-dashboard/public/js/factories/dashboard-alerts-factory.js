'use strict'

angular.module('skyfleet').factory('dashboardalertsFactory', [
  '$http',
  function ($http) {
    return {
      getAlerts: function (callback) {
        $http.post('/api/alerts/all-alerts')
          .then(function (response) {
            callback(response.data)
          }, function (error) {
            console.log(error)
            callback(null)
          })
      }
    }
  }
]
)
