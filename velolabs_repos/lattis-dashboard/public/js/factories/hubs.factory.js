"use strict";

angular.module("skyfleet").factory("hubsFactory", [
  "$http",
  "rootScopeFactory",
  function ($http, rootScopeFactory) {
    const currentFleet = () => {
      const fleetId = rootScopeFactory.getData("fleetId");

      return fleetId
        ? { fleet_id: fleetId }
        : JSON.parse(localStorage.getItem("currentFleet"));
    };

    return {
      currentFleet,
      list: function (filters = {}) {
        if (!filters.exclude_fleet) {
          filters.fleet_id =
            filters.fleet_id || currentFleet().fleet_id;
        }

        return $http.get("/api/integration/hubs", { params: filters }).then(
          ({
            data: {
              payload: { data },
            },
          }) => {
            if (data.length && data[0].hubs) {
              return data[0].hubs;
            }

            return data;
          }
        );
      },
      retrievePort: function (portUUID) {
        return $http.get(`/api/integration/ports/${portUUID}`).then(response => {
          return response.data && response.data
        }).catch((err) => {
          throw error
        });
      },
      updatePort: function(data) {
        return $http.put(`/api/integration/ports/${data.portUUID}`, data)
      },
      add: function (data) {
        return $http.post("/api/integration/hubs", data).then((response) => {
          return response.data
        }).catch((e) => {
          throw e;
        })
      },
      update: function (uuid, data, params) {
        return $http
          .put(`/api/integration/hubs/${uuid}`, data, { params })
          .then((response) => {
            return response.data
          });
      },
      retrieve: function (uuid) {
      const params = {
        params: { fleet_id: currentFleet().fleet_id },
      }
        return $http
          .get(`/api/integration/hubs/${uuid}`, params)
          .then(({ data, data: { payload } }) => {
            if (payload.data) {
              return {
                ...payload.data,
                ports: payload.data.ports || [],
                integration: payload.data.integration
                  ? payload.data.integration.toLowerCase()
                  : undefined,
              };
            }

            return payload.data;
          });
      },
      unlock: function (port) {
        return $http
          .post(`/api/bike-fleet/${port.portVehicleUUID}/iot/lock`, {
            action: "unlock",
            hubType: port.hubType
          })
          .then((response) => response.data);
      },
      unlockPort: function(uuid) {
        return $http.post(`/api/integration/ports/${uuid}/unlock`);
      },
      updateHubStatus: function (hub, status) {
        return $http
          .put(`/api/integration/hub/status`, {
            uuid: hub.hubUUID,
            status: status,
          })
          .then(({ data }) => data);
      },
      administerGreenRiders: function({action, vendor, key, portId}) {
        return $http.post(`/api/integration/ports/admin`, {
          action, vendor, key, portId
        });
      }
    };
  },
]);
