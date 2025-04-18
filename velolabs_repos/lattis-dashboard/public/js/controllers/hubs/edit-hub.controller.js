"use strict";

angular.module("skyfleet.controllers").controller("editHubController", [
  "$scope",
  "$window",
  "$state",
  "$stateParams",
  "notify",
  "hubsFactory",
  function ($scope, $window, $state, $stateParams, notify, hubsFactory) {
    $scope.integrations = [
      { title: "Kuhmute", value: "kuhmute" },
      { title: "DuckT", value: "duckt" },
      { title: "Custom", value: "custom" }
    ];
    $scope.types = [
      { title: "Docking Station", value: "docking_station" },
      { title: "Parking Station", value: "parking_station" },
    ];

    $scope.equipmentTypes = [
      { title: "Kisi", value: "Kisi" },
      { title: "SAS", value: "Sas" },
      { title: 'ParcelHive', value: 'ParcelHive'},
      { title: 'Manual Lock', value: 'ManualLock'},
    ]

    $scope.enclosureTypes = [
      { title: "Open", value: "open" },
      { title: "Closed", value: "closed" },
    ];

    $scope.showDuckTFields = false;
    $scope.editingHub = false;
    $scope.loading = false;

    function retrieve() {
      $scope.loading = true;
      hubsFactory
        .retrieve($stateParams.uuid)
        .then((hub) => {
          $scope.loading = false;
          $scope.hub = hub;
          $scope.hub.enclosureType = ['kuhmute', 'duckt'].includes(hub.integration) ? 'open' : hub.enclosureType
          if(hub.equipment) {
            $scope.hub.equipmentKey = hub.equipment.key;
            $scope.hub.equipmentType = hub.equipment.vendor;
          }
          hub.number_of_spots = ( hub.ports && hub.ports.length) || 0
          if ($scope.hub && $scope.hub.integration === 'duckt') {
            $scope.showDuckTFields = true;
          } else {
            $scope.showDuckTFields = false;
          }
        })
        .catch((e) => {
          console.error("Error retrieving hub: ", $stateParams.uuid, e);
          $scope.loading = false;
          notify({
            message: "Error retrieving hub.",
            duration: 5000,
            position: "right",
          });
        });
    }

    function init() {
      retrieve();
    }

    function selectedOption() {
      if ($scope.hub && $scope.hub.integration === 'duckt') {
        $scope.showDuckTFields = true;
      } else {
        $scope.showDuckTFields = false;
      }
    };

    function editHub(e) {
      e.preventDefault();

      $scope.error = false;
      $scope.editingHub = true;

      const updateData = {
        make: $scope.hub.make,
        model: $scope.hub.model,
        name: $scope.hub.hubName || '',
        description: $scope.hub.description,
        latitude: $scope.hub.latitude,
        longitude: $scope.hub.longitude,
        type: $scope.hub.type,
        image: $scope.hub.image,
        integration: $scope.hub.integration,
        equipmentType: $scope.hub.equipmentType,
        equipmentKey: $scope.hub.equipmentKey,
        lockerId: $scope.hub.lockerId,
        enclosureType: $scope.hub.enclosureType,
        qrCode: $scope.hub.qrCode,
        fleet_id: hubsFactory.currentFleet().fleet_id,
        status: "staging",
      }

      hubsFactory
        .update($scope.hub.hubUUID, updateData)
        .then(() => {
          $scope.editingHub = false;

          notify({
            message: "Hub successfully updated.",
            duration: 3000,
            position: "right",
          });

          $state.go("hub.details", { uuid: $scope.hub.hubUUID });
        })
        .catch((error) => {
          console.error(error);
          $scope.editingHub = false;

          notify({
            message: `An error occurred updating hub: ${
              error && error.response && error.response.data
                ? error.response.data.message
                : ""
            }`,
            duration: 5000,
            position: "right",
          });
        });
    }

    function dismissError() {
      $scope.error = false;
    }

    $scope.editHub = editHub;
    $scope.selectedOption = selectedOption;
    $scope.dismissError = dismissError;

    $scope.fileUpload = function (file) {
      $scope.hub.image = file;
      $scope.editHubForm.$setDirty();
    };

    $scope.goBack = function (e) {
      $window.history.back();
    };

    init();
  },
]);
