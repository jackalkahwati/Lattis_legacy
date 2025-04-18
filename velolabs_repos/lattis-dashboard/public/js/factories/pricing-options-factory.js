"use strict";

angular.module("skyfleet").factory("pricingOptionsFactory", function ($http) {
  return {
    create: function (fleetId, data) {
      return $http
        .post(`/api/fleet/${fleetId}/pricing-options`, data)
        .then((response) => response.data);
    },
    update: function ({ fleetId, pricingOptionId }, data) {
      return $http
        .put(`/api/fleet/${fleetId}/pricing-options/${pricingOptionId}`, data)
        .then((response) => response.data);
    },
    deactivate: function ({ fleetId, pricingOptionId }) {
      return $http
        .patch(
          `/api/fleet/${fleetId}/pricing-options/${pricingOptionId}/deactivate`
        )
        .then((response) => response.data);
    },
    activate: function ({ fleetId, pricingOptionId }) {
      return $http
        .patch(
          `/api/fleet/${fleetId}/pricing-options/${pricingOptionId}/activate`
        )
        .then((response) => response.data);
    },
    list: function (fleetId) {
      return $http
        .get(`/api/fleet/${fleetId}/pricing-options`)
        .then((response) => response.data);
    },
  };
});
