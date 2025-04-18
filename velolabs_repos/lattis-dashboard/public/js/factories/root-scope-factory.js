'use strict'

angular.module('skyfleet').factory(
  'rootScopeFactory', [function () {
    var root = {}
    return {
      setData: function (name, data) {
        root[name] = data
      },
      getData: function (name) {
        if (root[name]) {
          return root[name]
        }
      },
      clearAll: function () {
        root = {}
      }
    }
  }
  ]
)
