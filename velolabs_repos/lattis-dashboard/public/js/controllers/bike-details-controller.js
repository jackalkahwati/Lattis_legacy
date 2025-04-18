'use strict';

angular
  .module('skyfleet.controllers')
  .controller(
    'bikeDetailsController',
    function (
      $scope,
      $rootScope,
      $state,
      bikeFleetFactory,
      utilsFactory,
      mapFactory,
      sessionFactory,
      $timeout,
      rootScopeFactory,
      $location,
      $route,
      $q,
      tripAnimationFactory,
      _,
      $window,
      $http,
      notify,
      ngDialog
    ) {
      /* @param{object} $rootScope.maintanenceList Array of all the maintenance categories */
      $scope.selectLatestTrip = function (tripData) {
        let filteredTripData = _.sortBy(tripData.payload, 'trip_id');
        return filteredTripData[0];
      };

      $scope.keyName = 'KEY';
      $scope.iotLock = false;
      $scope.maxVehicleNameLength = 20

      bikeFleetFactory.getBikeDetails(+$state.params.bikeId).then(result=> {
        const bikeInfo = result && result.data && result.data.payload
        if (bikeInfo.lock) {
          $scope.lock = bikeInfo.lock
        }
        $scope.isDocked = bikeInfo.isDocked
      })

      $rootScope.goback = function () {
        if (
          $state.params.currentPage &&
          $state.params.currentPage.bikeStatus === 'staged'
        ) {
          $state.go('staged-bikes', {
            bikeData: $state.params.currentPage,
          });
        } else if (
          $state.params.currentPage &&
          $state.params.currentPage.bikeStatus === 'outofservice'
        ) {
          $state.go('out-of-service-bikes', {
            bikeData: $state.params.currentPage,
          });
        } else {
          $window.history.back();
        }
      };

      $scope.sendWorkshop = function () {
        bikeFleetFactory.bikeToWorkshop(
          {
            operator_id: sessionFactory.getCookieId(),
            maintenance_notes: $scope.maintenanceNotes,
            maintenance_category: utilsFactory.startLowerCase(
              utilsFactory.spaceToUnderscore(
                $scope.maintenanceSelect['maintenance_category']
              )
            ),
            bike_id: $state.params.bikeId,
            fleet_id: rootScopeFactory.getData('fleetId'),
            ellipse_id: $scope.ellipseId,
          },
          function (response) {
            if (response.status_code === 200) {
            }
          }
        );
      };

      $scope.sendToActiveFleet = function () {
        bikeFleetFactory.toActiveFleet(
          {
            operator_id: sessionFactory.getCookieId(),
            bikes: [$state.params.bikeId],
          },
          function (response) {
            if (response.status_code === 200) {
              // TODO Close the modal
            }
          }
        );
      };

      $scope.showDate = false;

      $scope.showDatePicker = function () {
        $scope.showDate = !$scope.showDate;
      };

      $scope.form = {};
      $scope.controllers = [];
      $scope.$on('gotBikeData', async () => {
        $scope.bikeData = rootScopeFactory.getData('currentBikeData');
        if ($scope.lock) $scope.bikeData.lock = $scope.lock
        $scope.oldDescription = angular.copy($scope.bikeData.description)
        if($scope.bikeData.bike_battery_level) $scope.bikeData.bike_battery_level = +$scope.bikeData.bike_battery_level
        if ($scope.bikeData.bike_battery_level && !isNaN($scope.bikeData.bike_battery_level)) $scope.bikeData.bike_battery_level = $scope.bikeData.bike_battery_level.toFixed();

        $scope.form.qrCode = $scope.bikeData.qr_code_id;
        $scope.form.bikeName = $scope.bikeData.bike_name;
        /* Vehicles can have multiple controllers. We only make remote locks on IoT not locks(BLE)
        this logic is to ensure if a bike has an IoT and a tracker, the remote lock is invoked on the IoT and not tracker */
        if ($scope.bikeData.controllers && $scope.bikeData.controllers.length) {
          $scope.controllers = $scope.bikeData.controllers;
          const [tapKeyController] = $scope.controllers.filter(controller => controller.vendor === 'Tap Key')
          if (tapKeyController) {
            const { data: lock } = await bikeFleetFactory.getLockUsingKey('id', tapKeyController.key, tapKeyController.fleet_id)
            $scope.tapkeyKey = lock.payload[0].title
          }
          $scope.controllers.forEach(controller => {
            if (controller.metadata) {
              controller.metadata = JSON.parse(controller.metadata)
              controller.key = controller.metadata.key || controller.key
            }
            if (['Nimbelink', 'ACTON', 'Geotab IoT'].includes(controller.vendor)) {
              controller.keyName = 'SERIAL NUMBER';
            }
            if (['ScoutIOT', 'Segway', 'Segway IoT EU', 'COMODULE Ninebot ES4'].includes(controller.vendor)) {
              controller.keyName = 'IMEI';
            }
            if (['Linka IoT'].includes(controller.vendor)) {
              controller.keyName = 'MAC ID';
            } if (['Duckt'].includes(controller.vendor)) {
              controller.keyName = 'ADAPTER UID';
            }
            if (['Tap Key'].includes(controller.vendor)) {
              controller.keyName = 'LOCK NAME';
              controller.key = $scope.tapkeyKey
            }
            if (['Manual Lock'].includes(controller.vendor)) {
              controller.keyName = 'CODE';
            }
          });
          if ($scope.bikeData.controllers.length === 1) {
            let controller = $scope.bikeData.controllers[0];
            $scope.segwayController = ['Segway', 'Segway IoT EU'].includes(
              controller.vendor
            );
            if (controller.device_type === 'iot') $scope.iotLock = controller;
          } else if ($scope.bikeData.controllers.length > 1) {
            let controllers = $scope.bikeData.controllers;
            let iot = controllers.find(
              (element) => element.device_type === 'iot'
            );
            $scope.segwayController = controllers.find(
              (controller) =>
                controller.vendor === 'Segway' ||
                controller.vendor === 'Segway IoT  EU'
            );
            $scope.iotLock = iot;
          }
        }
        if ($scope.iotLock.qr_code) {
          const iotVendor = $scope.iotLock.vendor;
          const selectedIoT = $scope.data.qrTypes.find(
            (iot) => iot.name === iotVendor
          );
          if (selectedIoT) {
            $scope.data.selectedQrType = selectedIoT;
          }
        }
        $scope.bikeControllerWithLockCapability = $scope.lock || $scope.iot;
        if ($scope.bikeData.type.toLowerCase() === 'regular') {
          $scope.bikeData.type = 'Bike';
        }

        if ($scope.bikeData.type.toLowerCase() === 'electric') {
          $scope.bikeData.type = 'Electric Bike';
        }
        $scope.tripData = rootScopeFactory.getData('currentTripData');
        initFormModel();
      });

      $rootScope.$on('$locationChangeStart', function (event, nextPage) {
        if (
          _.indexOf(
            ['live-bikes', 'staged-bikes', 'out-of-service-bikes'],
            nextPage.split('/').pop()
          ) !== -1
        ) {
          $rootScope.$evalAsync(function () {
            if ($state.params.currentPage) {
              if ($state.params.currentPage.bikeStatus === 'staged') {
                $state.go('staged-bikes', {
                  bikeData: $state.params.currentPage,
                });
              } else if (
                $state.params.currentPage.bikeStatus === 'outofservice'
              ) {
                $state.go('out-of-service-bikes', {
                  bikeData: $state.params.currentPage,
                });
              } else if ($state.params.currentPage.bikeStatus === 'active') {
                $state.go('live-bikes', {
                  bikeData: $state.params.currentPage,
                });
              }
            }
          });
        }
      });

      $scope.isEditingKey = false;
      $scope.isAddingEquipment = false;
      $scope.isEditingVehicle = false;
      $scope.currentCtrlId = undefined;
      $scope.startEditingKey = function (operation, ctrl = undefined) {
        // Operation can be adding or updating a ctrl
        $scope.isEditingKey = true;
        $scope.updateOperation = operation;
        $scope.currentCtrlId = ctrl.controller_id;
        if (lockName.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'LOCK NAME';
        }
      };

      $scope.startAddingEquipment = (operation) => {
        $scope.isAddingEquipment = true;
        $scope.updateOperation = operation;
      };

      $scope.startEditingVehicle = function () {
        $scope.isEditingVehicle = true;
      };

      $scope.showLengthError = false
      $scope.checkBikeNameLength = function (vehicleName) {
        $scope.showLengthError = vehicleName.length > $scope.maxVehicleNameLength;
      };

      let imeiList = [
        'ScoutIOT',
        'Segway',
        'Segway IoT EU',
        'COMODULE Ninebot ES4',
      ];

      let serialList = ['Nimbelink', 'ACTON', 'Geotab IoT'];
      let codeList = ['Manual Lock'];
      let macIdList = ['Linka IoT'];
      const lockName = ['Tap Key'];
      const adapterUidList = ['Duckt'];
      const lockIdList = ['Sas'];
      $scope.showCodeInput = false

      $scope.updateModuleType = function () {
        if (imeiList.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'IMEI';
        }

        if (serialList.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'SERIAL NUMBER';
        }

        if (lockName.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'LOCK NAME';
        }

        if (codeList.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'CODE';
        }
        if (macIdList.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'MAC_ID';
        }
        if (adapterUidList.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'ADAPTER UID';
        }
        if (lockIdList.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'LOCK ID';
        }
      };

      function initFormModel(params) {
        $scope.showLengthError = false
        if (imeiList.includes($scope.bikeData.iot_module_type)) {
          $scope.keyName = 'IMEI';
        }

        if (serialList.includes($scope.bikeData.iot_module_type)) {
          $scope.keyName = 'SERIAL NUMBER';
        }

        $scope.form.bikeName = $scope.bikeData.bike_name;
        $scope.form.qrCode = $scope.iotLock.qr_code
          ? $scope.iotLock.qr_code
          : $scope.bikeData.qr_code_id;
        $scope.form.iotModuleType = $scope.bikeData.iot_module_type;

        if (codeList.includes($scope.bikeData.iot_module_type)) {
          $scope.keyName = 'CODE'
        };

        if (adapterUidList.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'ADAPTER UID';
        };

        if (macIdList.includes($scope.bikeData.iot_module_type)) {
          $scope.keyName = 'MAC_ID';
        };

        if (lockName.includes($scope.bikeData.iot_module_type)) {
          $scope.keyName = 'LOCK NAME';
        };
        if (lockIdList.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'LOCK ID';
        }

        if ($scope.bikeData.controllers && $scope.bikeData.controllers.length) {
          const currentCtrl = $scope.controllers.filter(
            (ctrl) => ctrl.vendor === $scope.bikeData.iot_module_type
          )[0];
          $scope.form.iotKey =
            (currentCtrl && currentCtrl.key) || $scope.controllers[0].key;
        }
      }

      $scope.stopEditingKey = function () {
        $scope.isEditingKey = false;
        $scope.currentCtrlId = undefined
        if (lockName.includes($scope.form.iotModuleType)) {
          $scope.keyName = 'LOCK NAME';
        }
        initFormModel();
      };
      $scope.stopAddingEquipment = () => {
        $scope.isAddingEquipment = false;
        initFormModel();
      }

      $scope.stopEditingVehicle = function () {
        $scope.isEditingVehicle = false;
        initFormModel();
      };

      $scope.openImageUpload = function () {
        ngDialog.open({
          template: '../../html/modals/edit-bike-image.html',
          controller: 'selectImageController'
        })
      }
      $rootScope.$on('bikeImageUploadCompleted', (event, uploadedFile) => {
        $scope.uploadedFile = uploadedFile

      })

      // handle the upload of the image after modal disappears
      $rootScope.$on('saveBikeImage', async () => {
        $rootScope.showLoader = true
        const uploadedImageDetails = $scope.uploadedFile[1]
        uploadedImageDetails.fleetId = rootScopeFactory.getData('fleetId')
        let formData = new FormData()
        formData.append('pic', $scope.uploadedFile[1])
        formData.append('fleetId', rootScopeFactory.getData('fleetId'))
        formData.append('bikeId', $scope.bikeData.bike_id)
        try {
          const { data } = await $http.post('api/bike-fleet/update-image', formData, {headers: {'Content-Type': undefined}})
          $scope.bikeData.pic = data.payload.link
          $rootScope.showLoader = false
          notify({
            message: 'Bike image updated',
            duration: 2000,
            position: 'right',
          });
        } catch (error) {
          $rootScope.showLoader = false
          notify({
            message: `Bike image update failed: ${error.message}`,
            duration: 2000,
            position: 'right',
          });
        }
      })

      $scope.editDescription = false
      $scope.toggleEditDescription = (option) => {
        $scope.editDescription = !$scope.editDescription
        if (option === 'cancel') {
          $scope.bikeData.description = $scope.oldDescription
        }
      }

      $scope.updateDescription = async (description) => {
        $rootScope.showLoader = true
         const bikeDetails = {
          bikeId: $scope.bikeData.bike_id,
          description: $scope.bikeData.description.trim()
        }
        try {
          const { data } = await $http.post('api/bike-fleet/update-description', bikeDetails)
          $rootScope.showLoader = false
          $scope.oldDescription = $scope.bikeData.description
          $scope.toggleEditDescription()
          notify({
            message: 'Bike description updated',
            duration: 2000,
            position: 'right',
          });
        } catch (error) {
          $rootScope.showLoader = false
          notify({
            message: `Bike description update failed: ${error.message}`,
            duration: 2000,
            position: 'right',
          });
        }
      }


      $scope.data = {
        qrTypes: [
          { id: '1', name: 'Lattis' },
          { id: '2', name: 'Segway' },
          { id: '3', name: 'Grow' },
          { id: '4', name: 'ACTON' },
        ],
        selectedQrType: $scope.selectedQrType || { id: '1', name: 'Lattis' },
      };

      $scope.selectedQrType = function (type) {
        $scope.selectedType = type.name;
      };

      $scope.saveVehicleDetails = function () {
        $scope.saveVehicle();
      };

      $scope.saveVehicle = function () {
        $rootScope.showLoader = true;
        const isQrCodeLattis = $scope.data.selectedQrType.name === 'Lattis' || $scope.selectedType === 'Lattis'

        $http
          .post('api/bike-fleet/' + $state.params.bikeId + '/bikeDetails', {
            bike_name: $scope.form.bikeName,
            qr_code: $scope.form.qrCode,
            qr_code_type: isQrCodeLattis ? 'Lattis' : 'IoT',
            equipmentType: 'bike'
          })
          .then(
            function (response) {
              $scope.isEditingVehicle = false;
              $scope.form.bikeName = response.data.payload.bike_name;
              $rootScope.showLoader = false;
              $scope.form.qrCode = response.data.payload.qrCode
              $scope.bikeData.qr_code_id = response.data.payload.qrCode
              notify({
                message: 'Bike details saved successfully',
                duration: 2500,
                position: 'right',
              });
            },
            function (response) {
              $rootScope.showLoader = false;
              notify({
                message: response.data
                  ? response.data.error.message
                  : 'Failed to save bike details',
                duration: 5000,
                position: 'right',
              });
            }
          );
      };

      $scope.saveKey = async (data = null) => {
        $rootScope.showLoader = true;
        const fleetId = rootScopeFactory.getData('fleetId')
        if ($scope.bikeData.iot_module_type === 'Tap Key') {
          try {
            const { data: lock } = await bikeFleetFactory.getLockUsingKey('title', data ? data.key : $scope.form.iotKey, fleetId)
            const [boundLock] = lock.payload
            if (boundLock) {
              $scope.boundLock = boundLock
            } else {
              notify({
                message: 'Cannot find a lock by that name',
                duration: 5000,
                position: 'right',
              });
              $rootScope.showLoader = false;
              return
            }
          } catch (error) {
            notify({
              message: error
                ? error.message
                : 'Failed to save IoT key',
              duration: 5000,
              position: 'right',
            });
            $rootScope.showLoader = false;
            return
          }
        }
        const iotKey = data ? data.key : $scope.form.iotKey
        const requestData =  {
          bike_name: $scope.form.bikeName,
          iot_key: $scope.boundLock ? $scope.boundLock.id : iotKey,
          iot_module_type: data ? data.vendor : $scope.form.iotModuleType,
          operation: $scope.updateOperation,
          isEditingQRCode: $scope.isEditingVehicle,
          isEdittingControllerKey:
            $scope.isEditingKey && $scope.updateOperation === 'updating',
          isAddingControllerKey:
            $scope.isEditingKey && $scope.updateOperation === 'adding',
          autogenerateQRCode: $scope.form.autogenerateQR
        }
        $http
          .post('api/bike-fleet/' + $state.params.bikeId + '/iot', requestData)
          .then(
            function (response) {
              $rootScope.showLoader = false;
              $scope.isEditingKey = false;
              $scope.isAddingEquipment = false;
              $scope.currentCtrlId = undefined;
              $scope.form.qrCode = response.data.payload.qr_code;
              $scope.form.bikeName = response.data.payload.bike_name;
              $scope.form.autogenerateQR = false;
              notify({
                message: 'IoT key saved successfully',
                duration: 2500,
                position: 'right',
              });
              $scope.$emit('updateBikeDetails');
              $scope.isEditingVehicle = false;
              if ($scope.boundLock) {
                $scope.boundLock.id = undefined
              }
            },
            function (response) {
              $rootScope.showLoader = false;
              if ($scope.boundLock) {
                $scope.boundLock.id = undefined
              }
              notify({
                message: response.data
                  ? response.data.error.message
                  : 'Failed to save IoT key',
                duration: 5000,
                position: 'right',
              });
            }
          );
      };

      $scope.pollCommandStatus = async function (commandId) {
        const { data: commandStatus } = await $http.get(`api/bike-fleet/command-status/${ commandId }`)
        if (commandStatus.payload.data.status === 2) {
          return commandStatus
        } else {
          return await $scope.pollCommandStatus(commandStatus.payload.data.command_id)
        }
      }

      $scope.lockVehicle = function () {
        $rootScope.showLoader = true;
        $http
          .post('api/bike-fleet/' + $state.params.bikeId + '/iot/lock', {
            action: 'lock',
          })
          .then(
            async function (response) {
              const { data } = response
              const errorStatus = [
                'ERROR_UNLOCK_JAM',
                'ERROR_LOCKED_TIMEOUT',
                'ERROR_LOCKING_BLOCKED',
                'ERROR_STALL',
                'ERROR_LOW_BATTERY',
                'ERROR_NOT_ACTIVE',
                'ERROR_CONNECTION_TIMEOUT',
                'ERROR_LOCKED_NOTIFICATION _TIMEOUT',
                'TIMEOUT_ERROR_TRY_AGAIN'
              ]
              if (data.payload.status_desc && errorStatus.includes(data.payload.status_desc)) {
                $rootScope.showLoader = false;
                notify({
                  message: response.data
                    ? `Locking failed due to an error: ${ response.data.payload.status_desc }`
                    : 'Failed to lock Vehicle',
                  duration: 3000,
                  position: 'right',
                });
                return
              }
              if (data.payload && data.payload.integration_type && data.payload.integration_type === 'Linka' && data.payload.status !== 2) {
                const commandId = response.data.payload.command_id
                const { payload: commandStatus } = await $scope.pollCommandStatus(commandId)
                if (commandStatus.data.status_desc && errorStatus.includes(commandStatus.data.status_desc)) {
                  $rootScope.showLoader = false;
                  notify({
                    message: commandStatus.data.status_desc
                      ? `Locking failed due to an error: ${ commandStatus.data.status_desc }`
                      : 'Failed to lock Vehicle',
                    duration: 3000,
                    position: 'right',
                  });
                  return
                }
                if (commandStatus.data && commandStatus.data.status === 2) {
                  $rootScope.showLoader = false;
                  notify({
                    message: 'Vehicle locked successfully',
                    duration: 2000,
                    position: 'right',
                  });
                  $scope.bikeData.current_status = 'controller_assigned';
                  $scope.isEditingKey = false;
                  $scope.bikeData['locked'] = true;
                }
              } else {
                $rootScope.showLoader = false;
                notify({
                  message: 'Vehicle locked successfully',
                  duration: 2000,
                  position: 'right',
                });
                $scope.bikeData.current_status = 'controller_assigned';
                $scope.isEditingKey = false;
                $scope.bikeData['locked'] = true;
              }
            },
            function (response) {
              $rootScope.showLoader = false;
              notify({
                message: response.data
                  ? response.data.error.message
                  : 'Failed to lock Vehicle',
                duration: 3000,
                position: 'right',
              });
            }
          );
      };

      $scope.unlockVehicle = function () {
        $rootScope.showLoader = true;
        $http
          .post('api/bike-fleet/' + $state.params.bikeId + '/iot/lock', {
            action: 'unlock',
          })
          .then(
            async function (response) {
              const { data } = response
              const errorStatus = [
                'ERROR_LOCK_JAM',
                'ERROR_UNLOCKED_TIMEOUT',
                'ERROR_STALL',
                'ERROR_LOW_BATTERY',
                'ERROR_NOT_ACTIVE',
                'ERROR_CONNECTION_TIMEOUT',
                'ERROR_UNLOCKED_NOTIFICATION_TIMEOUT',
                'TIMEOUT_ERROR_TRY_AGAIN'
              ]
              if (data.payload.status_desc && errorStatus.includes(data.payload.status_desc)) {
                $rootScope.showLoader = false;
                notify({
                  message: response.data
                    ? `Unlocking failed due to an error: ${ response.data.payload.status_desc }`
                    : 'Failed to unlock Vehicle',
                  duration: 3000,
                  position: 'right',
                });
                return
              }
              if (data.payload && data.payload.integration_type && data.payload.integration_type === 'Linka' && data.payload.status !== 2) {
                const commandId = response.data.payload.command_id
                const { payload: commandStatus } = await $scope.pollCommandStatus(commandId)
                if (commandStatus.data.status_desc && errorStatus.includes(commandStatus.data.status_desc)) {
                  $rootScope.showLoader = false;
                  notify({
                    message: commandStatus.data.status_desc
                      ? `Unlocking failed due to an error: ${ commandStatus.data.status_desc }`
                      : 'Failed to unlock Vehicle',
                    duration: 3000,
                    position: 'right',
                  });
                  return
                }
                if (commandStatus.data && commandStatus.data.status === 2) {
                  $rootScope.showLoader = false;
                  notify({
                    message: 'Vehicle unlocked successfully',
                    duration: 2000,
                    position: 'right',
                  });
                  $scope.bikeData.current_status = 'controller_assigned';
                  $scope.isEditingKey = false;
                  $scope.bikeData['locked'] = false;
                }
              } else {
                $rootScope.showLoader = false;
                notify({
                  message: 'Vehicle unlocked successfully',
                  duration: 2000,
                  position: 'right',
                });
                $scope.bikeData.current_status = 'controller_assigned';
                $scope.isEditingKey = false;
                $scope.bikeData['locked'] = false;
              }
            },
            function (response) {
              $rootScope.showLoader = false;
              notify({
                message: response.data
                  ? response.data.error.message
                  : 'Failed to unlock Vehicle',
                duration: 3000,
                position: 'right',
              });
            }
          );
      };
      $scope.openBatteryCover = function () {
        const customMessage =
          'Error: This feature is not available on this vehicle';
        $rootScope.showLoader = true;
        $http
          .post('api/bike-fleet/' + $state.params.bikeId + '/iot/cover', {})
          .then(
            function (response) {
              $rootScope.showLoader = false;
              if (response.data.payload.success === true) {
                notify({
                  message: 'Battery cover opened successfully',
                  duration: 2000,
                  position: 'right',
                });
              } else {
                notify({
                  message:
                    response.data.payload.message === 'device execute failure' ||
                      response.data.payload.message === 'command timeout'
                      ? customMessage
                      : `Error: ${ response.data.payload.message }`,
                  duration: 2000,
                  position: 'right',
                });
              }
            },
            function (response) {
              $rootScope.showLoader = false;
              notify({
                message: response.data
                  ? response.data.error.message
                  : 'Failed to open battery cover',
                duration: 3000,
                position: 'right',
              });
            }
          );
      };

      $scope.deleteEquipment = (controller) => {
        $scope.deleteController = controller
        ngDialog.open({
          template: '../html/modals/delete-equipment.html',
          controller: 'deleteEquipmentModalController',
          data: {controller}
        })
      }

      $rootScope.$on('deleteEquipment', async (event, controller) => {
        $rootScope.showLoader = true
        let module = 'Controller'
        if (!controller.bike_id) controller.bike_id = $scope.bikeData.bike_id
        try {
          const {data: deleteResponse} = await $http.post('api/bike-fleet/delete-controller/', controller)
          if (controller.lock_id) {
            module = 'Lock'
            $scope.bikeData.lock_id = ''
          }
          notify({
            message: `${module} deleted successfully`,
            duration: 2000,
            position: 'right',
          });
          $rootScope.showLoader = false
          if (deleteResponse.payload.controllers) {
            $scope.controllers = deleteResponse.payload.controllers
            if ($scope.controllers.length) {
              const [tapKeyController] = $scope.controllers.filter(controller => controller.vendor === 'Tap Key')
              if (tapKeyController) {
                const { data: lock } = await bikeFleetFactory.getLockUsingKey('id', tapKeyController.key, tapKeyController.fleet_id)
                $scope.tapkeyKey = lock.payload[0].title
              }
              $scope.controllers.forEach(controller => {
                if (['Nimbelink', 'ACTON', 'Geotab IoT'].includes(controller.vendor)) {
                  controller.keyName = 'SERIAL NUMBER';
                }
                if (['ScoutIOT', 'Segway', 'Segway IoT EU', 'COMODULE Ninebot ES4'].includes(controller.vendor)) {
                  controller.keyName = 'IMEI';
                }
                if (['Linka IoT'].includes(controller.vendor)) {
                  controller.keyName = 'MAC ID';
                } if (['Duckt'].includes(controller.vendor)) {
                  controller.keyName = 'ADAPTER UID';
                }
                if (['Tap Key'].includes(controller.vendor)) {
                  controller.keyName = 'LOCK NAME';
                  controller.key = $scope.tapkeyKey
                }
                if (['Manual Lock'].includes(controller.vendor)) {
                  controller.keyName = 'CODE';
                }
              });
              if ($scope.controllers.length === 1) {
                let controller = $scope.controllers[0];
                $scope.segwayController = ['Segway', 'Segway IoT EU'].includes(
                  controller.vendor
                );
                if (controller.device_type === 'iot') $scope.iotLock = controller;
              } else if ($scope.controllers.length > 1) {
                let controllers = $scope.controllers;
                let iot = controllers.find(
                  (element) => element.device_type === 'iot'
                );
                $scope.segwayController = controllers.find(
                  (controller) =>
                    controller.vendor === 'Segway' ||
                    controller.vendor === 'Segway IoT  EU'
                );
                $scope.iotLock = iot;
              }
            }
          }
        } catch (error) {
          $rootScope.showLoader = false
          $scope.deleteController = ''
          const {data: errorData } = error
          if (errorData) {
            notify({
              message: errorData.error
                ? errorData.error || 'Failed to remove controller'
                : 'Failed to remove controller',
              duration: 3000,
              position: 'right',
            });
            return
          }
          notify({
            message: error
              ? error.message || 'Failed to remove controller'
              : 'Failed to remove controller',
            duration: 3000,
            position: 'right',
          });
        }
      })
    }
  );
