'use strict'

angular.module('skyfleet.controllers').controller(
  'passwordResetController',
  function ($scope, notify, $timeout, authFactory, $state, sessionFactory, $rootScope) {
    $scope.validatepassword = function (data) {
      data.$setDirty()
    }
    $scope.showLoader = true

    authFactory.validateToken({token: $state.params.hash}, function (response) {
      if (response && response.status === 200) {
        $scope.showLoader = false
      } else {
        $scope.showLoader = false
        $state.go('login')
      }
    })

    $scope.changePassword = function () {
      $scope.showLoader = true
      if ($scope.form.reenterpassword.$valid && $scope.form.newpassword.$valid) {
        authFactory.resetPassword({
          password: $scope.newpassword,
          token: $state.params.hash
        }, function (response) {
          if (response && response.status === 200) {
            notify({message: 'Your password has been changed successfully', duration: 2000, position: 'right'})
            if ($state.params.usrType === 'new-user') {
              authFactory.login({email: response.payload.email, password: $scope.newpassword}, function (error, response) {
                if (error) {
                  $scope.showLoader = false
                  notify({message: 'Something went wrong! Please try to login again', duration: 2000, position: 'right'})
                  $state.go('login')
                } else {
                  sessionFactory.setCookieData({
                    name: response.operator_info.operator_name,
                    operator_id: response.operator_info.operator_id
                  })
                  localStorage.setItem('acl', JSON.stringify(response.fleet_info))
                  if (response.operator_info.phone_number) {
                    $state.go('lattis-home')
                  } else {
                    $state.go('accountSettings', {isFirst: true})
                  }
                  $scope.showLoader = false
                }
              })
            } else {
              $scope.showLoader = true
              $state.go('login')
            }
          }
        })
      }
    }
    $rootScope.showLoader = false
  })
