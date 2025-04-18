'use strict'
angular.module('skyfleet.controllers')
  .controller('homeController',
    function ($scope, myFleetFactory, $state, sessionFactory, _, utilsFactory, $timeout) {
      $scope.name = sessionFactory.getCookieUser()
      $scope.fleetAutoComplete = []
      const currentUrl = window.location.href

      var dateObj = new Date()
      var currentHour = dateObj.getHours()
      var currentMin = dateObj.getMinutes()
      $scope.greeting = ''

      // @function getGreetingTime get Good morning, Afternoon, Evening
      getGreetingTime(currentHour, currentMin)
      $scope.viewIcon = false
      $scope.load = function () {
        $scope.viewIcon = true
        $timeout(function () {
          $scope.viewIcon = false
        }, 1000)
      }

      function getGreetingTime (currentHour, currentMin) {
        $scope.greeting = null
        var split_afternoon = 12 // 24hr time to split the afternoon
        var split_evening = 16 // 24hr time to split the evening
        var greetHour = currentHour + '.' + currentMin

        if (greetHour >= split_afternoon && greetHour <= split_evening) {
          $scope.greeting = 'Afternoon'
        } else if (greetHour >= split_evening) {
          $scope.greeting = 'Evening'
        } else {
          $scope.greeting = 'Morning'
        }

        return $scope.greeting
      }

      if (currentUrl && /code/.test(window.location.href)) {
        console.log(currentUrl)
        $state.go('profile', {currentUrl: currentUrl})
      }

      $scope.gofleet = function () {
        $state.go('myfleetdashboard.manage', {fleetId: $scope.fleetSearch.split(',')[0]})
      }
    })
