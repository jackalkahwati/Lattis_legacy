'use strict'

angular.module('skyfleet.controllers').controller(
  'sendToServiceModalController',
  function ($scope, bikeFleetFactory, sessionFactory, $rootScope, rootScopeFactory, utilsFactory, ngDialog, notify, $q, $timeout) {
    $q.all([bikeFleetFactory.getBikesData({fleet_id: rootScopeFactory.getData('fleetId')})]).then(function (response) {
      if (response[0].payload) {
        $scope.selectedBike = _.where(response[0].payload.bike_data, {bike_id:
                    parseInt(rootScopeFactory.getData('selectedBikes')[0])})
        $scope.serviceBikeName = $scope.selectedBike[0].bike_name
      }
    })

    $scope.mainTypes = [
      {name: 'Report Damage', value: 'damage_reported', disabled: false},
      {name: 'Maintenance due', value: 'service_due', disabled: false},
      {name: 'Report Theft', value: 'reported_theft', disabled: false},
      {name: 'Parked outside parking area', value: 'parking_outside_geofence', disabled: true}
    ]

    $scope.serviceCategory = $scope.mainTypes[0]

    $scope.maintenanceTicket = function () {
      let ticketForm = new FormData()
      ticketForm.append('category', $scope.serviceCategory.value)
      ticketForm.append('notes', $scope.serviceNotes)
      if ($scope.serviceImage) {
        ticketForm.append('operator_photo', $scope.serviceImage)
      }
      ticketForm.append('fleet_id', rootScopeFactory.getData('fleetId'))
      ticketForm.append('operator_id', sessionFactory.getCookieId())
      ticketForm.append('customer_id', rootScopeFactory.getData('customerId'))
      ticketForm.append('bike_id', rootScopeFactory.getData('selectedBikes')[0])
      ticketForm.append('reported_by_operator_id', sessionFactory.getCookieId())
      $rootScope.showLoader = true
      bikeFleetFactory.createTicket(ticketForm, function (res) {
        if (res && res.status === 200) {
          notify({message: 'Ticket successfully created', duration: 2000, position: 'right'})
          $rootScope.$broadcast('clearUpload')
          $rootScope.$emit('clearUpload')
          rootScopeFactory.setData('selectedBikes', null)
          $timeout(function () {
            $rootScope.$broadcast('clearBikeDataCache')
            $rootScope.$emit('clearBikeDataCache')
          }, 30)
          $rootScope.$emit('disableBikeOptions')
          $rootScope.$broadcast('disableBikeOptions')
          $rootScope.$broadcast('fleetChange', rootScopeFactory.getData('fleetId'))
          $rootScope.$emit('fleetChange', rootScopeFactory.getData('fleetId'))
          $rootScope.showLoader = false
          ngDialog.closeAll()
        } else {
          notify({message: 'Failed to send bike to service', duration: 2000, position: 'right'})
          rootScopeFactory.setData('selectedBikes', null)
          $rootScope.showLoader = false
          ngDialog.closeAll()
        }
      })
    }

    $scope.closeModal = function () {
      ngDialog.closeAll()
    }
  })
