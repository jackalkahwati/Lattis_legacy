"use strict";

angular.module("skyfleet.controllers").controller("hubsLiveController", [
  "$scope",
  "hubsFactory",
  function ($scope, hubsFactory) {
    $scope.hubs = [];

    function listHubs() {
      $scope.loading = true;

      return hubsFactory
        .list({ status: "live" })
        .then((hubs) => {
          $scope.loading = false;
          $scope.hubs = hubs;
        })
        .catch((e) => {
          console.error("Error listing hubs: ", e);
          $scope.loading = false;
        });
    }

    $scope.listHubs = listHubs;

    function init() {
      listHubs();
    }

    init();
  },
]);
