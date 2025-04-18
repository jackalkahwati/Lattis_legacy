'use strict'

angular.module('skyfleet.controllers').controller('issueRefundController', [
  'lattisConstants',
  '$scope',
  'tripAnimationFactory',
  function(lattisConstants, $scope, tripAnimationFactory) {
    function cancelRefund() {
      $scope.closeThisDialog()
    }

    function issueRefund() {
      $scope.error = false;
      $scope.refunding = true;

      tripAnimationFactory.refundTrip({
        trip_id: $scope.currentTrip.trip_id,
        amount: $scope.refundEntireAmount ? undefined : $scope.refundAmount
      })
      .then(res => {
        $scope.refunding = false;
        $scope.confirm(res)
      })
      .catch(function(error) {
        console.error('error', error);
        $scope.refunding = false;
        $scope.error = error && error.data && error.data.error && error.data.error.message;
      })
    }

    function dismissError() {
      $scope.error = false;
    }

    $scope.currencyMap = lattisConstants.currencyCodeSymbolMap
    $scope.currentTrip = $scope.ngDialogData.currentTrip
    $scope.currency = $scope.ngDialogData.currency
    $scope.maximumRefundable = (
      $scope.currentTrip.total - ($scope.currentTrip.total_refunded || 0)
    )

    $scope.cancelRefund = cancelRefund
    $scope.issueRefund = issueRefund
    $scope.dismissError = dismissError
  }
])
