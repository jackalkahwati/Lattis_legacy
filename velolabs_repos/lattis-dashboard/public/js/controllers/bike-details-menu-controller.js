'use strict'

angular.module('skyfleet.controllers')
  .controller('bikeDetailsMenuController',
    function (rootScopeFactory, $scope, bikeFleetFactory, $state, _, tripAnimationFactory, utilsFactory, $q, lattisConstants, $rootScope, sessionFactory, notify, $timeout, myFleetFactory, mapFactory, usersFactory, ngDialog, timezoneConstants) {
      rootScopeFactory.setData('selectedBikes', [$state.params.bikeId])
      $scope.bikeFleets = [{
        title: 'DETAILS',
        url: 'bikedetails'
      }, {
        title: 'LIVE STATUS',
        url: 'livestatus'
      }, {
        title: 'ACTIVITY LOG',
        url: 'activitylog'
      }, {
        title: 'RESERVATIONS',
        url: 'reservations'
      }]
      $scope.currentTab = 'bikedetails'

      $scope.onClickTab = function (tab) {
        if (tab.disabled) {

        } else {
          getData(rootScopeFactory.getData('fleetId'))
          $scope.currentTab = tab.url
        }
        // $scope.currentTab = tab.url;
      }
      $scope.hidestage = true
      $scope.isActiveTab = function (tabUrl) {
        $scope.hidestage = ($scope.currentTab === 'activitylog' || $scope.currentTab === 'reservations')
        return tabUrl === $scope.currentTab
      }

      $scope.resolveTicket = function (ticketId) {
        myFleetFactory.resolveTicket(ticketId, function (res) {
          if (res.status === 200) {
            bikeFleetFactory.clearBikeDataCache()
            myFleetFactory.clearTicketsCache()
            $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
            $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
            notify({message: 'Resolved the ticket successfully', duration: 2000, position: 'right'})
          } else {
            notify({
              message: 'Failed to resolve the ticket successfully',
              duration: 2000,
              position: 'right'
            })
          }
        })
      }

      $scope.damageReportModal = function (index) {
        rootScopeFactory.setData('damageId', [$scope.ticketForBike[index]])
        // $rootScope.$emit('damageId', [$scope.ticketForBike[index]]);
        ngDialog.open({
          template: '../../html/modals/damage-report.html',
          controller: 'damageReportModalController'
        })
      }
      $scope.endrideModal = function () {
        rootScopeFactory.setData('endRideTripId', $scope.tripData.trip_id)
        ngDialog.open({
          template: '../../html/modals/end-ride.html',
          controller: 'endRideModalController',
          onOpenCallback: function () {
            let endridemap = new mapboxgl.Map({
              container: 'endridemap',
              style: 'https://s3-us-west-2.amazonaws.com/mapbox.sprite/light-v9.json',
              center: [-97.5122717, 40.828412],
              zoom: 3
            })
            endridemap.on('load', function () {
              endridemap.addLayer({
                'id': 'points',
                'type': 'symbol',
                'source': {
                  'type': 'geojson',
                  'data': turf.point(utilsFactory.lastPointInStepsArray($scope.tripData.steps))
                },
                'layout': {
                  'icon-image': 'redpin'
                }
              })
            })
            mapFactory.mapFitBounds(endridemap, turf.point(utilsFactory.lastPointInStepsArray($scope.tripData.steps)), 'low')
          }
        })
      }

      $scope.parseData = function (bikeData, tripData, allTrips = []) {
        $scope.distancePreference = JSON.parse(localStorage.getItem('currentFleet')).distance_preference
        bikeData.payload.bike_data[0]['date_created'] = utilsFactory.dateStringToDate(bikeData.payload.bike_data[0].date_created)
        bikeData.payload.bike_data[0]['type'] = utilsFactory.startCapitalize(bikeData.payload.bike_data[0].type)
        // bikeData.payload.bike_data[0]['meters_until_service'] = utilsFactory.getMiles(bikeData.payload.bike_data[0].meters_until_service);
        const distanceCovered = allTrips.payload.reduce((acc, trip) =>  {
          acc = acc + trip.trip_distance || 0;
          return acc;
        },0);

        let totalMileage;
        let maintainanceSchedule;
        let distanceRemaining = bikeData.payload.bike_data[0].maintenance_schedule - bikeData.payload.bike_data[0].distance_after_service;

        if($scope.distancePreference === 'kilometers') {
          totalMileage = utilsFactory.meterToKm(distanceCovered);
          maintainanceSchedule = utilsFactory.meterToKm(bikeData.payload.bike_data[0].maintenance_schedule)
          distanceRemaining = utilsFactory.meterToKm(distanceRemaining)
        } else {
          totalMileage = utilsFactory.getMiles(distanceCovered)
          maintainanceSchedule = utilsFactory.getMiles(bikeData.payload.bike_data[0].maintenance_schedule)
          distanceRemaining = utilsFactory.getMiles(distanceRemaining)
        }

        totalMileage = utilsFactory.roundOff(totalMileage, 2);
        maintainanceSchedule = utilsFactory.roundOff(maintainanceSchedule, 2);
        distanceRemaining = utilsFactory.roundOff(totalMileage < maintainanceSchedule ? maintainanceSchedule - totalMileage : distanceRemaining, 2);

        bikeData.payload.bike_data[0]['total_mileage'] = totalMileage
        Number.isFinite(bikeData.payload.bike_data[0].last_service_date)
          ? bikeData.payload.bike_data[0]['last_service_date'] =
                        utilsFactory.dateStringToDate(bikeData.payload.bike_data[0].last_service_date)
          : bikeData.payload.bike_data[0]['last_service_date'] = bikeData.payload.bike_data[0].date_created


        bikeData.payload.bike_data[0]['distanceRemaining'] = distanceRemaining;
        if (!Number.isFinite(bikeData.payload.bike_data[0].bike_battery_level)) {
          bikeData.payload.bike_data[0]['bike_battery_level'] = 'N/A'
        }
        if (bikeData.payload.bike_data[0].longitude) {
          $scope.geometry = turf.point([bikeData.payload.bike_data[0].longitude, bikeData.payload.bike_data[0].latitude])
          utilsFactory.reverseGeoCode(bikeData.payload.bike_data[0].latitude, bikeData.payload.bike_data[0].longitude, function (response) {
            $scope.geoCodingResult = response
          })
        }
        // tripData['user_name'] = utilsFactory.startCapitalize(tripData.first_name) + ' ' + utilsFactory.startCapitalize(tripData.last_name);
        if (tripData) {
          var startTimeMs = utilsFactory.getTimeRaw(tripData.steps[0][2]);
          var endTimeMs = Date.now()
          var durationMs = endTimeMs - startTimeMs
          tripData['trip_duration'] = utilsFactory.formatDurationSeconds(durationMs / 1000)
          tripData['trip_distance'] = utilsFactory.getMiles(tripData.trip_distance)
          // tripData['trip_duration'] = utilsFactory.getDuration(tripData.trip_duration);
          tripData['date_create'] = utilsFactory.dateStringToDate(tripData.date_created)
          tripData['time_created'] = utilsFactory.getDateTime(tripData.date_created)
          var riderName = _.where($scope.allRiders, {user_id: tripData.user_id})[0]
          if (riderName) {
            tripData['rider'] = riderName.first_name + ' ' + riderName.last_name
          }
          if (bikeData.payload.bike_data[0].current_status === 'on_trip') {
            if (tripData.steps.length === 1) {
              let dummyTripData = [tripData.steps[0], tripData.steps[0]]
              $scope.geometry = utilsFactory.arrayToLineStringGeoJson(dummyTripData)
            } else {
              $scope.geometry = utilsFactory.arrayToLineStringGeoJson(tripData.steps)
            }
          }
        }
        $scope.bikeData = bikeData.payload.bike_data[0]
        $scope.tripData = tripData
        if (tripData) rootScopeFactory.setData('currentTripData', tripData)
        rootScopeFactory.setData('currentBikeData', bikeData.payload.bike_data[0])
        $rootScope.$broadcast('gotBikeData')
      }

      $scope.selectLatestTrip = function (tripData) {
        return utilsFactory.sortDescBasedOnKey(tripData.payload, 'trip_id')
      }

      $scope.addStatusDetails = function (bikeDataCopy) {
        let parsedData = bikeDataCopy.payload.bike_data[0]
        if (parsedData.current_status === 'parked') {
          parsedData['real_status'] = 'Parked'
        } else if (parsedData.current_status === 'on_trip') {
          parsedData['real_status'] = 'Trip in progress'
        } else if (parsedData.current_status === 'under_maintenance') {
          parsedData['real_status'] = 'Undergoing maintenance'
        } else if (parsedData.current_status === 'lock_not_assigned') {
          parsedData['real_status'] = 'Ride is dormant'
        } else if (parsedData.current_status === 'lock_assigned') {
          parsedData['real_status'] = 'Ready to deploy to live fleet'
        } else if (parsedData.current_status === 'reported_stolen') {
          parsedData['real_status'] = 'Ride has been reported stolen'
        } else if (parsedData.current_status === 'damaged') {
          parsedData['real_status'] = 'Ride has been reported damaged'
        }
      }

      if (rootScopeFactory.getData('fleetId')) {
        getData(rootScopeFactory.getData('fleetId'))
      }

      $scope.$on('fleetChange', function (event, id) {
        getData(id)
      })

      $scope.$on('updateBikeDetails', function (event) {
        getData(rootScopeFactory.getData('fleetId'))
      })

      function getData(fleetId) {
        $rootScope.showLoader = true
        $q.all([
          bikeFleetFactory.getBikesData({fleet_id: fleetId}),
          tripAnimationFactory.getAllTrips({fleet_id: fleetId}),
          myFleetFactory.getAllTickets(fleetId)
        ]).then(function (response) {
          $scope.bikeDataCopy = angular.copy(response[0])
          $scope.tripDataCopy = angular.copy(response[1])
          $scope.tripDataCopy.payload = $scope.tripDataCopy.payload.trips.filter(trip => trip.bike_id === parseInt($state.params.bikeId))
          let latestTrip = $scope.selectLatestTrip($scope.tripDataCopy)
          if (latestTrip) {
            usersFactory.getMember({user_id: latestTrip.user_id}).then(function (res) {
              if (res && res.payload) {
                $scope.allRiders = res.payload
                $scope.filterSelectedBike($scope.bikeDataCopy)
                $scope.addStatusDetails($scope.bikeDataCopy)
                $scope.parseData($scope.bikeDataCopy, latestTrip, $scope.tripDataCopy)
                $scope.checkBikeHasTicket(response[2].payload)
              }
            })
          } else {
            $scope.allRiders = []
            $scope.filterSelectedBike($scope.bikeDataCopy)
            $scope.addStatusDetails($scope.bikeDataCopy)
            $scope.parseData($scope.bikeDataCopy, null, $scope.tripDataCopy)
            $scope.checkBikeHasTicket(response[2].payload)
          }
          $rootScope.showLoader = false
        })
      }

      $scope.$on('clearBikeDataCache', function () {
        bikeFleetFactory.clearBikeDataCache()
        myFleetFactory.clearTicketsCache()
        $timeout(function () {
          $rootScope.showLoader = true
          $timeout(function(){
            $rootScope.showLoader = false
          }, 3000)
          $q.all([
            bikeFleetFactory.getBikesData({fleet_id: rootScopeFactory.getData('fleetId')}),
            tripAnimationFactory.getAllTrips({fleet_id: rootScopeFactory.getData('fleetId')}),
            myFleetFactory.getAllTickets(rootScopeFactory.getData('fleetId'))
          ]).then(function (response) {
            $scope.bikeDataCopy = angular.copy(response[0])
            $scope.tripDataCopy = angular.copy(response[1])
            $scope.tripDataCopy.payload = $scope.tripDataCopy.payload.filter(trip => trip.bike_id === parseInt($state.params.bikeId))
            let latestTrip = $scope.selectLatestTrip($scope.tripDataCopy)
            if (latestTrip) {
              usersFactory.getMember({user_id: latestTrip.user_id}).then(function (res) {
                if (res && res.payload) {
                  $scope.allRiders = res.payload
                  $scope.filterSelectedBike($scope.bikeDataCopy)
                  $scope.addStatusDetails($scope.bikeDataCopy)
                  $scope.parseData($scope.bikeDataCopy, latestTrip, $scope.tripDataCopy)
                  $scope.checkBikeHasTicket(response[2].payload)
                }
              })
            } else {
              $scope.allRiders = []
              $scope.filterSelectedBike($scope.bikeDataCopy)
              $scope.addStatusDetails($scope.bikeDataCopy)
              $scope.parseData($scope.bikeDataCopy, null, $scope.tripDataCopy)
              $scope.checkBikeHasTicket(response[2].payload)
            }
          })
        }, 50)
        rootScopeFactory.setData('selectedBikes', [$state.params.bikeId])
      })

      $scope.filterSelectedBike = function (data) {
        _.each(data.payload, function (element, key) {
          data.payload[key] = _.where(element, {'bike_id': parseInt($state.params.bikeId)})
        })
      }

      $scope.checkBikeHasTicket = function (data) {
        $scope.ticketForBike = _.where(data, {bike_id: parseInt($state.params.bikeId)})
        const fleet = JSON.parse(localStorage.getItem('currentFleet'))
        _.each($scope.ticketForBike, function (element, index) {
          $scope.ticketForBike[index]['createdOn'] = moment.unix($scope.ticketForBike[index].date_created).tz(fleet.fleet_timezone).format('LLL')
          // if($scope.ticketForBike[index]['category'] === "low_battery") {
            $scope.ticketForBike[index]['operator_notes'] = `${$scope.ticketForBike[index]['operator_notes']} on ${$scope.ticketForBike[index]['createdOn']}`
          // }
        })
      }

      $scope.moveToService = function () {
        $rootScope.$broadcast('sendToService')
        $rootScope.$emit('sendToService')
      }

      $scope.sendToStaging = function () {
        if (rootScopeFactory.getData('selectedBikes')) {
          bikeFleetFactory.sendToStaging({
            operator_id: sessionFactory.getCookieId(),
            bikes: [$state.params.bikeId]
          }, function (response) {
            if (response && response.status === 200) {
              notify({message: 'Rides sent to Staging successfully', duration: 2000, position: 'right'})
              bikeFleetFactory.clearBikeDataCache()
              $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
              $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
            } else {
              notify({message: 'Failed to send rides to Staging', duration: 2000, position: 'right'})
            }
          })
        }
      }

      $scope.activityFilter = function (item) {
        return item.ticket_category === 'reported_theft' || item.ticket_category === 'damage_reported' || item.ticket_category === 'parking_outside_geofence' || item.ticket_category === 'service_due' || item.ticket_category === 'low_battery' || item.ticket_category === 'potential_theft'
      }
    })
