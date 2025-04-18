'use strict'

angular.module('skyfleet.controllers')
  .controller('reservationsController',
    function (rootScopeFactory, $scope, bikeFleetFactory, $state, _, tripAnimationFactory, multipleKeyFilter, utilsFactory, $q, lattisConstants, $rootScope, sessionFactory, notify, $timeout, myFleetFactory, usersFactory) {
      $scope.activitytype = []
      $scope.dropdownlist = {}
      $scope.statusFilter = {}
      $scope.modifidiedWidth = false
      let usersArray = []
      $scope.showlistitem = 4


      $scope.bike_id = $state.params.bikeId

      if ($state.params.bikeId){
        myFleetFactory.getReservations($state.params.bikeId, function (response) {
          if (response && response.data.payload) {
            $scope.reservationsData = response.data.payload
            $scope.reservationsData.duration = moment.duration($scope.reservationsData.reservation_end ,  $scope.reservationsData.reservation_start)
          }
        })
      }
      usersFactory.getBikesActivity($state.params.bikeId).then(function (response) {
        if (response && response.payload) {
          $scope.activityLog = response.payload[0]

          _.each($scope.activityLog, function (element, index) {
            element.time_created = utilsFactory.getDateTime(element.timestamp)
            $scope.activitytype.push({
              property: 'activityType',
              name: element.headline,
              id: element.headline
            })
            $scope.dropdownlist = _.uniq($scope.activitytype, function (item, key) {
              if (item.name === 'BIKE IS UNDER MAINTENANCE') { $scope.modifidiedWidth = true }
              return item.name
            })
            if (element.user_id) {
              if (_.indexOf(usersArray, element.user_id) === -1) {
                usersArray.push(element.user_id)
              }
            }
          })
          if (usersArray.length) {
            usersFactory.getMember({user_id: usersArray}).then(function (res) {
              if (res && res.payload) {
                $scope.allRiders = res.payload
                _.each($scope.activityLog, function (element) {
                  if (element.user_id) {
                    let user = _.where($scope.allRiders, {user_id: parseInt(element.user_id)})
                    if (user.length) {
                      element.user_full_name = user[0].first_name + ' ' + user[0].last_name
                    }
                  }
                })
              }
            })
          }
          $scope.filterdData = $scope.activityLog
        }
      })
      jQuery('.datepicker').datetimepicker({
        format: 'MM/DD/YY',
        allowInputToggle: true,
        showClear: true,
        widgetPositioning: {
          vertical: 'bottom'
        }
      })

      $scope.$on('filterClear', function() {
        $scope.finalFilter = {}
        $scope.statusFilter = {}
        $scope.filterdData = $scope.activityLog
        jQuery('.datepicker').data("DateTimePicker").clear()
      })

      $scope.activityLogUpdate = function () {
        _.each(angular.element(document.getElementsByClassName('dropdown-container show')), function (element) {
          if (element.id === 'drop') {
            element.className = 'dropdown-container'
          }
        })
        $scope.filterdData = $scope.activityLog
        $scope.finalFilter = {}
        var filterTemp = {}
        var statusArr = []
        _.each($scope.statusFilter, function (value, key) {
          if (value) {
            if (_.indexOf(_.pluck($scope.dropdownlist, 'name'), key) !== -1) {
              statusArr.push($scope.dropdownlist[_.indexOf(_.pluck($scope.dropdownlist, 'name'), key)]['id'])
              filterTemp['headline'] = statusArr
            }
          }
        })
        if (_.isEmpty(filterTemp)) {
          $scope.filterdData = $scope.activityLog
        } else {
          $scope.filterdData = multipleKeyFilter($scope.activityLog, filterTemp)
        }
        $scope.startDate = jQuery('#datetimepicker2').val()
        if ($scope.startDate) {
          $scope.filterdData = _.filter($scope.filterdData, function (num) {
            if ($scope.startDate === num.time_created) {
              return num
            }
          })
        }
        $rootScope.$broadcast('checkboxClear')
        $rootScope.$emit('checkboxClear')
      }
    })
