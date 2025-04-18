'use strict'

angular.module('skyfleet.controllers').factory('sessionFactory', [
  '$cookies',
  'authFactory',
  function ($cookies, authFactory) {
    return {
      setCookieData: function (json) {
        $cookies.put('userName', json['name'])
        $cookies.put('adminId', json['operator_id'])
      },
      getCookieUser: function () {
        return $cookies.get('userName')
      },
      getCookieId: function () {
        const payload = authFactory.decodeTokenPayload()
        if (payload && payload.sub) {
          return payload.sub
        }

        return $cookies.get('adminId')
      },
      clearCookieData: function () {
        $cookies.remove('userName')
        $cookies.remove('adminId')
      }
    }
  }
])
