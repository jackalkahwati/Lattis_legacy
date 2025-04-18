'use strict'

angular.module('skyfleet').factory('reportFactory', [
  '$http',
  'sessionFactory',
  function ($http, sessionFactory) {
    return {
      generateFleetUtilizationReport: async (data) => {
        try {
          const fleetUtilizationReport = await $http.post('/api/reports/fleet-utilization', JSON.stringify(data))
          return fleetUtilizationReport
        } catch (error) {
          throw new Error((error.data && error.data.error) || 'An error occurred while fetching fleetUtilization report')
        }
      },
      getlocationReport: function (data, done) {
        $http.post('/api/reports/location-reports', JSON.stringify(data))
          .then(function (response) {
            done(response.data)
          }, function (error) {
            done(error)
          })
      },
      getmaintenanceReport: function (data, done) {
        $http.post('/api/reports/maintenance-utilisation', JSON.stringify(data))
          .then(function (response) {
            done(response.data)
          }, function (error) {
            done(error)
          })
      },
      getmemberReport: function (data, done) {
        $http.post('/api/reports/member-reports', JSON.stringify(data))
          .then(function (response) {
            done(response.data)
          }, function (error) {
            done(error)
          })
      },
      generateTripsReport: async (data) => {
        try {
          const tripsReport = await $http.post('/api/reports/trips', JSON.stringify(data))
          return tripsReport
        } catch (error) {
          throw new Error((error.data && error.data.error) || 'An error occurred while fetching trips report')
        }
      },
      getinventoryReport: function (data, done) {
        $http.post('/api/reports/inventory-reports', JSON.stringify(data))
          .then(function (response) {
            done(response.data)
          }, function (error) {
            done(error)
          })
      },

      getRevenueReport: function (data , done) {
        $http.post('/api/reports/revenue-reports', JSON.stringify(data))
          .then(function (response) {
            done(response.data)
          }, function (error) {
            done(error)
          })
      }
    }
  }
])
