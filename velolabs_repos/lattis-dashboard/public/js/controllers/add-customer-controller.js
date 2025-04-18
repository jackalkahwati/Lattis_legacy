'use strict'

angular.module('skyfleet.controllers').controller(
  'addCustomerController',
  function ($scope, sessionFactory, $rootScope, $state, $window, notify, rootScopeFactory, profileSettingFactory, regexPatterns) {
    $scope.validateFields = function (data) {
      data.$setDirty()
    }

    const [patterns] = regexPatterns
    $scope.emailRegexPatterns = patterns.email
    function toggleValidation (condition) {
      if (condition) {
        $scope.form.customername.$setDirty()
        $scope.form.firstname.$setDirty()
        $scope.form.lastname.$setDirty()
        $scope.form.phone.$setDirty()
        $scope.form.email.$setDirty()
      } else {
        $scope.form.customername.$setPristine()
        $scope.form.firstname.$setPristine()
        $scope.form.lastname.$setPristine()
        $scope.form.phone.$setPristine()
        $scope.form.email.$setPristine()
      }
    }
    $scope.newOperator = {}
    $scope.savecustomer = function () {
      toggleValidation(true)
      if ($scope.form.$dirty && !_.keys($scope.form.$error).length > 0) {
        profileSettingFactory.addCustomer({
          customer_name: $scope.newOperator.cname,
          first_name: $scope.newOperator.fname,
          last_name: $scope.newOperator.lname,
          email: $scope.newOperator.email,
          phone_number: $scope.newOperator.phoneNumber
        }, function (response) {
          if (response.status === 409) {
            notify({
              message: 'Email is already exists',
              duration: 2000,
              position: 'right'
            })
          } else if (response && response.status === 200) {
            notify({
              message: 'Customer added',
              duration: 2000,
              position: 'right'
            })
          } else {
            notify({
              message: 'Failed to customer adding',
              duration: 2000,
              position: 'right'
            })
          }
        })
      } else {
        notify({
          message: 'Fill all required fields',
          duration: 2000,
          position: 'right'
        })
      }
    }
  })
