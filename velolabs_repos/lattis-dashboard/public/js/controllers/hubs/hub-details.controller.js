"use strict";

angular.module("skyfleet.controllers").controller("hubDetailsController", [
  "$window",
  "$scope",
  "$state",
  "$stateParams",
  "ngDialog",
  "notify",
  "hubsFactory",
  "$q",
  "tripAnimationFactory",
  "bikeFleetFactory",
  "$rootScope",
  "myFleetFactory",
  function (
    $window,
    $scope,
    $state,
    $stateParams,
    ngDialog,
    notify,
    hubsFactory,
    $q,
    tripAnimationFactory,
    bikeFleetFactory,
    $rootScope,
    myFleetFactory
  ) {
    $scope.fields = [
      { title: "Name", key: "hubName" },
      { title: "Status", key: "localHubStatus" },
      { title: "Make", key: "make" },
      { title: "Model", key: "model" },
      { title: "Description", key: "description" },
      { title: "Setup", key: "enclosureType" },
      { title: "Date added", key: "dateCreated" },
    ];

    $scope.types = {
      docking_station: "Docking Station",
      parking_station: "Parking Station",
    };

    $scope.hub = {};

    $scope.loading = false;

    function retrievePortsHub(ports) {
      if (ports.length == 0) {
        return [];
      }
      const promises = ports.map(({ portUUID }) =>
        hubsFactory.retrievePort(portUUID)
      );
      return Promise.all(promises).then((fetchedports) => {
        return fetchedports.map((port) => port.payload);
      });
    }

    function retrieve() {
      $scope.loading = true;
      return hubsFactory
        .retrieve($stateParams.uuid)
        .then((hub) => {
          $scope.loading = false;
          $scope.hub = hub;
          $scope.hub.enclosureType === "closed" ? "close" : hub.enclosureType;
          if ($scope.hub.ports && $scope.hub.ports.length) {
            $scope.hub.ports.map(async (port) => {
              if (port.lastUser && port.lastUser.completedTrip) {
                port.usageMessage = "LastUser";
              } else if (port.lastUser) {
                port.usageMessage = "User";
              } else {
                $scope.usageMessage = "";
              }
              port.waiting = true;
              await $q
                .when(
                  tripAnimationFactory.getAllTrips({ port_id: port.portId })
                )
                .then((data) => {
                  if (data.payload.trips.length > 0) {
                    port.trip = data.payload.trips[0];
                    port.hasTrip = true;
                    port.waiting = false;
                    port.endingTrip = false;
                  } else {
                    port.hasTrip = false;
                    port.waiting = false;
                  }
                });
            });
          }
          const hasExtraFields =
            $scope.hub && $scope.hub.enclosureType === "closed";
          if (hasExtraFields) {
            if ($scope.hub.equipment) {
              $scope.hub.equipmentKey = $scope.hub.equipment.key;
              $scope.hub.equipmentVendor = $scope.hub.equipment.vendor;
            }
            const extraFields = [
              { title: "Equipment id", key: "equipmentKey" },
              { title: "Equipment Type", key: "equipmentVendor" },
              { title: "Qr code", key: "hubQrCode" },
            ];

            $scope.fields = [...$scope.fields, ...extraFields];
          }
          localStorage.setItem("enclosureType", hub.enclosureType);
        }).catch((e) => {
          console.error("Error retrieving hub: ", $stateParams.uuid, e);
          $scope.loading = false;
        });
    }

    function init() {
      retrieve();
    }

    function unlockPort(port) {
      $scope.unlockingPort = port.portId;
      port.hubType = $scope.hub.integration;

      if(port.equipment && port.equipment.vendor && ['Kisi', 'dck-mob-e', 'ParcelHive'].includes(port.equipment.vendor)) {
        return hubsFactory.unlockPort(port.portUUID).then(data=> {
          notify({
            message: `Successfully unlocked port number ${port.portNumber}`,
            duration: 3000,
            position: "right",
          });

          $state.reload();
        }).catch((e) => {
          $scope.unlockingPort = null;
          console.error("Failed to unlock port: ", e);
          const errorMessage = e.message || (e.data && e.data.error && e.data.error.message)
          notify({
            message: errorMessage || `Error unlocking port number ${port.portNumber}`,
            duration: 3000,
            position: "right",
          });
        });
      }

      return hubsFactory
        .unlock(port)
        .then((data) => {
          $scope.unlockingPort = null;
          if (
            data.payload.apiResponse.charge &&
            data.payload.apiResponse.charge.docked
          ) {
            port.bike = null;
            port.status = "Unavailable";
          }

          notify({
            message: `Successfully unlocked port number ${port.portNumber}`,
            duration: 3000,
            position: "right",
          });

          $state.reload();
        }).catch((e) => {
          $scope.unlockingPort = null;
          console.error("Failed to unlock port: ", e);
          const errorMessage = e.message || (e.data && e.data.error && e.data.error.message)
          notify({
            message: errorMessage || `Error unlocking port number ${port.portNumber}`,
            duration: 3000,
            position: "right",
          });
        });
    }

    $scope.endCurrentTrip = function endCurrentTrip(port) {
      const trip = port.trip;
      $scope.hub.ports.map((mapport) => {
        if (mapport.portId === port.portId) {
          port.endingTrip = true;
        }
      });
      bikeFleetFactory.endTripPort(
        {
          trip_id: trip.trip_id,
          to: "live",
          fleet_id: trip.fleet_id,
          acl: localStorage.getItem("acl"),
          ended_by_operator: true,
        },
        function (res) {
          if (res && res.status === 200) {
            bikeFleetFactory.clearBikeDataCache();
            myFleetFactory.clearTicketsCache();
            $rootScope.$broadcast("fleetChange", trip.fleet_id);
            $rootScope.$emit("fleetChange", trip.fleet_id);
            retrieve()
            $window.location.reload();
            notify({
              message: "Ended the trip successfully",
              duration: 2000,
              position: "right",
            });
          } else if (res && res.status === 403) {
            notify({
              message: "Cannot end the trip since it is a payment fleet",
              duration: 2000,
              position: "right",
            });
          } else {
            notify({
              message: "Failed to end the trip",
              duration: 2000,
              position: "right",
            });
          }
          $scope.hub.ports.map((mapport) => {
            if (mapport.portId === port.portId) {
              port.endingTrip = false;
            }
          });
        }
      );
    };

    $scope.isPortAvailable = function(hub, port) {
      if(hub && hub.type === 'docking_station') {
        return port.portStatus === 'Available' || !port.portVehicleUUID
      } else if (hub && hub.type === 'parking_station') {
        return !(port.currentPortStatus === 'on_trip') || !port.currentTripId // Means no vehicle is docked, the port can be rented out
      }
    }

    $scope.unlockPort = unlockPort;
    $scope.editHub = function editHub() {
      $state.go("hub-edit", { uuid: $scope.hub.hubUUID });
    };

    $scope.hasVehicle = function hasVehicle(hub) {
      return (
        hub.ports &&
        hub.ports.length &&
        hub.ports.filter((port) => !!port.bike).length
      );
    };

    $scope.hasDockedVehicle = function hasDockedVehicle(ports, status) {
      return (
        ports &&
        status == "staging" &&
        ports.length &&
        ports.filter(
          (port) => port.current_status == "on_trip" || !!port.vehicle_uuid
        ).length
      );
    };

    $scope.visitProfile = function (id) {
      $state.go("member-profile", { userId: id });
    };

    $scope.gotoPort = function gotoPort(portUUID) {
      $state.go("port-details", { uuid: portUUID });
    };

    $scope.goToHubsList = function goToHubsList() {
      if ($scope.hub.localHubStatus) {
        $state.go(`hubs-${$scope.hub.localHubStatus}`);
      } else {
        $window.history.back();
      }
    };

    $scope.changeStatus = function changeStatus(hub) {
      delete hub.image; // This is just an unnecessary huge value we don't need in this update
      $scope.changingStatus = true;

      const status = hub.localHubStatus === "live" ? "staging" : "live";

      const update = (moveBikes) => {
        const isParking = hub.type === "parking_station";
        return hubsFactory
          .update(
            hub.hubUUID,
            {
              status,
              fleet_id: hubsFactory.currentFleet().fleet_id,
              integration: hub.integration,
            },
            { moveBikes: isParking ? 1 : moveBikes }
          ).then((res) => {
            $scope.hub = res.payload.data;
            $scope.changingStatus = false;

            notify({
              message: `Successfully moved hub to ${
                hub.localHubStatus === "live" ? "staging" : "live"
              }`,
              duration: 3000,
              position: "right",
            });
          }).catch((error) => {
            $scope.changingStatus = false;
            notify({
              message: `Failed to move hub to ${
                hub.localHubStatus === "live" ? "staging" : "live"
              }`,
              duration: 3000,
              position: "right",
            });
          });
      };

      return retrievePortsHub(hub.ports).then((ports) => {
        if ($scope.hasDockedVehicle(ports, status)) {
          $scope.changingStatus = false;
          notify({
            message: `We cannot move this hub to ${status} it has vehicles docked to it.`,
            duration: 5000,
            position: "right",
          });
          return;
        }

        if (!$scope.hasVehicle(hub) && hub.integration !== "custom") {
          return update();
        }

        return ngDialog
          .openConfirm({
            showClose: false,
            template:
              "../../../html/modals/confirm-change-hub-bikes-status.html",
            data: {
              status,
              type: hub.integration === "custom" ? "ports" : "vehicles",
            },
            scope: $scope,
          }).then((confirm) => {
            const moveBikes = confirm ? 1 : undefined;

            return update(moveBikes);
          }).catch((cancel) => {
            if (cancel) {
              $scope.changingStatus = false;
              return;
            }

            return update();
          });
      });
    };

    init();
  },
]);
