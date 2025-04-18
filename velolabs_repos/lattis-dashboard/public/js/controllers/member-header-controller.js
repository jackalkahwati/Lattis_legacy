'use strict'

angular.module('skyfleet.controllers')
  .controller('memberHeaderController', function ($scope, usersFactory, rootScopeFactory, $state, _, $window) {
    $scope.ridersinfo = [
      {
        title: "PROFILE",
        url: "ridersProfile",
      },
      {
        title: "TRIP HISTORY",
        url: "ridersTriphistory",
      },
      {
        title: "SUBSCRIPTIONS",
        url: "membershipSubscriptions",
      },
      {
        title: "PROMOTIONS",
        url: 'userPromotions'
      }
    ]

    $scope.currenttab = 'ridersProfile'
    $scope.showDate = false
    $scope.showDatePicker = function () {
      $scope.showDate = !$scope.showDate
    }

    function getUserData (fleetId) {
      usersFactory.getMember({user_id: $state.params.userId}).then(function (response) {
        $scope.riderData = angular.copy(response.payload)
        $scope.customerdetails = $scope.riderData[0]
      })
    }

    if (rootScopeFactory.getData('fleetId')) {
      getUserData(rootScopeFactory.getData('fleetId'))
    }

    $scope.$on('fleetChange', function (event, id) {
      getUserData(id)
    })

    $scope.$on('$destroy', function () {
      rootScopeFactory.setData('fromBikeList', false)
    })

    $scope.gobackMemberList = function () {
      if (rootScopeFactory.getData('fromBikeList')) {
        $state.go('live-bikes')
      } else {
        $state.go('member-list')
      }
    }
  })
