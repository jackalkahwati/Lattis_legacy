angular
  .module("skyfleet.controllers")
  .controller(
    "pricingOptionsController",
    function (
      $scope,
      lattisConstants,
      notify,
      rootScopeFactory,
      pricingOptionsFactory
    ) {
      $scope.error = null;
      $scope.pricingOptions = [];
      $scope.loading = false;
      $scope.loadingIndex = false;
      $scope.fleetCurrency = $scope.$parent.fleetCurrency;
      $scope.currencyMap = lattisConstants.currencyCodeSymbolMap;

      $scope.$watch("$parent.fleetCurrency", function () {
        $scope.fleetCurrency = $scope.$parent.fleetCurrency;
      });

      function dismissError() {
        $scope.error = null;
      }

      function getErrorMessage(error) {
        const errorData = error && error.data;
        if (!errorData) {
          return null;
        }

        const message = errorData.message || errorData.error.message;

        return Array.isArray(errorData.error)
          ? errorData.error[0].message
          : message;
      }

      function currentFleet() {
        const fleetId = rootScopeFactory.getData("fleetId");

        return fleetId
          ? { fleet_id: fleetId }
          : JSON.parse(localStorage.getItem("currentFleet"));
      }

      function list(fleetId) {
        $scope.loading = true;

        pricingOptionsFactory
          .list(fleetId)
          .then((response) => {
            $scope.pricingOptions = response.payload;
            $scope.loading = false;
          })
          .catch((error) => {
            $scope.error = getErrorMessage(error);
            $scope.loading = false;
          });
      }

      function updateScopePricingOptions(pricingOption, newPricingOption) {
        return $scope.pricingOptions.map(function (pricing) {
          if (pricing.pricing_option_id === pricingOption.pricing_option_id) {
            return newPricingOption;
          }

          return pricing;
        });
      }

      function createPricingOption(pricingOption, index) {
        $scope.loading = true;
        $scope.loadingIndex = index;

        pricingOptionsFactory
          .create(currentFleet().fleet_id, {
            price: pricingOption.price,
            price_currency: $scope.fleetCurrency,
            duration: pricingOption.duration,
            duration_unit: pricingOption.duration_unit,
          })
          .then(function (response) {
            $scope.loading = false;
            $scope.loadingIndex = null;

            $scope.pricingOptions = updateScopePricingOptions(
              pricingOption,
              response.payload
            );

            notify({
              message: "Pricing option created successfully.",
              duration: 2000,
              position: "right",
            });
          })
          .catch(function (error) {
            console.error(error);
            $scope.loading = false;
            $scope.error = getErrorMessage(error);
          });
      }

      function updatePricingOption(pricingOption, index) {
        $scope.loading = true;
        $scope.loadingIndex = index;

        pricingOptionsFactory
          .update(
            {
              fleetId: currentFleet().fleet_id,
              pricingOptionId: pricingOption.pricing_option_id,
            },
            {
              price: pricingOption.price,
              duration: pricingOption.duration,
              duration_unit: pricingOption.duration_unit,
            }
          )
          .then(function (response) {
            $scope.pricingOptions = updateScopePricingOptions(
              pricingOption,
              response.payload
            );

            $scope.loading = false;
            $scope.loadingIndex = null;

            notify({
              message: "Successfully updated pricing option.",
              duration: 2000,
              position: "right",
            });
          })
          .catch(function (error) {
            console.error(error);
            $scope.loading = false;
            $scope.loadingIndex = null;
            $scope.error = getErrorMessage(error);
          });
      }

      function activatePricingOption(pricingOption, index) {
        $scope.activating = true;
        $scope.activatingIndex = index;

        pricingOptionsFactory
          .activate({
            fleetId: currentFleet().fleet_id,
            pricingOptionId: pricingOption.pricing_option_id,
          })
          .then(function (response) {
            $scope.pricingOptions = updateScopePricingOptions(
              pricingOption,
              response.payload
            );

            $scope.activating = false;
            $scope.activatingIndex = null;

            notify({
              message: "Pricing option activated.",
              duration: 2000,
              position: "right",
            });
          })
          .catch(function (error) {
            console.error(error);
            $scope.activating = false;
            $scope.activatingIndex = null;
            $scope.error = getErrorMessage(error);
          });
      }

      function deactivatePricingOption(pricingOption, index) {
        $scope.deactivating = true;
        $scope.deactivatingIndex = index;

        pricingOptionsFactory
          .deactivate({
            fleetId: currentFleet().fleet_id,
            pricingOptionId: pricingOption.pricing_option_id,
          })
          .then(function (response) {
            $scope.pricingOptions = updateScopePricingOptions(
              pricingOption,
              response.payload
            );

            $scope.deactivating = false;
            $scope.deactivatingIndex = null;

            notify({
              message: "Pricing option deactivated.",
              duration: 2000,
              position: "right",
            });
          })
          .catch(function (error) {
            console.error(error);
            $scope.deactivating = false;
            $scope.deactivatingIndex = null;
            $scope.error = getErrorMessage(error);
          });
      }

      function executePricingOption(pricingOption, index) {
        if (pricingOption.pricing_option_id) {
          updatePricingOption(pricingOption, index);
        } else {
          createPricingOption(pricingOption, index);
        }
      }

      function addPricingOption() {
        if ($scope.pricingOptions) {
          if (
            !$scope.pricingOptions.find(
              (m) => m.pricing_option_id === undefined
            )
          ) {
            $scope.pricingOptions.push({});
          }
        } else {
          $scope.pricingOptions = [{}];
        }
      }

      $scope.dismissError = dismissError;
      $scope.executePricingOption = executePricingOption;
      $scope.activatePricingOption = activatePricingOption;
      $scope.deactivatePricingOption = deactivatePricingOption;
      $scope.addPricingOption = addPricingOption;

      function init() {
        const fleetId = currentFleet().fleet_id;

        list(fleetId);
      }

      init();
    }
  );
