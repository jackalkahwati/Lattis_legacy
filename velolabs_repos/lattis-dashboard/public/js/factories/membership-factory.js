'use strict'

angular.module('skyfleet').factory('membershipFactory', function ($http) {
  return {
    create: function(data) {
      return $http
        .post('/api/memberships', JSON.stringify(data))
        .then(response => response.data)
    },
    update: function(fleet_membership_id, data) {
      return $http
        .put('/api/memberships/' + fleet_membership_id, JSON.stringify(data))
        .then(response => response.data)
    },
    retrieve: function(fleet_membership_id) {
      return $http
        .get('/api/memberships/' + fleet_membership_id)
        .then(response => response.data)
    },
    deactivate: function(fleet_membership_id) {
      return $http
        .patch('/api/memberships/' + fleet_membership_id + '/deactivate')
        .then(response => response.data)
    },
    activate: function(fleet_membership_id) {
      return $http
        .patch('/api/memberships/' + fleet_membership_id + '/activate')
        .then(response => response.data)
    },
    list: function(fleet_id) {
      return $http
        .get(`/api/fleet/${fleet_id}/memberships`)
        .then(response => response.data)
    },
    subscriptions: function({ fleet_id, user_id }) {
      return $http
        .get(`/api/fleet/${fleet_id}/subscriptions/${user_id}`)
        .then(response => response.data)
    }
  }
})
