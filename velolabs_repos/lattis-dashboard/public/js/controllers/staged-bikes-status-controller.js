'use strict'

angular.module('skyfleet.controllers')
  .controller('stagedBikesController',
    function ($scope, $rootScope, $state, bikeFleetFactory, _, multipleKeyFilter, utilsFactory, rootScopeFactory, $timeout, lattisConstants, $filter, $interval) {
      $scope.stagedBike = true
      $scope.statusFilter = {}
      let promise
      $scope.bikeBox = {}
      $scope.addBike = false
      $scope.hideBikeOptions = true
      $scope.showFilterOptions = false
      $scope.inactiveheader = ['Ride name', 'Date added', 'Ride status', '']
      $scope.filterdata = false
      $scope.bikeIdList = []
      $scope.selectedBikes = []
      $scope.serviceEnabled = true
      $scope.optionsEnabled = true

      // ADD BIKE CLICK
      $scope.addBikeClick = function () {
        $state.go('add-bikes')
      }

      $scope.dropdownlist = [{
        property: 'current_status',
        name: 'Ellipse assigned',
        id: 'ELLIPSE ASSIGNED'
      }, {
        property: 'current_status',
        name: 'Ellipse unassigned',
        id: 'NO ELLIPSE'
      }]
      $scope.showlistitem = 4
      function getBikesData (fleetId) {
        bikeFleetFactory.getBikesData({fleet_id: fleetId}).then(function (response) {
          if (response.payload) {
            let activeBikeData = angular.copy(response.payload)
            if (!activeBikeData.bike_data.length) {
              $state.go('alert-addbikes')
            }
            activeBikeData['bike_data'] = _.filter(activeBikeData.bike_data, function (bikeData) {
              return bikeData.status === lattisConstants.inactiveStatus
            })
            calculateData(activeBikeData)
            $scope.fleetData = activeBikeData.bike_data
            $scope.filterdData = $scope.fleetData
            filterInit($scope.filterdData)
            $scope.bikeFilter()
            $scope.update()
          } else {
            $state.go('alert-addbikes')
          }
        })
      }

      if (rootScopeFactory.getData('fleetId')) {
        getBikesData(rootScopeFactory.getData('fleetId'))
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
          getBikesData(rootScopeFactory.getData('fleetId'))
        }, 20)
      }

      $scope.$on('$destroy', function () {
        stopTimer()
      })

      $scope.$on('clearBikeDataCache', function () {
        bikeFleetFactory.clearBikeDataCache()
        $timeout(function () {
          getBikesData(rootScopeFactory.getData('fleetId'))
        }, 100)
      })

      $scope.update = function () {
        _.each(angular.element(document.getElementsByClassName('dropdown-container show')), function (element) {
          if (element.id == 'drop') {
            element.className = 'dropdown-container'
          }
        })
        $scope.filterdData = $scope.activefleetData
        $scope.finalFilter = {}
        var filterTemp = {}
        var batteryArr = []

        _.each($scope.statusFilter, function (value, key) {
          if (value) {
            if (_.indexOf(['ELLIPSE ASSIGNED', 'NO ELLIPSE'], key) !== -1) {
              batteryArr.push($scope.dropdownlist[_.indexOf(['ELLIPSE ASSIGNED', 'NO ELLIPSE'], key)]['id'])
              filterTemp['current_status'] = batteryArr
            }
          }
        })
        if (_.isEmpty(filterTemp)) {
          $scope.filterdData = $scope.fleetData
        } else {
          $scope.filterdData = multipleKeyFilter($scope.fleetData, filterTemp)
        }
      }

      $scope.clearAll = function () {
        $rootScope.$broadcast('bikeFleetMenuClear')
      }

      $scope.$on('fleetChange', function (event, id) {
        getBikesData(id)
        startTimer()
      })

      function calculateData (data) {
        _.each(data.bike_data, function (element) {
          if (element.controllers && element.controllers.length > 0) {
            var controller = element.controllers[0]
            element['current_status'] = (controller.vendor + ' ' + controller.device_type + ' assigned').toUpperCase()
            element['ready_to_deploy'] = true
          } else if (element.current_status === lattisConstants.lockAssignedSubStatus) {
            element['current_status'] = 'ELLIPSE ASSIGNED'
            element['ready_to_deploy'] = true
          } else if (element.current_status === lattisConstants.lockNotAssignedSubStatus) {
            element['current_status'] = 'NO ELLIPSE'
          } else if (element.current_status === lattisConstants.balancingSubStatus) {
            element['current_status'] = 'BALANCING'
          } else if (element.current_status === lattisConstants.transportSubStatus) {
            element['current_status'] = 'TRANSPORT'
          }
          element['date_created'] = utilsFactory.dateStringToDate(element.date_created)
        })
      }

      function filterInit (data) {
        $scope.filterdData = data
        $scope.bikeIdList.splice(0, $scope.bikeIdList.length)
        _.each(data, function (element) {
          $scope.bikeIdList.push({'value': _.pick(element, 'bike_name')['bike_name']})
        })
      }

      $rootScope.$on('filterClear', function () {
        jQuery('#search').val("")
        getBikesData(rootScopeFactory.getData('fleetId'))
      })

      /* Button click function to filter the the bike table with the bike id in the search box */
      $scope.bikeFilter = function () {
        const result = _.filter($scope.fleetData, function (value) {
          const searchField = $('#search').val()
          if (value.bike_name.toLowerCase().includes(searchField.toLowerCase())) {
            return value
          }
          if (!searchField) {
            return $scope.fleetData
          } else {

          }
        })
        $scope.filterdData = result
      }

      $scope.$on('disableBikeOptions', function () {
        $scope.filterdData = _.reject($scope.filterdData, function (bike) {
          return _.indexOf(rootScopeFactory.getData('selectedBikes'), bike.bike_id) >= 0
        })
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
    })
