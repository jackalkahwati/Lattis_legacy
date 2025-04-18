'use strict'

angular.module('skyfleet.controllers')
  .controller('memberTriphistoryController',
    function ($scope, $rootScope, $state, _, $filter, utilsFactory, multipleKeyFilter, rootScopeFactory, usersFactory, tripAnimationFactory) {
      $scope.ridersData = []
      $scope.tripIdList = []
      $scope.triphistoryheader = ['Date', 'Trip Type']
      $scope.paginationData = {}

      $scope.getUserData = function (args) {
        const { fleetId, currentPage, itemsPerPage } = args
        $rootScope.showLoader = true
        tripAnimationFactory
          .getAllTrips({user_id: parseInt($state.params.userId), fleet_id: fleetId, currentPage: currentPage || 1, itemsPerPage: itemsPerPage || 50})
          .then(function (res) {
            $rootScope.showLoader = false
            $scope.paginationData = res.payload.pagination
            _.each(res.payload.trips, function (element) {
              element.date_created = utilsFactory.dateStringToDate(
                (!element.date_created || isNaN(element.date_created))
                  ? element.date_charged
                  : element.date_created
              )

              element.trip_type = element.reservation_id ? 'Reserved' : 'Regular'

              if (element.date_created === 'Invalid date' && element.reservation) {
                element.date_created = utilsFactory.dateStringToDate(moment.utc(element.reservation_start).unix())
              }

              $scope.ridersData.push(_.pick(element, 'date_created', 'trip_id', 'trip_type'))
            })

            $scope.ridersData = _.sortBy($scope.ridersData, function (data) { return -data.trip_id })
            $scope.triphistory = $scope.ridersData
            filterInit($scope.ridersData)
          }).catch(function (err) {
            $rootScope.showLoader = false
            console.log(err)
          }
        )
      }

      jQuery('#riderhistroydate').datetimepicker({
        format: 'MM/DD/YYYY',
        allowInputToggle: true,
        widgetPositioning: {
          vertical: 'bottom'
        }
      })

      $scope.ridersinfo = [{
        title: 'PROFILE',
        url: 'ridersProfile'
      }, {
        title: 'TRIP HISTORY',
        url: 'ridersTriphistory'
      }]
      $scope.currenttab = 'ridersProfile'

      $scope.riderheader = ['User Name', 'Email address', 'Member_ID', 'Date joined', 'Member type']

      if (rootScopeFactory.getData('fleetId')) {
        $scope.getUserData({ fleetId: rootScopeFactory.getData('fleetId'), currentPage: 1, itemsPerPage: 50 })
      }

      $scope.$on('fleetChange', function (event, id) {
        if (!$scope.userData) {
          $scope.getUserData({ fleetId: id, currentPage: 1, itemsPerPage: 50 })
        }
      })
      $scope.showlistitem = 4
      function filterInit (data) {
        $scope.tripIdList.splice(0, $scope.tripIdList.length)
        _.each(data, function (element) {
          $scope.tripIdList.push({'value': _.pick(element, 'trip_id')['trip_id']})
        })
      }

      $scope.tripHistroyFilter = function () {
        $scope.triphistory = $filter('filter')($scope.ridersData, {trip_id: $scope.tripSearch})
      }

      $scope.membertripupdate = function () {
        $scope.joindate = jQuery('#riderhistroydate input').val()
        if ($scope.joindate) {
          $scope.triphistory = _.filter($scope.ridersData, function (num) {
            return num.date_created === $scope.joindate
          })
        }
      }

      $scope.showDate = false

      $scope.showDatePicker = function () {
        $scope.showDate = !$scope.showDate
      }
    })
