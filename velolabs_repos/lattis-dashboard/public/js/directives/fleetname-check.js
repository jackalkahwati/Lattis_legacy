'use strict'

angular.module('skyfleet')
  .directive('fleetnames', function ($q, addFleetFactory) {
    return {
      require: 'ngModel',
      link: function (scope, elm, attrs, ctrl) {
        ctrl.$asyncValidators.fleetnames = function (modelValue, viewValue) {
          var def = $q.defer()

          addFleetFactory.checkFleetName(modelValue, function (res) {
            (res.payload && res.payload.length > 0 && modelValue.toLowerCase() === res.payload[0].fleet_name.toLowerCase())
              ? def.reject() : def.resolve()
          })

          return def.promise
        }
      }
    }
  })
  .directive('customernames', function ($q, addFleetFactory) {
    return {
      require: 'ngModel',
      link: function (scope, elm, attrs, ctrl) {
        ctrl.$asyncValidators.customernames = function (modelValue, viewValue) {
          var def = $q.defer()

          addFleetFactory.checkCustomerName(modelValue, function (res) {
            (res.payload && res.payload.length > 0 && modelValue.toLowerCase() === res.payload[0].customer_name.toLowerCase())
              ? def.reject() : def.resolve()
          })

          return def.promise
        }
      }
    }
  })
