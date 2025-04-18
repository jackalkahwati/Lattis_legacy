'use strict'

angular.module('skyfleet.controllers').controller(
  'addUserModalController',
  function ($scope, profileSettingFactory, sessionFactory, $q, $rootScope, rootScopeFactory, usersFactory, ngDialog, bikeFleetFactory, notify) {
    $scope.newuser = {}

    $scope.customerList = [{
      customer_name: 'Primary administrator', value: 'admin'
    },
    {customer_name: 'Administrator', value: 'normal_admin'},
    {customer_name: 'Fleet Coordinator', value: 'coordinator'},
    {customer_name: 'Fleet Technician', value: 'maintenance'}]

    $scope.$watch('newuser.email', checkOperator)

    $scope.newuser.access_staff_name = 'Select user'

    function checkOperator () {
      $scope.mailtrue = false
      if ($scope.newuser.email) {
        let operator = $scope.newuser.email.toString().match(/^[-a-zA-Z0-9][-._!#$%&*+/=?`{|~}a-zA-Z0-9]*@[-.a-zA-Z0-9]+(\.[-.a-zA-Z0-9]+)*\.(com|edu|info|gov|mobi|int|mil|net|org|biz|name|museum|coop|aero|pro|io|[a-zA-Z]{2})$/g)
        if (operator && operator.length) {
          usersFactory.checkOperator($scope.newuser.email, sessionFactory.getCookieId(), function (response) {
            if (response.payload.operator === null) {
              $scope.disableInputs = false
              $scope.newuser.first_name = ''
              $scope.newuser.last_name = ''
            } else {
              $scope.newuser.operator_id = angular.copy(response.payload.operator.operator_id)
              $scope.disableInputs = true
              $scope.mailtrue = true
            }
            if (response.payload) {
              $scope.fleetList = angular.copy(response.payload.fleets)
              if (response.payload.fleets) {
                $scope.fleetNameModel = angular.copy(response.payload.fleets[0])
                $scope.newuser.first_name = angular.copy(response.payload.operator.first_name)
                $scope.newuser.last_name = angular.copy(response.payload.operator.last_name)
              }
            }
          })
        }
      } else {
        $scope.disableInputs = true
      }
    }

    $scope.selectRole = function (role) {
      if (!role.disabled) {
        $scope.newuser.access_staff = role.value
        $scope.newuser.access_staff_name = role.customer_name
      }
    }

    $scope.adduser = function () {
      $scope.newuser.customer_id = rootScopeFactory.getData('customerId')
      profileSettingFactory.addOperator($scope.newuser, function (response) {
        if (response && response.status === 200) {
          notify({
            message: 'Users have been added to the fleet successfully',
            duration: 2000,
            position: 'right'
          })
          $scope.newuser = {}
          $scope.fleetList = []
          profileSettingFactory.clearOnCallCache()
          $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
          $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
          ngDialog.closeAll()
        } else if (response && response.status === 409) {
          notify({
            message: $scope.domain.id + 'is already an registered operator',
            duration: 2000,
            position: 'right'
          })
        } else {
          notify({message: 'Failed to create users', duration: 2000, position: 'right'})
        }
      })
    }

    $scope.cancelUser = function () {
      ngDialog.closeAll()
    }
  })
