'use strict'

angular.module('skyfleet.controllers')
  .controller('memberListController',
    function ($rootScope, $scope, $state, _, $filter, utilsFactory, multipleKeyFilter, rootScopeFactory, usersFactory, bikeFleetFactory, $timeout, addFleetFactory) {
      $scope.riderIdList = []
      $scope.ridersFilterData = {}
      $scope.riderheader = ['User name', 'Email address', 'Date joined']
      $scope.showDate = false
      $scope.startDate = null
      $scope.date1 = ''
      $scope.showlistitem = 4
      $scope.fleetType = ''
      jQuery('#datetimepicker1').datetimepicker({
        format: 'MM/DD/YYYY',
        allowInputToggle: true,
        widgetPositioning: {
          vertical: 'bottom'
        }
      })
      jQuery('#datetimepicker1').on('dp.change', function (e) {
        $('#datetimepicker2').data('DateTimePicker').minDate(e.date)
      })
      jQuery('#datetimepicker2').datetimepicker({
        format: 'MM/DD/YYYY',
        allowInputToggle: true,
        widgetPositioning: {
          vertical: 'bottom'
        },
        useCurrent: false
      })
      jQuery('#datetimepicker2').on('dp.change', function (e) {
        $('#datetimepicker1').data('DateTimePicker').maxDate(e.date)
      })

      $scope.fetchFleetData = async () => {
        // sometimes an operator is changing fleet and the data stored is stale
        try {
          const currentFleet = JSON.parse(localStorage.getItem('currentFleet'))
          const {data: fleetData} = await bikeFleetFactory.fetchFleetData(currentFleet.fleet_id)
          $scope.fleetType = fleetData.payload.type
        } catch (error) {
          console.error('failed to fetch fleet data')
        }
      }

      function fetchUserData (fleetId) {
        usersFactory.getMemberList(fleetId).then(function (response) {
          $scope.ridersData = []
          $scope.userData = angular.copy(response.payload)
          if (!$scope.userData.length) {
            $state.go('add-member')
          }
          _.each($scope.userData, function (element) {
            if (element.first_name === null) element.first_name = ''
            if (element.last_name === null) element.last_name = ''
            element['name'] = element.first_name + ' ' + element.last_name
            element.date_created = utilsFactory.dateStringToDate(element.date_created)
            $scope.ridersData.push(_.pick(element, 'name', 'email', 'date_created', 'user_id'))
          })
          $scope.ridersFilterData = $scope.ridersData

          $scope.oldUsers = []
          usersFactory.getAvailableUsers(fleetId, function(response){
            $scope.availableUsers = response.payload
            if ($scope.availableUsers) {
              $scope.availableUsers.forEach(function (user) {
                $scope.oldUsers.push(user)
              })
            }
            $scope.newUsers = []
            const first_user = $scope.ridersData[0]
            $scope.ridersData.forEach(function (user) {
              $scope.customer = _.findWhere($scope.userData, {user_id: user.user_id});
              if ($scope.customer.access === undefined && $scope.fleetType === 'private') {
                $scope.customer.access = 0
                $scope.newUsers.push($scope.customer)
              }
            })

            $scope.usersToAdd = resultFilter($scope.newUsers, $scope.oldUsers)

            function resultFilter (newUsers, oldUsers) {
              return newUsers.filter(newUserItem =>
                !oldUsers.some(
                  oldUserItem => newUserItem.user_id === oldUserItem.user_id
                )
              )
            }
            if ($scope.usersToAdd.length > 0) {
              usersFactory.toggleAccess({
                fleet_id: rootScopeFactory.getData('fleetId'),
                user_id: first_user.user_id,
                access: 0,
                new_record: true,
                usersToAdd: $scope.usersToAdd
              }, function (response) {
                if (response && response.status === 200) {
                  usersFactory.clearMemberCSVCache()
                }
              })
            }
          })
          filterInit($scope.ridersData)
        })
      }

      function fetchFleetData (fleetId) {
        //get fleet data, get type from fleet, use it to negate manage members
       addFleetFactory.getFleetWithId({fleet_id: fleetId}, (response) => {
         $scope.fleetType = response.payload.type
       })
      }

      $scope.$on('fleetChange', function (event, id) {
        fetchFleetData(id)
        fetchUserData(id)
      })

      $rootScope.$on('fleetTypeUpdated', function (event, data) {
        $scope.fleetType = data.value
      })

      if (rootScopeFactory.getData('fleetId')) {
        fetchFleetData(rootScopeFactory.getData('fleetId'))
        fetchUserData(rootScopeFactory.getData('fleetId'))
      }

      $scope.showDate = false
      $scope.showDatePicker = function () {
        $scope.showDate = !$scope.showDate
      }
      $scope.ridersFilterData = $scope.ridersData

      $scope.memberlistupdate = function () {
        $scope.startDate = jQuery('#datetimepicker1').val()
        $scope.endDate = jQuery('#datetimepicker2').val()

        if ($scope.startDate) {
          $scope.ridersFilterData = _.filter($scope.ridersData, function (num) {
            return moment(num.date_created).isBetween($scope.startDate, $scope.endDate, null, '[]')
          })
        }
      }

      $rootScope.$on('filterClear', function () {
        jQuery('#datetimepicker1').val("")
        jQuery('#datetimepicker2').val("")
        jQuery('#tags').val("")
        fetchUserData(rootScopeFactory.getData('fleetId'))
      })

      function filterInit (data) {
        $scope.riderIdList.splice(0, $scope.riderIdList.length)
        _.each(data, function (element) {
          $scope.riderIdList.push({
            'value': _.pick(element, 'name')['name']
          })
        })
      }

      /* Button click function to filter the the bike table with the bike id in the search box */
      $scope.riderFilter = function () {
        // $scope.ridersFilterData = $filter('filter')($scope.ridersData, {name: $scope.riderSearch});
        const result = _.filter($scope.ridersData, function (value) {
          const searchField = $('#tags').val()
          if (value.name.toLowerCase().includes(searchField.toLowerCase())) {
            return value
          }
          if (!searchField) {
            return $scope.ridersData
          } else {

          }
        })
        $scope.ridersFilterData = result
      }

      $scope.filterSubscribers = function() {
        if ($scope.subscribersOnly) {
          $scope.ridersFilterData = $scope.userData.filter(
            ({ membership_subscriptions: subs }) => subs && subs.length
          )
        } else {
          $scope.ridersFilterData = $scope.userData
        }
      }
    })
