'use strict'

angular.module('skyfleet.controllers')
  .controller('outOfServiceBikesController',
    function ($scope, $rootScope, $state, bikeFleetFactory, _, $filter, multipleKeyFilter, utilsFactory, rootScopeFactory, lattisConstants, sessionFactory, notify, $timeout, $interval, myFleetFactory, $q) {
      let promise
      $scope.selectedBikes = []
      $scope.suspendheader = ['Ride name', 'Date added', 'Ride status', '']
      $scope.serviceEnabled = true
      $scope.optionsEnabled = true

      function fetchBikesData (fleetId) {
        $q.all([myFleetFactory.getAllTickets(fleetId),
          bikeFleetFactory.getBikesData({fleet_id: fleetId})]).then(function (response) {
          if (response[1].payload) {
            let inactiveBikeData = angular.copy(response[1].payload)
            if (!inactiveBikeData.bike_data.length) {
              $state.go('alert-addbikes')
            }
            inactiveBikeData['bike_data'] = _.filter(inactiveBikeData.bike_data, function (bikeData) {
              return bikeData.status === lattisConstants.suspendedStatus
            })
            let ticketData = angular.copy(response[0].payload)
            calculateData(inactiveBikeData, ticketData)
            $scope.suspendefleetData = inactiveBikeData.bike_data
            $scope.filterdData = $scope.suspendefleetData
            filterInit($scope.filterdData)
            $scope.bikeFilter()
            $scope.update()
          } else {
            $state.go('alert-addbikes')
          }
        })
      }

      if (rootScopeFactory.getData('fleetId')) {
        fetchBikesData(rootScopeFactory.getData('fleetId'))
        startTimer()
      }

      function startTimer () {
        stopTimer()
        promise = $interval(fetchBikes, 100000)
      }

      function stopTimer () {
        $interval.cancel(promise)
      }

      function fetchBikes () {
        bikeFleetFactory.clearBikeDataCache()
        $timeout(function () {
          fetchBikesData(rootScopeFactory.getData('fleetId'))
        }, 20)
      }

      $scope.$on('$destroy', function () {
        stopTimer()
      })

      function calculateData (data, tickets) {
        _.each(data.bike_data, function (element) {
          if (element.current_status === lattisConstants.stolenSubStatus ||
                        element.current_status === lattisConstants.reportedStolenSubStatus) {
            element['current_status'] = 'REPORTED STOLEN'
          } else if (element.current_status === lattisConstants.maintenanceSubStatus) {
            element['current_status'] = 'MAINTENANCE'
          } else if (element.current_status === lattisConstants.damagedSubStatus) {
            element['current_status'] = 'DAMAGE REPORTED'
          }
          let ticketsArray = _.sortBy(_.where(tickets, {bike_id: element.bike_id}), 'ticket_date_created').reverse()
          if (ticketsArray.length) {
            element['ticket_date_created'] = utilsFactory.dateStringToDate(ticketsArray[0].ticket_date_created)
          }
          element['date_created'] = utilsFactory.dateStringToDate(element.date_created)
          if (!_.isEmpty(_.where(data.maintenance, {bike_id: element.bike_id}))) {
            let service = _.where(data.maintenance, {bike_id: element.bike_id})
            element['service_date'] = utilsFactory.dateStringToDate(service[service.length - 1].service_start_date)
          }
          if (tickets.length) {
            _.each(tickets, function (ticket) {
              if (ticket.bike_id === element.bike_id) {
                element['has_ticket'] = true
              }
            })
          }
        })
      }

      $scope.dropdownlist = [{
        property: 'current_status',
        name: 'Report stolen',
        id: 'REPORTED STOLEN'
      }, {
        property: 'current_status',
        name: 'Undergoing maintenance',
        id: 'MAINTENANCE'
      }, {
        property: 'current_status',
        name: 'Damage reported',
        id: 'DAMAGE REPORTED'
      }]

      $scope.statusFilter = {}
      $scope.bikeIdList = []
      $scope.hideBikeOptions = true
      $scope.showFilterOptions = false

      $scope.$on('fleetChange', function (event, id) {
        fetchBikesData(id)
        startTimer()
      })

      $scope.update = function () {
        _.each(angular.element(document.getElementsByClassName('dropdown-container show')), function (element) {
          if (element.id == 'drop') {
            element.className = 'dropdown-container'
          }
        })
        $scope.filterdData = $scope.suspendefleetData
        $scope.finalFilter = {}
        var filterTemp = {}
        var batteryArr = []
        _.each($scope.statusFilter, function (value, key) {
          if (value) {
            if (_.indexOf(['REPORTED STOLEN', 'MAINTENANCE', 'DAMAGE REPORTED'], key) !== -1) {
              batteryArr.push($scope.dropdownlist[_.indexOf(['REPORTED STOLEN', 'MAINTENANCE', 'DAMAGE REPORTED'], key)]['id'])
              filterTemp['current_status'] = batteryArr
            }
          }
        })
        if (_.isEmpty(filterTemp)) {
          $scope.filterdData = $scope.suspendefleetData
        } else {
          $scope.filterdData = multipleKeyFilter($scope.filterdData, filterTemp)
        }
      }

      $scope.clearAll = function () {
        $rootScope.$broadcast('bikeFleetMenuClear')
      }

      function filterInit (data) {
        $scope.filterdData = data
        $scope.bikeIdList.splice(0, $scope.bikeIdList.length)
        _.each(data, function (element) {
          $scope.bikeIdList.push({'value': _.pick(element, 'bike_name')['bike_name']})
        })
      }

      $scope.suspendedClick = function () {
        $scope.filterdData = $scope.suspendefleetData
        var filterTemp = {}
        var maintanenceArr = []
        _.each($scope.statusFilter, function (value, key) {
          if (value) {
            if (_.indexOf(['REPORTED STOLEN', 'MAINTENANCE', 'DAMAGE REPORTED'], key) !== -1) {
              maintanenceArr.push($scope.dropdownlist[_.indexOf(['REPORTED STOLEN', 'MAINTENANCE', 'DAMAGE REPORTED'], key)]['id'])
              filterTemp['current_status'] = maintanenceArr
            }
          }
        })
        if (_.isEmpty(filterTemp)) {
          $scope.filterdData = $scope.suspendefleetData
        } else {
          $scope.filterdData = multipleKeyFilter($scope.suspendefleetData, filterTemp)
        }
      }

      $rootScope.$on('filterClear', function () {
        jQuery('#search').val("")
        fetchBikeData(rootScopeFactory.getData('fleetId'))
      })

      $scope.bikeFilter = function () {
        const result = _.filter($scope.suspendefleetData, function (value) {
          const searchField = $('#search').val()
          if (value.bike_name.toLowerCase().includes(searchField.toLowerCase())) {
            return value
          }
          if (!searchField) {
            return $scope.suspendefleetData
          } else {

          }
        })
        $scope.filterdData = result
      }

      $scope.$on('disableBikeOptions', function () {
        $scope.serviceEnabled = true
        $scope.optionsEnabled = true
      })

      $scope.$on('enableBikeOptions', function () {
        $scope.serviceEnabled = false
        $scope.optionsEnabled = false
      })

      $scope.$on('enableService', function () {
        $scope.serviceEnabled = false
      })

      $scope.$on('disableService', function () {
        $scope.serviceEnabled = true
      })

      $scope.sendToStaging = function (status = null, current_status = null) {
        if (rootScopeFactory.getData('selectedBikes')) {
          bikeFleetFactory.sendToStaging({
            operator_id: sessionFactory.getCookieId(),
            bikes: rootScopeFactory.getData('selectedBikes'),
            current_status: current_status,
            status: status
          }, function (response) {
            if (response && response.status === 200) {
              notify({message: 'Bikes sent to Staging successfully', duration: 2000, position: 'right'})
              $rootScope.$broadcast('clearBikeDataCache')
              $rootScope.$emit('clearBikeDataCache')
              rootScopeFactory.setData('selectedBikes', null)
              $scope.$emit('disableBikeOptions')
            } else {
              notify({message: 'Failed to send bikes to Staging', duration: 2000, position: 'right'})
              rootScopeFactory.setData('selectedBikes', null)
            }
          })
        }
      }

      $scope.$on('clearBikeDataCache', function () {
        bikeFleetFactory.clearBikeDataCache()
        $timeout(function () {
          fetchBikesData(rootScopeFactory.getData('fleetId'))
        }, 200)
      })
    })
