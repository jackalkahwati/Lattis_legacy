'use strict'

angular.module('skyfleet.controllers')
  .controller('activityFeedController',
    function ($scope, myFleetFactory, $state, sessionFactory, _, utilsFactory, rootScopeFactory, profileSettingFactory, bikeFleetFactory, $rootScope, $q, $interval, $timeout, notify, usersFactory) {
      $scope.list = {}
      let promise
      if (rootScopeFactory.getData('fleetId')) {
        $rootScope.showLoader = true
        $q.all([myFleetFactory.getAllTickets(rootScopeFactory.getData('fleetId')),
          myFleetFactory.cumulativeState(rootScopeFactory.getData('fleetId'))]).then(function (response) {
          getActivityData(response)
          if (response[1].payload) {
            $scope.fleetdata = response[1].payload
            setCummulativeData()
          }
          $rootScope.showLoader = false
        })
        startTimer()
      }

      $scope.reviewTicket = function (bikeId) {
        $state.go('bike-details', {bikeId: bikeId})
      }

      $scope.resolveTicket = function (ticketId) {
        myFleetFactory.resolveTicket(ticketId, function (res) {
          if (res.status === 200) {
            bikeFleetFactory.clearBikeDataCache()
            myFleetFactory.clearTicketsCache()
            profileSettingFactory.clearOnCallCache()
            notify({message: 'Resolved the ticket successfully', duration: 2000, position: 'right'})
            $timeout(function () {
              $rootScope.showLoader = true
              $q.all([myFleetFactory.getAllTickets(rootScopeFactory.getData('fleetId')),
                myFleetFactory.cumulativeState(rootScopeFactory.getData('fleetId'))]).then(function (response) {
                getActivityData(response)
                if (response[1].payload) {
                  $scope.fleetdata = response[1].payload
                  setCummulativeData()
                }
                $rootScope.showLoader = false
              })
            }, 20)
          } else {
            notify({message: 'Failed to resolve the ticket', duration: 2000, position: 'right'})
          }

          myFleetFactory.clearTicketsCache()
        })
      }

      $scope.$on('fleetChange', function (event, fleetId) {
        $rootScope.showLoader = true
        $q.all([myFleetFactory.getAllTickets(rootScopeFactory.getData('fleetId')),
          myFleetFactory.cumulativeState(rootScopeFactory.getData('fleetId'))]).then(function (response) {
          getActivityData(response)
          if (response[1].payload) {
            $scope.fleetdata = response[1].payload
            setCummulativeData()
          }
          $rootScope.showLoader = false
        })
        startTimer()
      })

      function startTimer () {
        stopTimer()
        promise = $interval(fetchBikes, 100000)
      }

      function stopTimer () {
        $interval.cancel(promise)
      }

      function fetchBikes () {
        $rootScope.showLoader = true
        myFleetFactory.clearTicketsCache()
        $timeout(function () {
          $q.all([myFleetFactory.getAllTickets(rootScopeFactory.getData('fleetId')),
            myFleetFactory.cumulativeState(rootScopeFactory.getData('fleetId'))]).then(function (response) {
            getActivityData(response)
            if (response[1].payload) {
              $scope.fleetdata = response[1].payload
              setCummulativeData()
            }
            $rootScope.showLoader = false
          })
        }, 20)
      }

      $scope.$on('$destroy', function () {
        stopTimer()
      })

      function getActivityData (response) {
        $scope.alertList = response[0].payload
        const fleet = JSON.parse(localStorage.getItem('currentFleet'))
        if ($scope.alertList.length) {
          $scope.alertList = _.reject($scope.alertList, function (num) { return num.ticket_category === 'maintenance_due' })
          for (let i = 0; i < $scope.alertList.length; i++) {
            $scope.alertList[i]['createdOn'] = moment.unix($scope.alertList[i].date_created).tz(fleet.fleet_timezone).format('LLL')
            if($scope.alertList[i]['category'] === "low_battery") {
              $scope.alertList[i]['operator_notes'] = `${$scope.alertList[i]['operator_notes']} on ${$scope.alertList[i]['createdOn']}`
            }
            if ($scope.alertList[i].bike_distance_after_service >= 0) {
              $scope.alertList[i].milesRemaining =
                            utilsFactory.roundOff(utilsFactory.getMiles(
                              $scope.alertList[i].bike_distance_after_service), 1)
            }
            if ($scope.alertList[i].bike_status) {
              $scope.alertList[i].bike_ticket_status =
                            $scope.alertList[i].bike_status
            }
          }
          $scope.alertList = _.sortBy($scope.alertList, 'ticket_id')
          $scope.alertList.reverse()
        }
      }

      function setCummulativeData () {
        $scope.cummulativehome = [{
          value: $scope.fleetdata.cumulative_bike_fleet_data.till_now,
          title: 'TOTAL RIDES IN FLEET',
          lastmonth: 'LAST MONTH ' + $scope.fleetdata.cumulative_bike_fleet_data.till_last_month,
          data_link: ''
        }, {
          value: $scope.fleetdata.cumulative_trip_fleet_data.till_now,
          title: 'TRIPS IN PROGRESS',
          lastmonth: 'THIS TIME YESTERDAY ' + $scope.fleetdata.cumulative_trip_fleet_data.till_yesterday,
          data_link: 'bike-locator'
        }, {
          value: $scope.fleetdata.cumulative_bikes_out_of_service.till_now,
          title: 'RIDES OUT OF SERVICE',
          lastmonth: 'THIS TIME YESTERDAY ' + $scope.fleetdata.cumulative_bikes_out_of_service.till_yesterday,
          data_link: 'out-of-service-bikes'
        }, {
          value: $scope.fleetdata.cumulative_ticket_data.from_yesterday,
          title: 'LATEST TICKETS',
          lastmonth: 'NEW TICKETS IN PAST 24 HRS',
          data_link: ''
        }, {
          value: $scope.fleetdata.cumulative_ticket_data.till_now,
          title: 'TOTAL TICKETS',
          lastmonth: 'TOTAL TICKETS OUTSTANDING',
          data_link: ''
        }]

        _.each($scope.cummulativehome, function (element) {
          if (element.title === 'TOTAL RIDES IN FLEET') {
            $scope.fleetdata.cumulative_bike_fleet_data.till_now >=
                        $scope.fleetdata.cumulative_bike_fleet_data.till_last_month ? element['marker'] = 'fa-arrow-up'
              : element['marker'] = 'fa-arrow-down'
          } else if (element.title === 'TRIPS IN PROGRESS') {
            $scope.fleetdata.cumulative_trip_fleet_data.till_now >=
                        $scope.fleetdata.cumulative_trip_fleet_data.till_yesterday ? element['marker'] = 'fa-arrow-up'
              : element['marker'] = 'fa-arrow-down'
          } else if (element.title === 'RIDES OUT OF SERVICE') {
            $scope.fleetdata.cumulative_bikes_out_of_service.till_now >=
                        $scope.fleetdata.cumulative_bikes_out_of_service.till_yesterday ? element['marker'] = 'fa-arrow-up'
              : element['marker'] = 'fa-arrow-down'
          }
        })
      }
      $scope.activityFilter = function (item) {
        return item.ticket_category === 'reported_theft' || item.ticket_category === 'damage_reported' || item.ticket_category === 'parking_outside_geofence' || item.ticket_category === 'service_due' || item.ticket_category === 'low_battery' || item.ticket_category === 'potential_theft'
      }
      // ACTIVITY LIST FEED
      $scope.showOptions = function (index, ticketId) {
        rootScopeFactory.setData('ticketSelected', ticketId)
        $rootScope.$broadcast('ticketSelected', ticketId)
        $rootScope.$emit('ticketSelected', ticketId)
        $scope.alertLists[index].state = !$scope.alertLists[index].state
      }
    })
