angular
  .module("skyfleet.controllers")
  .controller(
    "paymentGatewayController",
    function (
      $scope,
      $window,
      $location,
      $cookies,
      $state,
      lattisConstants,
      rootScopeFactory,
      paymentFactory,
      notify
    ) {
      if ($location.search().code) {
        getStripeConnectInfo($location.search().code);
      }

      const GATEWAYS = {
        stripe: "stripe",
        mercadopago: "mercadopago",
      };

      $scope.GATEWAYS = GATEWAYS;

      if (window.location.hostname === "bikeshare.lattis.io") {
        $scope.connect_stripe_id = lattisConstants.stripeProductionClientID;
      } else {
        $scope.connect_stripe_id = lattisConstants.stripeDevClientID;
      }

      $scope.connectedTo = function connectedTo(gateway) {
        return $scope.pricingdata.payment_gateway === gateway;
      };

      $scope.connectPaymentGateway = function connectPaymentGateway() {
        $scope.connectingToPaymentGateway = true;
      };

      $scope.connectMercadoPago = function connectMercadoPago() {
        $scope.connectingToPaymentGateway = GATEWAYS.mercadopago;

        const fleetId =
          rootScopeFactory.getData("fleetId") ||
          JSON.parse(localStorage.getItem("currentFleet")).fleet_id;

        $scope.mercadoPago.fleetId = fleetId;

        return paymentFactory
          .connectMercadoPago($scope.mercadoPago)
          .then(() => {
            notify({
              message: "Connected to Mercado Pago successfully.",
              duration: 2000,
              position: "right",
            });

            $scope.connectingToPaymentGateway = false;
          })
          .catch((error) => {
            console.log(error);
            $scope.connectingToPaymentGateway = true;

            notify({
              message: "Failed to connect to Mercado Pago",
              duration: 2000,
              position: "right",
            });
          });
      };

      $scope.stripeRedirectURI = function stripeRedirectURI() {
        return $window.encodeURIComponent($location.absUrl());
      };

      function getStripeConnectInfo(code) {
        paymentFactory.saveStripeConnectInfo(
          {
            code: code,
            fleet_id: $cookies.get("stripe_fleet_id"),
          },
          function (response) {
            if (response && response.status === 200) {
              notify({
                message: "Connected with the stripe account successfully",
                duration: 2000,
                position: "right",
              });

              $state.go("payments", {}, { location: "replace" });
            } else {
              notify({
                message: "Failed to connect to the stripe account",
                duration: 2000,
                position: "right",
              });
            }
          }
        );
      }
    }
  );
