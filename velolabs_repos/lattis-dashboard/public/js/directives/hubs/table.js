"use strict";

angular.module("skyfleet").directive("hubsTable", [
  "hubsFactory",
  "notify",
  "ngDialog",
  function (hubsFactory, notify, ngDialog) {
    return {
      restrict: "E",
      scope: {
        hubs: "=",
        status: "<",
        listHubs: "&listHubs",
      },
      templateUrl: "../../../html/hubs/table.html",
      link: function (scope) {
        scope.fields = {
          remoteHubStatus: { title: "Status" },
        };

        scope.allFields = {
          hubName: { title: "Name" },
          type: { title: "Type" },
          ...scope.fields,
          spots: { title: "Spots" },
        };

        scope.types = {
          docking_station: "Docking Station",
          parking_station: "Parking Station",
        };

        scope.changingStatus = null;

        $scope.isPortAvailable = function(hub, port) {
          if(hub && hub.type === 'docking_station') {
            return port.portStatus === 'Available' || !port.portVehicleUUID
          } else if (hub && hub.type === 'parking_station') {
            return !(port.currentPortStatus === 'on_trip') || !port.currentTripId // Means no vehicle is docked, the port can be rented out
          }
        }

        scope.availableSpots = function (hub) {
          return (hub.ports || []).filter(function (port) {
            return $scope.isPortAvailable(hub, port)
          });
        };

        scope.hasVehicle = function hasVehicle(hub) {
          return (
            hub.ports &&
            hub.ports.length &&
            hub.ports.filter((port) => !!port.bike).length
          );
        };

        function retrievePortsHub(ports) {
          if(ports.length == 0) {
            return [];
          }
          const promises = ports.map(({ portUUID }) => hubsFactory.retrievePort(portUUID))
          return Promise.all(promises).then((fetchedports) => {
            return fetchedports.map((port) => port.payload)
          });
        }

        scope.hasDockedVehicle = function hasVehicle(ports, status) {
          return (
            ports &&
            status == "staging" &&
            ports.length &&
            ports.filter((port) => port.current_status == "on_trip" || !!port.vehicle_uuid).length
          );
        };

        scope.changeStatus = function changeStatus(hub) {
          scope.changingStatus = hub.hubUUID;
          const status = scope.status === "live" ? "staging" : "live";

          const update = (moveBikes) => {
            const isParking = hub.type === "parking_station";
            return hubsFactory
              .update(
                hub.hubUUID,
                {
                  status: status,
                  fleet_id: hubsFactory.currentFleet().fleet_id,
                  integration: hub.integration,
                },
                { moveBikes: isParking ? 1 : moveBikes }
              )
              .then(() => {
                scope
                  .listHubs()
                  .then(() => {
                    scope.changingStatus = null;
                    notify({
                      message: `Successfully moved to ${status}`,
                      duration: 2000,
                      position: "right",
                    });
                  })
                  .catch((e) => {
                    console.error("Failed to fetch updated hubs.", e);
                    scope.changingStatus = null;
                    notify({
                      message:
                        "Failed to fetch updated hubs. Please reload the page.",
                      duration: 3000,
                      position: "right",
                    });
                  });
              })
              .catch((error) => {
                console.error(error);
                scope.changingStatus = null;
                notify({
                  message: `Failed to move to ${status}`,
                  duration: 3000,
                  position: "right",
                });
              });
          };

          return retrievePortsHub(hub.ports).then((ports) => {
            if (scope.hasDockedVehicle(ports, status)) {
              scope.changingStatus = false;
              notify({
                message: `We can't move this hub to ${status} it has vehicles docked to it.`,
                duration: 5000,
                position: "right",
              });
              return;
            }

          if (!scope.hasVehicle(hub)) {
            return update();
          }


          return ngDialog
            .openConfirm({
              showClose: true,
              template: "../../../html/modals/confirm-change-hub-bikes-status.html",
              data: { status },
              scope: scope,
            })
            .then((confirm) => {
              const moveBikes = confirm ? 1 : undefined;

              return update(moveBikes);
            })
            .catch(() => {
              scope.changingStatus = null;
              return;
            });
          });
        };
      },
    };
  },
]);
