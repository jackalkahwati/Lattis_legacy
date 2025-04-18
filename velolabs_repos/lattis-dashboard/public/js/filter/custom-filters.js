'use strict'

angular.module('skyfleet.controllers')
  .filter('object', ['_', function (_) {
    return function (data, filterKey) {
      var filteredData = []
      _.each(data, function (element) {
        _.each(filterKey, function (value, key) {
          if (element[key] == value) {
            filteredData.push(element)
          }
        })
      })
      return filteredData
    }
  }])
  .filter('multipleOptions', ['_', function (_) {
    return function (data, filterKey) {
      var filteredData = []
      _.each(data, function (element) {
        _.each(filterKey, function (value, key) {
          if (value.includes(element[key])) {
            filteredData.push(element)
          }
        })
      })
      return filteredData
    }
  }])
  .filter('multipleKey', ['_', function (_) {
    return function (data, filterKey) {
      var filteredData = []
      _.each(data, function (element) {
        var tempArray = []
        _.each(filterKey, function (value, key, list) {
          if (Array.isArray(value)) {
            if (value.includes(element[key])) {
              tempArray.push(element)
              if (tempArray.length == _.keys(list).length) {
                filteredData.push(element)
              }
            }
          } else {
            if (element[key] == value) {
              tempArray.push(element)
              if (tempArray.length == _.keys(list).length) {
                filteredData.push(element)
              }
            }
          }
        })
      })
      return filteredData
    }
  }])
