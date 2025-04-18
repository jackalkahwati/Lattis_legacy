"use strict";

angular.module("skyfleet.controllers").controller("addHubController", [
  "$scope",
  "$window",
  "$state",
  "notify",
  "hubsFactory",
  function ($scope, $window, $state, notify, hubsFactory) {
    $scope.integrations = [
      { title: "Kuhmute", value: "kuhmute" },
      { title: "DuckT", value: "duckt" },
      { title: "Custom", value: "custom" }
    ];

    $scope.types = [
      { title: "Docking Station", value: "docking_station" },
      { title: "Parking Station", value: "parking_station" },
    ];


    $scope.enclosureTypes = [
      { title: "Open", value: "open" },
      { title: "Closed", value: "closed" },
    ];

    $scope.equipmentTypes = [
      { title: "Kisi", value: "Kisi" },
      { title: "SAS", value: "Sas" },
      { title: 'Manual Lock', value: 'ManualLock'},
      // { title: 'ParcelHive', value: 'ParcelHive'},
    ];

    $scope.showDuckTFields = false;
    $scope.showCustomHubFields = false;

    $scope.hub = {
      integration: $scope.integrations[0],
    };

    $scope.hubs = [];

    $scope.addingHub = false;

    function listHubs() {
      $scope.listingHubs = true;

      return hubsFactory
        .list({ exclude_fleet: hubsFactory.currentFleet().fleet_id })
        .then((hubs) => {
          $scope.listingHubs = false;
          $scope.hubs = hubs;
        })
        .catch((e) => {
          $scope.listingHubs = false;
          console.error("Error listing hubs: ", e);
          notify({
            message: "Error fetching hubs to add.",
            duration: 3000,
            position: "right",
          });
        });
    }

    function init() {
      listHubs();
    }

    function selectedOption() {
      if ($scope.hub && $scope.hub.integration.value === 'custom') {
        $scope.showCustomHubFields = true;
      } else $scope.showCustomHubFields = false;
      if ($scope.hub && $scope.hub.integration.title === 'DuckT') {
        $scope.showDuckTFields = true;
      } else {
        $scope.showDuckTFields = false;
      }
    };

    function changeSetup() {
      if (['kuhmute', 'duckt'].includes($scope.hub.integration.value)) {
        $scope.hub.enclosureType = 'open';
      }else {
        $scope.hub.enclosureType = undefined
      }

    }

    function integrationInit() {
      selectedOption();
      changeSetup();
    }


    function addHub(e) {
      e.preventDefault();
      let key = $scope.hub.equipmentKey
      if ($scope.hub.equipmentType === 'ParcelHive') {
        key = `${$scope.hub.lockerId}-${$scope.hub.boxId}`
      }
      $scope.error = false;
      $scope.addingHub = true;
      const data = {
        make: $scope.hub.make,
        model: $scope.hub.model,
        name: $scope.hub.name || '',
        description: $scope.hub.description,
        type: $scope.hub.type.value,
        image: $scope.hub.image,
        integration: $scope.hub.integration.value,
        fleet_id: hubsFactory.currentFleet().fleet_id,
        longitude: $scope.hub.longitude,
        latitude: $scope.hub.latitude,
        qr_code: $scope.hub.qrCode,
        portsNumber: $scope.hub.number_of_spots,
        enclosureType: $scope.hub.enclosureType,
        status: 'staging',
        equipmentType: $scope.hub.equipmentType,
        lockerId: $scope.hub.lockerId,
        boxId: $scope.hub.boxId,
        equipmentKey: key,
      }

      const operation = $scope.hub.integration.value === 'custom' ? hubsFactory.add(data) : hubsFactory.update($scope.hub.selectedHub ? $scope.hub.selectedHub.hubUUID : $scope.hub.hubId, data)

      operation.then((response) => {

        if ($scope.hub.integration.value === 'custom') {
          $scope.hub = { ...$scope.hub, ...response.payload }
        }
        $scope.addingHub = false;
        notify({
          message: "Hub successfully added.",
          duration: 5000,
          position: "right",
        });

        listHubs().then(() => {
          $scope.addHubForm.$element[0].reset();

          $scope.hub = {
            integration: $scope.integrations[0],
          };

          $scope.addHubForm.$setPristine();
          $scope.addHubForm.$setUntouched();
          $scope.addHubForm.$submitted = false;
        });

        if ($scope.hub.integration.value === 'custom') {
          $state.go('hub.details', { uuid: response.payload.uuid });
        } else $state.go('hub.details', { uuid: $scope.hub.hubId });
      }).catch((error) => {
        console.error(error);
        $scope.addingHub = false;

        notify({
          message: `An error occurred adding/updating hub`,
          duration: 5000,
          position: "right",
        });
      });
    }

    function dismissError() {
      $scope.error = false;
    }

    $scope.addHub = addHub;
    $scope.selectedOption = selectedOption;
    $scope.dismissError = dismissError;
    $scope.changeSetup = changeSetup;
    $scope.integrationInit = integrationInit;

    $scope.fileUpload = function (file) {
      $scope.hub.image = file;
    };

    $scope.goBack = function (e) {
      $window.history.back();
    };

    init();
  },
]);
