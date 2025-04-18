
angular.module('skyfleet.controllers').controller(
  'enableReservationsModalController',
  function ($scope) {
    $scope.changesSave = function () {
      $scope.confirm(true)
    }

    $scope.changesCancel = function () {
      $scope.closeThisDialog()
    }
  })
