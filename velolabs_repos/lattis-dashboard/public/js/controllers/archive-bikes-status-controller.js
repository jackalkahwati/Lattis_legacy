'use strict'

angular.module('skyfleet.controllers')
  .controller('archivedBikesController',
    function ($scope, $rootScope, $state, bikeFleetFactory, _, $filter, multipleKeyFilter, utilsFactory, rootScopeFactory, lattisConstants) {
      $scope.hideBikeOptions = true
      $scope.bikeIdList = []
      $scope.statusFilter = {}
      $scope.selectedBikes = []
      $scope.archiveheader = ['Ride name', 'Date deleted', 'Ride status', '']
      $scope.serviceEnabled = true
      $scope.optionsEnabled = true
      $scope.dropdownlist = [{
        property: 'current_status',
        name: 'Total loss',
        id: 'TOTAL LOSS'
      }, {
        property: 'current_status',
        name: 'Defleeted',
        id: 'DEFLEETED'
      }, {
        property: 'current_status',
        name: 'Stolen',
        id: 'STOLEN'
      }]

      if (rootScopeFactory.getData('fleetId')) {
        bikeFleetFactory.getBikesData({fleet_id: rootScopeFactory.getData('fleetId')}).then(function (response) {
          if (response.payload) {
            let activeBikeData = angular.copy(response.payload)
            if (!activeBikeData.bike_data.length) {
              $state.go('alert-addbikes')
            }
            activeBikeData['bike_data'] = _.filter(activeBikeData.bike_data, function (bikeData) {
              return bikeData.status === lattisConstants.deletedStatus
            })
            calculateData(activeBikeData)
            $scope.archivedBikesData = activeBikeData.bike_data
            $scope.filterdData = $scope.archivedBikesData
            filterInit($scope.filterdData)
            $scope.bikeFilter()
            $scope.update()
          } else {
            $state.go('alert-addbikes')
          }
        })
      }

      $scope.$on('fleetChange', function (event, id) {
        bikeFleetFactory.getBikesData({fleet_id: id}).then(function (response) {
          if (response.payload) {
            let activeBikeData = angular.copy(response.payload)
            activeBikeData['bike_data'] = _.filter(activeBikeData.bike_data, function (bikeData) {
              return bikeData.status === lattisConstants.deletedStatus
            })
            calculateData(activeBikeData)
            $scope.archivedBikesData = activeBikeData.bike_data
            $scope.filterdData = $scope.archivedBikesData
            filterInit($scope.filterdData)
          }
        })
      })
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

      $scope.sendToStaging = function () {
        if (rootScopeFactory.getData('selectedBikes')) {
          bikeFleetFactory.sendToStaging({
            operator_id: sessionFactory.getCookieId(),
            bikes: rootScopeFactory.getData('selectedBikes')
          }, function (response) {
            if (response && response.status === 200) {
              notify({message: 'Rides sent to Staging successfully', duration: 2000, position: 'right'})
              $rootScope.$broadcast('clearBikeDataCache')
              $rootScope.$emit('clearBikeDataCache')
              rootScopeFactory.setData('selectedBikes', null)
              $scope.$emit('disableBikeOptions')
            } else {
              notify({message: 'Failed to send rides to Staging', duration: 2000, position: 'right'})
              rootScopeFactory.setData('selectedBikes', null)
            }
          })
        }
      }

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
            if (_.indexOf(['TOTAL LOSS', 'DEFLEETED', 'STOLEN'], key) !== -1) {
              batteryArr.push($scope.dropdownlist[_.indexOf(['TOTAL LOSS', 'DEFLEETED', 'STOLEN'], key)]['id'])
              filterTemp['current_status'] = batteryArr
            }
          }
        })
        if (_.isEmpty(filterTemp)) {
          $scope.filterdData = $scope.archivedBikesData
        } else {
          $scope.filterdData = multipleKeyFilter($scope.archivedBikesData, filterTemp)
        }
      }

      function calculateData (data) {
        _.each(data.bike_data, function (element) {
          if (element.current_status === lattisConstants.totalLossSubStatus) {
            element['current_status'] = 'TOTAL LOSS'
          } else if (element.current_status === lattisConstants.reportedStolenSubStatus) {
            element['current_status'] = 'STOLEN'
          } else if (element.current_status === lattisConstants.defleetedSubStatus) {
            element['current_status'] = 'DEFLEETED'
          }
          element['date_created'] = utilsFactory.dateStringToDate(element.date_created)
          element['meters_until_service'] = utilsFactory.getMiles(element.meters_until_service)
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
        $scope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
      })

      $scope.bikeFilter = function () {
        const result = _.filter($scope.archivedBikesData, function (value) {
          const searchField = $('#search').val()
          if (value.bike_name.toLowerCase().includes(searchField.toLowerCase())) {
            return value
          }
          if (!searchField) {
            return $scope.archivedBikesData
          } else {

          }
        })
        $scope.filterdData = result
      }
    })
