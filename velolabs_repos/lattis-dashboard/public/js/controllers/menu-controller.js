'use strict'

angular.module('skyfleet.controllers').controller(
  'menuController',
  function ($scope, rootScopeFactory, authFactory, $state, myFleetFactory, $rootScope, _, usersFactory, $cookies) {
    if (authFactory.isAuthenticated()) {
      $rootScope.showAfterLoad = true
      fetchFleets()
      }
    JSON.parse(sessionStorage.getItem('super_user')) ? $scope.superUser = true : $scope.superUser = false
    $('body').bind('mousewheel', function () {
      $('body, #menubar-scroll').niceScroll({ autohidemode: true, zindex: 999, smoothscroll: true, mousescrollstep: 40 }).resize()
    })
    function fetchFleets () {
      $rootScope.showLoader = true
      myFleetFactory.getFleetData().then(function (response) {
        $scope.responseCopy = angular.copy(response)
        $scope.bikeDataCopy = {}
        $scope.fleetNameModel = {}
        $scope.bikeDataCopy.fleet_data = []
          _.each(response.fleet_data, function (element) {
          element['fleetFullName'] = _.where(response.customer_data, {customer_id: element.customer_id})[0].customer_name + ' - ' + element.fleet_name
          if (!element.customer_name) {
            element['customer_name'] = _.where(response.customer_data, {customer_id: element.customer_id})[0].customer_name
          }
          $scope.bikeDataCopy.fleet_data.push(element)
        })

        $scope.fleetList = $scope.bikeDataCopy.fleet_data.length ? $scope.bikeDataCopy.fleet_data : 'null'

          $scope.fleetGroup = _.groupBy($scope.fleetList, function (x) {
          return x.customer_name
        })
        $scope.groupKeys = Object.keys($scope.fleetGroup)

        if (!localStorage.getItem('currentFleet')) {
          if ($scope.bikeDataCopy.fleet_data.length) {
            localStorage.setItem('currentFleet', JSON.stringify($scope.bikeDataCopy.fleet_data[0]))
            $scope.fleetNameModel = $scope.bikeDataCopy.fleet_data[0]
          } else {
            $scope.fleetNameModel.fleetFullName = 'Fleet'
            rootScopeFactory.setData('customerId', response.customer_data[0].customer_id)
            $rootScope.$broadcast('customerName', response.customer_data[0].customer_name)
            $rootScope.$emit('customerName', response.customer_data[0].customer_name)
          }
        } else {
          $scope.fleetNameModel = JSON.parse(localStorage.getItem('currentFleet'))
        }

        if ($scope.fleetNameModel.fleet_id) {
          moment.tz.setDefault($scope.fleetNameModel.fleet_timezone);
          $scope.selectedFleet = $scope.fleetNameModel.fleetFullName
          $rootScope.$broadcast('customerName', $scope.fleetNameModel.customer_name)
          $rootScope.$emit('customerName', $scope.fleetNameModel.customer_name)
          $rootScope.customer_Name = $scope.fleetNameModel.customer_name
          rootScopeFactory.setData('fleetId', $scope.fleetNameModel.fleet_id)
          rootScopeFactory.setData('fleetType', $scope.fleetNameModel.type)
          rootScopeFactory.setData('fleetCurrency', $scope.fleetNameModel.currency)
          rootScopeFactory.setData('customerId', _.where(response.customer_data, {customer_id: $scope.fleetNameModel.customer_id})[0].customer_id)
          $rootScope.$broadcast('fleetChange', $scope.fleetNameModel.fleet_id)
          $rootScope.$emit('fleetChange', $scope.fleetNameModel.fleet_id)
          $cookies.put('stripe_fleet_id', $scope.fleetNameModel.fleet_id)
          setACL(_.where(JSON.parse(localStorage.getItem('acl')), {fleet_id: $scope.fleetNameModel.fleet_id})[0])
          usersFactory.getMemberList($scope.fleetNameModel.fleet_id).then(function (response) {
            response.payload.length ? $scope.url = 'member-list' : $scope.url = 'add-member'
          })
          $scope.paymentActive = ($scope.fleetNameModel.type === 'private' || $scope.fleetNameModel.type === 'public')
        }
        $rootScope.showLoader = false
      }).catch(function () {
        $rootScope.showLoader = false
      }
    )
    }

    $scope.$on('fleetAdded', function () {
      fetchFleets()
    })

    /* TODO Implement check fleetsize to dynamically provide the state to the menu */

    /*            $scope.checkFleetSize = function (data) {
         if (_.where(data.fleet_data, {"fleet_id": rootScopeFactory.getData('fleetId')})[0].fleet_size > 0) {
         $scope.bikeFleetState = 'myfleetdashboard.bikefleet-listview';
         } else {
         $scope.bikeFleetState = 'myfleetdashboard.bikefleet';
         }
         }; */

      function setACL(userRole) {
        if (userRole) {
          $rootScope.adminACL = false;
          $rootScope.coordinatorACL = false;
          $rootScope.technicianACL = false;
          $rootScope.normalAdminACL = false;

          if (userRole.acl === "coordinator") {
            $rootScope.coordinatorACL = true;
          } else if (userRole.acl === "maintenance") {
            $rootScope.technicianACL = true;
          } else if (userRole.acl === "admin") {
            $rootScope.adminACL = true;
          } else if (userRole.acl === "normal_admin") {
            $rootScope.normalAdminACL = true;
          }

          rootScopeFactory.setData("adminACL", $rootScope.adminACL);
          rootScopeFactory.setData("normalAdminACL", $rootScope.adminACL);
          rootScopeFactory.setData("coordinatorACL", $rootScope.coordinatorACL);
          rootScopeFactory.setData("technicianACL", $rootScope.technicianACL);
        }
      }

    $scope.getLinkUrl = function () {
      if ($scope.url) {
        $state.go($scope.url)
      }
    }

    $scope.sideNavClick = function (item, group) {


      $state.go('activity-feed')
      $scope.fleetNameModel = item
      moment.tz.setDefault($scope.fleetNameModel.fleet_timezone);
      $rootScope.$broadcast('customerName', $scope.fleetNameModel.customer_name)
      $rootScope.$emit('customerName', $scope.fleetNameModel.customer_name)
      $rootScope.customer_Name = $scope.fleetNameModel.customer_name
      rootScopeFactory.setData('fleetId', item.fleet_id)
      rootScopeFactory.setData('fleetType', item.type)
      rootScopeFactory.setData('fleetCurrency', item.currency)
      rootScopeFactory.setData('customerId', _.where($scope.responseCopy.customer_data, {customer_id: item.customer_id})[0].customer_id)
      localStorage.setItem('currentFleet', JSON.stringify(item))
      $rootScope.$broadcast('fleetChange', item.fleet_id)
      $rootScope.$emit('fleetChange', item.fleet_id)
      $cookies.put('stripe_fleet_id', item.fleet_id)
      setACL(_.where(JSON.parse(localStorage.getItem('acl')), {fleet_id: item.fleet_id})[0])
      $scope.paymentActive = (item.type === 'private' || item.type === 'public')
      usersFactory.getMemberList(item.fleet_id).then(function (response) {
        response.payload.length ? $scope.url = 'member-list' : $scope.url = 'add-member'
      })
      $scope.selectedFleet = item.fleetFullName
      if (group) {
        let id = '#fleet' + _.indexOf($scope.groupKeys, group)
        $(id).collapse('hide')
      } else {
        $('#facebookuser').collapse('hide')
      }
    }
    $rootScope.showLoader = false
  }
)
