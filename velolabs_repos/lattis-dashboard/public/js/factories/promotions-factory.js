"use strict";

angular.module("skyfleet").factory("promotionsFactory", function ($http) {
  return {
    create: function (data) {
      return $http
        .post("/api/promotions", JSON.stringify(data))
        .then((response) => response.data);
    },
    deactivate: function (promotion_id) {
      return $http
        .patch("/api/promotions/" + promotion_id + "/deactivate")
        .then((response) => response.data);
    },
    list: function (fleet_id) {
      return $http
        .get("/api/promotions", { params: { fleet_id } })
        .then((response) => response.data);
    },
  };
});
