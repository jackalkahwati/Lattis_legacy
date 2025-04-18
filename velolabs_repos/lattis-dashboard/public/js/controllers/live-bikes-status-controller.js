'use strict'

angular.module('skyfleet.controllers')
  .controller('liveBikesController',
    function ($scope, $rootScope, $state, bikeFleetFactory, _, $filter, utilsFactory, multipleKeyFilter, rootScopeFactory, lattisConstants, sessionFactory, notify, $timeout, $interval, myFleetFactory, $q, tripAnimationFactory) {
      let promise
      $scope.hideBikeOptions = true
      $scope.showFilterOptions = false
      $scope.bikeIdList = []
      $scope.serviceEnabled = true
      $scope.optionsEnabled = true
      /* @param {object} List of all bikeID's for Autocomplete */
      $scope.selectedBikes = []
      /*
             * Data to be passed to the dropdown directive
             * @param {object} $scope.statusFilter, $scope.batteryFilter, $scope.maintanence Filter models for the dropdown filters
             * */
      $scope.statusFilter = {}
      $scope.batteryFilter = {}
      $scope.maintanenceFilter = {}
      $scope.dropdownlist = [{
        property: 'current_status',
        name: 'Parked',
        id: 'PARKED'
      }, {
        property: 'current_status',
        name: 'In ride',
        id: 'IN RIDE'
      }]
      $scope.batteryDropDown = [{
        property: 'battery_level',
        name: 'medium',
        id: 'medium'
      }, {
        property: 'battery_level',
        name: 'Full',
        id: 'full'
      }, {
        property: 'battery_level',
        name: 'Low',
        id: 'low'
      }]
      /* Table Dropdown Menu Options */
      $scope.dropdownactivelist = [{
        src: 'map',
        name: 'SEE ON MAP',
        href: '#F21B1B'
      }, {
        src: 'workshop',
        name: 'SEND TO WORKSHOP',
        href: '#workshopModal'
      }, {
        src: 'manage',
        name: 'MANAGE THIS RIDE',
        href: '#1B66F2'
      }, {
        src: 'delete',
        name: 'DELETE RIDE',
        href: '#deleteModal'
      }]
      $scope.activeheader = ['Ride name', 'Date Activity', 'Ride status', 'Distance until service', '']
      if (rootScopeFactory.getData('fleetId')) {
        fetchBikeData(rootScopeFactory.getData('fleetId'))
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
        tripAnimationFactory.clearTripsCache()
        $timeout(function () {
          fetchBikeData(rootScopeFactory.getData('fleetId'))
        }, 20)
      }

      $scope.$on('$destroy', function () {
        stopTimer()
      })

      function fetchBikeData (fleetId) {
        $rootScope.showLoader = true
        $q.all([
          myFleetFactory.getAllTickets(fleetId),
          bikeFleetFactory.getBikesData({fleet_id: fleetId}),
          tripAnimationFactory.getAllTrips({fleet_id: fleetId})
        ]).then(function (response) {
          console.log('response', response[1].payload);
          if (response[1].payload) {
            let activeBikeData = angular.copy(response[1].payload)
            if (!activeBikeData.bike_data.length) {
              $state.go('alert-addbikes')
            }
            activeBikeData['bike_data'] = _.filter(activeBikeData.bike_data, function (bikeData) {
              return bikeData.status === lattisConstants.liveStatus
            })
            $scope.distancePreference = JSON.parse(localStorage.getItem('currentFleet')).distance_preference
            calculateData(activeBikeData, response[0].payload, response[2].payload.trips)
            addTripsData(activeBikeData, response[2].payload.trips)
            $scope.activefleetData = activeBikeData.bike_data
            $scope.filterdData = $scope.activefleetData
            filterInit($scope.filterdData)
            $scope.bikeFilter()
            $scope.update()
          } else {
            $state.go('alert-addbikes')
          }
          $rootScope.showLoader = false
        }).catch(function () {
          $rootScope.showLoader = false
          notify.error("Error while fetching bike data")
        }
        )
      }

      function addTripsData (data, trips) {
        _.each(data.bike_data, function (element) {
          if (['PARKED', 'IN RIDE', 'COLLECT'].includes(element.current_status)) {
            let foundTrips = _.where(trips, {bike_id: element.bike_id})
            if (foundTrips.length) {
              element['trip_data'] = utilsFactory.sortDescBasedOnKey(foundTrips, 'trip_id')
              if (
                element.current_status === "IN RIDE" &&
                element.trip_data.steps[2]
              ) {
                element.action_date = utilsFactory.firstStepsDate(
                  element.trip_data.steps
                );
                element.action_time = utilsFactory.firstStepsHours(
                  element.trip_data.steps
                );
              } else if (
                ["PARKED", "COLLECT"].includes(element.current_status)
              ) {
                if (element.trip_data.reservation_id) {
                  // This reservation is pending or elapsed since the associated
                  // trip hasn't been taken. The most recent activity was the
                  // creation of the reservation, which we can infer from when
                  // the reservation was charged. However, sometimes, the fleet
                  // may not require payment (public_no_payment), in which case
                  // the reservation will not have been charged. Assume that the
                  // trip start date is (should have been) the reservation start.
                  const reservationCharged = element.trip_data.date_charged;

                  let reservationStart;
                  if (!reservationCharged && element.trip_data.reservation) {
                    reservationStart = moment
                      .utc(element.trip_data.reservation.reservation_start)
                      .unix();
                  }

                  const stepsStructure = [
                    [, , reservationCharged || reservationStart],
                  ];
                  element.action_date =
                    utilsFactory.lastStepsDate(stepsStructure);
                  element.action_time =
                    utilsFactory.lastStepsHours(stepsStructure);
                } else {
                  element.action_date = utilsFactory.lastStepsDate(
                    element.trip_data.steps
                  );
                  element.action_time = utilsFactory.lastStepsHours(
                    element.trip_data.steps
                  );
                }
              }
            } else {
              element.action_date = 'N/A'
              element.action_time = ''
            }
          }
        })
      }

      $scope.sendToStaging = function (current_status = null) {
        if (rootScopeFactory.getData('selectedBikes')) {
          bikeFleetFactory.sendToStaging({
            operator_id: sessionFactory.getCookieId(),
            bikes: rootScopeFactory.getData('selectedBikes'),
            current_status: current_status,
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

      function calculateData (data, tickets, trips) {
        _.each(data.bike_data, function (element) {
          if (element.current_status === lattisConstants.parkedSubStatus) {
            element['current_status'] = 'PARKED'
          } else if (element.current_status === lattisConstants.onTripSubStatus) {
            element['current_status'] = 'IN RIDE'
          } else if (element.current_status === lattisConstants.collectSubStatus) {
            element['current_status'] = 'COLLECT'
          } else if (element.current_status === lattisConstants.reservedSubStatus) {
            element['current_status'] = 'RESERVED'
          }

          let totalMileage;
          let maintainanceSchedule;
          let milesRemaining;

          const distanceCovered = trips.reduce((acc, trip) =>  {
            acc = trip.bike_id === element.bike_id ? acc + trip.trip_distance || 0 : acc
            return acc;
          },0);

          if ($scope.distancePreference === 'kilometers') {
            totalMileage = utilsFactory.meterToKm(distanceCovered)
            maintainanceSchedule = utilsFactory.meterToKm(element.maintenance_schedule);
            milesRemaining = utilsFactory.meterToKm(element.maintenance_schedule - element.distance_after_service)
          } else {
            totalMileage = utilsFactory.getMiles(distanceCovered)
            maintainanceSchedule = utilsFactory.getMiles(element.maintenance_schedule);
            milesRemaining = utilsFactory.getMiles(element.maintenance_schedule - element.distance_after_service)
          }

          element['distance_limit'] = maintainanceSchedule;
          element['distance_until_service'] = utilsFactory.roundOff(totalMileage < maintainanceSchedule ? maintainanceSchedule - totalMileage : milesRemaining, 2);
          element['status'] = utilsFactory.startCapitalize(element.status)
          element['date_created'] = utilsFactory.dateStringToDate(element.date_created)
          if (tickets.length) {
            _.each(tickets, function (ticket) {
              if (ticket.bike_id === element.bike_id) {
                element['has_ticket'] = true
              }
            })
          }
        })
      }

      /*
             * Drop down Filter update function
             */
      $scope.showlistitem = 4
      $scope.$watch('selectedBikes', function () {
        rootScopeFactory.setData('selectedBikes', $scope.selectedBikes)
      })

      bikeFleetFactory.getMaintenanceList().then(function (response) {
        $scope.maintanenceList = response.payload
        _.each($scope.maintanenceList, function (element) {
          element['maintenance_category'] = utilsFactory.startCapitalize(utilsFactory.underscoreToSpace(element['maintenance_category']))
        })
      })

      $scope.clearAll = function () {
        $rootScope.$broadcast('bikeFleetMenuClear')
      }

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
        var statusArr = []
        $scope.finalFilter = _.extend($scope.statusFilter, $scope.batteryFilter)
        _.each($scope.finalFilter, function (value, key) {
          if (value) {
            if (_.indexOf(['medium', 'full', 'low'], key) != -1) {
              batteryArr.push($scope.batteryDropDown[_.indexOf(['medium', 'full', 'low'], key)]['id'])
              filterTemp['battery_level'] = batteryArr
            } else if (_.indexOf(['PARKED', 'IN RIDE'], key) !== -1) {
              statusArr.push($scope.dropdownlist[_.indexOf(['PARKED', 'IN RIDE'], key)]['id'])
              filterTemp['current_status'] = statusArr
            }
          }
        })
        if (_.isEmpty(filterTemp)) {
          $scope.filterdData = $scope.activefleetData
        } else {
          $scope.filterdData = multipleKeyFilter($scope.activefleetData, filterTemp)
        }
        $rootScope.$broadcast('checkboxClear')
        $rootScope.$emit('checkboxClear')
      }
      /*
             * Watch for changes in FleetID and hits backend
             * and emits a broadcast event so that other controllers can listen to the changes
             * */

      $scope.$on('fleetChange', function (event, id) {
        fetchBikeData(id)
        startTimer()
      })

      $scope.moveToService = function () {
        $rootScope.$broadcast('sendToService')
        $rootScope.$emit('sendToService')
      }

      $scope.$on('clearBikeDataCache', function () {
        bikeFleetFactory.clearBikeDataCache()
        $scope.fleetData = {}
        $scope.activefleetdata = {}
        $timeout(function () {
          fetchBikeData(rootScopeFactory.getData('fleetId'))
        }, 200)
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

      function filterInit (data) {
        $scope.filterdData = data
        $scope.bikeIdList.splice(0, $scope.bikeIdList.length)
        _.each(data, function (element) {
          $scope.bikeIdList.push({'value': _.pick(element, 'bike_name')['bike_name']})
        })
      }

      $rootScope.$on('filterClear', function () {
        jQuery('#search').val("")
        fetchBikeData(rootScopeFactory.getData('fleetId'))
      })

      /* Button click function to filter the the bike table with the bike id in the search box */
      $scope.bikeFilter = function () {
        const result = _.filter($scope.activefleetData, function (value) {
          const searchField = $('#search').val()
          if (value.bike_name.toLowerCase().includes(searchField.toLowerCase())){
            return value
          }
          if (!searchField) {
            return $scope.activefleetData
          }  else {

          }
        })
        $scope.filterdData = result
      }
    })
