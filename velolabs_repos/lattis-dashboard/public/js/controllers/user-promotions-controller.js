"use strict";

angular
  .module("skyfleet.controllers")
  .controller(
    "userPromotionsController",
    function ($scope, $state, rootScopeFactory, usersFactory) {
      $scope.pages = [
        { label: 5, value: "option1" },
        { label: 10, value: "option2" },
        { label: 15, value: "option3" },
        { label: 20, value: "option4" },
        { label: 50, value: "option5" },
      ];

      $scope.pagination = {
        currentPage: 1,
        itemsPerPage: $scope.pages[1],
      };

      $scope.pagedItems = [];

      function init(fleetId) {
        listPromotions(fleetId);
      }

      function pageItems() {
        let startIndex =
          $scope.pagination.currentPage === 1
            ? 0
            : ($scope.pagination.currentPage - 1) *
              $scope.pagination.itemsPerPage.label;

        if ($scope.promotions.length < startIndex) {
          startIndex = 0;
        }

        $scope.pagedItems = $scope.promotions.slice(
          startIndex,
          startIndex + $scope.pagination.itemsPerPage.label
        );
      }

      function listPromotions(fleet_id) {
        const fleetId = fleet_id || rootScopeFactory.getData("fleetId");
        const userId = $state.params.userId;

        return usersFactory
          .promotions({ fleet_id: fleetId, user_id: userId })
          .then((response) => {
            $scope.promotions = response.payload;

            pageItems();
          })
          .catch((e) => {
            console.error("Error listing promotions", e);
          });
      }

      $scope.formatIncentive = (promotion) => {
        const { amount: incentive } = promotion;

        return `${Number(incentive)}% off`;
      };

      $scope.computeUserPromotionStatus = (promotion) => {
        const { promotion_users: user } = promotion;
        if (!user.claimed_at) {
          return 'Unclaimed'
        }

        return `Claimed At ${moment(user.claimed_at).format('YY-MM-DD hh:mmA')}`
      };

      $scope.$on("fleetChange", function (event, fleetId) {
        init(fleetId);
      });

      $scope.pageChange = (pageNum) => {
        $scope.pagination.currentPage = pageNum;

        pageItems();
      };

      $scope.setItemsPerPage = function (num) {
        $scope.pagination.itemsPerPage = num;
        $scope.pagination.currentPage = 1;

        pageItems();
      };

      if (rootScopeFactory.getData("fleetId")) {
        init(rootScopeFactory.getData("fleetId"));
      }
    }
  );
