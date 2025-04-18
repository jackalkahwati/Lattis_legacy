"use strict";

angular.module("skyfleet.controllers").controller("hubsStagingController", [
  "$scope",
  "hubsFactory",
  function ($scope, hubsFactory) {
    $scope.hubs = [];
    $scope.loading = false;

    function listHubs() {
      $scope.loading = true;

      return hubsFactory
        .list({ status: "staging" })
        .then((hubs) => {
          $scope.loading = false;
          $scope.hubs = hubs;
        })
        .catch((e) => {
          $scope.loading = false;
          console.error("Error listing hubs: ", e);
        });
    }

    $scope.listHubs = listHubs;

    function init() {
      listHubs();
    }

    init();
  },
]);
