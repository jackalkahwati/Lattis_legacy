'use strict'

angular.module('skyfleet')
  .directive('bikeTable', ['_', '$state', '$filter', '$compile', '$timeout', '$rootScope', 'rootScopeFactory', 'ngDialog', function (_, $state, $filter,  $compile, $timeout, $rootScope, rootScopeFactory, ngDialog) {
    return {
      restrict: 'AEC',
      scope: {
        reservations: '=',
        bikefleetdata: '=',
        originaldata: '=',
        type: '@',
        bikeoption: '=',
        filter: '=',
        header: '=',
        fleetstatus: '@',
        selectedlist: '=',
        numrowitem: '=',
        distancePreference: '=',
      },
      templateUrl: '../html/templates/bikefleet-table.html',
      link: function (scope) {
        scope.checkbox = {}
        scope.pagination = {}
        scope.numberOfData = ''
        scope.pagedItems = []
        scope.maxSize = 5
        scope.tblHeader = scope.header
        scope.rowitem = {}
        scope.tempSelect = 0

        if (scope.type == 'members' || scope.type == 'trip-history') {
          scope.hideCheckbox = true
        }
        scope.$on('bikeFleetMenuClear', function () {
          _.each(scope.checkbox, function (element, id) {
            scope.checkbox[id] = false
          })
          scope.bikeoption = true
          scope.filter = false
        })

        scope.visitProfile = function (id) {
          rootScopeFactory.setData('fromBikeList', true)
          $state.go('member-profile', {userId: id})
        }

        scope.pages = [
          {lable: 10, value: 'option1'},
          {lable: 15, value: 'option2'},
          {lable: 20, value: 'option3'},
          {lable: 50, value: 'option4'}]

        if (sessionStorage.getItem('getrowitem')) {
          sessionStorage.setItem('getrowitem', sessionStorage.getItem('getrowitem'))
        } else if (sessionStorage.getItem('getrowitem') == null) {
          sessionStorage.setItem('getrowitem', JSON.stringify(scope.pages[0]))
        }
        scope.rowitem = JSON.parse(sessionStorage.getItem('getrowitem'))
        scope.itemsPerPage = $state.params.bikeData ? $state.params.bikeData.rows : scope.rowitem

        scope.pagechange = function (pageno) {
          scope.pagination.currentPage = pageno
          scope.tempSelect = pageno
        }

        scope.removeFilters = function removeFilters () {
          scope.showError = false;
          $rootScope.$broadcast('filterClear')
        }

        scope.$watch('bikefleetdata', function () {
          if (scope.bikefleetdata && scope.bikefleetdata.length) {
            scope.fleet = JSON.parse(localStorage.getItem('currentFleet'))
            scope.nonDuplicatedData = _.uniq(scope.bikefleetdata, 'timestamp')
            scope.nonDuplicatedData.forEach(function (data) {
              data.time_of_log = moment.unix(data.timestamp).tz(scope.fleet.fleet_timezone).format('h:mm:ss A')
            })
            scope.numberOfData = scope.bikefleetdata.length
            scope.pagination.currentPage = $state.params.bikeData ? $state.params.bikeData.page : 1
            if (scope.tempSelect) {
              scope.pagination.currentPage = scope.tempSelect
            }
            scope.showError = false
          } else if (scope.bikefleetdata !== undefined && !scope.bikefleetdata.length) {
            scope.showError = true
          }
        })

        scope.$watch('reservations', function () {
          if (scope.reservations && scope.reservations.length) {
            scope.fleet = JSON.parse(localStorage.getItem('currentFleet'))
            scope.reservationsData = _.uniq(scope.reservations, 'created_at')
            scope.activeReservations = _.filter(scope.reservationsData, function(all_reservations) {
              return !all_reservations.reservation_terminated
            })
            scope.activeReservations.forEach(function (data) {
              var start = moment(data.reservation_start)
              var end = moment(data.reservation_end)
              data.duration= moment.utc(end.diff(start)).format("HH:mm:ss")
              data.breakdown = data.duration.split(":")
            })
            scope.numberOfData = scope.reservations.length
            scope.pagination.currentPage = $state.params.bikeData ? $state.params.bikeData.page : 1
            if (scope.tempSelect) {
              scope.pagination.currentPage = scope.tempSelect
            }
            scope.showError = false
          } else if (scope.reservations !== undefined && !scope.reservations.length) {
            scope.showError = true
          }
        })

        scope.formatReservationTime = function(reservation, time) {
          return moment(time)
            .tz(reservation.reservation_timezone)
            .format('MM/DD/YYYY HH:mmA z')
        }

        scope.setItemsPerPage = function (num) {
          sessionStorage.setItem('getrowitem', JSON.stringify(num))
          scope.itemsPerPage = JSON.parse(sessionStorage.getItem('getrowitem'))
          scope.rowitem = JSON.parse(sessionStorage.getItem('getrowitem'))
          scope.currentPage = 1 // reset to first page
        }

        scope.showInfo = function() {
          ngDialog.open({
            template: '../html/modals/date-activity-modal.html',
          })
        }

        scope.liveBikes = []
        scope.checkBoxChange = function (data) {
          if (scope.checkbox[data.bike_id]) {
            scope.bikeoption = false
            scope.filter = true
            if (data.bike_id) {
              if (data.current_status === 'IN RIDE' || data.current_status === 'NO ELLIPSE') {
                if (!scope.liveBikes.length) {
                  scope.$broadcast('disableBikeOptions')
                  scope.$emit('disableBikeOptions')
                }
                scope.liveBikes.push(data.bike_id)
              }
              scope.selectedlist.push(data.bike_id)
              rootScopeFactory.setData('selectedBikes', scope.selectedlist)
              if (!scope.liveBikes.length && scope.selectedlist.length === 1) {
                scope.$broadcast('enableBikeOptions')
                scope.$emit('enableBikeOptions')
              } else {
                scope.$broadcast('disableService')
                scope.$emit('disableService')
              }
            }
          } else {
            scope.selectedlist.splice(_.indexOf(scope.selectedList, data.bike_id), 1)
            if (scope.liveBikes.length && _.indexOf(scope.liveBikes, data.bike_id) >= 0) {
              scope.liveBikes.splice(_.indexOf(scope.liveBikes, data.bike_id), 1)
              if (!scope.liveBikes.length) {
                scope.$broadcast('enableBikeOptions')
                scope.$emit('enableBikeOptions')
                if (scope.selectedlist.length > 1) {
                  scope.$broadcast('disableService')
                  scope.$emit('disableService')
                }
              }
            }
            rootScopeFactory.setData('selectedBikes', scope.selectedlist)
            if (!scope.selectedlist.length) {
              scope.bikeoption = true
              scope.filter = false
              scope.$broadcast('disableBikeOptions')
              scope.$emit('disableBikeOptions')
            } else if (scope.selectedlist.length === 1) {
              scope.$broadcast('enableService')
              scope.$emit('enableService')
            }
          }
        }

        scope.$on('clearBikeDataCache', function () {
          scope.liveBikes = []
          scope.selectedlist = []
        })

        scope.reverse = false
        scope.userNamesRevese = false
        scope.rideNameReverse = false
        $( document ).ready(function() {
          $("#categories-toggle").on('click', function(e){
            e.preventDefault();
            $(this).toggleClass("down");
            scope.reverse = ! scope.reverse
          });

          $("#user-names-toggle").on('click', function(e){
            e.preventDefault();
            $(this).toggleClass("down");
            scope.userNamesRevese = ! scope.userNamesRevese
          });

          $("#ride-names-toggle").on('click', function(e){
            e.preventDefault();
            $(this).toggleClass("down");
            scope.rideNameReverse = ! scope.rideNameReverse
          });

        });

        scope.reverseOrder = function() {
          scope.bikefleetdata = $filter('orderBy')(scope.bikefleetdata, 'user_id', scope.reverse)
        }

        scope.reverseAlphabeticalOrder = function(){
          scope.bikefleetdata = $filter('orderBy')(scope.bikefleetdata, 'name', scope.userNamesRevese)
        }

        scope.reverseRideNameOrder = function() {
          scope.bikefleetdata = $filter('orderBy')(scope.bikefleetdata, 'bike_name', scope.rideNameReverse)
        }
        // /!* @function (selectAll) to select all tablerows (checkbox)  *!/
        scope.tableClick = function (data) {
          if (scope.type === 'riders') {
            $state.go('myfleetdashboard.riders_profile', {rider_id: data.user_id})
          } else if (scope.type === 'members') {
            $state.go('member-profile', {
              userId: data.user_id,
              currentPage: {
                page: scope.pagination.currentPage,
                rows: scope.itemsPerPage,
                bikeStatus: scope.fleetstatus
              }
            })
          } else if (scope.type === 'trip-history') {
            $state.go('trip-details', {trip_id: data.trip_id})
          } else {
            $state.go('bike-details', {
              bikeId: data.bike_id,
              currentPage: {
                page: scope.pagination.currentPage,
                rows: scope.itemsPerPage,
                bikeStatus: scope.fleetstatus
              }
            })
          }
        }
        var clickCount = 0
        scope.checkboxBikes = []
        scope.$on('checkboxClear', function () {
          _.each(scope.bikefleetdata, function (element, id) {
            scope.checkbox[element.bike_id] = false
          })
          scope.$broadcast('disableBikeOptions')
          scope.$emit('disableBikeOptions')
          scope.allSelect = false
          clickCount = 0
        })
        scope.selectAllBikes = function () {
          // $scope.riderData = _.where($scope.memberList, {user_id: parseInt($state.params.userId)});
          if (!clickCount) {
            _.each(scope.bikefleetdata, function (element, id) {
              scope.checkbox[element.bike_id] = true
              scope.checkboxBikes.push(element.bike_id)
              rootScopeFactory.setData('selectedBikes', scope.checkboxBikes)
            })
            scope.rideStatus = _.where(scope.bikefleetdata, {current_status: 'IN RIDE'})
            if (scope.rideStatus.length || scope.bikefleetdata.length === 0) {
              scope.$broadcast('disableBikeOptions')
              scope.$emit('disableBikeOptions')
            } else {
              scope.$broadcast('enableBikeOptions')
              scope.$emit('enableBikeOptions')
            }
            clickCount = 1
            // rootScopeFactory.setData('selectedBikes', scope.checkboxBikes);
          } else {
            _.each(scope.bikefleetdata, function (element, id) {
              scope.checkbox[element.bike_id] = false
            })
            scope.$broadcast('disableBikeOptions')
            scope.$emit('disableBikeOptions')
            clickCount = 0
          }
        }
      }
    }
  }])
