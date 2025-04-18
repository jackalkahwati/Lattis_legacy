"use strict";

angular
  .module("skyfleet.controllers")
  .controller(
    "membershipSubscriptionsController",
    function (
      $scope,
      $state,
      lattisConstants,
      rootScopeFactory,
      membershipFactory
    ) {
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
        listSubscriptions(fleetId);
      }

      function pageItems() {
        let startIndex =
          $scope.pagination.currentPage === 1
            ? 0
            : ($scope.pagination.currentPage - 1) *
              $scope.pagination.itemsPerPage.label;

        if ($scope.subscriptions.length < startIndex) {
          startIndex = 0;
        }

        $scope.pagedItems = $scope.subscriptions.slice(
          startIndex,
          startIndex + $scope.pagination.itemsPerPage.label
        );
      }

      function listSubscriptions(fleet_id) {
        const fleetId = fleet_id || rootScopeFactory.getData("fleetId");
        const userId = $state.params.userId;

        return membershipFactory
          .subscriptions({ fleet_id: fleetId, user_id: userId })
          .then((response) => {
            $scope.subscriptions = response.payload;

            pageItems();
          })
          .catch((e) => {
            console.error("Error listing subscriptions", error);
          });
      }

      function format(t) {
        return moment(t).format("YYYY-MM-DD");
      }

      $scope.formatPeriod = (sub) => {
        return `${format(sub.period_start)} / ${format(sub.period_end)}`;
      };

      $scope.formatIncentive = (sub) => {
        const {
          membership_incentive: incentive,
          membership_price_currency: currency,
          membership_price: price,
          payment_frequency: frequency,
        } = sub.fleet_membership;

        return `${Number(incentive)}% off for ${
          lattisConstants.currencyCodeSymbolMap[currency]
        }${price} per ${frequency.slice(0, -2)}`;
      };

      $scope.computeSubscriptionStatus = (sub) => {
        return moment().isBefore(moment(sub.period_end))
          ? "Active"
          : "Inactive";
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
