"use strict";

angular.module("skyfleet.controllers").controller("portDetailsController", [
  "$window",
  "$scope",
  "$state",
  "$stateParams",
  "ngDialog",
  "notify",
  "hubsFactory",
  function (
    $window,
    $scope,
    $state,
    $stateParams,
    ngDialog,
    notify,
    hubsFactory
  ) {
    $scope.port = {};

    $scope.loading = false;
    $scope.addingEquipment = false;
    $scope.updatingPort = false;
    $scope.editingEquipment = false;

    $scope.vendors = [
      {
        name: "Kisi", value: "Kisi",
      },
      {
        name: "Sas", value: "Sas",
      },
      {
        name: "Greenriders", value: "dck-mob-e",
      },
      {
        name: "ParcelHive", value: "ParcelHive",
      }
    ];

    $scope.formData = {
      equipmentKey: null,
      portQrCode: null,
      equipmentVendor: null,
    };
    const tripStatusMap = {
      'parked': 'Parked',
      'onTrip': 'On Trip',
      'on_trip': 'On Trip',
      'reserved': 'Reserved',
    }

    $scope.showUnlock = false;

    function retrieve() {
      $scope.loading = true;
      return hubsFactory
        .retrievePort($stateParams.uuid)
        .then((port) => {
          $scope.hubType = localStorage.getItem("enclosureType")
          $scope.loading = false;
          $scope.port = port.payload;
          if ($scope.port.equipment && $scope.port.equipment.vendor === 'ParcelHive') {
            $scope.port.equipment.lockerId = JSON.parse($scope.port.equipment.metadata).lockerId
            $scope.port.equipment.boxId = JSON.parse($scope.port.equipment.metadata).boxId
          }
          $scope.showUnlock = !!$scope.port.equipment && (!($scope.editingEquipment || $scope.addingEquipment || $scope.updatingPort)) && !!($scope.port.vehicle_uuid || $scope.port.current_status === 'on_trip')
          $scope.tripStatus = tripStatusMap[$scope.port.current_status] || 'Unknown'
          const equipmentKey =
            (port.payload.equipment && port.payload.equipment.key) || "";
          const portQrCode = $scope.port.port_qr_code || "";
          $scope.initialQRCode = $scope.port.port_qr_code;
          const equipmentVendor =
            ($scope.port &&
              $scope.port.equipment &&
              $scope.port.equipment.vendor) ||
            "";
          $scope.formData = { equipmentKey, portQrCode, equipmentVendor }
        })
        .catch((e) => {
          console.error("Error retrieving port: ", $stateParams.uuid, e);
          $scope.loading = false;
        });
    }

    $scope.showPasswordField = false

    $scope.$watch('formData.equipmentVendor', function(){
      if ($scope.formData && $scope.formData.equipmentVendor && $scope.formData.equipmentVendor.value === 'dck-mob-e') $scope.showPasswordField = true
      else $scope.showPasswordField = false
    });

    function editPort() {
      $scope.initialQRCode = $scope.port.port_qr_code;
      $scope.updatingPort = true;
    }

    function addEquipment() {
      $scope.addingEquipment = true;
    }

    function editEquipment() {
      $scope.editingEquipment = true;
    }

    function cancel() {
      $scope.updatingPort = false;
      $scope.addingEquipment = false;
      $scope.editingEquipment = false;
      $scope.loading = false;
    }

    function getError(response) {
      return response.data && response.data.error && response.data.error.message
    }

    function save() {
      $scope.loading = true;
      $scope.editingEquipment
      const vendor = $scope.formData.equipmentVendor && $scope.formData.equipmentVendor.value;

      if ($scope.addingEquipment) {
        $scope.operation = "addingEquipment";
        // Save equipment on port
      } else if ($scope.updatingPort) {
        $scope.operation = "updatingPort";
        // Editing other port info
        if ($scope.initialQRCode === $scope.formData.portQrCode) {
          //No change in QR code
          console.log("No change in QR code detected");
          cancel();
        }
      } else if ($scope.editingEquipment) {
        console.log('Editing equipment');
        $scope.operation = "editingEquipment";
      }

      let key = $scope.formData.equipmentKey
      if ($scope.formData.equipmentVendor.value === 'ParcelHive') {
        key = `${$scope.formData.lockerId}-${$scope.formData.boxId}`
      }
      const data = {
        portUUID: $stateParams.uuid,
        vendor: vendor,
        operation: $scope.operation,
        fleetId: $scope.port.hub.fleet_id,
        equipmentKey: key,
        portQrCode: $scope.formData.portQrCode,
        equipmentVendor: $scope.formData.equipmentVendor,
        lockerId: $scope.formData.lockerId,
        boxId: $scope.formData.boxId,
        equipmentType: 'port',
        portId: $scope.port.port_id
      };
      if(vendor === 'dck-mob-e') {
        if($scope.operation === 'addingEquipment') {
          if(!$scope.formData.password) {
            notify({
              message: `Client Id and passwords must be provided to register a Greenriders station`,
              duration: 3000,
              position: "right",
            });
            $scope.loading = false;
            return
          }
        }
        data.password = $scope.formData.password
      }
      if ($scope.editingEquipment && $scope.port.equipment) {
        data.equipmentId = $scope.port.equipment.controller_id;
      }
      return hubsFactory
        .updatePort(data)
        .then(() => {
          return retrieve()
            .then(() => {
              cancel();
              notify({
                message: `Port updated successfully`,
                duration: 3000,
                position: "right",
              });
            })
            .catch((error) => {
              cancel();
              notify({
                message: getError(error) || `Error updating port information`,
                duration: 3000,
                position: "right",
              });
            });
        })
        .catch((error) => {
          cancel();
          notify({
            message: getError(error) || `Error updating port information`,
            duration: 3000,
            position: "right",
          });
        });
    }

    $scope.addEquipment = addEquipment;
    $scope.editEquipment = editEquipment;
    $scope.save = save;
    $scope.editPort = editPort;
    $scope.cancel = cancel;

    function init() {
      retrieve();
    }

    function unlockPort() {
      $scope.unlockingPort = $scope.port.uuid;
      return hubsFactory
        .unlockPort($scope.port.uuid)
        .then((data) => {
          $scope.unlockingPort = null;
          $scope.port.current_status = 'parked'
          $scope.port.vehicle_uuid = null
          $scope.tripStatus = tripStatusMap[$scope.port.current_status] || 'Unknown'
          retrieve()
          notify({
            message: `Port unlocked successfully`,
            duration: 3000,
            position: "right",
          });
          $state.reload();
        })
        .catch((error) => {
          const message = error.data && error.data.error && error.data.error.message
          $scope.unlockingPort = null;
          notify({
            message: message || `Error unlocking port number ${$scope.port.number}`,
            duration: 3000,
            position: "right",
          });
        });
    }

    $scope.isPortAvailable = function(port) {
      if(port && port.hub && port.hub.type === 'docking_station') {
        return port.status === 'Available' || !port.vehicle_uuid
      } else if (port && port.hub && port.hub.type === 'parking_station') {
        return !(port.current_status === 'on_trip') || !port.current_trip_id
      }
    }

    $scope.administerGreenRiders = function(action, vendor, key) {
      $scope.adminsteringGR = true
      return hubsFactory.administerGreenRiders({ action, vendor, key, portId: $scope.port.port_id }).then((response)=> {
        if(response.data && response.data.payload && response.data.payload.data && response.data.payload.data.bike) {
          $scope.port.bike = response.data.payload.data.bike
          $scope.port.vehicle_uuid = response.data.payload.data.bike.bike_uuid
          $scope.port.status = 'Unavailable'
        }
        $scope.adminsteringGR = false
        const messages = {
          'reset': 'Reset command has been send to station',
          'reload': 'Station information updated successfully'
        }
        notify({
          message: messages[action],
          duration: 3000,
          position: "right",
        })
      }).catch((err)=> {
        $scope.port.online = false
        notify({
          message: `Port ${action} failed. Check it's online status`,
          duration: 3000,
          position: "right",
        });
        $scope.adminsteringGR = false
      });
    }

    $scope.unlockPort = unlockPort;
    init();
  },
]);
