'use strict'

angular.module('skyfleet.controllers')
  .controller('memberProfileController',
    async function ($scope, $rootScope, $state, _, $filter, utilsFactory, multipleKeyFilter, rootScopeFactory, bikeFleetFactory, usersFactory, $stateParams, notify, regexPatterns, $window) {

      const [patterns] = regexPatterns
      $scope.emailRegexPatterns = patterns.email
      $scope.accessChanged = false;
      $scope.shouldShowAccess = false
      const getUserData = async (fleetId) => {
        $rootScope.showLoader = true
        const userDetails = {
          fleet_id: rootScopeFactory.getData('fleetId') || fleetId,
          user_id: $state.params.userId
        }
        const {data: user} = await usersFactory.getUser(userDetails)
        $scope.customerdetails = user.payload
        if ($scope.shouldShowAccess && !$scope.customerdetails.access) {
          $scope.customerdetails.access = 0
        }
        $scope.$apply();
        $rootScope.showLoader = false
      }

      $scope.fetchUserData = async () => {
        try {
          const currentFleet = JSON.parse(localStorage.getItem('currentFleet'))
          await getUserData(rootScopeFactory.getData('fleetId') || currentFleet.fleet_id)
          const {data: fleetData} = await bikeFleetFactory.fetchFleetData(currentFleet.fleet_id)
          $scope.fleetType = fleetData.payload.type
          $scope.shouldShowAccess = ['private', 'private_no_payment'].includes($scope.fleetType);
          console.log('fleet, user data fetched data successfully...')
          $scope.$apply()
        } catch (error) {
          console.error('failed to fetch user or fleet data')
        }
      }

      if ($scope.adminACL === undefined) {
        $scope.adminACL = rootScopeFactory.getData('adminACL')
        $scope.coordinatorACL = rootScopeFactory.getData('coordinatorACL')
        $scope.technicianACL = rootScopeFactory.getData('technicianACL')
      }

      if (rootScopeFactory.getData('fleetId')) {
        await getUserData(rootScopeFactory.getData('fleetId'))
      }
      $scope.validateFields = function (data) {
        data.$setDirty()
      }

      $scope.accessInquiryClick = function () {
        $scope.accessInquiry = !$scope.accessInquiry
      }

      function toggleValidation () {
        $scope.form.firstname.$setDirty()
        $scope.form.lastname.$setDirty()
        $scope.form.phone.$setDirty()
        $scope.form.email.$setDirty()
      }

      $scope.setPrist = function () {
        $scope.accessChanged = true;
      }

      $scope.saveProfile = async () => {
        if (!$scope.form.$pristine) {
          // $scope.customerdetails.country_code = utilsFactory.appendPlus($scope.customerdetails.phone_number)
          toggleValidation(true)
          if (_.isEmpty($scope.form.$error.required) && _.isEmpty($scope.form.$error.pattern)) {
            usersFactory.updateMemberProfile(_.pick($scope.customerdetails, 'first_name', 'user_id',
              'last_name', 'country_code'), function (response) {
              if (response && response.status === 200) {
                notify({
                  message: 'User profile has been updated successfully',
                  duration: 2000,
                  position: 'right'
                })
              } else {
                notify({
                  message: 'Failed to update the member Archive',
                  duration: 2000,
                  position: 'right'
                })
              }
            })
          }
        /*                toggleValidation();
                 $scope.customerdetails.country_code = utilsFactory.appendPlus($scope.customerdetails.country_code);
                 if ($scope.form.$dirty && !_.keys($scope.form.$error).length > 0) {
                 usersFactory.updateUserData(_.pick($scope.customerdetails, 'user_id', 'first_name',
                 'last_name', 'country_code', 'phone_number', 'email'), function (response) {
                 });
                 } */
        }
        if ($scope.accessChanged) {
          try {
            const userDetails = {
              fleet_id: rootScopeFactory.getData('fleetId'),
              user_id: $state.params.userId,
              access: $scope.customerdetails.access,
              email: $scope.customerdetails.email,
            }
            const {data: toggleAccess} = await usersFactory.toggleIndividualAccess(userDetails)
            $scope.accessChanged = false
            $scope.customerdetails = toggleAccess.payload
            notify({
              message: 'User access has been updated',
              duration: 2000,
              position: 'right'
            })
          } catch (error) {
            notify({
              message: 'Failed to update update user access',
              duration: 2000,
              position: 'right'
            })
          }
        }
      }
    })

angular.module('skyfleet.controllers')
  .controller('tripDetailsController',
    function (
      $scope, $rootScope, $state, _, $filter, utilsFactory, multipleKeyFilter, rootScopeFactory,
      usersFactory, mapFactory, tripAnimationFactory, $window, $q, bikeFleetFactory, myFleetFactory,
      ngDialog, notify, lattisConstants, hubsFactory
    ) {

      // MapInitialization
      var map = mapFactory.initMap([-97.5122717, 40.828412], 3)
      map.addControl(new mapboxgl.NavigationControl({
        position: 'top-right'
      }))
      let currentFleetType = JSON.parse(localStorage.getItem('currentFleet')).type

      $scope.noPayment = currentFleetType.includes('no_payment');
      $scope.starRating = null;
      $scope.loadingTripInfo = true;

      $scope.filterSelectedBike = function (data, bikeId) {
        _.each(data.payload, function (element, key) {
          data.payload[key] = _.where(element, {'bike_id': bikeId})
        })
        return data.payload.bike_data[0]
      }

      $scope.getHubDetails = function (data) {
        var hubs = _.map(data, function (hub) {
          hub.ports = _.filter(hub.ports, function(port) { return port.portId === $scope.currentTrip.port_id })
          return hub
        })
        return _.filter(hubs, function(hub) { return hub.ports.length > 0 })[0]
      }

      $scope.goToBikeDetails = function () {
        $state.go('bike-details', {bikeId: $scope.currentTrip.bike_id})
      }
      $scope.goToPortDetails = function () {
        $state.go('port-details', {uuid: $scope.portData.ports[0].portUUID})
      }

      let fleetId = rootScopeFactory.getData('fleetId') || JSON.parse(localStorage.getItem('currentFleet')).fleet_id

      $q.all([
        bikeFleetFactory.getBikesData({fleet_id: fleetId}),
        tripAnimationFactory.getAllTrips({trip_id: $state.params.trip_id}),
        myFleetFactory.getAllTickets(fleetId, true),
        hubsFactory.list()
      ]).then(function (response) {
        $scope.currentTrip = response[1].payload.trips[0]
        let taxes = $scope.currentTrip.taxes
        if(taxes) {
          taxes = JSON.parse(taxes)
          $scope.currentTrip.taxes = taxes
        }
        $scope.portData = $scope.getHubDetails(response[3])
        $scope.calculateDistance($scope.currentTrip)
        $scope.currentTrip.tickets = response[2].payload.filter(ticket => ticket.trip_id === Number($state.params.trip_id))
        $scope.currentTrip.theftReported = !!$scope.currentTrip.tickets.find(i => i.ticket_category == "reported_theft")
        $scope.currentTrip.damageReported = !!$scope.currentTrip.tickets.find(i => i.ticket_category == "damage_reported")
        $scope.currentTrip.potentialTheft = !!$scope.currentTrip.tickets.find(i => i.ticket_category == "potential_theft")
        $scope.currentTrip.hasParkingViolation = !!$scope.currentTrip.tickets.find(ticket => {
          return ticket.ticket_category === 'parking_outside_geofence'
        })
        $scope.bike = $scope.filterSelectedBike(response[0], $scope.currentTrip.bike_id)

        $scope.loadingTripInfo = false;
      }).catch(function (response) {
        $scope.loadingTripInfo = false;
        if (response && response.data && response.data.message) {
          $scope.loadingTripError = response.data.message
        }
      })


      $scope.calculateDistance = function (trip) {
        var routeArray = trip.steps
        var hasSteps = routeArray.length > 0 && routeArray[0].length > 0

        var duration;
        if (trip.end_address) { // trip has ended
          duration = trip.duration
        } else if (trip.date_created) { // trips is in progress
          var startTimeMs = hasSteps ? utilsFactory.getTimeRaw(routeArray[0][2]) : utilsFactory.getTimeRaw(trip.date_created);
          var endTimeMs = Date.now()
          var durationMs = endTimeMs - startTimeMs
          duration = durationMs / 1000
        }
        trip.formatted_duration = duration ? utilsFactory.formatDurationSeconds(duration) : 'N/A';

        if (trip.rating) {
          $scope.starRating = trip.rating
        }

        const fleet = JSON.parse(localStorage.getItem('currentFleet'))
        trip['start_time'] = moment.unix(trip.date_created).tz(fleet.fleet_timezone).format('hh:mm a, MM/DD/YYYY')
        trip['end_time'] = moment.unix(trip.date_endtrip).tz(fleet.fleet_timezone).format('hh:mm a, MM/DD/YYYY')
        trip['geoJSON'] = hasSteps ? utilsFactory.arrayToLineStringGeoJson(routeArray) : null
        trip['startEnd'] = hasSteps
          ? turf.featureCollection([
            turf.point([routeArray[0][1], routeArray[0][0]], {icon: 'redpin'}),
            turf.point([routeArray[routeArray.length - 1][1], routeArray[routeArray.length - 1][0]], {icon: 'current'})
          ])
          : null

        if (trip.charge_for_duration) {
          trip.charge_for_duration = Number.parseFloat(trip.charge_for_duration).toFixed(2)
          trip.total = Number.parseFloat(trip.total).toFixed(2)
          trip.over_usage_fees = Number.parseFloat(trip.over_usage_fees).toFixed(2)
          trip.penalty_fees = Number.parseFloat(trip.penalty_fees).toFixed(2)
          $scope.fleetCurrency = lattisConstants.currencyCodeSymbolMap[
            (trip.currency ? trip.currency : rootScopeFactory.getData('fleetCurrency'))
          ] || ''
        }
      }

      $scope.formatReservationDuration = function(reservation) {
        return moment
          .duration(
            moment(reservation.reservation_end).diff(moment(reservation.reservation_start))
          )
          .format("d [days] h [hours] m [minutes]")
      }

      $scope.formatReservationTime = function(reservation, time) {
        return moment(time)
          .tz(reservation.reservation_timezone)
          .format('MM/DD/YYYY HH:mmA z')
      }

      $scope.reservationElapsed = reservation => {
        return (
          !reservation.reservation_terminated &&
          moment(reservation.reservation_end)
            .tz(reservation.reservation_timezone)
            .isBefore(moment())
        );
      }

      $scope.mapLoaded = false;
      $scope.$watchGroup(['currentTrip', 'mapLoaded'], function(){
        if($scope.currentTrip && $scope.mapLoaded) {
          try {
            map.addSource('trip', {
              'type': 'geojson',
              'data': $scope.currentTrip.geoJSON
            })
            map.addLayer({
              'id': 'trip',
              'type': 'line',
              'source': 'trip',
              'interactive': true,
              'layout': {
                'line-join': 'round',
                'line-cap': 'round'
              },
              'paint': {
                'line-color': '#fab',
                'line-width': 4
              }
            })

            map.addLayer({
              'id': 'points',
              'type': 'symbol',
              'source': {
                'type': 'geojson',
                'data': $scope.currentTrip.startEnd
              },
              'layout': {
                'icon-image': '{icon}',
                'icon-size': 1,
                'icon-offset': [0, -12]
              }
            })

            if($scope.currentTrip.bike_id) {
              mapFactory.mapFitBounds(map, $scope.currentTrip.geoJSON, 'low')
            }
            $scope.$on('fleetChange', function (event, id) {
              // getGeofenceData($scope.fleetData, id);
            })
          } catch (e) {
            console.error(e);
          }
        }
      })

      map.on('load', function () {
        $scope.mapLoaded = true;
      })

      $scope.goBack = function () {
        $state.go('member-profile', {userId: $scope.currentTrip.user_id})
      }
      var scrollDisable = document.getElementById('map')
      scrollDisable.addEventListener('wheel', function (e) {
        e.stopPropagation()
      }, false)

      $scope.issueRefund = function issueRefund() {
        ngDialog.openConfirm({
          showClose: false,
          template: '../../html/modals/issue-refund.html',
          controller: 'issueRefundController',
          data: { currentTrip: $scope.currentTrip, currency: $scope.fleetCurrency }
        }).then(function (refund) {
          notify({
            message: 'Refund applied successfully',
            duration: 2000,
            position: 'right'
          })

          if ($scope.currentTrip.refunds) {
            $scope.currentTrip.total_refunded += refund.payload.amount_refunded
            $scope.currentTrip.refunds.push(refund.payload)
          } else {
            $scope.currentTrip.total_refunded = refund.payload.amount_refunded
            $scope.currentTrip.refunds = [refund.payload]
          }
        }).catch(function(error) {
          if (error) {
            console.error(error);
            const message = (
              error && error.data && error.data.error && error.data.error.message ||
              'Failed to refund trip'
            )

            notify({
              message: message,
              duration: 2000,
              position: 'right'
            })
          }
        })
      }

      $scope.totalRefunded = function totalRefunded(trip) {
        return ((trip || {}).total_refunded || 0).toFixed(2)
      }
    })
